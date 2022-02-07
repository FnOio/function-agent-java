package be.ugent.idlab.knows.functions.agent.fno;

import be.ugent.idlab.knows.functions.agent.model.*;
import org.junit.Test;

import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * <p>Copyright 2022 IDLab (Ghent University - imec)</p>
 *
 * @author Gerald Haesendonck
 */
public class FnOFunctionLoaderTest {

    @Test
    public void testLoad() {
        FnOFunctionModelLoader functionLoader = new FnOFunctionModelLoader("src/test/resources/internalTestFunctions.ttl");
        checkSuccessFunctions(functionLoader.getFunctions());
    }

    @Test
    public void testLoadDeprecated() {
        FnOFunctionModelLoader functionLoader = new FnOFunctionModelLoader("src/test/resources/internalTestFunctions_old.ttl");
        checkSuccessFunctions(functionLoader.getFunctions());
    }

    @Test
    public void testFnoDocNotFound() {
        FnOFunctionModelLoader functionLoader = new FnOFunctionModelLoader("src/test/resources/doesnotexist.ttl");
        Collection<Function> functions = functionLoader.getFunctions();
        assertTrue("No functions should be loaded when parsing document fails", functions.isEmpty());
    }

    private void checkSuccessFunctions(final Collection<Function> functions) {
        assertEquals("Number of functions parsed should be 1 ", 1, functions.size());
        Function function = functions.iterator().next();
        assertEquals("Wrong function id", "http://example.org/sum", function.getId());
        assertEquals("Wrong function name", "Sum of two integers", function.getName());
        assertEquals("Wrong function description", "Returns the sum of two given integers", function.getDescription());

        // check input parameters
        List<Parameter> inputParameters = function.getArgumentParameters();
        assertEquals(2, inputParameters.size());

        for (int pi = 0; pi < inputParameters.size(); pi++) {
            int i = pi + 1;
            Parameter parameter = inputParameters.get(pi);
            assertEquals("Wrong parameter " + i + " name ", "integer " + i, parameter.getName());
            assertEquals("Wrong parameter " + i + " predicateUri ", "http://example.org/p_int" + i, parameter.getPredicateUri());
            assertEquals("Wrong parameter " + i + " type ", "http://www.w3.org/2001/XMLSchema#integer", parameter.getTypeUri());
            assertTrue("Required should be true for parameter " + i , parameter.isRequired());
        }

        // check output parameters
        assertEquals("Wrong number of return parameters", 1, function.getReturnParameters().size());
        Parameter returnParameter = function.getReturnParameters().get(0);
        assertEquals("Wrong return parameter name ", "integer output", returnParameter.getName());
        assertEquals("Wrong return parameter predicateUri ", "http://example.org/o_int", returnParameter.getPredicateUri());
        assertEquals("Wrong return parameter type ", "http://www.w3.org/2001/XMLSchema#integer", returnParameter.getTypeUri());
        assertTrue("Required should be true for return parameter", returnParameter.isRequired());

        // check mapping
        FunctionMapping mapping = function.getFunctionMapping();
        assertEquals("wrong function id for mapping", "http://example.org/sum", mapping.getFunctionId());

        MethodMapping methodMapping = mapping.getMethodMapping();
        assertEquals("Wrong method name", "sum", methodMapping.getMethodName());
        assertEquals("Wrong method type", "https://w3id.org/function/vocabulary/mapping#StringMethodMapping", methodMapping.getType());

        Implementation implementation = mapping.getImplementation();
        assertEquals("Wrong implementation class name", "be.ugent.idlab.knows.InternalTestFunctions", implementation.getClassName());
        assertEquals("Implementation: location should be empty.","", implementation.getLocation());
    }
}
