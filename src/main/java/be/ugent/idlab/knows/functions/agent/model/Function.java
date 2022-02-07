package be.ugent.idlab.knows.functions.agent.model;

import lombok.Data;

import java.util.List;

/**
 * <p>Copyright 2022 IDLab (Ghent University - imec)</p>
 *
 * @author Gerald Haesendonck
 */
@Data
public class Function {

    // a unique identifier for the function
    private final String id;

    // a name for this function
    private final String name;

    // a description for this function
    private final String description;

    // a list of function arguments
    private final List<Parameter> argumentParameters;

    // a list of returned arguments. This should be only 1 in Java
    private final List<Parameter> returnParameters;

    // a function mapping containing a method name mapping and an implementation
    private FunctionMapping functionMapping;
}
