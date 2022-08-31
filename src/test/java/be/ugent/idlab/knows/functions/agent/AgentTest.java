package be.ugent.idlab.knows.functions.agent;

import be.ugent.idlab.knows.functions.agent.dataType.DataTypeConverterProvider;
import be.ugent.idlab.knows.functions.agent.functionIntantiation.Instantiator;
import be.ugent.idlab.knows.functions.agent.functionIntantiation.exception.InstantiationException;
import be.ugent.idlab.knows.functions.agent.functionModelProvider.FunctionModelProvider;
import be.ugent.idlab.knows.functions.agent.functionModelProvider.fno.FnOFunctionModelProvider;
import be.ugent.idlab.knows.functions.agent.functionModelProvider.fno.exception.FunctionNotFoundException;
import be.ugent.idlab.knows.functions.agent.functionModelProvider.fno.exception.ParameterNotFoundException;
import be.ugent.idlab.knows.functions.agent.model.Function;
import be.ugent.idlab.knows.functions.internalfunctions.InternalTestFunctions;
import be.ugent.idlab.knows.misc.FileFinder;
import org.junit.Assert;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * <p>Copyright 2022 IDLab (Ghent University - imec)</p>
 *
 * @author Gerald Haesendonck
 */
public class AgentTest {
    private static final String FNS = "http://example.com/functions#";
    private static final String EX = "http://example.org/";
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
        Agent agent = AgentFactory.createFromFnO("internalTestFunctions.ttl");
        execute(agent);
    }

    @Test
    public void testGrelClassesOnClassPath() throws Exception {
       Agent agent = AgentFactory.createFromFnO("functions_grel.ttl", "grel_java_mapping.ttl");
       executeGrel(agent);
    }

    @Test
    public void testGrelClassesOnClassPathRemoteFnODoc() throws Exception {
        Agent agent = AgentFactory.createFromFnO("https://users.ugent.be/~bjdmeest/function/grel.ttl", "grel_java_mapping.ttl");
        executeGrel(agent);
    }

    @Test
    public void testBooleanConversionAsStringListGrel() throws Exception {
        Agent agent = AgentFactory.createFromFnO("functions_grel.ttl", "grel_java_mapping.ttl");

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
                .add(EX+"p_int1", "5")
                .add(EX+"p_int2", "1");

        // execute the function
        Object result = agent.execute(EX+"sum", arguments);
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

    @Test
    public void testCompositeFunctionSum3() throws Exception {
        final Agent agent = AgentFactory.createFromFnO("sum-composition.ttl", "generalFunctions.ttl");

        Arguments arguments = new Arguments()
                // fns:aParameter fns:bParameter fns:cParameter
                .add(FNS+"a", "1")
                .add(FNS+"b", "2")
                .add(FNS+"c", "3");

        Object result = agent.execute(FNS+"sum3", arguments);

        assertEquals("1 + 2 + 3 should be 6", 6L, result);
    }

    @Test
    public void testCompositeFunctionAdd10() throws Exception{
        final Agent agent = AgentFactory.createFromFnO("add10.ttl");

        Arguments arguments = new Arguments().add(FNS+"b10", "5");

        Object result = agent.execute(FNS+"add10", arguments);
        assertEquals("5 + 10 should be 15", 15L, result);
    }

    @Test
    public void testCompositeFunctionComplexLinearDependencies() throws Exception {
        final Agent agent = AgentFactory.createFromFnO("generalFunctions.ttl", "computation.ttl");

        Arguments arguments = new Arguments()
                .add(EX+"p_int1", 4)
                .add(EX+"p_int2", 2)
                .add(EX+"p_int3", 3)
                .add(EX+"p_int4", 5);
        Object result = agent.execute(FNS + "computation", arguments);
        assertEquals("4*(2+3^5) should be 980", 980L, result);
    }

    @Test
    public void testCompositeFunctionComplexNonLinearDependencies() throws Exception{
        // calculate (a+b)² with the formula a² + 2ab + b²
        final Agent agent = AgentFactory.createFromFnO("sum-composition.ttl", "squareOfSum.ttl", "generalFunctions.ttl");

        Arguments arguments = new Arguments()
                .add(EX+"p_int1", 4)
                .add(EX+"p_int2", 5);

        Object result = agent.execute(FNS+"squareOfSum", arguments);
        assertEquals("(4+5)² should be ", 81L, result);
    }

    @Test
    public void testIdentityFunctionNoImplemenation() throws Exception{
        // the identity function implemented only as a composition
        final Agent agent = AgentFactory.createFromFnO("generalFunctions.ttl", "identityInteger.ttl");

        Arguments arguments = new Arguments()
                .add(EX+"p_int1", "1");
        Object result = agent.execute(FNS+"identityInteger", arguments);
        assertEquals("Identity function should return input", 1L, result);


        arguments = new Arguments()
                .add(EX+"p_int1", "2");
        result = agent.execute(FNS+"identityInteger", arguments);
        assertEquals("Identity function should return input", 2L, result);

    }

    @Test
    public void testThrowExceptionForCyclicDependencies() throws Exception {
        final Agent agent = AgentFactory.createFromFnO("generalFunctions.ttl", "identityInteger.ttl", "cyclic.ttl");
        Arguments arguments = new Arguments()
                .add(EX+"p_int1", "1");
        assertThrows("expected an exception to be thrown", InstantiationException.class, () -> agent.execute(FNS + "cyclicFunction", arguments));
    }

    @Test
    public void aliasTest() throws Exception{
        final Agent agent = AgentFactory.createFromFnO("generalFunctions.ttl", "aliasTest.ttl");
        Arguments arguments = new Arguments()
                .add(EX+"p_int1", 1)
                .add(EX+"p_int2", 2);
        Object result = agent.execute(FNS+"sumAlias", arguments);
        assertEquals("alias should work as original function", 3L, result);
    }

    @Test
    public void testThrowExceptionForNonExistingFunction() throws Exception{
        final Agent agent = AgentFactory.createFromFnO("badFunction.ttl", "generalFunctions.ttl");
        Arguments arguments = new Arguments()
                .add(EX+"p_int1", 1);
        assertThrows("expected an exception", InstantiationException.class, () -> agent.execute(FNS+"bad", arguments));
    }

    @Test
    public void testThrowExceptionForNonExistingParameter() throws Exception{
        final Agent agent = AgentFactory.createFromFnO("badParameter.ttl", "generalFunctions.ttl");
        Arguments arguments = new Arguments()
                .add(EX+"p_int1", 1);
        assertThrows("expected an exception", InstantiationException.class, () -> agent.execute(FNS+"bad", arguments));
    }

    @Test
    public void testCompositeFunctionSum3WithDebug() throws Exception {
        final Agent agent = AgentFactory.createFromFnO("sum-composition.ttl", "generalFunctions.ttl");
        Arguments arguments = new Arguments()
                // fns:aParameter fns:bParameter fns:cParameter
                .add(FNS+"a", "1")
                .add(FNS+"b", "2")
                .add(FNS+"c", "3");

        Object result = agent.execute(FNS+"sum3", arguments, true);

        assertEquals("1 + 2 + 3 should be 6", 6L, result);
        URL file = FileFinder.findFile("test.txt");
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(file.openStream()));
        String line = bufferedReader.readLine();
        assertEquals("print effect did not trigger", 6, Integer.parseInt(line));

    }

    @Test
    public void testCompositeFunctionAdd10WithDebug() throws Exception{
        final Agent agent = AgentFactory.createFromFnO("add10.ttl");

        Arguments arguments = new Arguments().add(FNS+"b10", "5");

        Object result = agent.execute(FNS+"add10", arguments, true);
        assertEquals("5 + 10 should be 15", 15L, result);
    }

    @Test
    public void testCompositeFunctionComplexLinearDependenciesWithDebug() throws Exception {
        final Agent agent = AgentFactory.createFromFnO("generalFunctions.ttl", "computation.ttl");

        Arguments arguments = new Arguments()
                .add(EX+"p_int1", 4)
                .add(EX+"p_int2", 2)
                .add(EX+"p_int3", 3)
                .add(EX+"p_int4", 5);
        Object result = agent.execute(FNS + "computation", arguments, true);
        assertEquals("4*(2+3^5) should be 980", 980L, result);
    }

    @Test
    public void testCompositeFunctionComplexNonLinearDependenciesWithDebug() throws Exception{
        // calculate (a+b)² with the formula a² + 2ab + b²
        final Agent agent = AgentFactory.createFromFnO("sum-composition.ttl", "squareOfSum.ttl", "generalFunctions.ttl");

        Arguments arguments = new Arguments()
                .add(EX+"p_int1", 4)
                .add(EX+"p_int2", 5);

        Object result = agent.execute(FNS+"squareOfSum", arguments, true);
        assertEquals("(4+5)² should be ", 81L, result);
    }

    @Test
    public void testCompositeFunctionWithComplexSidePath() throws Exception {
        final Agent agent = AgentFactory.createFromFnO("complex_side_path.ttl", "generalFunctions.ttl");
        Arguments arguments = new Arguments()
                // fns:aParameter fns:bParameter fns:cParameter
                .add(FNS + "a", "1")
                .add(FNS + "b", "2")
                .add(FNS + "c", "3");
        Object result = agent.execute(FNS + "complexSidePath", arguments, true);

        assertEquals("1 + 2 + 3 should be 6", 6L, result);
        URL file = FileFinder.findFile("test0.txt");
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(file.openStream()));
        String line = bufferedReader.readLine();
        assertEquals("print effect did not trigger", 12L, Integer.parseInt(line));
    }
    @Test
    public void testPartialFunctionApplication() throws Exception{
        final Agent agent = AgentFactory.createFromFnO("generalFunctions.ttl", "add10PartialApplication.ttl");
        Arguments arguments = new Arguments().add(EX+"p_int2", 15);
        Object result = agent.execute(FNS+"add10", arguments);
        assertEquals("15 + 10 should be 25", 25L, result);
    }

    @Test
    public void testPartialFunctionApplicationWithCompositions() throws Exception{
        final Agent agent = AgentFactory.createFromFnO("generalFunctions.ttl", "add10PartialApplicationWithComposition.ttl", "sum-composition.ttl");
        Arguments arguments = new Arguments().add(FNS+"a", 15);
        Object result = agent.execute(FNS+"add10", arguments);
        assertEquals("15 + 10 should be 25", 25L, result);
    }

    @Test
    public void testPartialApplicationThrowsExceptionNonExistingFunction() {
        assertThrows("expected an exception", FunctionNotFoundException.class, () ->AgentFactory.createFromFnO("generalFunctions.ttl", "badPartialApplicationFunction.ttl"));
    }
    @Test
    public void testPartialApplicationThrowsExceptionNonExistingParameter() {
        assertThrows("expected an exception", ParameterNotFoundException.class, () -> AgentFactory.createFromFnO("generalFunctions.ttl", "badPartialApplicationParameter.ttl"));
    }

    @Test
    public void testPartialApplicationWithoutMappingsIsApplies() throws Exception {
        final Agent agent = AgentFactory.createFromFnO("partialApplicationNoMappings.ttl", "generalFunctions.ttl");
        Arguments arguments = new Arguments().add(EX+"p_int1", 5).add(EX+"p_int2", 15);
        Object result = agent.execute(FNS+"add10", arguments);
        assertEquals("15 + 5 should be 20", 20L, result);
    }

    @Test
    public void functionWithoutReturnValue() throws Exception {
    final Agent agent = AgentFactory.createFromFnO("generalFunctions.ttl");
        Arguments arguments = new Arguments()
                // fns:aParameter fns:bParameter fns:cParameter
                .add(EX + "p_int1", "1")
                .add(EX + "p_string", "test1.txt");
        Object result = agent.execute(EX+"writeToFileNoReturn", arguments, false);
        assertNull(result);
    }


    @Test
    public void loadNonStaticFunctionThrowsException() throws Exception{
        final Agent agent = AgentFactory.createFromFnO();
        assertThrows("Expected thrown exception", Exception.class, () -> agent.loadFunction(this.getClass().getMethods()[0])); // all test methods are non static

    }
    @Test
    public void loadJavaFunctionTest() throws Exception{
        final Agent agent = AgentFactory.createFromFnO();
        String functionId = agent.loadFunction(InternalTestFunctions.class.getMethod("sum", long.class, long.class));
        List<String> list = agent.getParameterPredicates(functionId);
        Arguments args = new Arguments()
                .add(list.get(0), 5)
                .add(list.get(1), 6);
        Object result = agent.execute(functionId, args);
        Assert.assertEquals("5 + 6 is 11", 11L, result);
    }
    @Test
    public void testWriteModel() throws Exception{
        Agent agent = AgentFactory.createFromFnO("generalFunctions.ttl");
        agent.writeModel("testFileWrite.ttl");
        agent = AgentFactory.createFromFnO("testFileWrite.ttl");
        execute(agent);
    }

}
