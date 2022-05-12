package be.ugent.idlab.knows.functions.agent.dataType;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

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

    protected final Set<Class<?>> superClasses;

    @Getter
    @Setter(AccessLevel.PROTECTED)
    private Class<?> typeClass;

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


    protected DataTypeConverter(final Class<T> typeClass, TypeCategory typeCategory, Class<?>... superClasses) {
        this.typeClass = typeClass;
        this.typeCategory = typeCategory;
        this.superClasses = new HashSet<>(Arrays.asList(superClasses));
    }

    public boolean isSuperTypeOf(final Class<?> clazz) {
        Set<String> superclasses = getSuperTypesOf(clazz);
        String className = typeClass.isArray()? "_array" : typeClass.getName();
        return superclasses.contains(className);
    }

    public boolean isSubTypeOf(final Class<?> clazz) {
        Set<String> superclasses = getSuperTypesOf(typeClass);
        for (Class<?> superclass : this.superClasses) {
            superclasses.addAll(getSuperTypesOf(superclass));
        }
        String className = clazz.isArray()? "_array" : clazz.getName();
        return superclasses.contains(className);
    }

    private static Set<String> getSuperTypesOf(final Class<?> clazz) {

        if (clazz == null) {
            return Collections.emptySet();
        }

        final Set<Class<?>> typesToCheck = new HashSet<>(2);
        final Set<String> superTypes = new HashSet<>(2);

        // add the given class
        String classTypeName = clazz.getTypeName();

        // convert Numbers to their primitive counterpart and vice versa
        switch (classTypeName) {
            case "int":
                typesToCheck.add(Integer.class);
                break;
            case "java.lang.Integer":
                superTypes.add("int");
                break;
            case "short":
                typesToCheck.add(Short.class);
                break;
            case "java.lang.Short":
                superTypes.add("short");
                break;
            case "long":
                typesToCheck.add(Long.class);
                break;
            case "java.lang.Long":
                superTypes.add("long");
                break;
            case "java.lang.Float":
                superTypes.add("float");
                break;
            case "float" :
                typesToCheck.add(Float.class);
                break;
            case "double":
                typesToCheck.add(Double.class);
                break;
            case "java.lang.Double":
                superTypes.add("double");
                break;
            case "byte":
                typesToCheck.add(Byte.class);
                break;
            case "java.lang.Byte":
                superTypes.add("byte");
                break;
            case "boolean":
                typesToCheck.add(Boolean.class);
                break;
            case "java.lang.Boolean":
                superTypes.add("boolean");
                break;
            case "java.lang.Character":
                superTypes.add("char");
                break;
            case "char":
                typesToCheck.add(Character.class);
                break;
            case "java.util.List":
                superTypes.add("_array");   // there is no real array class name...
                break;
        }

        // check if array
        if (clazz.isArray()) {
            superTypes.add("_array");
            typesToCheck.add(List.class);
        } else {
            superTypes.add(classTypeName);
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

        // check compatible types
        for (Class<?> typeToCheck : typesToCheck) {
            superTypes.addAll(getSuperTypesOf(typeToCheck));
        }

        return superTypes;
    }
}
