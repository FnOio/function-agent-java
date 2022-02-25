package be.ugent.idlab.knows.functions.agent;

import be.ugent.idlab.knows.functions.agent.dataType.DataTypeConverterException;
import be.ugent.idlab.knows.functions.agent.functionIntantiation.Instantiator;
import be.ugent.idlab.knows.functions.agent.functionIntantiation.exception.InstantiationException;
import be.ugent.idlab.knows.functions.agent.model.Function;
import be.ugent.idlab.knows.functions.agent.model.Parameter;

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
    private final Map<String, Function> functionId2Function;
    private final Instantiator instantiator;

    public AgentImpl(final Map<String, Function> functionId2Function, final Instantiator instantiator) {
        this.functionId2Function = functionId2Function;
        this.instantiator = instantiator;
    }

    @Override
    public Object execute(final String functionId, final Map<String, Object> parameterId2Value) throws InstantiationException, InvocationTargetException, IllegalAccessException, DataTypeConverterException {

        // find a method with the given name
        final Method method = instantiator.getMethod(functionId);

        // "fill in" the argument parameters
        final List<Object> valuesInOrder = new ArrayList<>(parameterId2Value.size());
        final Function function = functionId2Function.get(functionId);
        for (Parameter parameter : function.getArgumentParameters()) {
            Object untypedValue = parameterId2Value.get(parameter.getId());
            Object convertedValue = parameter.getTypeConverter().convert(untypedValue);
            valuesInOrder.add(convertedValue);

        }
        // TODO: check if every parameter is used? OR pass null value?
        // for now every expected parameter is expected to have a value.

        // now execute the method
        return method.invoke(null, valuesInOrder.toArray());
    }
}
