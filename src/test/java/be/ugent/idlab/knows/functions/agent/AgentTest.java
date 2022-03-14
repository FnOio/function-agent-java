package be.ugent.idlab.knows.functions.agent;

import be.ugent.idlab.knows.functions.agent.functionIntantiation.Instantiator;
import be.ugent.idlab.knows.functions.agent.functionModelProvider.FunctionModelProvider;
import be.ugent.idlab.knows.functions.agent.functionModelProvider.fno.FnOFunctionModelProvider;
import be.ugent.idlab.knows.functions.agent.model.Function;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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

    @Test
    public void testGrelJarOnClassPath() throws Exception {
       Agent agent = AgentFactory.createFromFnO("src/test/resources/functions_grel.ttl", "grel_java_mapping.ttl");
       executeGrel(agent);
    }

    @Test
    public void testGrelJarOnClassPathRemoteFnODoc() throws Exception {
        Agent agent = AgentFactory.createFromFnO("https://users.ugent.be/~bjdmeest/function/grel.ttl", "grel_java_mapping.ttl");
        executeGrel(agent);
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

    private void executeGrel(final Agent agent) throws Exception {
        List<Boolean> booleanParameters = Arrays.asList(false, true);
        Map<String, Object> parameterId2Value = new HashMap<>();
        parameterId2Value.put("http://users.ugent.be/~bjdmeest/function/grel.ttl#param_rep_b", booleanParameters);

        // execute the function
        Object result = agent.execute("http://users.ugent.be/~bjdmeest/function/grel.ttl#boolean_or", parameterId2Value);
        assertTrue("false | true should be true", (Boolean)result);

    }
}
