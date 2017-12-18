package se.kodarkatten.casual.api.testdata;

import se.kodarkatten.casual.api.buffer.type.fielded.annotation.CasualFieldElement;
import se.kodarkatten.casual.api.buffer.type.fielded.mapper.LocalDateMapper;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class PojoWithMappableParamList implements Serializable
{
    private static final long serialVersionUID = 1;
    private List<LocalDate> dates;
    private PojoWithMappableParamList(final List<LocalDate> dates)
    {
        this.dates = dates;
    }
    public static PojoWithMappableParamList of(final List<LocalDate> dates)
    {
        Objects.requireNonNull(dates);
        return new PojoWithMappableParamList(dates.stream().collect(Collectors.toList()));
    }

    @CasualFieldElement(name ="FLD_STRING1", lengthName = "FLD_LONG1", mapper = LocalDateMapper.class)
    public List<LocalDate> getDates()
    {
        return dates.stream().collect(Collectors.toList());
    }

    public PojoWithMappableParamList setDates(@CasualFieldElement(name ="FLD_STRING1", lengthName = "FLD_LONG1", mapper = LocalDateMapper.class)
                                              final List<LocalDate> dates)
    {
        this.dates = dates.stream().collect(Collectors.toList());
        return this;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        PojoWithMappableParamList that = (PojoWithMappableParamList) o;
        return Objects.equals(dates, that.dates);
    }

    @Override
    public int hashCode()
    {

        return Objects.hash(dates);
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder("PojoWithMappableFieldList{");
        sb.append("dates=").append(dates);
        sb.append('}');
        return sb.toString();
    }
}
