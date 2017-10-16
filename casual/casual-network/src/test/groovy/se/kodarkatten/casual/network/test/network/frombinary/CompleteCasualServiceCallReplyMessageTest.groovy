package se.kodarkatten.casual.network.test.network.frombinary

import se.kodarkatten.casual.network.io.CasualNetworkReader
import se.kodarkatten.casual.network.io.CasualNetworkWriter
import se.kodarkatten.casual.network.messages.CasualNWMessage
import se.kodarkatten.casual.network.messages.parseinfo.MessageHeaderSizes
import se.kodarkatten.casual.network.messages.service.CasualServiceCallReplyMessage
import se.kodarkatten.casual.network.utils.LocalAsyncByteChannel
import se.kodarkatten.casual.network.utils.LocalByteChannel
import se.kodarkatten.casual.network.utils.ResourceLoader
import se.kodarkatten.casual.network.utils.WriteCompletionHandler
import spock.lang.Shared
import spock.lang.Specification

import java.nio.ByteBuffer
import java.util.concurrent.CompletableFuture

/**
 * Created by aleph on 2017-03-28.
 */
class CompleteCasualServiceCallReplyMessageTest extends Specification
{
    @Shared
    def resource = '/protocol/bin/message.service.call.Reply.3101.bin'

    @Shared
    def data

    def setupSpec()
    {
        data = ResourceLoader.getResourceAsByteArray(resource)
        then:
        data != null
        data.length == 141
    }

    def "get header"()
    {
        setup:
        def headerData = Arrays.copyOfRange(data, 0, MessageHeaderSizes.headerNetworkSize)
        when:
        def header = CasualNetworkReader.networkHeaderToCasualHeader(headerData)
        then:
        header != null
    }

    def "roundtrip header"()
    {
        setup:
        def headerData = Arrays.copyOfRange(data, 0, MessageHeaderSizes.headerNetworkSize)
        def header = CasualNetworkReader.networkHeaderToCasualHeader(headerData)
        when:
        def resurrectedHeader = CasualNetworkReader.networkHeaderToCasualHeader(header.toNetworkBytes())
        then:
        header != null
        resurrectedHeader != null
        resurrectedHeader == header
        resurrectedHeader.payloadSize == 109
    }

    def "roundtrip message - chunk"()
    {
        setup:
        List<byte[]> payload = new ArrayList<>()
        payload.add(data)
        def sink = new LocalAsyncByteChannel()
        payload.each{
            bytes ->
                CompletableFuture<Void> future = new CompletableFuture<>()
                ByteBuffer buffer = ByteBuffer.wrap(bytes)
                sink.write(buffer, null, WriteCompletionHandler.of(future, buffer, sink))
                future.get()
        }
        when:
        CasualNWMessage<CasualServiceCallReplyMessage> msg = CasualNetworkReader.read(sink)
        msg.getMessage().setMaxMessageSize(1)
        CasualNetworkWriter.write(sink, msg)
        // force chunking when reading
        CasualNetworkReader.setMaxSingleBufferByteSize(1)
        CasualNWMessage<CasualServiceCallReplyMessage> resurrectedMsg = CasualNetworkReader.read(sink)
        CasualNetworkReader.setMaxSingleBufferByteSize(Integer.MAX_VALUE)
        then:
        msg != null
        msg.getMessage() == resurrectedMsg.getMessage()
        msg == resurrectedMsg
    }

    def "roundtrip message - no chunking"()
    {
        setup:
        List<byte[]> payload = new ArrayList<>()
        payload.add(data)
        def sink = new LocalAsyncByteChannel()
        payload.each{
            bytes ->
                CompletableFuture<Void> future = new CompletableFuture<>()
                ByteBuffer buffer = ByteBuffer.wrap(bytes)
                sink.write(buffer, null, WriteCompletionHandler.of(future, buffer, sink))
                future.get()
        }
        when:
        CasualNWMessage<CasualServiceCallReplyMessage> msg = CasualNetworkReader.read(sink)
        CasualNetworkWriter.write(sink, msg)
        CasualNWMessage<CasualServiceCallReplyMessage> resurrectedMsg = CasualNetworkReader.read(sink)
        then:
        msg != null
        msg.getMessage() == resurrectedMsg.getMessage()
        msg == resurrectedMsg
    }

    def "roundtrip message sync - chunk"()
    {
        setup:
        List<byte[]> payload = new ArrayList<>()
        payload.add(data)
        def sink = new LocalByteChannel()
        payload.each{
            bytes ->
                ByteBuffer buffer = ByteBuffer.wrap(bytes)
                sink.write(buffer)
        }
        when:
        CasualNWMessage<CasualServiceCallReplyMessage> msg = CasualNetworkReader.read(sink)
        msg.getMessage().setMaxMessageSize(1)
        CasualNetworkWriter.write(sink, msg)
        // force chunking when reading
        CasualNetworkReader.setMaxSingleBufferByteSize(1)
        CasualNWMessage<CasualServiceCallReplyMessage> resurrectedMsg = CasualNetworkReader.read(sink)
        CasualNetworkReader.setMaxSingleBufferByteSize(Integer.MAX_VALUE)
        then:
        msg != null
        msg.getMessage() == resurrectedMsg.getMessage()
        msg == resurrectedMsg
    }

    def "roundtrip message sync - no chunking"()
    {
        setup:
        List<byte[]> payload = new ArrayList<>()
        payload.add(data)
        def sink = new LocalByteChannel()
        payload.each{
            bytes ->
                ByteBuffer buffer = ByteBuffer.wrap(bytes)
                sink.write(buffer)
        }
        when:
        CasualNWMessage<CasualServiceCallReplyMessage> msg = CasualNetworkReader.read(sink)
        CasualNetworkWriter.write(sink, msg)
        CasualNWMessage<CasualServiceCallReplyMessage> resurrectedMsg = CasualNetworkReader.read(sink)
        then:
        msg != null
        msg.getMessage() == resurrectedMsg.getMessage()
        msg == resurrectedMsg
    }

}