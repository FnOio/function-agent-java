package be.ugent.idlab.knows.functions.agent.dataType;

import java.util.Arrays;
import java.util.List;

/**
 * <p>Copyright 2022 IDLab (Ghent University - imec)</p>
 *
 * @author Gerald Haesendonck
 */
public class XSDoubleConverter implements DataTypeConverter<Double> {
    @Override
    public Double convert(Object value) {
        if (value instanceof Double) {
            return (Double)value;
        }
        if (value instanceof String) {
            return Double.parseDouble(value.toString());
        }
        return null;
    }

    @Override
    public List<Class<?>> getTypeClasses() {
        return Arrays.asList(Double.class, double.class);
    }
}
