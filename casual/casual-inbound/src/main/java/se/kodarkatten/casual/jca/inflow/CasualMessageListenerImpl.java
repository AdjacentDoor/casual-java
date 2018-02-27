package se.kodarkatten.casual.jca.inflow;

import io.netty.channel.Channel;
import se.kodarkatten.casual.api.flags.XAFlags;
import se.kodarkatten.casual.api.network.protocol.messages.CasualNWMessage;
import se.kodarkatten.casual.api.service.ServiceInfo;
import se.kodarkatten.casual.api.xa.XAReturnCode;
import se.kodarkatten.casual.api.xa.XID;
import se.kodarkatten.casual.jca.CasualResourceAdapterException;
import se.kodarkatten.casual.jca.inbound.handler.service.ServiceHandler;
import se.kodarkatten.casual.jca.inbound.handler.service.ServiceHandlerFactory;
import se.kodarkatten.casual.jca.inbound.handler.service.ServiceHandlerNotFoundException;
import se.kodarkatten.casual.jca.inflow.work.CasualServiceCallWork;
import se.kodarkatten.casual.network.protocol.messages.CasualNWMessageImpl;
import se.kodarkatten.casual.network.protocol.messages.domain.CasualDomainConnectReplyMessage;
import se.kodarkatten.casual.network.protocol.messages.domain.CasualDomainConnectRequestMessage;
import se.kodarkatten.casual.network.protocol.messages.domain.CasualDomainDiscoveryReplyMessage;
import se.kodarkatten.casual.network.protocol.messages.domain.CasualDomainDiscoveryRequestMessage;
import se.kodarkatten.casual.network.protocol.messages.domain.Service;
import se.kodarkatten.casual.network.protocol.messages.service.CasualServiceCallRequestMessage;
import se.kodarkatten.casual.network.protocol.messages.transaction.CasualTransactionResourceCommitReplyMessage;
import se.kodarkatten.casual.network.protocol.messages.transaction.CasualTransactionResourceCommitRequestMessage;
import se.kodarkatten.casual.network.protocol.messages.transaction.CasualTransactionResourcePrepareReplyMessage;
import se.kodarkatten.casual.network.protocol.messages.transaction.CasualTransactionResourcePrepareRequestMessage;
import se.kodarkatten.casual.network.protocol.messages.transaction.CasualTransactionResourceRollbackReplyMessage;
import se.kodarkatten.casual.network.protocol.messages.transaction.CasualTransactionResourceRollbackRequestMessage;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.resource.NotSupportedException;
import javax.resource.spi.XATerminator;
import javax.resource.spi.work.TransactionContext;
import javax.resource.spi.work.WorkException;
import javax.resource.spi.work.WorkManager;
import javax.transaction.xa.XAException;
import javax.transaction.xa.Xid;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@MessageDriven(messageListenerInterface = CasualMessageListener.class,
        activationConfig =
                {
                        @ActivationConfigProperty(propertyName = "resourceAdapterJndiName", propertyValue = "eis/casualResouceAdapter"),

                })
public class CasualMessageListenerImpl implements CasualMessageListener
{
    private static Logger log = Logger.getLogger(CasualMessageListenerImpl.class.getName());

    private static final Long CASUAL_PROTOCOL_VERSION = 1000L;

    @Override
    public void domainConnectRequest(CasualNWMessage<CasualDomainConnectRequestMessage> message, Channel channel)
    {
        log.finest( "domainConnectRequest()." );

        CasualDomainConnectReplyMessage reply = CasualDomainConnectReplyMessage.createBuilder()
                .withDomainId( message.getMessage().getDomainId() )
                .withDomainName( message.getMessage().getDomainName() )
                .withExecution( message.getMessage().getExecution() )
                .withProtocolVersion(CASUAL_PROTOCOL_VERSION)
                .build();
        CasualNWMessage<CasualDomainConnectReplyMessage> replyMessage = CasualNWMessageImpl.of( message.getCorrelationId(), reply );
        channel.writeAndFlush(replyMessage);
    }

    @Override
    public void domainDiscoveryRequest(CasualNWMessage<CasualDomainDiscoveryRequestMessage> message, Channel channel)
    {
        log.finest( "domainDiscoveryRequest()." );

        CasualDomainDiscoveryReplyMessage reply = CasualDomainDiscoveryReplyMessage.of( message.getMessage().getExecution(), message.getMessage().getDomainId(), message.getMessage().getDomainName() );

        List<Service> services = new ArrayList<>();

        for( String service: message.getMessage().getServiceNames() )
        {
            try
            {
                ServiceHandler handler = ServiceHandlerFactory.getHandler( service );
                ServiceInfo info = handler.getServiceInfo( service );

                services.add( Service.of( info.getServiceName(), info.getCategory(), info.getTransactionType()) );
            }
            catch( ServiceHandlerNotFoundException e )
            {
                //Service does not exist. Continue with the next one in the list.
            }
        }
        reply.setServices( services );

        CasualNWMessage<CasualDomainDiscoveryReplyMessage> replyMessage = CasualNWMessageImpl.of( message.getCorrelationId(), reply );
        channel.writeAndFlush(replyMessage);
    }

