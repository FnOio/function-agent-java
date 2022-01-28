package be.ugent.idlab.knows.functions.agent;

import be.ugent.idlab.knows.functions.agent.FunctionLoader;
import org.junit.Test;

/**
 * <p>Copyright 2022 IDLab (Ghent University - imec)</p>
 *
 * @author Gerald Haesendonck
 */
public class FunctionLoaderTest {

    @Test
    public void testLoad() {
        FunctionLoader functionLoader = new FunctionLoader();
        functionLoader.load("src/test/resources/internalTestFunctions.ttl");
    }
}
