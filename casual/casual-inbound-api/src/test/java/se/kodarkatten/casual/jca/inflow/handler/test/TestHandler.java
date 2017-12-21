package se.kodarkatten.casual.jca.inflow.handler.test;

import se.kodarkatten.casual.jca.inbound.handler.CasualHandler;
import se.kodarkatten.casual.jca.inbound.handler.InboundRequest;
import se.kodarkatten.casual.jca.inbound.handler.InboundResponse;

public class TestHandler implements CasualHandler
{
    public static final String SERVICE_1 = "testService1";

    @Override
    public boolean canHandleService(String serviceName)
    {
        if( serviceName.equals( SERVICE_1 ) )
        {
            return true;
        }
        return false;
    }

    @Override
    public boolean isServiceAvailable(String serviceName)
    {
        return false;
    }

    @Override
    public InboundResponse invokeService(InboundRequest request)
    {
        return null;
    }


}