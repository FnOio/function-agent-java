package be.ugent.idlab.knows.functions.agent.functionModelProvider.fno;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import be.ugent.idlab.knows.functions.agent.dataType.DataTypeConverter;
import be.ugent.idlab.knows.functions.agent.dataType.DataTypeConverterProvider;
import be.ugent.idlab.knows.functions.agent.functionModelProvider.FunctionModelProvider;
import be.ugent.idlab.knows.functions.agent.functionModelProvider.fno.exception.FnOException;
import be.ugent.idlab.knows.functions.agent.model.Function;
import be.ugent.idlab.knows.functions.agent.model.FunctionMapping;
import be.ugent.idlab.knows.functions.agent.model.Implementation;
import be.ugent.idlab.knows.functions.agent.model.MethodMapping;
import be.ugent.idlab.knows.functions.agent.model.Parameter;
import be.ugent.idlab.knows.misc.FileFinder;

/**
 * <p>Copyright 2022 IDLab (Ghent University - imec)</p>
 *
 * @author Gerald Haesendonck
 */
public class FnOFunctionProviderTest {
    final DataTypeConverterProvider dataTypeConverterProvider = new DataTypeConverterProvider();

    @Test
    public void testLoad() throws FnOException {
        FunctionModelProvider functionProvider = new FnOFunctionModelProvider(dataTypeConverterProvider, "src/test/resources/internalTestFunctions.ttl");
        checkSuccessFunctions(functionProvider.getFunctions().values());
    }

    @Test
    public void testLoadDeprecated() throws FnOException {
        FunctionModelProvider functionProvider = new FnOFunctionModelProvider(dataTypeConverterProvider, "src/test/resources/internalTestFunctions_old.ttl");
        checkSuccessFunctions(functionProvider.getFunctions().values());
    }

    @Test
    public void testFnoDocNotFound() {
        assertThrows(Throwable.class,
                () -> new FnOFunctionModelProvider(dataTypeConverterProvider, "src/test/resources/doesnotexist.ttl"),
                "Constructing an FnOFunctionModelProvider from an unexisting file should fail.");
    }

    @Test
    public void testFnoDocumentAsDirectInput() throws URISyntaxException, IOException, FnOException {

        // read internalTestFunctions.ttl as String
        URL itUrl = FileFinder.findFile("internalTestFunctions.ttl");
        String fnoSource = Files.readString(Paths.get(itUrl.toURI()));

        // pass it to the function model provider
        FnOFunctionModelProvider functionProvider = new FnOFunctionModelProvider(dataTypeConverterProvider, fnoSource);
        checkSuccessFunctions(functionProvider.getFunctions().values());
    }

    private void checkSuccessFunctions(final Collection<Function> functionCollection) {
        List<Function> functions = new ArrayList<>(functionCollection);
        functions.sort(Comparator.comparing(Function::getId));

        assertEquals(2, functions.size(), "Number of functions parsed should be 2 ");
        Function rawListLenFunction = functions.get(0);
        assertEquals("http://example.org/rawListLen", rawListLenFunction.getId(), "Wrong function id");
        assertEquals("length of a raw list", rawListLenFunction.getName(), "Wrong function name");
        assertEquals("Returns the length of a raw list, i.e. without parameterized type", rawListLenFunction.getDescription(), "Wrong function description");

        Function sumFunction = functions.get(1);
        assertEquals("http://example.org/sum", sumFunction.getId(), "Wrong function id");
        assertEquals("Sum of two integers", sumFunction.getName(), "Wrong function name");
        assertEquals("Returns the sum of two given integers", sumFunction.getDescription(), "Wrong function description");

        // check input parameters
        List<Parameter> inputParameters = sumFunction.getArgumentParameters();
        assertEquals(2, inputParameters.size());

        for (int pi = 0; pi < inputParameters.size(); pi++) {
            int i = pi + 1;
            Parameter parameter = inputParameters.get(pi);
            assertEquals("integer " + i, parameter.getName(), "Wrong parameter " + i + " name ");
            assertEquals("http://example.org/p_int" + i, parameter.getId(), "Wrong parameter " + i + " id ");
            DataTypeConverter<?> typeConverter = parameter.getTypeConverter();
            assertTrue(typeConverter.isSubTypeOf(Long.class), "Wrong type converter class");
            assertTrue(parameter.isRequired(), "Required should be true for parameter " + i);
        }

        // check output parameters
        assertEquals(1, sumFunction.getReturnParameters().size(), "Wrong number of return parameters");
        Parameter returnParameter = sumFunction.getReturnParameters().get(0);
        assertEquals("integer output", returnParameter.getName(), "Wrong return parameter name ");
        assertEquals("http://example.org/o_int", returnParameter.getId(), "Wrong return parameter id ");
        DataTypeConverter<?> typeConverter = returnParameter.getTypeConverter();
        assertTrue(typeConverter.isSuperTypeOf(Long.class), "Wrong type converter class");
        assertTrue(returnParameter.isRequired(), "Required should be true for return parameter");

        // check mapping
        FunctionMapping mapping = sumFunction.getFunctionMapping();
        assertEquals("http://example.org/sum", mapping.functionId(), "wrong function id for mapping");

        MethodMapping methodMapping = mapping.methodMapping();
        assertEquals("sum", methodMapping.methodName(), "Wrong method name");
        assertEquals("https://w3id.org/function/vocabulary/mapping#StringMethodMapping", methodMapping.type(), "Wrong method type");

        Implementation implementation = mapping.implementation();
        assertEquals("be.ugent.idlab.knows.functions.internalfunctions.InternalTestFunctions", implementation.className(), "Wrong implementation class name");
        assertEquals("", implementation.location(), "Implementation: location should be empty.");
    }

    @Test
    public void testChangeLocation() throws FnOException {
        // map the location of AaabimFunctions.jar to somewhere else
        final String newLocation = "/some/other/path";
        Map<String, String> mappedAaabimJarLocationMap = Collections.singletonMap("AaabimFunctions.jar", newLocation);
        FunctionModelProvider functionProvider = new FnOFunctionModelProvider(dataTypeConverterProvider, mappedAaabimJarLocationMap, "aaabim_java_mapping.ttl");

        // now check
        functionProvider.getFunctions().values().forEach(function -> {
            String functionId = function.getId();
            String location = function.getFunctionMapping().implementation().location();
            assertEquals(newLocation, location, "Wrong location of function '" + functionId + "'.");
        });
    }

    /**
     * When the implementation location is not in the implementation location map, it should remain the same.
     */
    @Test
    public void testChangeUnexistingLocation() throws FnOException {
        FunctionModelProvider functionProvider = new FnOFunctionModelProvider(dataTypeConverterProvider, "aaabim_java_mapping.ttl");

        // now check
        functionProvider.getFunctions().values().forEach(function -> {
            String functionId = function.getId();
            String location = function.getFunctionMapping().implementation().location();
            assertEquals("AaabimFunctions.jar", location, "Wrong location of function '" + functionId + "'.");
        });
    }

    @Test
    public void testCompositeFunctionFound() throws FnOException{
        FunctionModelProvider functionProvider = new FnOFunctionModelProvider(dataTypeConverterProvider, "sum-composition.ttl");
        assertNotEquals(0, functionProvider.getFunctions().size(), "No functions were found");
    }

    @Test
    public void testCompositeFunctionWithLiteralFound() throws FnOException{
        FunctionModelProvider functionProvider = new FnOFunctionModelProvider(dataTypeConverterProvider, "add10.ttl");
        assertNotEquals(0, functionProvider.getFunctions().size(), "no functions were found");
    }

}
