package be.ugent.idlab.knows.functions.agent.dataType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * <p>Copyright 2022 IDLab (Ghent University - imec)</p>
 *
 * @author Gerald Haesendonck
 */
public class ListConverter extends CollectionConverter<List> {
    public ListConverter() {
        super(List.class, TypeCategory.COLLECTION);
    }

    @Override
    public List<?> convert(Object value) throws DataTypeConverterException {
        if (value instanceof Collection<?>) {
            return convertCollection((List<?>) value);
        }
        if (value.getClass().isArray()) {
            return convert(Arrays.asList((Object[])value));
        }
        // TODO: if String, parse as JSON list?
        throw new DataTypeConverterException("Cannot convert object of type " + value.getClass().getName() + " to a List.");
    }

    private List<?> convertCollection(final Collection<?> values) throws DataTypeConverterException {
        List<Object> result = new ArrayList<>();
        for (Object o : values) {
            result.add(getArgumentTypeConverter().convert(o));
        }
        return result;
    }
}
