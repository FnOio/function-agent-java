package be.ugent.idlab.knows.functions.agent;

import be.ugent.idlab.knows.functions.agent.functionIntantiation.Instantiator;
import be.ugent.idlab.knows.functions.agent.functionIntantiation.exception.InstantiationException;
import be.ugent.idlab.knows.functions.agent.functionModelProvider.FunctionModelProvider;
import be.ugent.idlab.knows.functions.agent.model.Function;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * <p>Copyright 2021 IDLab (Ghent University - imec)</p>
 *
 * @author Gerald Haesendonck
 */
public class AgentImpl implements Agent {
    private final FunctionModelProvider functionModelProvider;
    private final Instantiator instantiator;

    public AgentImpl(final FunctionModelProvider functionModelProvider, final Instantiator instantiator) {
        this.functionModelProvider = functionModelProvider;
        this.instantiator = instantiator;
    }

    @Override
    public Object execute(final String functionId, final Map<String, Object> parameterId2Value) throws InstantiationException, InvocationTargetException, IllegalAccessException {

        // find a method with the given name
        final Method method = instantiator.getMethod(functionId);

        // "fill in" the argument parameters
        final List<Object> valuesInOrder = new ArrayList<>(parameterId2Value.size());
        final Function function = functionModelProvider.getFunctions().get(functionId);
        function.getArgumentParameters().forEach(parameter -> {
            Object untypedValue = parameterId2Value.get(parameter.getId());
            Object convertedValue = parameter.getTypeConverter().convert(untypedValue);
            valuesInOrder.add(convertedValue);
                    
        });
        // TODO: check if every parameter is used? OR pass null value?
        // for now every expected parameter is expected to have a value.

        // now execute the method
        return method.invoke(null, valuesInOrder.toArray());
    }
}