package be.ugent.idlab.knows.functions.agent.model;


import lombok.Data;

/**
 * Data class to store the reference to a parameter or output of a function;
 */
@Data
public class FunctionFieldPair {
    private final String function;
    private final String field;

}