package be.ugent.idlab.knows.misc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * <p>Copyright 2022 IDLab (Ghent University - imec)</p>
 *
 * @author Gerald Haesendonck
 */
public class FileFinder {
    private final static Logger logger = LoggerFactory.getLogger(FileFinder.class);

    /**
     * Searches for a file URL. It is searched for as follows:
     * <ol>
     *     <li>If the path starts with <code>http://</code> or <code>https://</code>, it is concidered a remote URL</li>
     *     <li>Else, the path is considered a local file path. If it is not absolute, it is considered relative to the current working directory.</li>
     *     <li>If it is not found by now, it is searched for on the class path.</li>
     * </ol>
     * @param path  The location of the file to search.
     * @return      The  URL corresponding to the given path or <code>null</code>
     *              if the file is not found or the operation is not permitted.
     */
    public static URL findFile(final String path) {
        logger.debug("Finding file '{}'", path);
        URL url = null;
        if (path.startsWith("http://") || path.startsWith("https://")) {
            logger.debug("Found remote URL for path '{}'", path);
            try {
                url = new URL(path);
            } catch (MalformedURLException e) {
                logger.debug(e.getMessage());
            }
        }
        if (url == null) {
            url = findFileRelativeToWorkingDir(path);
            if (url == null) {
                url = findFileOnClassPath(path);
            }
        }
        return url;
    }

    /**
     * Searches for a file on the classpath and returns its URL.
     * @param path  The path of the file
     * @return      The URL corresponding to the given path or <code>null</code>
     *              if the file is not found or the operation is not permitted.
     */
    static URL findFileOnClassPath(final String path) {
        logger.debug("Searching '{}' on classpath", path);
        final ClassLoader classLoader = FileFinder.class.getClassLoader();
        return classLoader.getResource(path);
    }

    /**
     * Searches for a file relative to the current working dir and returns its URL.
     * Also works for absolute paths, in which case the current working directory is ignored.
     * @param path  The path of the file
     * @return      The URL corresponding to the given path or <code>null</code>
     *              if the file is not found or the operation is not permitted.
     */
    static URL findFileRelativeToWorkingDir(final String path) {
        logger.debug("Searching '{}' (absolute or relative to current working dir)", path);
        final String workingDir = System.getProperty("user.dir");
        final Path workingDirPath = Paths.get(workingDir);
        final Path resolved = workingDirPath.resolve(path);
        final File file = resolved.toFile();
        if (file.exists()) {
            try {
                return file/*.getCanonicalFile()*/.toURI().toURL();
            } catch (MalformedURLException e) {
                logger.info(e.getMessage());
            }
        }
        return null;
    }
}
