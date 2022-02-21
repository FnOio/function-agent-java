package be.ugent.idlab.knows.functions.agent.dataType;

import java.util.List;

/**
 * A implementation of DataTypeConverter converts any object holding some value to an instance of type T.
 *
 * <p>Copyright 2022 IDLab (Ghent University - imec)</p>
 *
 * @author Gerald Haesendonck
 */
public interface DataTypeConverter<T> {

    /**
     * Converts a given value object to a value of type T.
     * E.g., if <code>value</code> is the String <code>"5"</code> and <code>T</code> is <code>Integer</code>,
     * then this method will parse the given String into an Integer with value <code>5</code>.
     * @param value The value that needs to be converted to a value of data type T.
     * @return      An object of type T representing the given value.
     */
    T convert(final Object value);

    /**
     * Get compatible type classes to T, i.e. types that are valid for the returned object when invoking {@link DataTypeConverter#convert(Object)}
     * E.g., when <code>T</code> is <code>Integer</code>, this method might return a list (Integer, int) since <code>int</code> is compatible
     * with <code>Integer</code>.
     * @return  A list of compatible type classes.
     */
    List<Class<?>> getTypeClasses();
}
