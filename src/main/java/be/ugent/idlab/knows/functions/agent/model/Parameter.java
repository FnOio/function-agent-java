package be.ugent.idlab.knows.functions.agent.model;

import be.ugent.idlab.knows.functions.agent.dataType.DataTypeConverter;

/**
 * <p>Copyright 2022 IDLab (Ghent University - imec)</p>
 *
 * @author Gerald Haesendonck
 */
public class Parameter {
    private final String name;  // TODO do we need this?
    private final String id;    // for FnO this is the predicateUri;
    private DataTypeConverter<?> typeConverter;
    private final boolean isRequired;

    public Parameter(String name, String id, DataTypeConverter<?> typeConverter, boolean isRequired) {
        this.name = name;
        this.id = id;
        this.typeConverter = typeConverter;
        this.isRequired = isRequired;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public DataTypeConverter<?> getTypeConverter() {
        return typeConverter;
    }

    public boolean isRequired() {
        return isRequired;
    }

    public void setTypeConverter(DataTypeConverter<?> typeConverter) {
        this.typeConverter = typeConverter;
    }
}
