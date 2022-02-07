package be.ugent.idlab.knows.functions.agent.functionModelProvider;

import be.ugent.idlab.knows.functions.agent.model.Function;

import java.util.Collection;

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
     * @return  A collection of functions known to the function model provider.
     */
    Collection<Function> getFunctions();
}
