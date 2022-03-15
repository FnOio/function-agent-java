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
        logger.debug("Executing function '{}'", functionId);

        // find a method with the given name
        final Method method = instantiator.getMethod(functionId);

        // find the corresponding function
        final Function function = functionId2Function.get(functionId);

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

        // TODO: check if every parameter is used? OR pass null value?

        // now execute the method
        return method.invoke(null, valuesInOrder.toArray());
    }
}
