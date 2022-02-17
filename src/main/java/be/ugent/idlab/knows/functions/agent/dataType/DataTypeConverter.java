package be.ugent.idlab.knows.functions.agent.dataType;

import java.util.List;

/**
 * A DataType
 *
 * <p>Copyright 2022 IDLab (Ghent University - imec)</p>
 *
 * @author Gerald Haesendonck
 */
public interface DataTypeConverter<T> {
    public T convert(final Object value);
    List<Class<?>> getTypeClasses();
}
