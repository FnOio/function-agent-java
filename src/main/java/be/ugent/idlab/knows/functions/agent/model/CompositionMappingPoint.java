package be.ugent.idlab.knows.functions.agent.model;

public record CompositionMappingPoint (
    String functionId,

    // contains parameter id. if isLiteral == true, contains literal
    String parameterId,

    boolean isOutput,
    boolean isLiteral
){}
