package se.kodarkatten.casual.jca.inbound.handler.service.casual;

import se.kodarkatten.casual.network.messages.domain.TransactionType;

import javax.ejb.TransactionAttributeType;
import java.util.EnumMap;
import java.util.Objects;

import static javax.ejb.TransactionAttributeType.MANDATORY;
import static javax.ejb.TransactionAttributeType.NEVER;
import static javax.ejb.TransactionAttributeType.NOT_SUPPORTED;
import static javax.ejb.TransactionAttributeType.REQUIRED;
import static javax.ejb.TransactionAttributeType.REQUIRES_NEW;
import static javax.ejb.TransactionAttributeType.SUPPORTS;
import static se.kodarkatten.casual.network.messages.domain.TransactionType.ATOMIC;
import static se.kodarkatten.casual.network.messages.domain.TransactionType.AUTOMATIC;
import static se.kodarkatten.casual.network.messages.domain.TransactionType.JOIN;
import static se.kodarkatten.casual.network.messages.domain.TransactionType.NONE;

/**
 * Mappings for Java EE TransactionAttributeType to Casual Transaction Types:
 *
 * MANDATORY	    Join
 * NEVER			None
 * NOT_SUPPORTED	None
 * REQUIRED		    Auto
 * REQUIRES_NEW	    Atomic
 * SUPPORTS		    Join
 */
public final class TransactionTypeMapper
{
    private static EnumMap<TransactionAttributeType, TransactionType> mappings = new EnumMap<>(TransactionAttributeType.class);

    static{
        mappings.put( MANDATORY,        JOIN );
        mappings.put( NEVER,            NONE );
        mappings.put( NOT_SUPPORTED,    NONE );
        mappings.put( REQUIRED,         AUTOMATIC );
        mappings.put( REQUIRES_NEW,     ATOMIC );
        mappings.put( SUPPORTS,         JOIN );
    }

    /**
     * Map the provided {@link TransactionAttributeType} to the appropriate
     * {@link TransactionType}.
     * @param type to map
     * @return mapped results.
     */
    public static TransactionType map( TransactionAttributeType type )
    {
        Objects.requireNonNull( type,"TransactionAttributeType can not be null." );
        return mappings.get( type );
    }

    private TransactionTypeMapper()
    {

    }
}
