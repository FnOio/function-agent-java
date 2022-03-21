package be.ugent.idlab.knows.functions.agent.dataType;

import java.util.Collection;

/**
 * This convert can be used to convert collections with elements of a certain type.
 * A converter can be provided to convert the element types.
 *
 * <p>Copyright 2022 IDLab (Ghent University - imec)</p>
 *
 * @author Gerald Haesendonck
 */
public abstract class CollectionConverter<T extends Collection> extends DataTypeConverter<T> {

    private DataTypeConverter<?> argumentTypeConverter = new DefaultDataTypeConverter();    // use default converter if no given

    protected CollectionConverter(Class<T> typeClass, TypeCategory typeCategory) {
        super(typeClass, typeCategory);
    }

    protected DataTypeConverter<?> getArgumentTypeConverter() {
        return argumentTypeConverter;
    }

    public void setArgumentTypeConverter(DataTypeConverter<?> argumentTypeConverter) {
        this.argumentTypeConverter = argumentTypeConverter;
    }
}
