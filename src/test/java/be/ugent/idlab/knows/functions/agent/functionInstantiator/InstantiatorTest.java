package be.ugent.idlab.knows.functions.agent.functionInstantiator;

import be.ugent.idlab.knows.functions.agent.dataType.DataTypeConverterProvider;
import be.ugent.idlab.knows.functions.agent.functionIntantiation.Instantiator;
import be.ugent.idlab.knows.functions.agent.functionIntantiation.exception.InstantiationException;
import be.ugent.idlab.knows.functions.agent.functionModelProvider.FunctionModelProvider;
import be.ugent.idlab.knows.functions.agent.functionModelProvider.fno.FnOFunctionModelProvider;
import be.ugent.idlab.knows.functions.agent.functionModelProvider.fno.exception.FnOException;
import be.ugent.idlab.knows.functions.agent.model.Function;
import be.ugent.idlab.knows.functions.internalfunctions.InternalTestFunctions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * <p>Copyright 2022 IDLab (Ghent University - imec)</p>
 *
 * @author Gerald Haesendonck
 */
public class InstantiatorTest {

    @Test
    public void testClassOnClasspath() throws InstantiationException, InvocationTargetException, IllegalAccessException, FnOException {
        final DataTypeConverterProvider dataTypeConverterProvider = new DataTypeConverterProvider();

        // load function descriptions
        FunctionModelProvider functionProvider = new FnOFunctionModelProvider(dataTypeConverterProvider, "src/test/resources/internalTestFunctions.ttl");
        Map<String, Function> functions = functionProvider.getFunctions();
        Instantiator instantiator = new Instantiator(functions, dataTypeConverterProvider);

        Method sum = instantiator.getMethod("http://example.org/sum");
        Object result = sum.invoke(null, 5, 8);
        assertTrue(result instanceof Long);
        Long resultValue = (Long) result;
        assertEquals(13, resultValue.intValue(), "5 + 8 should be 13");
    }

    @Test
    public void testLoadSumWithNonPrimitives() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Class<?> clazz = Class.forName("be.ugent.idlab.knows.functions.internalfunctions.InternalTestFunctions");
        Method method = clazz.getMethod("sum", long.class, long.class);
        Object result = method.invoke(null, Long.valueOf(1), Long.valueOf(3));
        assertEquals(4L, result, "1 + 3 should be 4");
        Class<?>[] genericParameterTypes = method.getParameterTypes();
        assertArrayEquals(new Class<?>[]{long.class, long.class}, genericParameterTypes);
    }

    @Test
    public void testRawListParameter() throws FnOException, InstantiationException, InvocationTargetException, IllegalAccessException {
        final DataTypeConverterProvider dataTypeConverterProvider = new DataTypeConverterProvider();

        // load function descriptions
        FunctionModelProvider functionProvider = new FnOFunctionModelProvider(dataTypeConverterProvider, "src/test/resources/internalTestFunctions.ttl");
        Map<String, Function> functions = functionProvider.getFunctions();
        Instantiator instantiator = new Instantiator(functions, dataTypeConverterProvider);

        List anEmptyRawList = new ArrayList();
        Method rawListLen = instantiator.getMethod("http://example.org/rawListLen");
        Object result = rawListLen.invoke(null, anEmptyRawList);
        assertEquals(0L, result, "An empty list should have size 0");
    }

    @Test
    public void testClose() throws FnOException, InstantiationException {
        final DataTypeConverterProvider dataTypeConverterProvider = new DataTypeConverterProvider();

        // load function descriptions
        FunctionModelProvider functionProvider = new FnOFunctionModelProvider(dataTypeConverterProvider, "src/test/resources/internalTestFunctions.ttl");
        Map<String, Function> functions = functionProvider.getFunctions();
        Instantiator instantiator = new Instantiator(functions, dataTypeConverterProvider);

        // first invoke a method to actually load the class
        // without loading, it cannot be closed ;)
        Method sum = instantiator.getMethod("http://example.org/sum");
        assertEquals("sum", sum.getName());


        instantiator.close();
        assertTrue(InternalTestFunctions.isClosed());
    }
}
