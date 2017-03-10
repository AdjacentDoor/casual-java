package se.kodarkatten.casual.network.io.readers;

import se.kodarkatten.casual.network.io.readers.utils.CasualNetworkReaderUtils;
import se.kodarkatten.casual.network.io.readers.utils.DynamicArrayIndexPair;
import se.kodarkatten.casual.network.messages.exceptions.CasualTransportException;
import se.kodarkatten.casual.network.messages.parseinfo.DiscoveryReplySizes;
import se.kodarkatten.casual.network.messages.queue.Queue;
import se.kodarkatten.casual.network.messages.reply.CasualDomainDiscoveryReplyMessage;
import se.kodarkatten.casual.network.messages.service.Service;
import se.kodarkatten.casual.network.messages.service.TransactionType;

import java.nio.ByteBuffer;
import java.util.*;

/**
 * Created by aleph on 2017-03-08.
 */
public final class CasualDomainDiscoveryReplyMessageReader
{
    private CasualDomainDiscoveryReplyMessageReader()
    {}

    public static CasualDomainDiscoveryReplyMessage fromNetworkBytes(final List<byte[]> message)
    {
        Objects.requireNonNull(message, "byte[] is null");
        if(message.isEmpty())
        {
            throw new CasualTransportException("0 sized message");
        }
        if(1 == message.size())
        {
            return getMessage(message.get(0));
        }
        return getMessage(message);
    }

    private static CasualDomainDiscoveryReplyMessage getMessage(byte[] bytes)
    {
        int currentOffset = 0;
        final UUID execution = CasualNetworkReaderUtils.getAsUUID(Arrays.copyOfRange(bytes, currentOffset, DiscoveryReplySizes.EXECUTION.getNetworkSize()));
        currentOffset +=  DiscoveryReplySizes.EXECUTION.getNetworkSize();
        final UUID domainId = CasualNetworkReaderUtils.getAsUUID(Arrays.copyOfRange(bytes, currentOffset, currentOffset + DiscoveryReplySizes.DOMAIN_ID.getNetworkSize()));
        currentOffset += DiscoveryReplySizes.DOMAIN_ID.getNetworkSize();
        final int domainNameSize = (int) ByteBuffer.wrap(bytes, currentOffset , DiscoveryReplySizes.DOMAIN_NAME_SIZE.getNetworkSize()).getLong();
        currentOffset += DiscoveryReplySizes.DOMAIN_NAME_SIZE.getNetworkSize();
        final String domainName = CasualNetworkReaderUtils.getAsString(bytes, currentOffset, domainNameSize);
        currentOffset += domainNameSize;
        final long numberOfServices = ByteBuffer.wrap(bytes, currentOffset, DiscoveryReplySizes.SERVICES_SIZE.getNetworkSize()).getLong();
        currentOffset += DiscoveryReplySizes.SERVICES_SIZE.getNetworkSize();
        DynamicArrayIndexPair services = getServices(bytes, currentOffset, numberOfServices);
        currentOffset = services.getIndex();
        final long numberOfQueues = ByteBuffer.wrap(bytes, currentOffset, DiscoveryReplySizes.QUEUES_SIZE.getNetworkSize()).getLong();
        currentOffset += DiscoveryReplySizes.QUEUES_SIZE.getNetworkSize();
        DynamicArrayIndexPair queues = getQueues(bytes, currentOffset, numberOfQueues);

        return CasualDomainDiscoveryReplyMessage.of(execution, domainId, domainName)
                                                .setServices(services.getBytes())
                                                .setQueues(queues.getBytes());
    }

    private static DynamicArrayIndexPair getServices(final byte[] bytes, int currentOffset, long numberOfServices)
    {
        final List<Service> l = new ArrayList<>();
        int offset = currentOffset;
        for(int i = 0; i < numberOfServices; ++i)
        {
            offset = addService(bytes, offset, l);
        }
        return DynamicArrayIndexPair.of(l, offset);
    }

