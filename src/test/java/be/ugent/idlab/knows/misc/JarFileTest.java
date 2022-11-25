package be.ugent.idlab.knows.misc;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

/**
 * <p>Copyright 2022 IDLab (Ghent University - imec)</p>
 *
 * @author Gerald Haesendonck
 */
public class JarFileTest {

    @Test
    public void testExtractClassesFromJar() throws IOException {
        String jarFilePath = "./src/test/resources/GrelFunctions.jar";
        //String jarFilePath = "/home/gerald/projects/rml/rml-streamer/target/RMLStreamer-2.2.2.jar";
        JarFile jarFile = new JarFile(jarFilePath);
        System.out.println("name: " + jarFile.getName());
        try (URLClassLoader cl = URLClassLoader.newInstance(
                new URL[] { new URL("jar:file:" + jarFilePath + "!/") })) {
            jarFile
                    .stream()
                    .map(ZipEntry::getName)
                    .filter(name -> name.endsWith(".class") && !name.contains("$"))
                    .map(name -> name.substring(0, name.lastIndexOf('.')).replaceAll("/", "."))
                    .forEach(className -> {
                        System.out.println("class: " + className);
                        try {
                            Class<?> clazz = cl.loadClass(className);
                            Method[] methods = clazz.getDeclaredMethods();
                            for (Method method : methods) {
                                System.out.println("  " + method.getName());
                            }
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                    });
        }

    }
}
