package be.ugent.idlab.knows.functions.agent.dataType;

import lombok.Getter;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * A subclass of DataTypeConverter converts any object holding some value to an instance of type T.
 *
 * <p>Copyright 2022 IDLab (Ghent University - imec)</p>
 *
 * @author Gerald Haesendonck
 */
public abstract class DataTypeConverter<T> {

    public enum TypeCategory {
        PRIMITIVE,
        COLLECTION,
        OBJECT
    }

    @Getter
    private final Class<T> typeClass;

    @Getter
    private final TypeCategory typeCategory;

    /**
     * Converts a given value object to a value of type T.
     * E.g., if <code>value</code> is the String <code>"5"</code> and <code>T</code> is <code>Integer</code>,
     * then this method will parse the given String into an Integer with value <code>5</code>.
     * @param value The value that needs to be converted to a value of data type T.
     * @return      An object of type T representing the given value.
     */
    public abstract T convert(final Object value) throws DataTypeConverterException;

    protected DataTypeConverter(final Class<T> typeClass, TypeCategory typeCategory) {
        this.typeClass = typeClass;
        this.typeCategory = typeCategory;
    }

    public boolean isSubTypeOf(final Class<?> clazz) {
        Set<Class<?>> superclasses = getSuperTypesOf(typeClass);
        return superclasses.contains(clazz);
    }

    public boolean isSuperTypeOf(final Class<?> clazz) {
        Set<Class<?>> superclasses = getSuperTypesOf(clazz);
        return superclasses.contains(typeClass);
    }

    private static Set<Class<?>> getSuperTypesOf(final Class<?> clazz) {

        if (clazz == null) {
            return Collections.emptySet();
        }

        final Set<Class<?>> superTypes = new HashSet<>(2);

        // add the given class
        superTypes.add(clazz);

        // convert Numbers to their primitive counterpart and vice versa
        switch (clazz.getSimpleName()) {
            case "Short":
                superTypes.add(short.class);
                break;
            case "short":
                superTypes.add(Short.class);
                break;
            case "Integer":
                superTypes.add(int.class);
                break;
            case "int":
                superTypes.add(Integer.class);
                break;
            case "Long":
                superTypes.add(long.class);
                break;
            case "long":
                superTypes.add(Long.class);
                break;
            case "Float":
                superTypes.add(float.class);
                break;
            case "float":
                superTypes.add(Float.class);
                break;
            case "Double":
                superTypes.add(double.class);
                break;
            case "double":
                superTypes.add(Double.class);
                break;
            case "Byte":
                superTypes.add(byte.class);
                break;
            case "byte":
                superTypes.add(Byte.class);
                break;
            case "Boolean":
                superTypes.add(boolean.class);
                break;
            case "boolean":
                superTypes.add(Boolean.class);
        }

        // add superclass if any
        Class<?> superclass = clazz.getSuperclass();
        if (superclass != null) {
            superTypes.addAll(getSuperTypesOf(superclass));
        }

        // add interfaces being implemented if any
        Class<?>[] interfaces = clazz.getInterfaces();
        for (Class<?> anInterface : interfaces) {
            superTypes.addAll(getSuperTypesOf(anInterface));
        }

        return superTypes;
    }
}
