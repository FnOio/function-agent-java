package be.ugent.idlab.knows.functions.agent.dataType;

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

    public DataTypeConverter<?> getDataTypeConverter(final String type) {
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
