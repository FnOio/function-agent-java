package be.ugent.idlab.knows.functions.agent.functionModelProvider.fno;

import be.ugent.idlab.knows.functions.agent.dataType.DataTypeConverter;
import be.ugent.idlab.knows.functions.agent.functionModelProvider.FunctionModelProvider;
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
public class FnOFunctionProviderTest {

    @Test
    public void testLoad() {
        FunctionModelProvider functionProvider = new FnOFunctionModelProvider("src/test/resources/internalTestFunctions.ttl");
        checkSuccessFunctions(functionProvider.getFunctions());
    }

    @Test
    public void testLoadDeprecated() {
        FunctionModelProvider functionProvider = new FnOFunctionModelProvider("src/test/resources/internalTestFunctions_old.ttl");
        checkSuccessFunctions(functionProvider.getFunctions());
    }

    @Test
    public void testFnoDocNotFound() {
        FnOFunctionModelProvider functionLoader = new FnOFunctionModelProvider("src/test/resources/doesnotexist.ttl");
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
            DataTypeConverter<?> typeConverter = parameter.getTypeConverter();
            assertEquals("Wrong type converter class", Integer.class, typeConverter.getTypeClasses().get(0));
            assertTrue("Required should be true for parameter " + i , parameter.isRequired());
        }

        // check output parameters
        assertEquals("Wrong number of return parameters", 1, function.getReturnParameters().size());
        Parameter returnParameter = function.getReturnParameters().get(0);
        assertEquals("Wrong return parameter name ", "integer output", returnParameter.getName());
        assertEquals("Wrong return parameter predicateUri ", "http://example.org/o_int", returnParameter.getPredicateUri());
        DataTypeConverter<?> typeConverter = returnParameter.getTypeConverter();
        assertEquals("Wrong type converter class", Integer.class, typeConverter.getTypeClasses().get(0));
        assertTrue("Required should be true for return parameter", returnParameter.isRequired());

        // check mapping
        FunctionMapping mapping = function.getFunctionMapping();
        assertEquals("wrong function id for mapping", "http://example.org/sum", mapping.getFunctionId());

        MethodMapping methodMapping = mapping.getMethodMapping();
        assertEquals("Wrong method name", "sum", methodMapping.getMethodName());
        assertEquals("Wrong method type", "https://w3id.org/function/vocabulary/mapping#StringMethodMapping", methodMapping.getType());

        Implementation implementation = mapping.getImplementation();
        assertEquals("Wrong implementation class name", "be.ugent.idlab.knows.functions.internalfunctions.InternalTestFunctions", implementation.getClassName());
        assertEquals("Implementation: location should be empty.","", implementation.getLocation());
    }
}
