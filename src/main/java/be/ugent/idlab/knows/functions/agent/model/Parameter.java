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
    private final String name;
    private final String predicateUri;  // TODO: make optional? makes only sense in an RDF context like RML, no?
    private final DataTypeConverter<?> typeConverter;
    private final boolean isRequired;
}
