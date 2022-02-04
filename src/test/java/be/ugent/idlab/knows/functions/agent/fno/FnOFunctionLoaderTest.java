package be.ugent.idlab.knows.functions.agent.fno;

import org.junit.Test;

/**
 * <p>Copyright 2022 IDLab (Ghent University - imec)</p>
 *
 * @author Gerald Haesendonck
 */
public class FnOFunctionLoaderTest {

    @Test
    public void testLoad() {
        FnOFunctionModelLoader functionLoader = new FnOFunctionModelLoader();
        functionLoader.load("src/test/resources/internalTestFunctions.ttl");
    }
}
