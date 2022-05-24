package be.ugent.idlab.knows.functions.agent.functionIntantiation;

import be.ugent.idlab.knows.functions.agent.dataType.ArrayConverter;
import be.ugent.idlab.knows.functions.agent.dataType.CollectionConverter;
import be.ugent.idlab.knows.functions.agent.dataType.DataTypeConverter;
import be.ugent.idlab.knows.functions.agent.dataType.DataTypeConverterProvider;
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
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

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

    // a 'cache' of loaded classes
    private final Map<String, Class<?>> className2ClassMap = new HashMap<>();

    private final DataTypeConverterProvider dataTypeConverterProvider;

    /**
     * Creates a new instance of an Initiator.
     * @param functions The function descriptions used to find for possible implementations in the form of a map function ID -> Function.
     * @param dataTypeConverterProvider
     */
    public Instantiator(final Map<String, Function> functions, final DataTypeConverterProvider dataTypeConverterProvider) {
        id2functionMap = functions;
        this.dataTypeConverterProvider = dataTypeConverterProvider;
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
            Method method = null;
            try {
                method = getMethod(clazz, methodName, parameters, function.getReturnParameters().get(0));
                logger.debug("Found method {}", method.getName());
                id2MethodMap.put(functionId, method);
                return method;
            } catch (java.lang.ClassNotFoundException e) {
                throw new ClassNotFoundException(e.getMessage());
            }
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
        // check if the class is in the cache
        if (className2ClassMap.containsKey(className)) {
            return className2ClassMap.get(className);
        }

        // check if class is already loaded by the current class loader
        try {
            Class<?> cls = Class.forName(className);
            className2ClassMap.put(className, cls);
            return cls;
        } catch (java.lang.ClassNotFoundException e) {
            logger.debug("Class '{}' not found by current class loader. Checking location '{}'", className, location);
            final URL locationUrl = FileFinder.findFile(location);
            logger.debug("Trying to load '{}' for JAR file '{}'", className, location);

            // get all classes from JAR file. This is necessary to be sure to have imports as well.
            try {
                loadClassesFromJAR(locationUrl);
                if (className2ClassMap.containsKey(className)) {
                    return className2ClassMap.get(className);
                } else {
                    logger.warn("No class '{}' found in JAR file '{}'", className, locationUrl);
                }
            } catch (IOException ex) {
                logger.warn("An error occurred trying to load classes of file '{}'. Note that only JAR files are supported at the moment.", locationUrl, ex);
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
    private Method getMethod(final Class<?> clazz, final String methodName, final List<Parameter> expectedParameters, final Parameter expectedReturnParameter) throws MethodNotFoundException, java.lang.ClassNotFoundException {
        logger.debug("Trying to find method with name {}", methodName);
        Method[] methods = clazz.getMethods();
        for (Method method : methods) {
            boolean qualifies = false;
            if (method.getName().equals(methodName) && method.getParameterCount() == expectedParameters.size()) {
                // possible candidate
                qualifies = true;
                logger.debug("Found method with matching name {} and matching parameter count ({})", methodName, expectedParameters.size());

                // get generic parameter types to refine converters of collections
                Type[] parameterTypes = method.getGenericParameterTypes();
                for (int i = 0; i < parameterTypes.length; i++) {
                    Type parameterType = parameterTypes[i];
                    Class<?> methodParameterClass = method.getParameterTypes()[i];
                    DataTypeConverter<?> dataTypeConverter = expectedParameters.get(i).getTypeConverter();
                    if (dataTypeConverter.isSubTypeOf(methodParameterClass)) {
                        if (dataTypeConverter.getTypeCategory().equals(DataTypeConverter.TypeCategory.COLLECTION)) {
                            if (parameterType instanceof ParameterizedType) {
                                // we found a Java collection. Now check if we need converters for type arguments of the class,
                                // e.g List<Boolean>: we potentiay din't know about the 'Boolean' argument type yet
                                ParameterizedType pType = (ParameterizedType) parameterType;
                                Type[] typeArgs = pType.getActualTypeArguments();
                                DataTypeConverter<?> argumentDataTypeConverter = dataTypeConverterProvider.getDataTypeConverter(typeArgs[0].getTypeName());
                                ((CollectionConverter<?>) dataTypeConverter).setArgumentTypeConverter(argumentDataTypeConverter);
                            } else if (methodParameterClass.isArray()) {
                                // We found a Java array. Now check if we need converters for type arguments of the class,
                                // e.g Boolean[]: we potentiay din't know about the 'Boolean' argument type yet
                                // change the ListConverter by an ArrayConverter
                                Class<?> componentType = methodParameterClass.getComponentType();
                                ArrayConverter arrayConverter = new ArrayConverter();
                                arrayConverter.setArgumentTypeConverter(dataTypeConverterProvider.getDataTypeConverter(componentType.getTypeName()));
                                expectedParameters.get(i).setTypeConverter(arrayConverter);
                            } else {
                                // check if raw collection. If so, do nothing and use the DefaultDataTypeConverter. If not, we can't use the list converter.
                                Class<?>[] interfaces = methodParameterClass.getInterfaces();
                                if (!Arrays.asList(interfaces).contains(Collection.class)) {
                                    throw new MethodNotFoundException("No suitable data type converter found for class '" + clazz.getName() + "', method '" + methodName + "', parameter '"
                                            + expectedParameters.get(i).getName() + "' which should be of type '" + parameterType.getTypeName() + "'.");
                                }
                            }
                        }

                    } else {
                        qualifies = false;
                        break;
                    }
                }
                Class<?>[] methodParameterTypes = method.getParameterTypes();
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
                Class<?> methodReturnType = method.getReturnType();
                if (expectedReturnParameter.getTypeConverter().isSuperTypeOf(methodReturnType)) {
                    logger.debug("Found method!");
                    return method;
                } else {
                    logger.warn("Return type '{}' of method '{}' does not match expeted return type '{}' (class '{}')",
                            methodReturnType.getName(), method.getName(), expectedReturnParameter.getTypeConverter().getTypeClass(), clazz.getName());
                }
            }
        }
        throw new MethodNotFoundException("No suitable method '" + methodName + "' with matching parameter types found in class '" + clazz.getName() + "'.");
    }

    private void loadClassesFromJAR(final URL jarFileUrl) throws IOException {
        // TODO add jarfile cache?

        try (
                JarFile jarFile = new JarFile(jarFileUrl.getFile());
                URLClassLoader cl = URLClassLoader.newInstance(new URL[]{jarFileUrl})) {

            jarFile
                    .stream()
                    .map(ZipEntry::getName)
                    .filter(name -> name.endsWith(".class") && !name.contains("$"))
                    .map(name -> name.substring(0, name.lastIndexOf('.')).replaceAll("/", "."))
                    .forEach(className -> {
                        logger.debug("JAR file '{}': found class name '{}'", jarFileUrl, className);
                        if (!className2ClassMap.containsKey(className)) {
                            try {
                                Class<?> cls = Class.forName(className, true, cl);
                                className2ClassMap.put(className, cls);
                            } catch (java.lang.ClassNotFoundException e) {
                                logger.warn("Class '{}' in JAR file '{}'", className, jarFileUrl, e);
                            }
                        } else {
                            logger.debug("Class '{}' already in cache.", className);
                        }
                    });
        }
    }
    
}
