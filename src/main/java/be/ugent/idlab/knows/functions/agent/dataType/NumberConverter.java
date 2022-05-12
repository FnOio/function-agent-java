package be.ugent.idlab.knows.functions.agent.dataType;

import java.math.BigDecimal;

/**
 * <p>Copyright 2022 IDLab (Ghent University - imec)</p>
 *
 * @author Gerald Haesendonck
 */
public class NumberConverter extends DataTypeConverter<Number> {

    public NumberConverter() {
                super(Number.class, TypeCategory.PRIMITIVE, Integer.class, Short.class, Float.class, Double.class);
    }

    @Override
    public Number convert(Object value) {
        if (value instanceof Number) {
            return (Number) value;
        } else {
            return new BigDecimal(value.toString());
        }
    }
}
