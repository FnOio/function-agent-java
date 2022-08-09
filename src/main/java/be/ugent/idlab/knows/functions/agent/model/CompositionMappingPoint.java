package be.ugent.idlab.knows.functions.agent.model;

import lombok.Data;

@Data
public class CompositionMappingPoint {
    private final String functionId;

    private final String parameterId;

    private final boolean isOutput;
    private boolean isLiteral = false;
}
