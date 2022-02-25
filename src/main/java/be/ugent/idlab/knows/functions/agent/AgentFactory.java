package be.ugent.idlab.knows.functions.agent;

import be.ugent.idlab.knows.functions.agent.functionIntantiation.Instantiator;
import be.ugent.idlab.knows.functions.agent.functionModelProvider.FunctionModelProvider;
import be.ugent.idlab.knows.functions.agent.functionModelProvider.fno.FnOFunctionModelProvider;
import be.ugent.idlab.knows.functions.agent.functionModelProvider.fno.exception.FnOException;
import be.ugent.idlab.knows.functions.agent.model.Function;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>Copyright 2022 IDLab (Ghent University - imec)</p>
 *
 * @author Gerald Haesendonck
 */
public class AgentFactory {

    /**
     * Creates an Agent executing functions described in one or more FnO documents.
     * @param pathToFnoDoc  One or more FnO documents describing functions.
     * @return              An agent capable of executing the functions as described in the FnO document(s).
     */
    public static Agent createFromFnO(final String... pathToFnoDoc) throws FnOException {

        // parse all FnO documents
        final Map<String, Function> functionId2Function = new HashMap<>();
        for (String fnoDoc : pathToFnoDoc) {
            FunctionModelProvider functionModelProvider = new FnOFunctionModelProvider(fnoDoc);
            functionId2Function.putAll(functionModelProvider.getFunctions());
        }

        // create an instantiator for these functions
        Instantiator instantiator = new Instantiator(functionId2Function);

        // now return the Agent implementation
        return new AgentImpl(functionId2Function, instantiator);
    }

}
