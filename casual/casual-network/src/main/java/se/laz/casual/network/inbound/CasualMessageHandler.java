package se.laz.casual.network.inbound;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import se.kodarkatten.casual.api.network.protocol.messages.CasualNWMessage;
import se.kodarkatten.casual.jca.inflow.CasualMessageListener;
import se.kodarkatten.casual.network.protocol.messages.domain.CasualDomainConnectRequestMessage;
import se.kodarkatten.casual.network.protocol.messages.domain.CasualDomainDiscoveryRequestMessage;
import se.kodarkatten.casual.network.protocol.messages.service.CasualServiceCallRequestMessage;
import se.kodarkatten.casual.network.protocol.messages.transaction.CasualTransactionResourceCommitRequestMessage;
import se.kodarkatten.casual.network.protocol.messages.transaction.CasualTransactionResourcePrepareRequestMessage;
import se.kodarkatten.casual.network.protocol.messages.transaction.CasualTransactionResourceRollbackRequestMessage;

import javax.resource.spi.XATerminator;
import javax.resource.spi.endpoint.MessageEndpoint;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.resource.spi.work.WorkManager;
import java.util.Objects;
import java.util.logging.Logger;

@ChannelHandler.Sharable
public final class CasualMessageHandler extends SimpleChannelInboundHandler<CasualNWMessage<?>>
{
    private static Logger log = Logger.getLogger(CasualMessageHandler.class.getName());
    private final MessageEndpointFactory factory;
    private final XATerminator xaTerminator;
    private final WorkManager workManager;

    private CasualMessageHandler(MessageEndpointFactory factory, XATerminator xaTerminator, WorkManager workManager)
    {
        this.factory = factory;
        this.xaTerminator = xaTerminator;
        this.workManager = workManager;
    }

    public static CasualMessageHandler of(final MessageEndpointFactory factory, final XATerminator xaTerminator, final WorkManager workManager)
    {
        Objects.requireNonNull(factory, "factory can not be null");
        Objects.requireNonNull(xaTerminator, "xaTerminator can not be null");
        Objects.requireNonNull(workManager, "workManager can not be null");
        return new CasualMessageHandler(factory, xaTerminator, workManager);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, CasualNWMessage<?> message) throws Exception
    {
        MessageEndpoint endpoint = factory.createEndpoint(null);
        CasualMessageListener listener = (CasualMessageListener) endpoint;
        switch ( message.getType() )
        {
            case COMMIT_REQUEST:
                listener.commitRequest((CasualNWMessage<CasualTransactionResourceCommitRequestMessage>)message, ctx.channel(), xaTerminator);
                break;
            case PREPARE_REQUEST:
                listener.prepareRequest((CasualNWMessage<CasualTransactionResourcePrepareRequestMessage>)message, ctx.channel(), xaTerminator);
                break;
            case REQUEST_ROLLBACK:
                listener.requestRollback((CasualNWMessage<CasualTransactionResourceRollbackRequestMessage>)message, ctx.channel(), xaTerminator);
                break;
            case SERVICE_CALL_REQUEST:
                listener.serviceCallRequest((CasualNWMessage<CasualServiceCallRequestMessage>)message, ctx.channel(), workManager);
                break;
            case DOMAIN_CONNECT_REQUEST:
                listener.domainConnectRequest((CasualNWMessage<CasualDomainConnectRequestMessage>)message, ctx.channel());
                break;
            case DOMAIN_DISCOVERY_REQUEST:
                listener.domainDiscoveryRequest((CasualNWMessage<CasualDomainDiscoveryRequestMessage>)message, ctx.channel());
                break;
            default:
                log.warning("Message type not supported: " + message.getType());
        }
    }

}
