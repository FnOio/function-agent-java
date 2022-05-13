package be.ugent.idlab.knows.functions.agent.dataType;

/**
 * <p>Copyright 2022 IDLab (Ghent University - imec)</p>
 *
 * @author Gerald Haesendonck
 */
public class FloatConverter extends DataTypeConverter<Float> {
    public FloatConverter() {
        super(Float.class, TypeCategory.PRIMITIVE);
    }

    @Override
    public Float convert(Object value) throws DataTypeConverterException {
        if (value instanceof Float) {
            return  (Float) value;
        } else {
            return Float.parseFloat(value.toString());
        }
    }
}
