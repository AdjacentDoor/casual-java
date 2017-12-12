package se.kodarkatten.casual.jca.inflow.work;

import se.kodarkatten.casual.jca.CasualResourceAdapterException;

import javax.resource.spi.work.WorkException;
import java.nio.channels.SocketChannel;
import java.util.function.Consumer;

public final class SocketChannelConsumer implements Consumer<SocketChannel>
{
    private final CasualInboundWork work;

    public SocketChannelConsumer( CasualInboundWork work )
    {
        this.work = work;
    }

    @Override
    public void accept(SocketChannel socketChannel)
    {
        try
        {
            work.getWorkManager().startWork( new CasualSocketWork( socketChannel, work ) );
        } catch (WorkException e)
        {
            throw new CasualResourceAdapterException( "Error starting worker.", e );
        }
    }
}