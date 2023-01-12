package be.ugent.idlab.knows.functions.agent;

import be.ugent.idlab.knows.functions.agent.dataType.DataTypeConverterProvider;
import be.ugent.idlab.knows.functions.agent.exception.AgentException;
import be.ugent.idlab.knows.functions.agent.functionIntantiation.Instantiator;
import be.ugent.idlab.knows.functions.agent.functionIntantiation.exception.InstantiationException;
import be.ugent.idlab.knows.functions.agent.functionModelProvider.FunctionModelProvider;
import be.ugent.idlab.knows.functions.agent.functionModelProvider.fno.FnOFunctionModelProvider;
import be.ugent.idlab.knows.functions.agent.functionModelProvider.fno.exception.FnOException;
import be.ugent.idlab.knows.functions.agent.functionModelProvider.fno.exception.FunctionNotFoundException;
import be.ugent.idlab.knows.functions.agent.functionModelProvider.fno.exception.ParameterNotFoundException;
import be.ugent.idlab.knows.functions.agent.model.Function;
import be.ugent.idlab.knows.functions.internalfunctions.InternalTestFunctions;
import be.ugent.idlab.knows.misc.FileFinder;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static be.ugent.idlab.knows.functions.agent.functionModelProvider.fno.NAMESPACES.IDLABFN;
import static be.ugent.idlab.knows.functions.agent.functionModelProvider.fno.NAMESPACES.RDF;
import static org.junit.jupiter.api.Assertions.*;

