package be.ugent.idlab.knows.functions.agent;

import be.ugent.idlab.knows.functions.agent.functionIntantiation.Instantiator;
import be.ugent.idlab.knows.functions.agent.functionModelProvider.FunctionModelProvider;
import be.ugent.idlab.knows.functions.agent.functionModelProvider.fno.FnOFunctionModelProvider;
import be.ugent.idlab.knows.functions.agent.functionModelProvider.fno.exception.FnOException;
import be.ugent.idlab.knows.functions.agent.model.Function;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * <p>Copyright 2022 IDLab (Ghent University - imec)</p>
 *
 * @author Gerald Haesendonck
 */
public class AgentTest {

    @Test
    public void testEverythingGoesWell() throws Exception {

        // first initialize a functionModelProvider
        FunctionModelProvider functionProvider = new FnOFunctionModelProvider("src/test/resources/internalTestFunctions.ttl");
        Map<String, Function> functions = functionProvider.getFunctions();

        // construct an initiator using those functions
        Instantiator instantiator = new Instantiator(functions);

        // Construct an Agent
        Agent agent = new AgentImpl(functions, instantiator);
        execute(agent);
    }

    @Test
    public void testWithFactory() throws Exception {
        Agent agent = AgentFactory.createFromFnO("src/test/resources/internalTestFunctions.ttl");
        execute(agent);
    }

    @Ignore
    public void testGrelJarOnClassPath() throws FnOException {
       Agent agent = AgentFactory.createFromFnO("src/test/resources/functions_grel.ttl");
        System.out.println();
       // TODO actual test
    }

    private void execute(final Agent agent) throws Exception {
        // prepare the parameters for the function
        Map<String, Object> parameterId2Value = new HashMap<>();
        parameterId2Value.put("http://example.org/p_int1", "5");
        parameterId2Value.put("http://example.org/p_int2", "1");

        // execute the function
        Object result = agent.execute("http://example.org/sum", parameterId2Value);
        assertEquals("5 + 1 should be 6", 6L, result);
    }
}
