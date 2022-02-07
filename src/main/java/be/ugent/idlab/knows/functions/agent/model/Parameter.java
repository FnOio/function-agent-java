package be.ugent.idlab.knows.functions.agent.model;

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
    private final String typeUri;   // TODO: use FnO mapping to come to unified "type" in common model
    private final boolean isRequired;
}
