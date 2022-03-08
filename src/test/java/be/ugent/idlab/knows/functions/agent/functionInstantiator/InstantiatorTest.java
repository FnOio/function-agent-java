package be.ugent.idlab.knows.functions.agent.functionInstantiator;

import be.ugent.idlab.knows.functions.agent.functionIntantiation.Instantiator;
import be.ugent.idlab.knows.functions.agent.functionIntantiation.exception.InstantiationException;
import be.ugent.idlab.knows.functions.agent.functionModelProvider.FunctionModelProvider;
import be.ugent.idlab.knows.functions.agent.functionModelProvider.fno.FnOFunctionModelProvider;
import be.ugent.idlab.knows.functions.agent.functionModelProvider.fno.exception.FnOException;
import be.ugent.idlab.knows.functions.agent.model.Function;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * <p>Copyright 2022 IDLab (Ghent University - imec)</p>
 *
 * @author Gerald Haesendonck
 */
public class InstantiatorTest {

    @Test
    public void testClassOnClasspath() throws InstantiationException, InvocationTargetException, IllegalAccessException, FnOException {
        // load function descriptions
        FunctionModelProvider functionProvider = new FnOFunctionModelProvider("src/test/resources/internalTestFunctions.ttl");
        Map<String, Function> functions = functionProvider.getFunctions();
        Instantiator instantiator = new Instantiator(functions);

        Method sum = instantiator.getMethod("http://example.org/sum");
        Object result = sum.invoke(null, 5, 8);
        assertTrue(result instanceof Long);
        Long resultValue = (Long) result;
        assertEquals("5 + 8 should be 13", 13, resultValue.intValue());
    }

    @Test
    public void testLoadSumWithNonPrimitives() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Class<?> clazz = Class.forName("be.ugent.idlab.knows.functions.internalfunctions.InternalTestFunctions");
        Method method = clazz.getMethod("sum", long.class, long.class);
        Object result = method.invoke(null, Long.valueOf(1), Long.valueOf(3));
        assertEquals("1 + 3 should be 4", 4L, result);
        Class<?>[] genericParameterTypes = method.getParameterTypes();
        assertArrayEquals(new Class<?>[]{long.class, long.class}, genericParameterTypes);
    }
}
