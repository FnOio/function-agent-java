package be.ugent.idlab.knows.functions.agent.dataType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * <p>Copyright 2022 IDLab (Ghent University - imec)</p>
 *
 * @author Gerald Haesendonck
 */
public class DataTypeConverterProvider {

    private final Map<String, DataTypeConverter<?>> nameToConverter = new HashMap<>();

    public DataTypeConverterProvider() {
        addJavaConverters();
        addXMLSchemaConverters();
        addRDFConverters();
    }

    /**
     * Returns a DataTypeConverter for the given data type.
     * @param type  The data type to find a DataTypeConverter for
     * @return      A DataTypeConverter for the given data type, or the DefaultDataTypeConverter if no converter
     *              for the given data type is found.
     */
    public DataTypeConverter<?> getDataTypeConverter(final String type) {
        return nameToConverter.getOrDefault(type, new DefaultDataTypeConverter());
    }

    /**
     * Returns a DataTypeConverter for the given data type. If no converter is found for the given type,
     * it looks for a DataTypeConverter that can handle a subtype of the given type.
     *
     * @param type  The data type to find a DataTypeConverter for
     * @return      A DataTypeConverter for the given data type (or a subtype thereof),
     *              or the DefaultDataTypeConverter if no converter for the given data type is found.
     */
    public DataTypeConverter<?> getDataTypeConverterWhichProcessesSubTypeOf(final String type) {
        if (nameToConverter.containsKey(type)) {
            return nameToConverter.get(type);
        } else {
            try {
                Class<?> theClass = Class.forName(type);
                Optional<DataTypeConverter<?>> candidate = nameToConverter.values().stream()
                        .filter(dataTypeConverter -> dataTypeConverter.isSubTypeOf(theClass))
                        .findFirst();
                return candidate.orElse(new DefaultDataTypeConverter());
            } catch (ClassNotFoundException e) {
                return new DefaultDataTypeConverter();
            }

        }
    }

    private void addJavaConverters() {
        nameToConverter.put(Byte.class.getName(), new ByteConverter());
        nameToConverter.put(byte.class.getName(), new ByteConverter());
        nameToConverter.put(Character.class.getName(), new CharacterConverter());
        nameToConverter.put(char.class.getName(), new CharacterConverter());
        nameToConverter.put(Short.class.getName(), new ShortConverter());
        nameToConverter.put(short.class.getName(), new ShortConverter());
        nameToConverter.put(Integer.class.getName(), new IntegerConverter());
        nameToConverter.put(int.class.getName(), new IntegerConverter());
        nameToConverter.put(Long.class.getName(), new LongConverter());
        nameToConverter.put(long.class.getName(), new LongConverter());
        nameToConverter.put(Float.class.getName(), new FloatConverter());
        nameToConverter.put(float.class.getName(), new FloatConverter());
        nameToConverter.put(Double.class.getName(), new DoubleConverter());
        nameToConverter.put(double.class.getName(), new DoubleConverter());
        nameToConverter.put(Boolean.class.getName(), new BooleanConverter());
        nameToConverter.put(boolean.class.getName(), new BooleanConverter());
        nameToConverter.put(String.class.getName(), new StringConverter());
        nameToConverter.put(List.class.getName(), new ListConverter());
    }

    private void addXMLSchemaConverters() {
        nameToConverter.put("http://www.w3.org/2001/XMLSchema#integer", new LongConverter());
        nameToConverter.put("http://www.w3.org/2001/XMLSchema#int", new IntegerConverter());
        nameToConverter.put("http://www.w3.org/2001/XMLSchema#long", new LongConverter());
        nameToConverter.put("http://www.w3.org/2001/XMLSchema#double", new DoubleConverter());
        nameToConverter.put("http://www.w3.org/2001/XMLSchema#decimal", new NumberConverter());
        nameToConverter.put("http://www.w3.org/2001/XMLSchema#boolean", new XSBooleanConverter());
        nameToConverter.put("http://www.w3.org/2001/XMLSchema#string", new StringConverter());

    }

    public void addRDFConverters() {
        nameToConverter.put("http://www.w3.org/1999/02/22-rdf-syntax-ns#List", new ListConverter());
    }

    //////////////////////////////////////////////////////////////////////////////////
    // Below is a list of conversions used by RMLMappper. TODO: implement all of them
    //////////////////////////////////////////////////////////////////////////////////
    /*
     *             case "http://www.w3.org/2001/XMLSchema#any":
     *                 return Object.class;
     *             case "http://www.w3.org/2001/XMLSchema#string":
     *                 return String.class;
     *             case "http://www.w3.org/2001/XMLSchema#unsignedLong":
     *             case "http://www.w3.org/2001/XMLSchema#long":
     *                 return Long.class;
     *             case "http://www.w3.org/2001/XMLSchema#integer":
     *             case "http://www.w3.org/2001/XMLSchema#int":
     *             case "http://www.w3.org/2001/XMLSchema#short":
     *             case "http://www.w3.org/2001/XMLSchema#byte":
     *             case "http://www.w3.org/2001/XMLSchema#nonNegativeInteger":
     *             case "http://www.w3.org/2001/XMLSchema#positiveInteger":
     *             case "http://www.w3.org/2001/XMLSchema#unsignedInt":
     *             case "http://www.w3.org/2001/XMLSchema#unsignedShort":
     *             case "http://www.w3.org/2001/XMLSchema#unsignedByte":
     *             case "http://www.w3.org/2001/XMLSchema#nonPositiveInteger":
     *             case "http://www.w3.org/2001/XMLSchema#negativeInteger":
     *                 return Integer.class;
     *             case "http://www.w3.org/2001/XMLSchema#boolean":
     *                 return Boolean.class;
     *             case "http://www.w3.org/2001/XMLSchema#date":
     *                 // "Local" just means "without a time zone"
     *                 return LocalDate.class;
     *             case "http://www.w3.org/2001/XMLSchema#dateTime":
     *                 // again "Local" means "without a time zone"
     *                 // (An xsd:dateTime actually has an OPTIONAL time zone, so there is a small semantic difference
     *                 // with java.time.LocalDateTime, this is a best effort.)
     *                 return LocalDateTime.class;
     *             case "http://www.w3.org/2001/XMLSchema#dateTimeStamp":
     *                 return ZonedDateTime.class;
     *             case "http://www.w3.org/2001/XMLSchema#dayTimeDuration":
     *             case "http://www.w3.org/2001/XMLSchema#yearMonthDuration":
     *                 return Duration.class;
     *             case "http://www.w3.org/2001/XMLSchema#gDay":
     *                 // (There is java.time.DayOfWeek, but xsd:day would corresponds to java.time.DayOfMonth .)
     *                 throw new DateTimeException("There is no java.time equivalent of xsd:day. Crashing.");
     *             case "http://www.w3.org/2001/XMLSchema#gMonth":
     *                 return Month.class;
     *             case "http://www.w3.org/2001/XMLSchema#gMonthDay":
     *                 return MonthDay.class;
     *             case "http://www.w3.org/2001/XMLSchema#gYear":
     *                 return Year.class;
     *             case "http://www.w3.org/2001/XMLSchema#gYearMonth":
     *                 return YearMonth.class;
     *             case "http://www.w3.org/2001/XMLSchema#decimal":
     *             case "http://www.w3.org/2001/XMLSchema#double":
     *             case "http://www.w3.org/2001/XMLSchema#float":
     *                 return Double.class;
     *             case "http://www.w3.org/1999/02/22-rdf-syntax-ns#List":
     *                 return List.class;
     *             default:
     *                 throw new Error("Couldn't derive type from " + type);
     */
}
