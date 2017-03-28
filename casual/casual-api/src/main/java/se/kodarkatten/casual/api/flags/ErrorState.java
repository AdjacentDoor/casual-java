package se.kodarkatten.casual.api.flags;

/**
 * @author jone
 */
public enum ErrorState
{
    TPEBADDESC(2),
    TPEBLOCK(3),
    TPEINVAL(4),
    TPELIMIT(5),
    TPENOENT(6),
    TPEOS(7),
    TPEPROTO(9),
    TPESVCERR(10),
    TPESVCFAIL(11),
    TPESYSTEM(12),
    TPETIME(13),
    TPETRAN(14),
    TPGOTSIG(15),
    TPEITYPE(17),
    TPEOTYPE(18),
    TPEEVENT(22),
    TPEMATCH(23);

    private final int value;

    ErrorState(final int value)
    {
        this.value = value;
    }

    public int getValue()
    {
        return value;
    }
}