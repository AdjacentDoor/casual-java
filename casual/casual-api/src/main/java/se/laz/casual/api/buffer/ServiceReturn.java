/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.api.buffer;

import se.laz.casual.api.flags.ErrorState;
import se.laz.casual.api.flags.ServiceReturnState;

/**
 * The result of a service call
 * Note that you should never yourself instantiate an instance of this class but it
 * is returned to you upon a service call via the @see CasualServiceApi
 * @author jone
 */
public final class ServiceReturn<X extends CasualBuffer>
{
    private final X replyBuffer;

    private final ServiceReturnState serviceReturnState;

    private final ErrorState errorState;

    private final long userDefinedCode;

    public ServiceReturn(X replyBuffer, ServiceReturnState serviceReturnState, ErrorState errorState, long userDefinedCode)
    {
        this.replyBuffer = replyBuffer;
        this.serviceReturnState = serviceReturnState;
        this.errorState = errorState;
        this.userDefinedCode = userDefinedCode;
    }

    /**
     * @return a subtype of CasualBuffer
     */
    public X getReplyBuffer()
    {
        return replyBuffer;
    }

    /**
     * @return either {@code ServiceReturnState.TPFAIL}TPFAIL or {@code ServiceReturnState.TPSUCCESS}
     */
    public ServiceReturnState getServiceReturnState()
    {
        return serviceReturnState;
    }

    /**
     * Both {@code ErrorState.OK} and {@code ErrorState.TPESVCFAIL} could imply that there's data to handle in the reply buffer
     * That is, on ErrorState.TPESVCFAIL extra information could be transported - for instance an exception
     * On ErrorState.OK the buffer would contain whatever the service normally returns - if anything
     * @return The error state
     */
    public ErrorState getErrorState()
    {
        return errorState;
    }

    /**
     * @return user defined code if any - 0 if not
     */
    public long getUserDefinedCode()
    {
        return userDefinedCode;
    }
}
