package se.kodarkatten.casual.api.buffer.type.fielded;

public final class Constants
{
    public static final int CASUAL_FIELD_TYPE_BASE = 0x2000000;
    /**
     * Environment variable that should point to your json
     */
    public static final String CASUAL_FIELD_TABLE = "CASUAL_FIELD_TABLE";
    /**
     * Only used for internal testing, you should set the environment variable CASUAL_FIELD_TABLE to point to your json
     */
    public static final String CASUAL_FIELD_JSON_EMBEDDED = "casual-fields.json";
    private Constants()
    {}
}