    @Override
    public void serviceCallRequest(CasualNWMessage<CasualServiceCallRequestMessage> message, Channel channel, WorkManager workManager )
    {
        log.finest( "serviceCallRequest()." );

        CasualServiceCallWork work = new CasualServiceCallWork(message.getCorrelationId(), message.getMessage() );

        Xid xid = message.getMessage().getXid();

        try
        {
            long startup = isServiceCallTransactional( xid ) ?
                    workManager.startWork( work, WorkManager.INDEFINITE, createTransactionContext( xid, message.getMessage().getTimeout() ), new ServiceCallWorkListener( channel ) ) :
                    workManager.startWork( work, WorkManager.INDEFINITE, null, new ServiceCallWorkListener( channel ));
            log.finest( ()->"Service call startup: "+ startup + "ms.");
        }
        catch (WorkException e)
        {
            throw new CasualResourceAdapterException( "Error starting work.", e );
        }
    }

    private boolean isServiceCallTransactional( Xid xid )
    {
        return ! xid.equals( XID.NULL_XID);
    }

    private TransactionContext createTransactionContext( Xid xid, long timeout )
    {
        TransactionContext context = new TransactionContext();
        context.setXid(xid);

        if (timeout > 0)
        {
            try
            {
                context.setTransactionTimeout(timeout);
            }
            catch (NotSupportedException e)
            {
                log.warning("Timeout is not set as is not supported. " + e.getMessage());
            }
        }
        return context;
    }

    @Override
    public void prepareRequest(CasualNWMessage<CasualTransactionResourcePrepareRequestMessage> message, Channel channel, XATerminator xaTerminator)
    {
        log.finest( "prepareRequest()." );

        Xid xid = message.getMessage().getXid();
        int status = -1;
        try
        {
            status = xaTerminator.prepare( xid );

        } catch (XAException e)
        {

            status = e.errorCode;
            log.log( Level.WARNING, e, ()-> "XAExcception prepare()" + e.getMessage() );
        }
        finally
        {

            CasualTransactionResourcePrepareReplyMessage reply =
                    CasualTransactionResourcePrepareReplyMessage.of(
                            message.getMessage().getExecution(),
                            xid,
                            message.getMessage().getResourceId(),
                            XAReturnCode.unmarshal(status)
                    );
            CasualNWMessageImpl<CasualTransactionResourcePrepareReplyMessage> replyMessage = CasualNWMessageImpl.of(message.getCorrelationId(), reply);
            channel.writeAndFlush(replyMessage);
        }
    }

    @Override
    public void commitRequest(CasualNWMessage<CasualTransactionResourceCommitRequestMessage> message, Channel channel, XATerminator xaTerminator)
    {
        log.finest( "commitRequest()." );

        Xid xid = message.getMessage().getXid();
        boolean onePhase = message.getMessage().getFlags().isSet( XAFlags.TMONEPHASE );

        int status = -1;
        try
        {
            xaTerminator.commit( xid, onePhase );

        } catch (XAException e)
        {
            status = e.errorCode;
            log.log( Level.WARNING, e, ()-> "XAExcception commit()" + e.getMessage() );
        }
        finally
        {
            CasualTransactionResourceCommitReplyMessage reply =
                    CasualTransactionResourceCommitReplyMessage.of(
                            message.getMessage().getExecution(),
                            xid,
                            message.getMessage().getResourceId(),
                            status == -1 ? XAReturnCode.XA_OK : XAReturnCode.unmarshal( status )
                    );
            CasualNWMessageImpl<CasualTransactionResourceCommitReplyMessage> replyMessage = CasualNWMessageImpl.of( message.getCorrelationId(), reply );
            channel.writeAndFlush(replyMessage);
        }
    }

    @Override
    public void requestRollback(CasualNWMessage<CasualTransactionResourceRollbackRequestMessage> message, Channel channel, XATerminator xaTerminator)
    {
        log.finest( "requestRollback()." );

        Xid xid = message.getMessage().getXid();

        int status = -1;
        try
        {
            xaTerminator.rollback( xid );

        } catch (XAException e)
        {
            status = e.errorCode;
            log.log( Level.WARNING, e, ()-> "XAException rollback()" + e.getMessage() );
        }
        finally
        {
            CasualTransactionResourceRollbackReplyMessage reply =
                    CasualTransactionResourceRollbackReplyMessage.of(
                            message.getMessage().getExecution(),
                            xid,
                            message.getMessage().getResourceId(),
                            status == -1 ? XAReturnCode.XA_OK : XAReturnCode.unmarshal( status )
                    );
            CasualNWMessage<CasualTransactionResourceRollbackReplyMessage> replyMessage = CasualNWMessageImpl.of( message.getCorrelationId(), reply );
            channel.writeAndFlush(replyMessage);
        }
    }
}
