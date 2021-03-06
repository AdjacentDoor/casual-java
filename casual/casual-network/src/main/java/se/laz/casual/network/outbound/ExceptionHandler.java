/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.outbound;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import se.laz.casual.internal.jca.ManagedConnectionInvalidator;
import se.laz.casual.network.CasualDecoderException;
import se.laz.casual.network.connection.CasualConnectionException;
import se.laz.casual.api.network.protocol.messages.exception.CasualProtocolException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class ExceptionHandler extends ChannelInboundHandlerAdapter
{
    private final ManagedConnectionInvalidator invalidator;
    private final Correlator correlator;

    private ExceptionHandler(ManagedConnectionInvalidator invalidator, Correlator correlator)
    {
        this.invalidator = invalidator;
        this.correlator = correlator;
    }

    public static ExceptionHandler of(final ManagedConnectionInvalidator invalidator, final Correlator correlator)
    {
        Objects.requireNonNull(invalidator, "invalidator can not be null");
        Objects.requireNonNull(correlator, "correlator can not be null");
        return new ExceptionHandler(invalidator, correlator);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
    {
        if(cause instanceof CasualDecoderException)
        {
            // we can handle if it was a protocol exception
            CasualDecoderException e = (CasualDecoderException)cause;
            if(e.getCause() instanceof CasualProtocolException)
            {
                List<UUID> l = new ArrayList<>();
                l.add(e.getCorrid());
                correlator.completeExceptionally(l, e);
                return;
            }
        }
        // anything else and we can not handle it ( ie network connection gone)
        correlator.completeAllExceptionally(new CasualConnectionException(cause));
        invalidator.invalidate(new CasualConnectionException(cause));
    }
}
