package se.kodarkatten.casual.network.io;

import se.kodarkatten.casual.network.messages.CasualNWMessage;
import se.kodarkatten.casual.network.messages.CasualNetworkTransmittable;
import se.kodarkatten.casual.network.messages.exceptions.CasualTransportException;
import se.kodarkatten.casual.network.utils.ByteUtils;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Created by aleph on 2017-03-12.
 */
public final class CasualNetworkWriter
{
    private CasualNetworkWriter()
    {}
    public static <T extends CasualNetworkTransmittable> void write(final AsynchronousByteChannel channel, final CasualNWMessage<T> msg)
    {
        for(final byte[] bytes : msg.toNetworkBytes())
        {
            CompletableFuture<Void> f = ByteUtils.writeFully(channel, ByteBuffer.wrap(bytes));
            try
            {
                f.get();
            }
            catch (InterruptedException | ExecutionException e)
            {
                throw new CasualTransportException("failed writing", e);
            }
        }
    }

    public static <T extends CasualNetworkTransmittable> void write(final WritableByteChannel channel, final CasualNWMessage<T> msg)
    {
        for(final byte[] bytes : msg.toNetworkBytes())
        {
            ByteUtils.writeFully(channel, ByteBuffer.wrap(bytes), bytes.length);
        }
    }

    /**
     * Lock the channel for write and then write the msg fully to the channel.
     *
     * Blocks until write lock is acquired and write operation is complete.
     *
     * @param channel to write msg to.
     * @param msg to send
     * @param <T> type of message that is being sent.
     */
    public static <T extends CasualNetworkTransmittable> void write( final LockableSocketChannel channel, final CasualNWMessage<T> msg )
    {
        try
        {
            channel.lockWrite();
            write( channel.getSocketChannel(), msg );
        }
        finally
        {
            channel.unlockWrite();
        }
    }

}
