package be.ugent.idlab.knows.functions.agent.dataType;

import be.ugent.idlab.knows.functions.agent.functionModelProvider.fno.exception.UnsupportedDataTypeException;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>Copyright 2022 IDLab (Ghent University - imec)</p>
 *
 * @author Gerald Haesendonck
 */
public class DataTypeConverterProvider {

    private final Map<String, DataTypeConverter<?>> nameToConverter = new HashMap<>();

    public DataTypeConverterProvider() {
        addXMLSchemaConverters();
        addRDFConverters();
    }

    public DataTypeConverter<?> getDataTypeConverter(final String type) throws UnsupportedDataTypeException {
        return nameToConverter.getOrDefault(type, new DefaultDataTypeConverter());
    }

    private void addXMLSchemaConverters() {
        nameToConverter.put("http://www.w3.org/2001/XMLSchema#integer", new LongConverter());
        nameToConverter.put("http://www.w3.org/2001/XMLSchema#int", new IntegerConverter());
        nameToConverter.put("http://www.w3.org/2001/XMLSchema#long", new LongConverter());
        nameToConverter.put("http://www.w3.org/2001/XMLSchema#double", new DoubleConverter());
        nameToConverter.put("http://www.w3.org/2001/XMLSchema#decimal", new BigDecimalConverter());
        nameToConverter.put("http://www.w3.org/2001/XMLSchema#boolean", new XSBooleanConverter());
        nameToConverter.put("http://www.w3.org/2001/XMLSchema#string", new StringConverter());

    }

    public void addRDFConverters() {
        nameToConverter.put("http://www.w3.org/1999/02/22-rdf-syntax-ns#List", new ListConverter());
    }
}
