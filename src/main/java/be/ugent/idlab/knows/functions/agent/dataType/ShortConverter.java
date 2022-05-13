package be.ugent.idlab.knows.functions.agent.dataType;

/**
 * <p>Copyright 2022 IDLab (Ghent University - imec)</p>
 *
 * @author Gerald Haesendonck
 */
public class ShortConverter extends DataTypeConverter<Short> {

    public ShortConverter() {
        super(Short.class, TypeCategory.PRIMITIVE);
    }
    @Override
    public Short convert(Object value) throws DataTypeConverterException {
        if (value instanceof Short) {
            return  (Short) value;
        } else {
            return Short.parseShort(value.toString());
        }
    }
}
