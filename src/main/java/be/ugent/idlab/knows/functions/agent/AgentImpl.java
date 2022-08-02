package be.ugent.idlab.knows.functions.agent;

import be.ugent.idlab.knows.functions.agent.dataType.DataTypeConverter;
import be.ugent.idlab.knows.functions.agent.functionIntantiation.Instantiator;
import be.ugent.idlab.knows.functions.agent.model.Function;
import be.ugent.idlab.knows.functions.agent.model.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * <p>Copyright 2021 IDLab (Ghent University - imec)</p>
 *
 * @author Gerald Haesendonck
 */
public class AgentImpl implements Agent {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final Map<String, Function> functionId2Function;
    private final Instantiator instantiator;

    public AgentImpl(final Map<String, Function> functionId2Function, final Instantiator instantiator) {
        this.functionId2Function = functionId2Function;
        this.instantiator = instantiator;
    }

    @Override
    public Object execute(String functionId, Arguments arguments) throws Exception {
        return execute(functionId, arguments, false);
    }

    /**
     * executes the function with the given ID and arguments. Enabling debug mode will execute all elements of a
     * Function Composition and not only the required ones for output.
     * @param functionId the function ID
     * @param arguments the arguments of the function
     * @param debug debug mode to enable all execution
     * @return the result of the function
     * @throws Exception Something went wrong. A subclass will specify what with a message.
     */
    public Object execute(String functionId, Arguments arguments, boolean debug) throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug("Execution debug mode: {}", debug);
            logger.debug("Executing function '{}' with arguments '{}'", functionId, arguments.toString());
        }

        // find the corresponding function
        final Function function = functionId2Function.get(functionId);

        Method method = null;
        // get the method if the function is not a composition
        if(!function.isComposite()){
            method = instantiator.getMethod(functionId);
        }


        // "fill in" the argument parameters
        final List<Object> valuesInOrder = new ArrayList<>(arguments.size());
        for (Parameter argumentParameter : function.getArgumentParameters()) {
            Collection<Object> valueCollection = arguments.get(argumentParameter.getId());
            if (argumentParameter.getTypeConverter().getTypeCategory().equals(DataTypeConverter.TypeCategory.COLLECTION)) {
                Object convertedValue = argumentParameter.getTypeConverter().convert(valueCollection);
                valuesInOrder.add(convertedValue);
            } else {
                if (valueCollection.isEmpty()) {
                    logger.debug("No value found for parameter '{}' in function {}. Considering it to be 'null'.", argumentParameter.getId(), functionId);
                    valuesInOrder.add(null);
                } else {
                    Object convertedValue = argumentParameter.getTypeConverter().convert(valueCollection.stream().findFirst()
                            .orElseThrow(() -> new IllegalArgumentException("Value expected for parameter '" + argumentParameter.getId() + "' in function '" + functionId + "'.")));
                    valuesInOrder.add(convertedValue);
                }
            }
        }

        // now execute the method
        if(!function.isComposite()){
            return method.invoke(null, valuesInOrder.toArray());
        }
        // if the function is a composition, there is no specific method associated with.
        return instantiator.getCompositeMethod(functionId, debug).apply(this, valuesInOrder.toArray());
    }
}
