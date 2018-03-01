package se.kodarkatten.casual.network.protocol.decoding.decoders.queue;

import se.kodarkatten.casual.api.util.Pair;
import se.kodarkatten.casual.network.protocol.decoding.decoders.NetworkDecoder;
import se.kodarkatten.casual.network.protocol.decoding.decoders.utils.CasualMessageDecoderUtils;
import se.kodarkatten.casual.network.protocol.messages.parseinfo.CommonSizes;
import se.kodarkatten.casual.network.protocol.messages.parseinfo.DequeueRequestSizes;
import se.kodarkatten.casual.network.protocol.messages.queue.CasualDequeueRequestMessage;
import se.kodarkatten.casual.network.protocol.utils.ByteUtils;
import se.kodarkatten.casual.network.protocol.utils.XIDUtils;

import javax.transaction.xa.Xid;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.Arrays;
import java.util.UUID;

// I don't agree here, in these cases it makes it more readable
@SuppressWarnings("squid:UselessParenthesesCheck")
public final class CasualDequeueRequestMessageDecoder implements NetworkDecoder<CasualDequeueRequestMessage>
{
    private CasualDequeueRequestMessageDecoder()
    {}

    public static CasualDequeueRequestMessageDecoder of()
    {
        return new CasualDequeueRequestMessageDecoder();
    }

    @Override
    public CasualDequeueRequestMessage readSingleBuffer(final ReadableByteChannel channel, int messageSize)
    {
        ByteBuffer b = ByteUtils.readFully(channel, messageSize);
        return getMessage(b.array());
    }

    @Override
    public CasualDequeueRequestMessage readChunked(final ReadableByteChannel channel)
    {
        UUID execution = CasualMessageDecoderUtils.readUUID(channel);
        int queueNameSize = (int) ByteUtils.readFully(channel, DequeueRequestSizes.NAME_SIZE.getNetworkSize()).getLong();
        String queueName = CasualMessageDecoderUtils.readString(channel, queueNameSize);
        Xid xid = XIDUtils.readXid(channel);
        int selectorPropertiesSize = (int) ByteUtils.readFully(channel, DequeueRequestSizes.SELECTOR_PROPERTIES_SIZE.getNetworkSize()).getLong();
        String selectorProperties = (0 == selectorPropertiesSize) ? "" : CasualMessageDecoderUtils.readString(channel, selectorPropertiesSize);
        UUID selectorId = CasualMessageDecoderUtils.readUUID(channel);
        boolean block = (1 == (int) ByteUtils.readFully(channel, DequeueRequestSizes.BLOCK.getNetworkSize()).get());
        return CasualDequeueRequestMessage.createBuilder()
                                          .withExecution(execution)
                                          .withQueueName(queueName)
                                          .withXid(xid)
                                          .withSelectorProperties(selectorProperties)
                                          .withSelectorUUID(selectorId)
                                          .withBlock(block)
                                          .build();
    }

    @Override
    public CasualDequeueRequestMessage readSingleBuffer(byte[] data)
    {
        return getMessage(data);
    }

    private static CasualDequeueRequestMessage getMessage(final byte[] bytes)
    {
        int currentOffset = 0;
        UUID execution = CasualMessageDecoderUtils.getAsUUID(Arrays.copyOfRange(bytes, currentOffset, CommonSizes.EXECUTION.getNetworkSize()));
        currentOffset += CommonSizes.EXECUTION.getNetworkSize();
        int queueNameSize = (int)ByteBuffer.wrap(bytes, currentOffset , DequeueRequestSizes.NAME_SIZE.getNetworkSize()).getLong();
        currentOffset += DequeueRequestSizes.NAME_SIZE.getNetworkSize();
        String queueName = CasualMessageDecoderUtils.getAsString(bytes, currentOffset, queueNameSize);
        currentOffset += queueNameSize;
        Pair<Integer, Xid> xidInfo = CasualMessageDecoderUtils.readXid(bytes, currentOffset);
        currentOffset = xidInfo.first();
        Xid xid = xidInfo.second();
        int selectorPropertiesSize = (int)ByteBuffer.wrap(bytes, currentOffset , DequeueRequestSizes.SELECTOR_PROPERTIES_SIZE.getNetworkSize()).getLong();
        currentOffset += DequeueRequestSizes.SELECTOR_PROPERTIES_SIZE.getNetworkSize();
        String selectorProperties = (0 == selectorPropertiesSize) ? "" : CasualMessageDecoderUtils.getAsString(bytes, currentOffset, selectorPropertiesSize);
        currentOffset += selectorPropertiesSize;
        UUID selectorId = CasualMessageDecoderUtils.getAsUUID(Arrays.copyOfRange(bytes, currentOffset, currentOffset + DequeueRequestSizes.SELECTOR_ID_SIZE.getNetworkSize()));
        currentOffset += CommonSizes.EXECUTION.getNetworkSize();
        boolean block = (1 == (int)ByteBuffer.wrap(bytes, currentOffset , DequeueRequestSizes.BLOCK.getNetworkSize()).get());
        return CasualDequeueRequestMessage.createBuilder()
                                          .withExecution(execution)
                                          .withQueueName(queueName)
                                          .withXid(xid)
                                          .withSelectorProperties(selectorProperties)
                                          .withSelectorUUID(selectorId)
                                          .withBlock(block)
                                          .build();
    }
}
