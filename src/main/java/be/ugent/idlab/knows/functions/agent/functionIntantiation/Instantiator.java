package be.ugent.idlab.knows.functions.agent.functionIntantiation;

import be.ugent.idlab.knows.functions.agent.functionIntantiation.exception.ClassNotFoundException;
import be.ugent.idlab.knows.functions.agent.functionIntantiation.exception.FunctionNotFoundException;
import be.ugent.idlab.knows.functions.agent.functionIntantiation.exception.InstantiationException;
import be.ugent.idlab.knows.functions.agent.functionIntantiation.exception.MethodNotFoundException;
import be.ugent.idlab.knows.functions.agent.model.Function;
import be.ugent.idlab.knows.functions.agent.model.FunctionMapping;
import be.ugent.idlab.knows.functions.agent.model.Parameter;
import be.ugent.idlab.knows.misc.FileFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An Instantiator tries to find an implementation (Java Method for now) for any given {@link Function}.
 * The method can be executed to perform the actual function.
 *
 * <p>Copyright 2022 IDLab (Ghent University - imec)</p>
 *
 * @author Gerald Haesendonck
 */
public class Instantiator {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Map<String, Function> id2functionMap;

    // a 'cache' of Methods associated with function ids.
    private final Map<String, Method> id2MethodMap = new HashMap<>();

    /**
     * Creates a new instance of an Initiator.
     * @param functions The function descriptions used to find for possible implementations in the form of a map function ID -> Function.
     */
    public Instantiator(Map<String, Function> functions) {
        id2functionMap = functions;
    }

    /**
     * Tries to get a method to execute the function with the given ID.
     * @param functionId    The identifier of the function for which a method has to be found.
     * @return              An executable method which is an implementation of the function with the given functionId.
     * @throws InstantiationException   Something goes wrong finding a method. A subclass of this exception gives more details aboud what goes wrong.
     */
    public Method getMethod(final String functionId) throws InstantiationException {
        logger.debug("Getting instantiation for {}", functionId);
        if (id2MethodMap.containsKey(functionId)) {
            logger.debug("Method for {} found in cache.", functionId);
            return id2MethodMap.get(functionId);
        }
        if (id2functionMap.containsKey(functionId)) {
            final Function function = id2functionMap.get(functionId);
            final FunctionMapping mapping = function.getFunctionMapping();

            final String location = mapping.getImplementation().getLocation();

            final String className = mapping.getImplementation().getClassName();
            Class<?> clazz = getClass(className, location);
            logger.debug("Found class {}", clazz);

            // now get the method
            final String methodName = mapping.getMethodMapping().getMethodName();
            final List<Parameter> parameters = function.getArgumentParameters();
            Method method = getMethod(clazz, methodName, parameters, function.getReturnParameters().get(0));
            logger.debug("Found method {}", method.getName());
            id2MethodMap.put(functionId, method);
            return method;
        } else {
            throw new FunctionNotFoundException("No function found with id " + functionId);
        }
    }

    /**
     * Try to get the class with a given name and location. Classes are searched for in this order:
     * <ol>
     *     <li>On the class path, i.e. the current class loader knows the location</li>
     *     <li>On the given location</li>
     * </ol>
     * @param className The name of the class to get the Class object for.
     * @param location  The location to search the class for, e.g. path to a Jar file, or a Java source file.
     * @return          The Class object associated with the class or interface with the given className.
     * @throws ClassNotFoundException If no class could be found.
     */
    private Class<?> getClass(final String className, final String location) throws ClassNotFoundException {
        logger.debug("Trying to find a Class for {}", className);
        // check if class is already loaded by the current class loader
        try {
            return Class.forName(className);
        } catch (java.lang.ClassNotFoundException e) {
            logger.debug("Class '{}' not found by current class loader. Checking location '{}'", className, location);
            final URL locationUrl = FileFinder.findFile(location);
            if (locationUrl.toString().endsWith(".jar")) {
                logger.debug("Trying to load '{}' for JAR file '{}'", className, location);
                try (URLClassLoader cl = URLClassLoader.newInstance(new URL[]{locationUrl})) {
                    return Class.forName(className, true, cl);
                } catch (IOException ex) {
                    logger.warn("An error occurred while trying to load JAR file at '" + locationUrl + '"', e);
                } catch (java.lang.ClassNotFoundException ex) {
                    logger.warn("class '" + className + "' not found in JAR ar location '" + locationUrl + '"', e);
                }
            } else {
                logger.warn("Only JAR files are supported as location file type at the moment...");
            }

            throw new ClassNotFoundException("No class found for " + className);
        }
    }

    /**
     * Try to get a method in the given class with a certain name and matching parameters. The matching algorithm is as
     * follows:
     * <ol>
     *     <li>Get an array of all methods of the given class with a matching name.</li>
     *     <li>Keep the ones with a matching number of arguments</li>
     *     <li>
     *         For every remaining method:
     *         <ol>
     *             <li>For every argument, check if its type is compatible with the type(s) of the corresponding expected parameter.</li>
     *             <li>If false, the method is not a match; continue with the next method</li>
     *             <li>If true, check the return parameter type with the expected return parameter type. If compatible, the candidate method is
     *             found. If not, continue with the next method.</li>
     *         </ol>
     *     </li>
     * </ol>
     * @param clazz                     The class to get a method from
     * @param methodName                The name of the method to be found
     * @param expectedParameters        The expected parameter types of the method
     * @param expectedReturnParameter   The expected return parameter type of the method
     * @return A method matching the name and parameter types.
     */
    private Method getMethod(final Class<?> clazz, final String methodName, final List<Parameter> expectedParameters, final Parameter expectedReturnParameter) throws MethodNotFoundException {
        logger.debug("Trying to find method with name {}", methodName);
        Method[] declaredMethods = clazz.getDeclaredMethods();
        for (Method declaredMethod : declaredMethods) {
            boolean qualifies = false;
            if (declaredMethod.getName().equals(methodName) && declaredMethod.getParameterCount() == expectedParameters.size()) {
                // possible candidate
                qualifies = true;
                Class<?>[] methodParameterTypes = declaredMethod.getParameterTypes();
                for (int i = 0; i < methodParameterTypes.length; i++) {
                    Class<?> methodParameterType = methodParameterTypes[i];
                    if (!expectedParameters.get(i).getTypeConverter().isSubTypeOf(methodParameterType)) {
                        qualifies = false;
                        break;
                    }
                }
            }
            if (qualifies) {
                logger.debug("Found method by name and expected arguments. Checking return type...");
                Class<?> methodReturnType = declaredMethod.getReturnType();
                if (expectedReturnParameter.getTypeConverter().isSuperTypeOf(methodReturnType)) {
                    logger.debug("Found method!");
                    return declaredMethod;
                } else {
                    logger.warn("Return type '{}' of method '{}' does not match expeted return type '{}' (class '{}')",
                            methodReturnType.getName(), declaredMethod.getName(), expectedReturnParameter.getTypeConverter().getTypeClass(), clazz.getName());
                }
            }
        }
        throw new MethodNotFoundException("No suitable method '" + methodName + "' with matching parameter types found in class '" + clazz.getName() + "'.");
    }
    
}
