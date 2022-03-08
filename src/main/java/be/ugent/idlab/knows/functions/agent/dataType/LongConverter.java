package be.ugent.idlab.knows.functions.agent.dataType;

/**
 * <p>Copyright 2022 IDLab (Ghent University - imec)</p>
 *
 * @author Gerald Haesendonck
 */
public class LongConverter extends DataTypeConverter<Long> {

    protected LongConverter() {
        super(Long.class);
    }

    @Override
    public Long convert(Object value) throws DataTypeConverterException {
        if (value instanceof Long) {
            return  (Long)value;
        } else {
            return Long.parseLong(value.toString());
        }
    }
}
