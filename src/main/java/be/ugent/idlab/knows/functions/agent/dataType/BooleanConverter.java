package be.ugent.idlab.knows.functions.agent.dataType;

/**
 * <p>Copyright 2022 IDLab (Ghent University - imec)</p>
 *
 * @author Gerald Haesendonck
 */
public class BooleanConverter extends DataTypeConverter<Boolean> {

    public BooleanConverter() {
        super(Boolean.class, TypeCategory.PRIMITIVE);
    }

    @Override
    public Boolean convert(Object value) throws DataTypeConverterException {
        if (value instanceof Boolean) {
            return (Boolean) value;
        } else {
            return Boolean.parseBoolean(value.toString());
        }
    }
}
