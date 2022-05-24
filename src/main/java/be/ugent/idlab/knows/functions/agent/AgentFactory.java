package be.ugent.idlab.knows.functions.agent;

import be.ugent.idlab.knows.functions.agent.dataType.DataTypeConverterProvider;
import be.ugent.idlab.knows.functions.agent.functionIntantiation.Instantiator;
import be.ugent.idlab.knows.functions.agent.functionModelProvider.FunctionModelProvider;
import be.ugent.idlab.knows.functions.agent.functionModelProvider.fno.FnOFunctionModelProvider;
import be.ugent.idlab.knows.functions.agent.functionModelProvider.fno.exception.FnOException;
import be.ugent.idlab.knows.functions.agent.model.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;

/**
 * <p>Copyright 2022 IDLab (Ghent University - imec)</p>
 *
 * @author Gerald Haesendonck
 */
public class AgentFactory {
    private final static Logger logger = LoggerFactory.getLogger(AgentFactory.class);

    /**
     * Creates an Agent executing functions described in one or more FnO documents.
     * @param pathToFnoDocs  One or more FnO documents describing functions. One fnoDocPath can be a path to a
     *                       file, a URL to a file or a String containing FnO triples in Turtle format.
     * @return              An agent capable of executing the functions as described in the FnO document(s).
     */
    public static Agent createFromFnO(final String... pathToFnoDocs) throws FnOException {
        return createFromFnO(Collections.emptyMap(), pathToFnoDocs);
    }

    /**
     * Creates an Agent executing functions described in one or more FnO documents.
     * @param pathToFnoDocs  One or more FnO documents describing functions. One fnoDocPath can be a path to a
     *                       file, a URL to a file or a String containing FnO triples in Turtle format.
     * @return              An agent capable of executing the functions as described in the FnO document(s).
     */
    public static Agent createFromFnO(final Map<String, String> implementationLocationMap, final String... pathToFnoDocs) throws FnOException {
        // initialise a DataTypeConverterProvider
        logger.debug("Initialising DataTypeConverterProvider...");
        final DataTypeConverterProvider dataTypeConverterProvider = new DataTypeConverterProvider();
        logger.debug("DataTypeConverterProvider initialised!");

        // parse all FnO documents
        logger.debug("Initialising FunctionModelProvider...");
        FunctionModelProvider functionModelProvider = new FnOFunctionModelProvider(dataTypeConverterProvider, implementationLocationMap, pathToFnoDocs);
        logger.debug("FunctionModelProvider initialised!");
        final Map<String, Function> functionId2Function = functionModelProvider.getFunctions();

        // create an instantiator for these functions
        logger.debug("Initialising Instantiator...");
        Instantiator instantiator = new Instantiator(functionId2Function, dataTypeConverterProvider);
        logger.debug("Instantiator initialised!");

        // now return the Agent implementation
        logger.debug("Initialising AgentImpl...");
        final Agent agent = new AgentImpl(functionId2Function, instantiator);
        logger.debug("AgentImpl initialised!");
        return agent;
    }
}
