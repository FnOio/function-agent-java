package be.ugent.idlab.knows.functions.agent.functionModelProvider;

import be.ugent.idlab.knows.functions.agent.model.Function;

import java.util.Map;

/**
 * A FunctionModelProvider is responsible for providing an internal function model,
 * i.e. a collection of {@link Function}s
 *
 * <p>Copyright 2022 IDLab (Ghent University - imec)</p>
 *
 * @author Gerald Haesendonck
 */
public interface FunctionModelProvider {
    /**
     * Functions known to the function model provider.
     * @return  A map of function ID to function known to the function model provider.
     */
    Map<String, Function> getFunctions();
}