/**
 * <p>
 * Copyright 2022 IDLab (Ghent University - imec)
 * </p>
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
        Agent agent = AgentFactory.createFromFnO("https://raw.githubusercontent.com/FnOio/grel-functions-fno/752738bd613614947105f1b32859d1cb1f0bcf20/grel.ttl", "grel_java_mapping.ttl");
        executeGrel(agent);
    }

    @Test
    public void testBooleanConversionAsStringListGrel() throws Exception {
        try (Agent agent = AgentFactory.createFromFnO("functions_grel.ttl", "grel_java_mapping.ttl")) {

            // prepare the parameters for the function
            Arguments arguments = new Arguments()
                    .add("http://users.ugent.be/~bjdmeest/function/grel.ttl#param_rep_b", "false")
                    .add("http://users.ugent.be/~bjdmeest/function/grel.ttl#param_rep_b", "false");

            // execute the function
            Object result = agent.execute("http://users.ugent.be/~bjdmeest/function/grel.ttl#boolean_and", arguments);
            assertFalse((Boolean) result, "\"false\" | \"false\" should be false");
        }
    }

    @Test
    public void testAaabimFromJar() throws Exception {
        Object result;
        try (Agent agent = AgentFactory.createFromFnO("aaabim_java_mapping.ttl")) {

            final String aaabimPrefix = "http://users.ugent.be/~tdlva/function/aaabim.ttl#";
            final Arguments arguments = new Arguments().add(aaabimPrefix + "p_milliseconds", 1591782480076L);

            // execute the function
            result = agent.execute(aaabimPrefix + "millisecondsToInstant", arguments);
        }
        assertTrue(result instanceof Instant);
        Instant resultingTimeInstant = (Instant) result;
        String resultFormattedTime = resultingTimeInstant.toString();
        assertEquals("2020-06-10T09:48:00.076Z", resultFormattedTime);
        System.out.println();
    }

    @Test
    public void testAaabimWithImportsFromJar() throws Exception {
        Object result;
        try (Agent agent = AgentFactory.createFromFnO("aaabim_java_mapping.ttl")) {

            final String aaabimPrefix = "http://users.ugent.be/~tdlva/function/aaabim.ttl#";
            final Arguments arguments = new Arguments().add(aaabimPrefix + "p_geohash", "u14dhw2phg54");

            // execute the function
            result = agent.execute(aaabimPrefix + "geoHashToLatitude", arguments);
        }
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
        Object result;
        try (Agent agent = AgentFactory.createFromFnO("src/test/resources/functions_grel.ttl", "grel_java_mapping.ttl")) {

            // prepare the parameter: a list of ints to take the sum of
            Arguments arguments = new Arguments()
                    .add("http://users.ugent.be/~bjdmeest/function/grel.ttl#p_array_a", 1)
                    .add("http://users.ugent.be/~bjdmeest/function/grel.ttl#p_array_a", 4);

            // Execute the array_sum function
            result = agent.execute("http://users.ugent.be/~bjdmeest/function/grel.ttl#array_sum", arguments);
        }
        assertEquals(5, result, "1 + 4 should be 5");
    }

    private void execute(final Agent agent) throws Exception {
        // prepare the parameters for the function
        Arguments arguments = new Arguments()
                .add(EX + "p_int1", "5")
                .add(EX + "p_int2", "1");

        // execute the function
        Object result = agent.execute(EX + "sum", arguments);
        assertEquals(6L, result, "5 + 1 should be 6");
    }

    private void executeGrel(final Agent agent) throws Exception {
        // prepare the parameters for the function
        Arguments arguments = new Arguments()
                .add("http://users.ugent.be/~bjdmeest/function/grel.ttl#param_rep_b", false)
                .add("http://users.ugent.be/~bjdmeest/function/grel.ttl#param_rep_b", true);

        // execute the function
        Object result = agent.execute("http://users.ugent.be/~bjdmeest/function/grel.ttl#boolean_or", arguments);
        assertTrue((Boolean) result, "false | true should be true");

    }

    @Test
    public void testCompositeFunctionSum3() throws Exception {
        Object result;
        try (Agent agent = AgentFactory.createFromFnO("sum-composition.ttl", "generalFunctions.ttl")) {

            Arguments arguments = new Arguments()
                    // fns:aParameter fns:bParameter fns:cParameter
                    .add(FNS + "a", "1")
                    .add(FNS + "b", "2")
                    .add(FNS + "c", "3");

            result = agent.execute(FNS + "sum3", arguments);
        }

        assertEquals(6L, result, "1 + 2 + 3 should be 6");
    }

    @Test
    public void testCompositeFunctionAdd10() throws Exception {
        Object result;
        try (Agent agent = AgentFactory.createFromFnO("add10.ttl")) {

            Arguments arguments = new Arguments().add(FNS + "b10", "5");

            result = agent.execute(FNS + "add10", arguments);
        }
        assertEquals(15L, result, "5 + 10 should be 15");
    }

    @Test
    public void testCompositeFunctionComplexLinearDependencies() throws Exception {
        Object result;
        try (Agent agent = AgentFactory.createFromFnO("generalFunctions.ttl", "computation.ttl")) {

            Arguments arguments = new Arguments()
                    .add(EX + "p_int1", 4)
                    .add(EX + "p_int2", 2)
                    .add(EX + "p_int3", 3)
                    .add(EX + "p_int4", 5);
            result = agent.execute(FNS + "computation", arguments);
        }
        assertEquals(980L, result, "4*(2+3^5) should be 980");
    }

    @Test
    public void testCompositeFunctionComplexNonLinearDependencies() throws Exception {
        // calculate (a+b)² with the formula a² + 2ab + b²
        Object result;
        try (Agent agent = AgentFactory.createFromFnO("sum-composition.ttl", "squareOfSum.ttl", "generalFunctions.ttl")) {

            Arguments arguments = new Arguments()
                    .add(EX + "p_int1", 4)
                    .add(EX + "p_int2", 5);

            result = agent.execute(FNS + "squareOfSum", arguments);
        }
        assertEquals(81L, result, "(4+5)² should be 81");
    }

    @Test
    public void testIdentityFunctionNoImplemenation() throws Exception {
        // the identity function implemented only as a composition
        Object result;
        try (Agent agent = AgentFactory.createFromFnO("generalFunctions.ttl", "identityInteger.ttl")) {

            Arguments arguments = new Arguments()
                    .add(EX + "p_int1", "1");
            result = agent.execute(FNS + "identityInteger", arguments);
            assertEquals(1L, result, "Identity function should return input");

            arguments = new Arguments()
                    .add(EX + "p_int1", "2");
            result = agent.execute(FNS + "identityInteger", arguments);
        }
        assertEquals(2L, result, "Identity function should return input");

    }

    @Test
    public void testThrowExceptionForCyclicDependencies() throws Exception {
        try (Agent agent = AgentFactory.createFromFnO("generalFunctions.ttl", "identityInteger.ttl", "cyclic.ttl")) {
            Arguments arguments = new Arguments()
                    .add(EX + "p_int1", "1");
            assertThrows(InstantiationException.class, () -> agent.execute(FNS + "cyclicFunction", arguments), "expected an exception to be thrown");
        }
    }

    @Test
    public void aliasTest() throws Exception {
        Object result;
        try (Agent agent = AgentFactory.createFromFnO("generalFunctions.ttl", "aliasTest.ttl")) {
            Arguments arguments = new Arguments()
                    .add(EX + "p_int1", 1)
                    .add(EX + "p_int2", 2);
            result = agent.execute(FNS + "sumAlias", arguments);
        }
        assertEquals(3L, result, "alias should work as original function");
    }

    @Test
    public void testThrowExceptionForNonExistingFunction() throws Exception {
        try (Agent agent = AgentFactory.createFromFnO("badFunction.ttl", "generalFunctions.ttl")) {
            Arguments arguments = new Arguments()
                    .add(EX + "p_int1", 1);
            assertThrows(Exception.class, () -> agent.execute(FNS + "bad", arguments), "expected an exception");
        }
    }

    @Test
    public void testThrowExceptionForNonExistingParameter() throws Exception {
        try (Agent agent = AgentFactory.createFromFnO("badParameter.ttl", "generalFunctions.ttl")) {
            Arguments arguments = new Arguments()
                    .add(EX + "p_int1", 1);
            assertThrows(InstantiationException.class, () -> agent.execute(FNS + "bad", arguments), "expected an exception");
        }
    }

    @Test
    public void testCompositeFunctionSum3WithDebug() throws Exception {
        Object result;
        try (Agent agent = AgentFactory.createFromFnO("sum-composition.ttl", "generalFunctions.ttl")) {
            Arguments arguments = new Arguments()
                    // fns:aParameter fns:bParameter fns:cParameter
                    .add(FNS + "a", "1")
                    .add(FNS + "b", "2")
                    .add(FNS + "c", "3");

            result = agent.execute(FNS + "sum3", arguments, true);
        }

        assertEquals(6L, result, "1 + 2 + 3 should be 6");
        URL file = FileFinder.findFile("test.txt");
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(file.openStream()));
        String line = bufferedReader.readLine();
        assertEquals(6, Integer.parseInt(line), "print effect did not trigger");

    }

    @Test
    public void testCompositeFunctionAdd10WithDebug() throws Exception {
        Object result;
        try (Agent agent = AgentFactory.createFromFnO("add10.ttl")) {

            Arguments arguments = new Arguments().add(FNS + "b10", "5");

            result = agent.execute(FNS + "add10", arguments, true);
        }
        assertEquals(15L, result, "5 + 10 should be 15");
    }

    @Test
    public void testCompositeFunctionComplexLinearDependenciesWithDebug() throws Exception {
        Object result;
        try (Agent agent = AgentFactory.createFromFnO("generalFunctions.ttl", "computation.ttl")) {

            Arguments arguments = new Arguments()
                    .add(EX + "p_int1", 4)
                    .add(EX + "p_int2", 2)
                    .add(EX + "p_int3", 3)
                    .add(EX + "p_int4", 5);
            result = agent.execute(FNS + "computation", arguments, true);
        }
        assertEquals(980L, result, "4*(2+3^5) should be 980");
    }

    @Test
    public void testCompositeFunctionComplexNonLinearDependenciesWithDebug() throws Exception {
        // calculate (a+b)² with the formula a² + 2ab + b²
        Object result;
        try (Agent agent = AgentFactory.createFromFnO("sum-composition.ttl", "squareOfSum.ttl", "generalFunctions.ttl")) {

            Arguments arguments = new Arguments()
                    .add(EX + "p_int1", 4)
                    .add(EX + "p_int2", 5);

            result = agent.execute(FNS + "squareOfSum", arguments, true);
        }
        assertEquals(81L, result, "(4+5)² should be 81");
    }

    @Test
    public void testCompositeFunctionWithComplexSidePath() throws Exception {
        Object result;
        try (Agent agent = AgentFactory.createFromFnO("complex_side_path.ttl", "generalFunctions.ttl")) {
            Arguments arguments = new Arguments()
                    // fns:aParameter fns:bParameter fns:cParameter
                    .add(FNS + "a", "1")
                    .add(FNS + "b", "2")
                    .add(FNS + "c", "3");
            result = agent.execute(FNS + "complexSidePath", arguments, true);
        }

        assertEquals(6L, result, "1 + 2 + 3 should be 6");
        URL file = FileFinder.findFile("test0.txt");
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(file.openStream()));
        String line = bufferedReader.readLine();
        assertEquals(12L, Integer.parseInt(line), "print effect did not trigger");
    }

    @Test
    public void testPartialFunctionApplication() throws Exception {
        Object result;
        try (Agent agent = AgentFactory.createFromFnO("generalFunctions.ttl", "add10PartialApplication.ttl")) {
            Arguments arguments = new Arguments().add(EX + "p_int2", 15);
            result = agent.execute(FNS + "add10", arguments);
        }
        assertEquals(25L, result, "15 + 10 should be 25");
    }

    @Test
    public void testPartialFunctionApplicationWithCompositions() throws Exception {
        Object result;
        try (Agent agent = AgentFactory.createFromFnO("generalFunctions.ttl", "add10PartialApplicationWithComposition.ttl", "sum-composition.ttl")) {
            Arguments arguments = new Arguments().add(FNS + "a", 15);
            result = agent.execute(FNS + "add10", arguments);
        }
        assertEquals(25L, result, "15 + 10 should be 25");
    }

    @Test
    public void testPartialApplicationThrowsExceptionNonExistingFunction() {
        assertThrows(FunctionNotFoundException.class, () -> {
            Agent agent = AgentFactory.createFromFnO("generalFunctions.ttl", "badPartialApplicationFunction.ttl");
            agent.close();
        }, "expected an exception");
    }

    @Test
    public void testPartialApplicationThrowsExceptionNonExistingParameter() {
        assertThrows(ParameterNotFoundException.class, () -> {
            Agent agent = AgentFactory.createFromFnO("generalFunctions.ttl", "badPartialApplicationParameter.ttl");
            agent.close();
        }, "expected an exception");
    }

    @Test
    public void testPartialApplicationWithoutMappingsIsApplies() throws Exception {
        Object result;
        try (Agent agent = AgentFactory.createFromFnO("partialApplicationNoMappings.ttl", "generalFunctions.ttl")) {
            Arguments arguments = new Arguments().add(EX + "p_int1", 5).add(EX + "p_int2", 15);
            result = agent.execute(FNS + "add10", arguments);
        }
        assertEquals(20L, result, "15 + 5 should be 20");
    }

    @Test
    public void functionWithoutReturnValue() throws Exception {
        Object result;
        try (Agent agent = AgentFactory.createFromFnO("generalFunctions.ttl")) {
            Arguments arguments = new Arguments()
                    // fns:aParameter fns:bParameter fns:cParameter
                    .add(EX + "p_int1", "1")
                    .add(EX + "p_string", "test1.txt");
            result = agent.execute(EX + "writeToFileNoReturn", arguments, false);
        }
        assertNull(result);
    }

    @Test
    public void rdfListFromSeqTest1Element() throws Exception {
        Object result;
        try (Agent agent = AgentFactory.createFromFnO("rdfSeq.ttl")) {
            Arguments arguments = new Arguments()
                    .add(RDF + "_1", "a");
            result = agent.execute(IDLABFN + "makeListFromSeq", arguments);
        }
        List<String> correct = new ArrayList<>();
        correct.add("a");
        assertEquals(correct, result, "should be a list with element a");
    }

    @Test
    public void rdfListFromSeqTest2Elements() throws Exception {
        Object result;
        try (Agent agent = AgentFactory.createFromFnO("rdfSeq.ttl")) {
            Arguments arguments = new Arguments()
                    .add(RDF + "_1", "a")
                    .add(RDF + "_2", "b");
            result = agent.execute(IDLABFN + "makeListFromSeq", arguments);
        }
        List<String> correct = new ArrayList<>();
        correct.add("a");
        correct.add("b");
        assertEquals(correct, result, "should be a list with elements a and b");
    }

    @Test
    public void rdfListFromSeqTest4ElementOneMissing() throws Exception {
        try (Agent agent = AgentFactory.createFromFnO("rdfSeq.ttl")) {
            Arguments arguments = new Arguments()
                    .add(RDF + "_1", "a")
                    .add(RDF + "_2", "b")
                    .add(RDF + "_4", "d");

            assertThrows(AgentException.class, () -> agent.execute(IDLABFN + "makeListFromSeq", arguments), "expected exception for missing _3 parameter");
        }
    }

    @Test
    public void rdfSeqTest1Element() throws Exception {
        Object result;
        try (Agent agent = AgentFactory.createFromFnO("rdfSeq.ttl")) {
            Arguments arguments = new Arguments()
                    .add(RDF + "_1", "a");
            result = agent.execute(IDLABFN + "makeListFromSeq", arguments);
        }
        List<String> correct = new ArrayList<>();
        correct.add("a");
        assertEquals(correct, result, "should be a list with element a");
    }

    @Test
    public void rdfSeqTest2Element() throws Exception {
        Object result;
        try (Agent agent = AgentFactory.createFromFnO("rdfSeq.ttl")) {
            Arguments arguments = new Arguments()
                    .add(RDF + "_2", "b")
                    .add(RDF + "_1", "a");
            result = agent.execute(IDLABFN + "makeListFromSeq", arguments);
        }
        List<String> correct = new ArrayList<>();
        correct.add("a");
        correct.add("b");
        assertEquals(correct, result, "should be a list with elements a and b");
    }

    @Test
    public void rdfSeqTestNoErrorForNegativeIndex() throws Exception {
        Object result;
        try (Agent agent = AgentFactory.createFromFnO("rdfSeq.ttl")) {
            Arguments arguments = new Arguments()
                    .add(RDF + "_-1", "a")
                    .add(RDF + "_1", "x");
            result = agent.execute(IDLABFN + "makeListFromSeq", arguments);
        }
        List<String> correct = new ArrayList<>();
        correct.add("x");
        assertEquals(correct, result, "should be a list with element x");
    }

    @Test
    public void testExtraDependencies1() throws Exception {
        try (Agent agent = AgentFactory.createFromFnO("generalFunctions.ttl", "weirdComposition1.ttl")) {
            Arguments arguments = new Arguments()
                    .add(EX + "p_int1", "5")
                    .add(EX + "p_int2", "4");
            assertEquals(90L, agent.execute(FNS + "comp", arguments), "expected 90");
        }
    }

    @Test
    public void testExtraDependencies2() throws Exception {
        try (Agent agent = AgentFactory.createFromFnO("generalFunctions.ttl", "weirdComposition2.ttl")) {
            Arguments arguments = new Arguments()
                    .add(EX + "p_int1", "5")
                    .add(EX + "p_int2", "4");
            assertEquals(28L, agent.execute(FNS + "comp", arguments), "expected 28");
        }
    }

    @Test
    public void loadNonStaticFunctionThrowsException() throws FnOException {
        try (AgentImpl agent = (AgentImpl) AgentFactory.createFromFnO()) {
            assertThrows(Exception.class, () -> agent.loadFunction(this.getClass().getMethods()[0])); // all test methods are non static
        }

    }

    @Test
    public void loadJavaFunctionTest() throws Exception {
        Object result;
        try (AgentImpl agent = (AgentImpl) AgentFactory.createFromFnO()) {
            String functionId = agent.loadFunction(InternalTestFunctions.class.getMethod("sum", long.class, long.class));
            List<String> list = agent.getParameterPredicates(functionId);
            Arguments args = new Arguments()
                    .add(list.get(0), 5)
                    .add(list.get(1), 6);
            result = agent.execute(functionId, args);
        }
        assertEquals(11L, result, "5 + 6 is 11");
    }

    @Test
    public void testWriteModel() throws Exception {
        try (AgentImpl agent = (AgentImpl) AgentFactory.createFromFnO("generalFunctions.ttl")) {
            agent.writeModel("testFileWrite.ttl");
        }
        try (AgentImpl agent = (AgentImpl) AgentFactory.createFromFnO("testFileWrite.ttl")) {
            execute(agent);
        }
    }

    @Test
    public void testWriteExecutionToFile() throws Exception {
        try (AgentImpl agent = (AgentImpl) AgentFactory.createFromFnO("internalTestFunctions.ttl")) {
            // prepare the parameters for the function
            Arguments arguments = new Arguments().add(EX + "p_int1", "5").add(EX + "p_int2", "1");

            // execute the function
            agent.executeToFile(EX + "sum", arguments, "testExecution.ttl");
        }
    }

//    @Test
//    public void testWriteModelWithComposition() throws Exception{
//        Agent agent = AgentFactory.createFromFnO("generalFunctions.ttl", "sum-composition.ttl");
//        agent.writeModel("testFileWriteWithComposition.ttl");
//        agent = AgentFactory.createFromFnO("testFileWriteWithComposition.ttl");
//        Arguments arguments = new Arguments()
//                // fns:aParameter fns:bParameter fns:cParameter
//                .add(FNS+"a", "1")
//                .add(FNS+"b", "2")
//                .add(FNS+"c", "3");
//
//        Object result = agent.execute(FNS+"sum3", arguments);
//
//        assertEquals("1 + 2 + 3 should be 6", 6L, result);
//    }

    @Test
    public void testGenericsInCollectionAsFunctionParameter() throws Exception {
        Object result;
        try (Agent agent = AgentFactory.createFromFnO("rdfSeqGenerics.ttl")) {
            Arguments arguments = new Arguments()
                    .add(RDF + "_1", "a")
                    .add(RDF + "_2", "b")
                    .add(EX + "delimiter", " precedes ");
            result = agent.execute(EX + "concatSequence", arguments, true);
        }
        assertEquals("a precedes b", result );
    }

}
