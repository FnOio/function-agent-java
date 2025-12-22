package be.ugent.idlab.knows.functions.agent.model;

/**
 * Data class to store the reference to a parameter or output of a function;
 */
public record FunctionFieldPair (
    String function,
    String field
){}