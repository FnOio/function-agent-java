package be.ugent.idlab.knows.functions.agent.functionIntantiation;

import be.ugent.idlab.knows.functions.agent.functionIntantiation.exception.ClassNotFoundException;
import be.ugent.idlab.knows.functions.agent.functionIntantiation.exception.FunctionNotFoundException;
import be.ugent.idlab.knows.functions.agent.functionIntantiation.exception.InstantiationException;
import be.ugent.idlab.knows.functions.agent.model.Function;
import be.ugent.idlab.knows.functions.agent.model.FunctionMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>Copyright 2022 IDLab (Ghent University - imec)</p>
 *
 * @author Gerald Haesendonck
 */
public class Instantiator {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Map<String, Function> id2functionMap = new HashMap<>();
    // todo keep map id -> loaded method?

    public Instantiator(Collection<Function> functions) {
        functions.forEach(function -> id2functionMap.put(function.getId(), function));
    }

    public void getWhatever(final String functionId) throws InstantiationException {
        logger.debug("Getting instantiation for {}", functionId);
        if (id2functionMap.containsKey(functionId)) {
            final Function function = id2functionMap.get(functionId);
            final FunctionMapping mapping = function.getFunctionMapping();
            final String methodName = mapping.getMethodMapping().getMethodName();
            final String className = mapping.getImplementation().getClassName();
            final String location = mapping.getImplementation().getLocation();
            Class<?> clazz = getClass(className, location);

            logger.debug("Found class {}", clazz);
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
            logger.debug("Class {} not found by current class loader.", className);
            throw new ClassNotFoundException("No class found for " + className);
        }
    }
    
}