    private static int addService(final byte[] bytes, final int currentOffset, final List<Service> l)
    {
        int offset = currentOffset;
        final int nameSize = (int)ByteBuffer.wrap(bytes, offset, DiscoveryReplySizes.SERVICES_ELEMENT_NAME_SIZE.getNetworkSize()).getLong();
        offset += DiscoveryReplySizes.SERVICES_ELEMENT_NAME_SIZE.getNetworkSize();
        final String name = CasualNetworkReaderUtils.getAsString(bytes, offset, nameSize);
        offset += nameSize;
        final int categorySize = (int)ByteBuffer.wrap(bytes, offset, DiscoveryReplySizes.SERVICES_ELEMENT_CATEGORY_SIZE.getNetworkSize()).getLong();
        offset += DiscoveryReplySizes.SERVICES_ELEMENT_CATEGORY_SIZE.getNetworkSize();
        final String category = CasualNetworkReaderUtils.getAsString(bytes, offset, categorySize);
        offset += categorySize;
        final short transaction = ByteBuffer.wrap(bytes, offset, DiscoveryReplySizes.SERVICES_ELEMENT_TRANSACTION.getNetworkSize()).getShort();
        offset += DiscoveryReplySizes.SERVICES_ELEMENT_TRANSACTION.getNetworkSize();
        final long timeout = ByteBuffer.wrap(bytes, offset, DiscoveryReplySizes.SERVICES_ELEMENT_TIMEOUT.getNetworkSize()).getLong();
        offset += DiscoveryReplySizes.SERVICES_ELEMENT_TIMEOUT.getNetworkSize();
        final long hops = ByteBuffer.wrap(bytes, offset, DiscoveryReplySizes.SERVICES_ELEMENT_HOPS.getNetworkSize()).getLong();
        offset += DiscoveryReplySizes.SERVICES_ELEMENT_HOPS.getNetworkSize();
        final Service s = Service.of(name, category, TransactionType.unmarshal(transaction))
                                 .setTimeout(timeout)
                                 .setHops(hops);
        l.add(s);
        return offset;
    }

    private static DynamicArrayIndexPair getQueues(final byte[] bytes, final int currentOffset, final long numberOfServices)
    {
        final List<Queue> l = new ArrayList<>();
        int offset = currentOffset;
        for(int i = 0; i < numberOfServices; ++i)
        {
            offset = addQueue(bytes, offset, l);
        }
        return DynamicArrayIndexPair.of(l, offset);
    }

    private static int addQueue(final byte[] bytes, int currentOffset, final List<Queue> l)
    {
        int offset = currentOffset;
        final int nameSize = (int)ByteBuffer.wrap(bytes, offset, DiscoveryReplySizes.QUEUES_ELEMENT_SIZE.getNetworkSize()).getLong();
        offset += DiscoveryReplySizes.QUEUES_ELEMENT_SIZE.getNetworkSize();
        final String name = CasualNetworkReaderUtils.getAsString(bytes, offset, nameSize);
        offset += nameSize;
        final long retries = ByteBuffer.wrap(bytes, offset, DiscoveryReplySizes.QUEUES_ELEMENT_RETRIES.getNetworkSize()).getLong();
        offset += DiscoveryReplySizes.QUEUES_ELEMENT_RETRIES.getNetworkSize();
        final Queue q = Queue.of(name)
                             .setRetries(retries);
        l.add(q);
        return offset;
    }

    /**
     * Used when header payload > Integer.MAX_VALUE
     * @see CasualDomainDiscoveryReplyMessage::toNetworkBytesMultipleBuffers
     * To understand how message should be structured
     **/
    private static CasualDomainDiscoveryReplyMessage getMessage(List<byte[]> message)
    {
        int currentIndex = 0;
        final UUID execution = CasualNetworkReaderUtils.getAsUUID(message.get(currentIndex++));
        final UUID domainId = CasualNetworkReaderUtils.getAsUUID(message.get(currentIndex++));
        final ByteBuffer domainNameSizeBuffer = ByteBuffer.wrap(message.get(currentIndex++));
        final int domainNameSize = (int) domainNameSizeBuffer.getLong();
        final byte[] domainNameBytes = message.get(currentIndex++);
        if(domainNameBytes.length != domainNameSize)
        {
            throw new CasualTransportException("domainNameSize: " + domainNameSize + " but buffer has a length of " + domainNameBytes.length);
        }
        final String domainName = CasualNetworkReaderUtils.getAsString(domainNameBytes);
        final long numberOfServices = ByteBuffer.wrap(message.get(currentIndex++)).getLong();
        List<Service> services = new ArrayList<>();
        for(int i = 0; i < numberOfServices; ++i)
        {
            addService(message.get(currentIndex++), 0, services);
        }
        final long numberOfQueues = ByteBuffer.wrap(message.get(currentIndex++)).getLong();
        List<Queue> queues = new ArrayList<>();
        for(int i = 0; i < numberOfQueues; ++i)
        {
            addQueue(message.get(currentIndex++), 0, queues);
        }
        return CasualDomainDiscoveryReplyMessage.of(execution, domainId, domainName)
                                                .setServices(services)
                                                .setQueues(queues);
    }

}
