package be.ugent.idlab.knows.functions.agent.dataType;

import java.math.BigDecimal;

/**
 * <p>Copyright 2022 IDLab (Ghent University - imec)</p>
 *
 * @author Gerald Haesendonck
 */
public class BigDecimalConverter extends DataTypeConverter<BigDecimal> {

    public BigDecimalConverter() {
        super(BigDecimal.class, TypeCategory.PRIMITIVE);
    }

    @Override
    public BigDecimal convert(Object value) throws DataTypeConverterException {
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        } else {
            return new BigDecimal(value.toString());
        }
    }
}
