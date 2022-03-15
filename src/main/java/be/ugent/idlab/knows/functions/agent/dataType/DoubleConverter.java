package be.ugent.idlab.knows.functions.agent.dataType;

/**
 * <p>Copyright 2022 IDLab (Ghent University - imec)</p>
 *
 * @author Gerald Haesendonck
 */
public class DoubleConverter extends DataTypeConverter<Double> {
    public DoubleConverter() {
        super(Double.class, TypeCategory.PRIMITIVE);
    }

    @Override
    public Double convert(Object value) throws DataTypeConverterException {
        if (value instanceof Double) {
            return (Double)value;
        } else {
            return Double.parseDouble(value.toString());
        }
    }
}
