package be.ugent.idlab.knows.functions.agent.functionModelProvider.fno;

import be.ugent.idlab.knows.functions.agent.dataType.*;
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
                // TODO: this should be converted to a 'long', see https://www.w3.org/TR/xmlschema-2/#built-in-datatypes
            case "http://www.w3.org/2001/XMLSchema#int":
                return new IntegerConverter();
            case "http://www.w3.org/2001/XMLSchema#double":
                return new DoubleConverter();
            case "http://www.w3.org/2001/XMLSchema#string":
                return new StringConverter();
            case "http://www.w3.org/1999/02/22-rdf-syntax-ns#List":
                return new RDFListConverter();
            case "http://www.w3.org/2001/XMLSchema#decimal":
                return new BigDecimalConverter();
            case "http://www.w3.org/2001/XMLSchema#boolean":
                return new XSBooleanConverter();

            default:
                throw new UnsupportedDataTypeException("No data type implementation found for " + type);
        }
    }
}
