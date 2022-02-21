package be.ugent.idlab.knows.functions.agent.model;

import be.ugent.idlab.knows.functions.agent.dataType.DataTypeConverter;
import lombok.Data;

/**
 * <p>Copyright 2022 IDLab (Ghent University - imec)</p>
 *
 * @author Gerald Haesendonck
 */
@Data
public class Parameter {
    private final String name;  // TODO do we need this?
    private final String id;    // for FnO this is the predicateUri;
    private final DataTypeConverter<?> typeConverter;
    private final boolean isRequired;
}
