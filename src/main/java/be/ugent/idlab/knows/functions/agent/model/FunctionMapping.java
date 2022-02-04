package be.ugent.idlab.knows.functions.agent.model;

import lombok.Data;

/**
 * <p>Copyright 2022 IDLab (Ghent University - imec)</p>
 *
 * @author Gerald Haesendonck
 */
@Data
public class FunctionMapping {
    // The unique identifier of the function this mapping is for
    private final String functionId;

    // The method mapping for this function
    private final MethodMapping methodMapping;

    // The implementation of this function
    private final Implementation implementation;
}
