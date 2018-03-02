package se.kodarkatten.casual.jca.inbound.handler.service.casual;

import se.kodarkatten.casual.api.service.CasualService;

import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

public class MethodBasedTransactionAnnotation implements SomeInterface
{
    @CasualService(name="someMethod" )
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    @Override
    public void someMethod()
    {

    }
}
