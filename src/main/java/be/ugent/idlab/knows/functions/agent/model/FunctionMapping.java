package be.ugent.idlab.knows.functions.agent.model;

/**
 * <p>Copyright 2022 IDLab (Ghent University - imec)</p>
 *
 * @author Gerald Haesendonck
 */
public record FunctionMapping(
    // The unique identifier of the function this mapping is for
    String functionId,

    // The method mapping for this function
    MethodMapping methodMapping,

    // The implementation of this function
    Implementation implementation
    ){}
