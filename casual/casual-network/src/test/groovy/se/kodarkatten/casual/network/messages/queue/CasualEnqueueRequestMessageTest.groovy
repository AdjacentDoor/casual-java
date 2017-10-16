package se.kodarkatten.casual.network.messages.queue

import se.kodarkatten.casual.api.xa.XID
import se.kodarkatten.casual.network.io.CasualNetworkReader
import se.kodarkatten.casual.network.messages.CasualNWMessage
import se.kodarkatten.casual.network.messages.service.ServiceBuffer
import se.kodarkatten.casual.network.utils.LocalAsyncByteChannel
import se.kodarkatten.casual.network.utils.LocalByteChannel
import se.kodarkatten.casual.network.utils.TestUtils
import spock.lang.Shared
import spock.lang.Specification

import java.time.LocalDateTime
import java.time.OffsetDateTime

class CasualEnqueueRequestMessageTest extends Specification
{
    @Shared
    def serviceData
    @Shared
    def serviceType = 'application/json'
    @Shared
    def serviceBuffer
    @Shared
    def asyncSink
    @Shared
    def syncSink

    def setupSpec()
    {
        List<byte[]> l = new ArrayList<>()
        l.add('{"hello" : "world"}'.bytes)
        serviceData = l
        serviceBuffer = ServiceBuffer.of(serviceType, serviceData)
    }

    def setup()
    {
        asyncSink = new LocalAsyncByteChannel()
        syncSink = new LocalByteChannel()
    }

    def cleanup()
    {
        asyncSink = null
        syncSink = null
    }

    def "roundtrip"()
    {
        setup:
        def enqueueMsg = EnqueueMessage.createBuilder()
                                       .withId(UUID.randomUUID())
                                       .withReplyData("replydata")
                                       .withProperties("properties")
                                       .withAvailableForDequeueSince(LocalDateTime.now().toEpochSecond(OffsetDateTime.now().getOffset()))
                                       .withPayload(serviceBuffer)
                                       .build()
        def requestMsg = CasualEnqueueRequestMessage.createBuilder()
                                                    .withExecution(UUID.randomUUID())
                                                    .withQueueName("best.queue.ever")
                                                    .withXid(XID.of())
                                                    .withMessage(enqueueMsg)
                                                    .build()
        CasualNWMessage msg = CasualNWMessage.of(UUID.randomUUID(), requestMsg)
        when:
        def networkBytes = msg.toNetworkBytes()
        CasualNWMessage<CasualEnqueueRequestMessage> asyncResurrectedMsg = TestUtils.roundtripMessage(msg, asyncSink)
        CasualNWMessage<CasualEnqueueRequestMessage> syncResurrectedMsg = TestUtils.roundtripMessage(msg, syncSink)
        then:
        networkBytes != null
        msg == asyncResurrectedMsg
        msg == syncResurrectedMsg
    }

    def "roundtrip - force chunking"()
    {
        setup:
        def enqueueMsg = EnqueueMessage.createBuilder()
                .withId(UUID.randomUUID())
                .withReplyData("replydata")
                .withProperties("properties")
                .withAvailableForDequeueSince(LocalDateTime.now().toEpochSecond(OffsetDateTime.now().getOffset()))
                .withPayload(serviceBuffer)
                .build()
        def requestMsg = CasualEnqueueRequestMessage.createBuilder()
                .withExecution(UUID.randomUUID())
                .withQueueName("best.queue.ever")
                .withXid(XID.of())
                .withMessage(enqueueMsg)
                .build()
        CasualNWMessage msg = CasualNWMessage.of(UUID.randomUUID(), requestMsg)
        when:
        def networkBytes = msg.toNetworkBytes()
        CasualNetworkReader.setMaxSingleBufferByteSize(1)
        CasualNWMessage<CasualEnqueueRequestMessage> asyncResurrectedMsg  = TestUtils.roundtripMessage(msg, asyncSink)
        CasualNWMessage<CasualEnqueueRequestMessage> syncResurrectedMsg  = TestUtils.roundtripMessage(msg, syncSink)
        CasualNetworkReader.setMaxSingleBufferByteSize(Integer.MAX_VALUE)
        then:
        networkBytes != null
        msg == asyncResurrectedMsg
        msg == syncResurrectedMsg
    }

}