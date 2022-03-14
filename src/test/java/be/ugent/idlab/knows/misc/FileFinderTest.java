package be.ugent.idlab.knows.misc;

import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * <p>Copyright 2022 IDLab (Ghent University - imec)</p>
 *
 * @author Gerald Haesendonck
 */
public class FileFinderTest {

    @Test
    public void testFindFileOnClassPath() throws IOException {
        findFileOnClassPath("functions_grel.ttl");
    }

    @Test
    public void testFindFileOnClassPathExternalDependency() throws IOException {
        findFileOnClassPath("grel_java_mapping.ttl");
    }

    @Test
    public void testfindFileRelativeToWorkingDir() throws IOException {
        final String path = "src/test/java/be/ugent/idlab/knows/misc/FileFinderTest.java";
        URL url = FileFinder.findFileRelativeToWorkingDir(path);
        checkURL(url, path);
    }

    @Test
    public void testfindFileRelativeToWorkingDirAbsolute() {
        final String path = System.getProperty("java.io.tmpdir");
        URL url = FileFinder.findFileRelativeToWorkingDir(path);
        assertNotNull(url);
    }

    @Test
    public void testFindFile_relative() throws IOException {
        final String path = "src/test/java/be/ugent/idlab/knows/misc/FileFinderTest.java";
        URL url = FileFinder.findFile(path);
        checkURL(url, path);
    }

    @Test
    public void testFindFile_onClassPath() throws IOException {
        final String path = "simplelogger.properties";
        URL url = FileFinder.findFile(path);
        checkURL(url, path);
    }

    @Test
    public void testFindFile_remote() throws IOException {
        final String path = "https://users.ugent.be/~bjdmeest/function/grel.ttl";
        URL url = FileFinder.findFile(path);
        checkURL(url, path);
    }

    private void findFileOnClassPath(final String path) throws IOException {
        URL url = FileFinder.findFileOnClassPath(path);
        checkURL(url, path);
    }

    private void checkURL(final URL url, final String path) throws IOException {
        assertNotNull("URL with path " + path + " should not be null", url);
        try (InputStream in = url.openStream()) {
            int bite = in.read();
            assertTrue("There should be contents available on URL " + url, bite >= 0);
        }
    }
}
