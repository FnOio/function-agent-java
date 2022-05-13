package be.ugent.idlab.knows.functions.agent.dataType;

import lombok.Getter;

import java.lang.reflect.Array;
import java.util.Collection;

/**
 * <p>Copyright 2022 IDLab (Ghent University - imec)</p>
 *
 * @author Gerald Haesendonck
 */
public class ArrayConverter extends DataTypeConverter<Object[]> {
    @Getter
    private DataTypeConverter<?> argumentTypeConverter = new DefaultDataTypeConverter();

    public ArrayConverter() {
        super(Object[].class, TypeCategory.COLLECTION);
    }

    public void setArgumentTypeConverter(final DataTypeConverter<?> argumentTypeConverter) {
        this.argumentTypeConverter = argumentTypeConverter;
        // because it is an array, this also changes the typeClass! e.g. Object[] != Integer[]
        Object[] typeArray = initArray(0);
        setTypeClass(typeArray.getClass());

    }

    @Override
    public Object[] convert(Object value) throws DataTypeConverterException {
        if (value.getClass().isArray()) {
            return (Object[])value;
        } else if (value instanceof Collection) {
            return convertArray((Collection<?>)value);
        }
        throw new DataTypeConverterException("Cannot convert object of type " + value.getClass().getName() + " to an array.");
    }

    private Object[] convertArray(Collection<?> value) throws DataTypeConverterException {
        Object[] result = initArray(value.size());
        Object[] valueArr = value.toArray();
        for (int i = 0; i < valueArr.length; i++) {
            Object o = valueArr[i];
            result[i] = getArgumentTypeConverter().convert(o);
        }
        return result;
    }

    private Object[] initArray(int length) {
        Class<?> componentTypeClass = getArgumentTypeConverter().getTypeClass();
        return (Object[]) Array.newInstance(componentTypeClass, length);
    }
}
