package se.kodarkatten.casual.api.buffer.type.fielded;

import se.kodarkatten.casual.api.buffer.type.fielded.impl.FieldedDataImpl;
import se.kodarkatten.casual.api.buffer.type.fielded.json.CasualField;
import se.kodarkatten.casual.api.buffer.type.fielded.json.CasualFieldedLookup;
import se.kodarkatten.casual.api.buffer.type.fielded.json.CasualFieldedLookupException;
import se.kodarkatten.casual.api.util.Pair;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.nio.ByteBuffer;

public final class FieldedTypeBufferDecoder
{
    private FieldedTypeBufferDecoder()
    {}

    public static Map<String, List<FieldedData<?>>> decode(final List<byte[]> l)
    {
        Map<String, List<FieldedData<?>>> m = new HashMap<>();
        for(final byte[] b : l)
        {
            parseData(m, b);
        }
        if(m.isEmpty())
        {
            throw new CasualFieldedLookupException("could not parse any data, this is clearly wrong");
        }
        return m;
    }

    private static void parseData(final Map<String, List<FieldedData<?>>> m, final byte[] b)
    {
        int currentIndex = 0;
        while(currentIndex < b.length)
        {
            long realFieldId = getNextRealFieldId(b, currentIndex);
            currentIndex += FieldSize.FIELD_ID.getSize();
            CasualField f = CasualFieldedLookup.forRealId(realFieldId).orElseThrow(() -> new CasualFieldedLookupException("field with real id: " + realFieldId + " does not exist!"));
            if (!m.containsKey(f.getName()))
            {
                m.put(f.getName(), new ArrayList<>());
            }
            List<FieldedData<?>> lf = m.get(f.getName());
            Pair<FieldedData<?>, Integer> p = createFieldData(f.getType(), b, currentIndex);
            lf.add(p.first());
            currentIndex = p.second();
        }
    }

    private static long getNextRealFieldId(final byte[] b, int currentIndex)
    {
        return ByteBuffer.wrap(b, currentIndex, FieldSize.FIELD_ID.getSize()).getLong();
    }

    @SuppressWarnings("squid:S1226")
    private static Pair<FieldedData<?>, Integer> createFieldData(FieldType type, final byte[] b, int currentIndex)
    {
        int size = (int)ByteBuffer.wrap(b, currentIndex, FieldSize.FIELD_SIZE.getSize()).getLong();
        currentIndex += FieldSize.FIELD_SIZE.getSize();
        byte[] data = allocateFieldStorage(size, type);
        ByteBuffer.wrap(b, currentIndex, data.length).get(data);
        currentIndex += size;
        return Pair.of(getFieldedData(type, data), currentIndex);
    }

    private static byte[] allocateFieldStorage(int size, FieldType type)
    {
        // fielded strings are transported as nullterminated cstrings regardless of actual encoding
        return (type == FieldType.CASUAL_FIELD_STRING) ? new byte[size - 1] : new byte[size];
    }

    private static FieldedData<?> getFieldedData(FieldType type, byte[] data)
    {
        switch(type)
        {
            case CASUAL_FIELD_SHORT:
                return FieldedDataImpl.of(ByteBuffer.wrap(data).getShort(), FieldType.CASUAL_FIELD_SHORT);
            case CASUAL_FIELD_LONG:
                return FieldedDataImpl.of(ByteBuffer.wrap(data).getLong(), FieldType.CASUAL_FIELD_LONG);
            case CASUAL_FIELD_CHAR:
                return FieldedDataImpl.of(ByteBuffer.wrap(data).get(), FieldType.CASUAL_FIELD_CHAR);
            case CASUAL_FIELD_FLOAT:
                return FieldedDataImpl.of(ByteBuffer.wrap(data).getFloat(), FieldType.CASUAL_FIELD_FLOAT);
            case CASUAL_FIELD_DOUBLE:
                return FieldedDataImpl.of(ByteBuffer.wrap(data).getDouble(), FieldType.CASUAL_FIELD_DOUBLE);
            case CASUAL_FIELD_STRING:
                return FieldedDataImpl.of(new String(data, StandardCharsets.UTF_8), FieldType.CASUAL_FIELD_STRING);
            case CASUAL_FIELD_BINARY:
                return FieldedDataImpl.of(data, FieldType.CASUAL_FIELD_BINARY);
            default:
                throw new CasualFieldedLookupException("Unsupported field type: " + type);
        }
    }

}