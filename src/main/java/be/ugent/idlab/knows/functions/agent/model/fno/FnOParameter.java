package be.ugent.idlab.knows.functions.agent.model.fno;

import be.ugent.idlab.knows.functions.agent.dataType.DataTypeConverter;
import be.ugent.idlab.knows.functions.agent.model.Parameter;

public class FnOParameter extends Parameter {

    private final String resourceId; // for FnO this is the parameter resource id
    private final String dataTypeIri; // for FnO this is the parameter data type uri

    public FnOParameter(String name, String id, DataTypeConverter<?> typeConverter, boolean isRequired, String resourceId, String dataTypeIri) {
        super(name, id, typeConverter, isRequired);
        this.resourceId = resourceId;
        this.dataTypeIri = dataTypeIri;
    }

    public String getResourceId() {
        return resourceId;
    }

    public String getDataTypeUri() {
        return dataTypeIri;
    }
}
