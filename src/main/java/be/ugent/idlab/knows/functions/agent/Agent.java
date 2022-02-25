package be.ugent.idlab.knows.functions.agent;

import java.util.Map;

/**
 * <p>Copyright 2021 IDLab (Ghent University - imec)</p>
 *
 * @author Gerald Haesendonck
 */
public interface Agent {

    /**
     * Executes the function with a given id and given parameters.
     * @param functionId    The unique identifier of a function.
     * @param parameterId2Value  A map parameterId -> value. (For FnO, the parameter id is the 'predicate' of the parameter.)
     */
    Object execute(final String functionId, final Map<String, Object> parameterId2Value) throws Exception;
}
