package be.ugent.idlab.knows.functions.agent.functionInstantiator;

import be.ugent.idlab.knows.functions.agent.functionIntantiation.Instantiator;
import be.ugent.idlab.knows.functions.agent.functionIntantiation.exception.InstantiationException;
import be.ugent.idlab.knows.functions.agent.functionModelProvider.FunctionModelProvider;
import be.ugent.idlab.knows.functions.agent.functionModelProvider.fno.FnOFunctionModelProvider;
import be.ugent.idlab.knows.functions.agent.model.Function;
import org.junit.Test;

import java.util.Collection;

/**
 * <p>Copyright 2022 IDLab (Ghent University - imec)</p>
 *
 * @author Gerald Haesendonck
 */
public class InstantiatorTest {

    @Test
    public void testClassOnClasspath() throws InstantiationException {
        // load function descriptions
        FunctionModelProvider functionProvider = new FnOFunctionModelProvider("src/test/resources/internalTestFunctions.ttl");
        Collection<Function> functions = functionProvider.getFunctions();
        Instantiator instantiator = new Instantiator(functions);

        instantiator.getWhatever("http://example.org/sum");
    }
}
