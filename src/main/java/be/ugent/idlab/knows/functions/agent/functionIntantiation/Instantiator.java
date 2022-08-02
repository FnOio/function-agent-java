package be.ugent.idlab.knows.functions.agent.functionIntantiation;

import be.ugent.idlab.knows.functions.agent.Agent;
import be.ugent.idlab.knows.functions.agent.Arguments;
import be.ugent.idlab.knows.functions.agent.dataType.ArrayConverter;
import be.ugent.idlab.knows.functions.agent.dataType.CollectionConverter;
import be.ugent.idlab.knows.functions.agent.dataType.DataTypeConverter;
import be.ugent.idlab.knows.functions.agent.dataType.DataTypeConverterProvider;
import be.ugent.idlab.knows.functions.agent.functionIntantiation.exception.ClassNotFoundException;
import be.ugent.idlab.knows.functions.agent.functionIntantiation.exception.InstantiationException;
import be.ugent.idlab.knows.functions.agent.functionIntantiation.exception.*;
import be.ugent.idlab.knows.functions.agent.model.*;
import be.ugent.idlab.knows.misc.FileFinder;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.util.*;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
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

    // a 'cache' for constructed composition methods.
    private final Map<String, ThrowableFunction> id2CompositionLambdaMap = new HashMap<>();

    // a 'cache' of loaded classes
    private final Map<String, Class<?>> className2ClassMap = new HashMap<>();

    private final DataTypeConverterProvider dataTypeConverterProvider;

    /**
     * Creates a new instance of an Initiator.
     * @param functions The function descriptions used to find for possible implementations in the form of a map function ID -> Function.
     * @param dataTypeConverterProvider provides the converters for the arguments
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
     * Generates a lambda that executes the composite method for the given function
     * @param functionId the name of the function for which the function composition must be generated
     * @return a lambda that is the composite function
     * @throws InstantiationException if the given functionId is not a function composition, this function will throw an error
     */
    public ThrowableFunction getCompositeMethod(final String functionId, boolean debug) throws InstantiationException {
        logger.debug("constructing composite method for {}", functionId);
        if(id2CompositionLambdaMap.containsKey(functionId)){
            logger.debug("found composition for {} in cache!", functionId);
            return id2CompositionLambdaMap.get(functionId);
        }

        final Function function = id2functionMap.get(functionId);

        if(!function.isComposite()){
            throw new NotACompositeFunctionException("the provided functionId is not a function composition");
        }

        // get the composition
        final FunctionComposition functionComposition = function.getFunctionComposition();

        // maps a specific parameter of a function on a value (outputs included)
        Map<String, Map<String, Object>> values = new HashMap<>();
        // functions depend on certain parameters of other functions
        MultiValuedMap<String, String> dependencies = new ArrayListValuedHashMap<>();
        // K receives values from V, can be multiple values. Arguments class takes care of this.
        MultiValuedMap<FunctionFieldPair, FunctionFieldPair> parametermap = new ArrayListValuedHashMap<>();

        // function dependencies and parameter maps
        for(CompositionMappingElement el : functionComposition.getMappings()){
            CompositionMappingPoint from = el.getFrom();
            CompositionMappingPoint to = el.getTo();
            if(from.isLiteral()){
                Map<String, Object> functionValues = values.getOrDefault(to.getFunctionId(), new HashMap<>());
                functionValues.put(to.getParameterId(), from.getParameterId());
                values.put(to.getFunctionId(), functionValues);
                continue;
            }

            // check for the use of actual parameters
            checkFunction(from);
            checkFunction(to);

            parametermap.put(
                    new FunctionFieldPair(to.getFunctionId(), to.getParameterId()),
                    new FunctionFieldPair(from.getFunctionId(), from.getParameterId())
            );

            // add function dependencies
            // if the source value is an output, the target function is dependent on the source function
            if(from.isOutput()){
                dependencies.put(to.getFunctionId(), from.getFunctionId());
            }
        }

        // checks for cycles in dependencies
        checkDependencyCycles(dependencies, functionId);


        // order of function execution is determined with the debug parameter
        Deque<String> execStack = new ArrayDeque<>();

        // effective implementation: stack of necessary function calls to get output.
        List<String> toadd = new ArrayList<>();
        List<String> willBeAdded = new ArrayList<>();
        toadd.add(functionId);
        while(!toadd.isEmpty()) {
            toadd.forEach(execStack::push);
            for (String fId : toadd) {
                Collection<String> dep = dependencies.get(fId);
                dep.stream().filter((String funcName) -> !execStack.contains(funcName))
                        .forEach(willBeAdded::add);
            }
            toadd.clear();
            toadd.addAll(willBeAdded);
            willBeAdded.clear();
        }

        if(debug){
            // less effective implementation: all nodes of the composition graph will be executed.
            // they will be added after the necessary functions for the output and before the actual function.
            execStack.removeLast();
            Set<String> sideSet = dependencies.keySet().stream().filter(name -> !(execStack.contains(name))).collect(Collectors.toSet());
            while(!sideSet.isEmpty()) {
                List<String> toRemove = new ArrayList<>();
                for(String s : sideSet){
                    if(execStack.containsAll(dependencies.get(s))){
                        execStack.addLast(s);
                        toRemove.add(s);
                    }
                }
                toRemove.forEach(sideSet::remove);
                toRemove.clear();
            }
            execStack.addLast(functionId);
        }


        // construct the lambda that represents the function
        // don't call the getMethod function from the instantiator to evaluate other functions, since the used functions can also be function compositions
        ThrowableFunction returnFunction = (Agent agent, Object[] args) -> {
            // make arguments available
            List<Parameter> fArgs = function.getArgumentParameters();
            for(int i = 0; i < fArgs.size(); i++){
                Parameter p = fArgs.get(i);
                Object value = args[i];
                Map<String, Object> vals = values.getOrDefault(functionId, new HashMap<>());
                vals.put(p.getId(), value);
                values.put(functionId, vals);
            }

            /*
                for each function, we will get for each parameter all the values that are mapped to that parameter
                and we will execute the function when we have all the arguments
             */
            while (execStack.size() > 1) {
                String f = execStack.pop();
                Arguments arguments = new Arguments();
                Function func = id2functionMap.get(f);
                for(Parameter p : func.getArgumentParameters()){
                    FunctionFieldPair ffp = new FunctionFieldPair(f, p.getId());
                    Collection<FunctionFieldPair> ffpc = new ArrayList<>();
                    Collection<FunctionFieldPair> toAdd = new ArrayList<>(parametermap.get(ffp));
                    Collection<FunctionFieldPair> willBeAdded2 = new ArrayList<>();
                    // check for multiple references: a -> b -> c so a should take a value from c and d
                    //                                       -> d
                    while(!toAdd.isEmpty()){
                        ffpc.addAll(toAdd);
                        for(FunctionFieldPair functionFieldPair : toAdd){
                            willBeAdded2.addAll(parametermap.get(functionFieldPair));
                        }
                        toAdd.clear();
                        toAdd.addAll(willBeAdded2);
                        willBeAdded2.clear();
                    }
                    if(ffpc.isEmpty()){
                        arguments = arguments.add(p.getId(), values.get(f).get(p.getId()));
                    }
                    for (FunctionFieldPair functionFieldPair: ffpc) {
                        arguments = arguments.add(p.getId(), values.get(functionFieldPair.getFunction()).get(functionFieldPair.getField()));
                    }
                }
                Object result = agent.execute(f, arguments);
                Map<String, Object> functionValues = values.getOrDefault(f, new HashMap<>());
                functionValues.put(func.getReturnParameters().get(0).getId(), result);
                values.put(f, functionValues);
            }
            // for java, we get the first returnparameter to use to get the output of the function
            Collection<FunctionFieldPair> returnFfp = parametermap.get(new FunctionFieldPair(functionId, function.getReturnParameters().get(0).getId()));
            List<Object> returnList = new ArrayList<>();
            returnFfp.forEach((FunctionFieldPair functionFieldPair) -> returnList.add(values.get(functionFieldPair.getFunction()).get(functionFieldPair.getField())));
            return returnList.get(0);
        };
        // cache the constructed function
        id2CompositionLambdaMap.put(functionId, returnFunction);
        return returnFunction;
    }

    private void checkFunction(CompositionMappingPoint compositionMappingPoint) throws InstantiationException{
        Function fromFunction = id2functionMap.get(compositionMappingPoint.getFunctionId());
        if(Objects.isNull(fromFunction)){
            throw new CompositionReferenceException("the used function "+compositionMappingPoint.getFunctionId() + " could not be found");
        }
        List<Parameter> fromInputParameters = fromFunction.getArgumentParameters();
        List<Parameter> fromReturnParameters = fromFunction.getReturnParameters();
        if(
                fromInputParameters.stream().map(Parameter::getId).noneMatch(id -> Objects.equals(id, compositionMappingPoint.getParameterId()))
                        &&
                        fromReturnParameters.stream().map(Parameter::getId).noneMatch(id -> Objects.equals(id, compositionMappingPoint.getParameterId()))
        ){
            throw new CompositionReferenceException("the used parameter "+compositionMappingPoint.getParameterId() + " of function " + compositionMappingPoint.getFunctionId() + " could not be found");

        }
    }


    /**
     * Checks a MultivaluedMap of function dependencies for cycles, starting with a start node
     * @param dependencies the map of dependencies
     * @param start the node to start at
     * @throws InstantiationException Throws an exception if a cycle is detected
     */
    private void checkDependencyCycles(MultiValuedMap<String, String> dependencies, String start) throws InstantiationException{
        logger.debug("checking for cyclic dependencies...");
        Stack<String> path = new Stack<>();
        boolean hasCycle = cycleRecursive(dependencies, start, new HashSet<>(), path);
        if(hasCycle){
            throw new CyclicDependencyException("Cycle detected in function dependencies. Path of cycle: " + path);
        }
    }

    private boolean cycleRecursive(MultiValuedMap<String, String> dependencies, String current, Set<String> visited, Stack<String> path) {
        path.add(current);
        visited.add(current);
        Collection<String> next = dependencies.get(current);
        Optional<String> function = next.stream().filter(path::contains).findFirst();
        boolean stop = function.isPresent();
        if(stop){
            path.push(function.get());
            return true;
        }
        stop = next.stream().filter(function1 -> !visited.contains(function1)).anyMatch(function1 -> cycleRecursive(dependencies, function1, visited, path));
        if(!stop){ // keep path intact to show in exception message
            path.pop();
        }
        return stop;
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
                // url decoder for special characters in path.
                JarFile jarFile = new JarFile(URLDecoder.decode(jarFileUrl.getPath(), "utf-8"));
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
