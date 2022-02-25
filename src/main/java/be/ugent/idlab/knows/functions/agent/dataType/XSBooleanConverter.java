package be.ugent.idlab.knows.functions.agent.dataType;

/**
 * <p>Copyright 2022 IDLab (Ghent University - imec)</p>
 *
 * @author Gerald Haesendonck
 */
public class XSBooleanConverter extends DataTypeConverter<Boolean> {
    public XSBooleanConverter() {
        super(Boolean.class);
    }

    @Override
    public Boolean convert(Object value) throws DataTypeConverterException {
        if (value instanceof Boolean) {
            return (Boolean) value;
        } else {
            final String valueString = value.toString();
            if (valueString.equals("true") || valueString.equals("1")) {
                return true;
            } else if (valueString.equals("false") || valueString.equals("0")) {
                return false;
            }
        }
        throw new DataTypeConverterException("Cannot convert object of type " + value.getClass().getName() + " to a XML Schema Boolean");
    }
}
