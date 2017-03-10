package se.kodarkatten.casual.network.messages.service;

import java.util.Arrays;
import java.util.Optional;

/**
 * Created by aleph on 2017-03-07.
 */
public enum TransactionType
{
    AUTOMATIC((short)0),
    JOIN((short)1),
    ATOMIC((short)2),
    NONE((short)3);

    private final short id;
    TransactionType(short id)
    {
        this.id = id;
    }

    public short getId()
    {
        return id;
    }

    public static short marshal(TransactionType t)
    {
        return t.getId();
    }

    public static final TransactionType unmarshal(short n)
    {
        Optional<TransactionType> t = Arrays.stream(TransactionType.values())
                                            .filter(v -> v.getId() == n)
                                            .findFirst();
        return t.orElseThrow(() -> new IllegalArgumentException("TransactionType:" + n));
    }

}
