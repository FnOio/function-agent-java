package be.ugent.idlab.knows.functions.agent.dataType;

import java.util.Arrays;
import java.util.List;

/**
 * <p>Copyright 2022 IDLab (Ghent University - imec)</p>
 *
 * @author Gerald Haesendonck
 */
public class ListConverter extends DataTypeConverter<List> {
    public ListConverter() {
        super(List.class, TypeCategory.COLLECTION);
    }

    @Override
    public List<?> convert(Object value) throws DataTypeConverterException {
        if (value instanceof List<?>) {
            return (List<?>) value;
        }
        if (value instanceof Object[]) {
            return Arrays.asList((Object[])value);
        }
        // TODO: if String, parse as JSON list
        throw new DataTypeConverterException("Cannot convert object of type " + value.getClass().getName() + " to a List");
    }
}
