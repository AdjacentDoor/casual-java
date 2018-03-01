package se.kodarkatten.casual.network.protocol.decoding.decoders;

import se.kodarkatten.casual.api.network.protocol.messages.CasualNetworkTransmittable;

import java.nio.channels.ReadableByteChannel;
import java.util.Objects;

/**
 * Created by aleph on 2017-03-28.
 */
public final class MessageDecoder<T extends CasualNetworkTransmittable>
{
    final NetworkDecoder<T> networkDecoder;
    final int maxSingleBufferByteSize;
    // It is very much used thank you
    @SuppressWarnings("squid:UnusedPrivateMethod")
    private MessageDecoder(final NetworkDecoder<T> networkDecoder, int maxSingleBufferByteSize)
    {
        this.networkDecoder = networkDecoder;
        this.maxSingleBufferByteSize = maxSingleBufferByteSize;
    }

    public static <T extends CasualNetworkTransmittable> MessageDecoder<T> of(final NetworkDecoder<T> r)
    {
        return of(r, Integer.MAX_VALUE);
    }

    public static <T extends CasualNetworkTransmittable> MessageDecoder<T> of(final NetworkDecoder<T> r, int maxSingleBufferByteSize)
    {
        Objects.requireNonNull(r, "networkDecoder can not be null!");
        return new MessageDecoder<>(r, maxSingleBufferByteSize);
    }

    @SuppressWarnings("squid:S2095")
    public T read(final ReadableByteChannel channel, long messageSize)
    {
        Objects.requireNonNull(channel, "channel is null");
        if (messageSize <= maxSingleBufferByteSize)
        {
            return networkDecoder.readSingleBuffer(channel, (int) messageSize);
        }
        return networkDecoder.readChunked(channel);
    }

    public T read(final byte[] data)
    {
        Objects.requireNonNull(data, "data is null");
        return networkDecoder.readSingleBuffer(data);
    }

}
