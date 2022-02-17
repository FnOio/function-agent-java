package be.ugent.idlab.knows.functions.agent.functionModelProvider.fno;

import be.ugent.idlab.knows.functions.agent.dataType.DataTypeConverter;
import be.ugent.idlab.knows.functions.agent.dataType.IntegerConverter;
import be.ugent.idlab.knows.functions.agent.dataType.StringConverter;
import be.ugent.idlab.knows.functions.agent.dataType.XSDoubleConverter;
import be.ugent.idlab.knows.functions.agent.functionModelProvider.fno.exception.UnsupportedDataTypeException;

/**
 * <p>Copyright 2022 IDLab (Ghent University - imec)</p>
 *
 * @author Gerald Haesendonck
 */
public class FnODataTypeConverterProvider {
    public static DataTypeConverter<?> getDataTypeConverter(final String type) throws UnsupportedDataTypeException {
        switch (type) {
            case "http://www.w3.org/2001/XMLSchema#integer":
                return new IntegerConverter();
            case "http://www.w3.org/2001/XMLSchema#double":
                return new XSDoubleConverter();
            case "http://www.w3.org/2001/XMLSchema#string":
                return new StringConverter();
            default:
                throw new UnsupportedDataTypeException("No data type implementation found for " + type);
        }
    }
}
