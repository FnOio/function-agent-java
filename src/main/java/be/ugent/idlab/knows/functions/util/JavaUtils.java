package be.ugent.idlab.knows.functions.util;

import java.time.*;
import java.util.List;

/**
 * <p>Copyright 2022 IDLab (Ghent University - imec)</p>
 *
 * @author Gerald Haesendonck
 */
public class JavaUtils {

    public static Class<?> getParamType(String type) throws ClassNotFoundException {
        switch (type) {
            // This is quite crude, based on https://www.w3.org/TR/xmlschema11-2/#built-in-datatypes
            case "http://www.w3.org/2001/XMLSchema#any":
                return Object.class;
            case "http://www.w3.org/2001/XMLSchema#string":
                return String.class;
            case "http://www.w3.org/2001/XMLSchema#unsignedLong":
            case "http://www.w3.org/2001/XMLSchema#long":
                return Long.class;
            case "http://www.w3.org/2001/XMLSchema#integer":
            case "http://www.w3.org/2001/XMLSchema#int":
            case "http://www.w3.org/2001/XMLSchema#short":
            case "http://www.w3.org/2001/XMLSchema#byte":
            case "http://www.w3.org/2001/XMLSchema#nonNegativeInteger":
            case "http://www.w3.org/2001/XMLSchema#positiveInteger":
            case "http://www.w3.org/2001/XMLSchema#unsignedInt":
            case "http://www.w3.org/2001/XMLSchema#unsignedShort":
            case "http://www.w3.org/2001/XMLSchema#unsignedByte":
            case "http://www.w3.org/2001/XMLSchema#nonPositiveInteger":
            case "http://www.w3.org/2001/XMLSchema#negativeInteger":
                return Integer.class;
            case "http://www.w3.org/2001/XMLSchema#boolean":
                return Boolean.class;
            case "http://www.w3.org/2001/XMLSchema#date":
                // "Local" just means "without a time zone"
                return LocalDate.class;
            case "http://www.w3.org/2001/XMLSchema#dateTime":
                // again "Local" means "without a time zone"
                // (An xsd:dateTime actually has an OPTIONAL time zone, so there is a small semantic difference
                // with java.time.LocalDateTime, this is a best effort.)
                return LocalDateTime.class;
            case "http://www.w3.org/2001/XMLSchema#dateTimeStamp":
                return ZonedDateTime.class;
            case "http://www.w3.org/2001/XMLSchema#dayTimeDuration":
            case "http://www.w3.org/2001/XMLSchema#yearMonthDuration":
                return Duration.class;
            case "http://www.w3.org/2001/XMLSchema#gDay":
                // TODO there is no java.time equivalent of xsd:day
                // (There is java.time.DayOfWeek, but xsd:day would corresponds to java.time.DayOfMonth .)
                throw new DateTimeException("There is no java.time equivalent of xsd:day. Crashing.");
            case "http://www.w3.org/2001/XMLSchema#gMonth":
                return Month.class;
            case "http://www.w3.org/2001/XMLSchema#gMonthDay":
                return MonthDay.class;
            case "http://www.w3.org/2001/XMLSchema#gYear":
                return Year.class;
            case "http://www.w3.org/2001/XMLSchema#gYearMonth":
                return YearMonth.class;
            case "http://www.w3.org/2001/XMLSchema#decimal":
            case "http://www.w3.org/2001/XMLSchema#double":
            case "http://www.w3.org/2001/XMLSchema#float":
                return Double.class;
            case "http://www.w3.org/1999/02/22-rdf-syntax-ns#List":
                return List.class;
            default:
                throw new ClassNotFoundException("Couldn't derive Java class from " + type);
        }
    }
}
