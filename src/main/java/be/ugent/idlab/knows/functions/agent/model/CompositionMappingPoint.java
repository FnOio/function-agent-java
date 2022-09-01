package be.ugent.idlab.knows.functions.agent.model;

import lombok.Data;

@Data
public class CompositionMappingPoint {
    private final String functionId;

    /**
     *  contains parameter id. if isLiteral == true, contains literal
     */
    private final String parameterId;

    private final boolean isOutput;
    private boolean isLiteral = false;
}
