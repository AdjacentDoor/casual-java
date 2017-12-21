package se.kodarkatten.casual.jca

import spock.lang.Shared
import spock.lang.Specification

class CasualRaMetaDataTest extends Specification
{
    @Shared CasualRaMetaData instance

    def setup()
    {
       instance = new CasualRaMetaData()
    }

    def "GetAdapterVersion is not null."()
    {
        expect:
        instance.getAdapterVersion() != null
    }

    def "GetAdapterVendorName is not null."()
    {
        expect:
        instance.getAdapterVendorName() != null
    }

    def "GetAdapterName is not null."()
    {
        expect:
        instance.getAdapterName() != null
    }

    def "GetAdapterShortDescription is not null."()
    {
        expect:
        instance.getAdapterShortDescription() != null
    }

    def "GetSpecVersion is not null."()
    {
        expect:
        instance.getSpecVersion() == "1.7"
    }

    def "GetInteractionSpecsSupported"()
    {
        expect:
        instance.getInteractionSpecsSupported() == null
    }

    def "SupportsExecuteWithInputAndOutputRecord"()
    {
        expect:
        !instance.supportsExecuteWithInputAndOutputRecord()
    }

    def "SupportsExecuteWithInputRecordOnly"()
    {
        expect:
        !instance.supportsExecuteWithInputRecordOnly()
    }

    def "SupportsLocalTransactionDemarcation"()
    {
        expect:
        !instance.supportsLocalTransactionDemarcation()
    }
}
