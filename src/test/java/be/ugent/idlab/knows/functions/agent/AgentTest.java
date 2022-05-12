package be.ugent.idlab.knows.functions.agent;

import be.ugent.idlab.knows.functions.agent.dataType.DataTypeConverterProvider;
import be.ugent.idlab.knows.functions.agent.functionIntantiation.Instantiator;
import be.ugent.idlab.knows.functions.agent.functionModelProvider.FunctionModelProvider;
import be.ugent.idlab.knows.functions.agent.functionModelProvider.fno.FnOFunctionModelProvider;
import be.ugent.idlab.knows.functions.agent.model.Function;
import org.junit.Test;

import java.time.Instant;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * <p>Copyright 2022 IDLab (Ghent University - imec)</p>
 *
 * @author Gerald Haesendonck
 */
public class AgentTest {

    @Test
    public void testEverythingGoesWell() throws Exception {
        // first initialize a DataTaypeConverterProvider
        final DataTypeConverterProvider dataTypeConverterProvider = new DataTypeConverterProvider();

        // first initialize a functionModelProvider
        FunctionModelProvider functionProvider = new FnOFunctionModelProvider(dataTypeConverterProvider, "src/test/resources/internalTestFunctions.ttl");
        Map<String, Function> functions = functionProvider.getFunctions();

        // construct an initiator using those functions
        Instantiator instantiator = new Instantiator(functions, dataTypeConverterProvider);

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
    public void testGrelClassesOnClassPath() throws Exception {
       Agent agent = AgentFactory.createFromFnO("src/test/resources/functions_grel.ttl", "grel_java_mapping.ttl");
       executeGrel(agent);
    }

    @Test
    public void testGrelClassesOnClassPathRemoteFnODoc() throws Exception {
        Agent agent = AgentFactory.createFromFnO("https://users.ugent.be/~bjdmeest/function/grel.ttl", "grel_java_mapping.ttl");
        executeGrel(agent);
    }

    @Test
    public void testBooleanConversionAsStringListGrel() throws Exception {
        Agent agent = AgentFactory.createFromFnO("https://users.ugent.be/~bjdmeest/function/grel.ttl", "grel_java_mapping.ttl");

        // prepare the parameters for the function
        Arguments arguments = new Arguments()
                .add("http://users.ugent.be/~bjdmeest/function/grel.ttl#param_rep_b", "false")
                .add("http://users.ugent.be/~bjdmeest/function/grel.ttl#param_rep_b", "false");

        // execute the function
        Object result = agent.execute("http://users.ugent.be/~bjdmeest/function/grel.ttl#boolean_and", arguments);
        assertFalse("\"false\" | \"false\" should be false", (Boolean)result);
    }

    @Test
    public void testAaabimFromJar() throws Exception {
        final Agent agent = AgentFactory.createFromFnO("aaabim_java_mapping.ttl");

        final String aaabimPrefix = "http://users.ugent.be/~tdlva/function/aaabim.ttl#";
        final Arguments arguments = new Arguments().add(aaabimPrefix + "p_milliseconds", 1591782480076L);

        // execute the function
        Object result = agent.execute(aaabimPrefix + "millisecondsToInstant", arguments);
        assertTrue(result instanceof Instant);
        Instant resultingTimeInstant = (Instant) result;
        String resultFormattedTime = resultingTimeInstant.toString();
        assertEquals("2020-06-10T09:48:00.076Z", resultFormattedTime);
        System.out.println();
    }

    @Test
    public void testAaabimWithImportsFromJar() throws Exception {
        final Agent agent = AgentFactory.createFromFnO("aaabim_java_mapping.ttl");

        final String aaabimPrefix = "http://users.ugent.be/~tdlva/function/aaabim.ttl#";
        final Arguments arguments = new Arguments().add(aaabimPrefix + "p_geohash", "u14dhw2phg54");

        // execute the function
        Object result = agent.execute(aaabimPrefix + "geoHashToLatitude", arguments);
        assertTrue(result instanceof Double);
        assertEquals(3.7135869450867176d, result);
        System.out.println();
    }

    /**
     * Tests if a function that takes an RDF List as parameter in the descriptions works with
     * an implementation that has a method that takes a Java array as input parameter.
     */
    @Test
    public void testListFnO_ArrayImplementation() throws Exception {
        Agent agent = AgentFactory.createFromFnO("src/test/resources/functions_grel.ttl", "grel_java_mapping.ttl");

        // prepare the parameter: a list of ints to take the sum of
        Arguments arguments = new Arguments()
                .add("http://users.ugent.be/~bjdmeest/function/grel.ttl#p_array_a", 1)
                .add("http://users.ugent.be/~bjdmeest/function/grel.ttl#p_array_a", 4);

        // Execute the array_sum function
        Object result = agent.execute("http://users.ugent.be/~bjdmeest/function/grel.ttl#array_sum", arguments);
        assertEquals("1 + 4 should be 5", 5, result);
    }

    private void execute(final Agent agent) throws Exception {
        // prepare the parameters for the function
        Arguments arguments = new Arguments()
                .add("http://example.org/p_int1", "5")
                .add("http://example.org/p_int2", "1");

        // execute the function
        Object result = agent.execute("http://example.org/sum", arguments);
        assertEquals("5 + 1 should be 6", 6L, result);
    }

    private void executeGrel(final Agent agent) throws Exception {
        // prepare the parameters for the function
        Arguments arguments = new Arguments()
                .add("http://users.ugent.be/~bjdmeest/function/grel.ttl#param_rep_b", false)
                .add("http://users.ugent.be/~bjdmeest/function/grel.ttl#param_rep_b", true);

        // execute the function
        Object result = agent.execute("http://users.ugent.be/~bjdmeest/function/grel.ttl#boolean_or", arguments);
        assertTrue("false | true should be true", (Boolean)result);

    }
}
