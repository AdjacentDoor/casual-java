/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca.inbound.handler.buffer

import se.laz.casual.jca.inbound.handler.test.TestBufferHandler
import se.laz.casual.jca.inbound.handler.test.TestBufferHandler2
import spock.lang.Specification
import spock.lang.Unroll

class BufferHandlerFactoryTest extends Specification
{

    def "GetHandlers"()
    {
        when:
        List<BufferHandler> actual = BufferHandlerFactory.getHandlers()

        then:
        actual.size() == 2
    }

    @Unroll
    def "GetHandler with a buffer type returns the handler that responses for that buffer type."()
    {
        when:
        BufferHandler h = BufferHandlerFactory.getHandler( serviceName )
        BufferHandler h2 = BufferHandlerFactory.getHandler( serviceName )

        then:
        h.getClass() == handlerType
        h2.getClass() == handlerType
        h == h2

        where:
        serviceName                     | handlerType
        TestBufferHandler.BUFFER_TYPE_1 | TestBufferHandler.class
        TestBufferHandler2.BUFFER_TYPE_2| TestBufferHandler2.class
    }

    def "GetHandler buffer type unknown, returns passthrough handler."()
    {
        when:
        BufferHandler h = BufferHandlerFactory.getHandler( "unknownn" )

        then:
        h.getClass() == PassThroughBufferHandler.class
    }

    def "GetHandler service in the right order when multiple can handle."()
    {
        when:
        BufferHandler h = BufferHandlerFactory.getHandler( bufferName )

        then:
        h.getClass() == handlerType

        where:
        bufferName            | handlerType
        TestBufferHandler.BUFFER_COMMON  | TestBufferHandler2.class
        TestBufferHandler2.BUFFER_COMMON | TestBufferHandler2.class
    }
}
