package be.ugent.idlab.knows.functions.agent.model;

/**
 * <p>Copyright 2022 IDLab (Ghent University - imec)</p>
 *
 * @author Gerald Haesendonck
 */
public enum DataType {

//    ANY(Object.class),
//
//    STRING(String.class),
//
//    UNSIGNED_LONG(Long.class),
//    LONG(Long.class),
//
//    INTEGER(Integer.class),
//    NON_NEGATIVE_INTEGER(Integer.class),
//    POSITIVE_INTEGER(Integer.class),
//    NON_POSITIVE_INTEGER(Integer.class),
//    NEGATIVE_INTEGER(Integer.class),
//    UNSIGNED_INTEGER(Integer.class),
//
//    SHORT(Short.class),
//    UNSIGNED_SHORT(Short.class),
//
//    BYTE(Byte.class),
//    UNSIGNED_BYTE(Byte.class),
//
//    BOOLEAN(Boolean.class),
//
//    FLOAT(Float.class),

    // TODO: following types are usually marchalled to String; is there a point in providing these here?
//    
//    DATE(LocalDate.class),
//    DATE_TIME(LocalDateTime.class),
//    DATE_TIMESTAMP(ZonedDateTime.class),
//
//    // Gregorian calendar based durations
//    DAY_TIME_DURATION(Duration.class),
//    YEAR_MONTH_DURATUION(Duration.class),
//    DURATION(Duration.class),
//
//
//    //////////////////////////////////////////////////
//    // Data types below are not based on XML Schema //
//    //////////////////////////////////////////////////
//
//    // Date-based amount of time in the ISO-8601 calendar system, such as '2 years, 3 months and 4 days'.
//    PERIOD(Period.class),
//
//    // A time-based amount of time, such as '34.5 seconds'.
//    TIME_DURATION(java.time.Duration.class),
//
//    OBJECT(Object.class),
    ;

    private final Class<?> javaType;

    DataType(Class javaType) {
        this.javaType = javaType;
    }
}
