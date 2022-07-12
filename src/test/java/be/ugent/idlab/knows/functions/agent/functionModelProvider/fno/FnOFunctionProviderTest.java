package be.ugent.idlab.knows.functions.agent.functionModelProvider.fno;

import be.ugent.idlab.knows.functions.agent.dataType.DataTypeConverter;
import be.ugent.idlab.knows.functions.agent.dataType.DataTypeConverterProvider;
import be.ugent.idlab.knows.functions.agent.functionModelProvider.FunctionModelProvider;
import be.ugent.idlab.knows.functions.agent.functionModelProvider.fno.exception.FnOException;
import be.ugent.idlab.knows.functions.agent.model.*;
import be.ugent.idlab.knows.misc.FileFinder;
import org.apache.jena.ext.com.google.common.io.CharSource;
import org.apache.jena.ext.com.google.common.io.Files;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.junit.Assert.*;

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
        assertThrows("Constructing an FnOFunctionModelProvider from an unexisting file should fail.", Throwable.class,
                () -> new FnOFunctionModelProvider(dataTypeConverterProvider, "src/test/resources/doesnotexist.ttl"));
    }

    @Test
    public void testFnoDocumentAsDirectInput() throws URISyntaxException, IOException, FnOException {

        // read internalTestFunctions.ttl as String
        URL itUrl = FileFinder.findFile("internalTestFunctions.ttl");
        CharSource fnoSource = Files.asCharSource(new File(itUrl.toURI()), StandardCharsets.UTF_8);

        // pass it to the function model provider
        FnOFunctionModelProvider functionProvider = new FnOFunctionModelProvider(dataTypeConverterProvider, fnoSource.read());
        checkSuccessFunctions(functionProvider.getFunctions().values());
    }

    private void checkSuccessFunctions(final Collection<Function> functionCollection) {
        List<Function> functions = new ArrayList<>(functionCollection);
        functions.sort(Comparator.comparing(Function::getId));

        assertEquals("Number of functions parsed should be 2 ", 2, functions.size());
        Function rawListLenFunction = functions.get(0);
        assertEquals("Wrong function id", "http://example.org/rawListLen", rawListLenFunction.getId());
        assertEquals("Wrong function name", "length of a raw list", rawListLenFunction.getName());
        assertEquals("Wrong function description", "Returns the length of a raw list, i.e. without parameterized type", rawListLenFunction.getDescription());

        Function sumFunction = functions.get(1);
        assertEquals("Wrong function id", "http://example.org/sum", sumFunction.getId());
        assertEquals("Wrong function name", "Sum of two integers", sumFunction.getName());
        assertEquals("Wrong function description", "Returns the sum of two given integers", sumFunction.getDescription());

        // check input parameters
        List<Parameter> inputParameters = sumFunction.getArgumentParameters();
        assertEquals(2, inputParameters.size());

        for (int pi = 0; pi < inputParameters.size(); pi++) {
            int i = pi + 1;
            Parameter parameter = inputParameters.get(pi);
            assertEquals("Wrong parameter " + i + " name ", "integer " + i, parameter.getName());
            assertEquals("Wrong parameter " + i + " id ", "http://example.org/p_int" + i, parameter.getId());
            DataTypeConverter<?> typeConverter = parameter.getTypeConverter();
            assertTrue("Wrong type converter class", typeConverter.isSubTypeOf(Long.class));
            assertTrue("Required should be true for parameter " + i , parameter.isRequired());
        }

        // check output parameters
        assertEquals("Wrong number of return parameters", 1, sumFunction.getReturnParameters().size());
        Parameter returnParameter = sumFunction.getReturnParameters().get(0);
        assertEquals("Wrong return parameter name ", "integer output", returnParameter.getName());
        assertEquals("Wrong return parameter id ", "http://example.org/o_int", returnParameter.getId());
        DataTypeConverter<?> typeConverter = returnParameter.getTypeConverter();
        assertTrue("Wrong type converter class", typeConverter.isSuperTypeOf(Long.class));
        assertTrue("Required should be true for return parameter", returnParameter.isRequired());

        // check mapping
        FunctionMapping mapping = sumFunction.getFunctionMapping();
        assertEquals("wrong function id for mapping", "http://example.org/sum", mapping.getFunctionId());

        MethodMapping methodMapping = mapping.getMethodMapping();
        assertEquals("Wrong method name", "sum", methodMapping.getMethodName());
        assertEquals("Wrong method type", "https://w3id.org/function/vocabulary/mapping#StringMethodMapping", methodMapping.getType());

        Implementation implementation = mapping.getImplementation();
        assertEquals("Wrong implementation class name", "be.ugent.idlab.knows.functions.internalfunctions.InternalTestFunctions", implementation.getClassName());
        assertEquals("Implementation: location should be empty.","", implementation.getLocation());
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
            String location = function.getFunctionMapping().getImplementation().getLocation();
            assertEquals("Wrong location of function '" + functionId + "'.", newLocation, location);
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
            String location = function.getFunctionMapping().getImplementation().getLocation();
            assertEquals("Wrong location of function '" + functionId + "'.", "AaabimFunctions.jar", location);
        });
    }

    @Test
    public void testCompositeFunctionFound() throws FnOException{
        FunctionModelProvider functionProvider = new FnOFunctionModelProvider(dataTypeConverterProvider, "sum-composition.ttl");
        assertNotEquals("No functions were found", 0, functionProvider.getFunctions().size());
    }

    @Test
    public void testCompositeFunctionWithLiteralFound() throws FnOException{
        FunctionModelProvider functionProvider = new FnOFunctionModelProvider(dataTypeConverterProvider, "add10.ttl");
        assertNotEquals("no functions were found", 0, functionProvider.getFunctions().size());
    }

}
