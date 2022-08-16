package be.ugent.idlab.knows.functions.agent;

import be.ugent.idlab.knows.functions.agent.dataType.DataTypeConverter;
import be.ugent.idlab.knows.functions.agent.exception.MissingRDFSeqIndexException;
import be.ugent.idlab.knows.functions.agent.functionIntantiation.Instantiator;
import be.ugent.idlab.knows.functions.agent.functionModelProvider.fno.exception.FunctionNotFoundException;
import be.ugent.idlab.knows.functions.agent.model.Function;
import be.ugent.idlab.knows.functions.agent.model.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Pattern;

import static be.ugent.idlab.knows.functions.agent.functionModelProvider.fno.NAMESPACES.RDF;

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
    @Override
    public Object execute(String functionId, Arguments arguments, boolean debug) throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug("Execution debug mode: {}", debug);
            logger.debug("Executing function '{}' with arguments '{}'", functionId, arguments.toString());
        }

        // find the corresponding function
        final Function function = functionId2Function.get(functionId);

        if(Objects.isNull(function)){
            throw new FunctionNotFoundException("Function with id " + functionId + " not found");
        }

        Method method = null;
        // get the method if the function is not a composition
        if(!function.isComposite()){
            method = instantiator.getMethod(functionId);
        }


        // "fill in" the argument parameters
        final List<Object> valuesInOrder = new ArrayList<>(arguments.size());
        for (Parameter argumentParameter : function.getArgumentParameters()) {
            logger.debug("finding value for parameter {}", argumentParameter.getId());
            Collection<Object> valueCollection = arguments.get(argumentParameter.getId());
            if(argumentParameter.getId().equals(RDF+"_nnn")){
                logger.debug("found sequential parameter (_nnn), looking for values");
                // get the highest available sequence index
                Optional<Integer> optionalInteger = arguments.getArgumentNames().stream()
                                                        .filter(name -> Pattern.compile(RDF +"_\\d+").matcher(name).matches())
                                                        .map(i -> Integer.parseInt(i.substring(RDF.toString().length()+1)))
                                                        .max(Integer::compareTo);

                if(!optionalInteger.isPresent()){ // no parameters of type _nnn available
                    valuesInOrder.add(null);
                    continue;
                }
                int max = optionalInteger.get(); // get the highest used parameter
                Object[] values = new Object[max];
                // start from 0
                // if < 0, it could be a correct parameter predicate for another argument:
                // https://fno.io/spec/#fn-parameter
                for (int i = 0; i < max; i++) {
                    int finalI = i+1;
                    Object value = arguments.get(RDF+"_"+(finalI)).stream().findFirst().orElseThrow(() -> new MissingRDFSeqIndexException("no parameter found for _" + (finalI)));
                    values[i] = value;
                }
                valuesInOrder.add(values);
            }
            else if (argumentParameter.getTypeConverter().getTypeCategory() == DataTypeConverter.TypeCategory.COLLECTION) {
                logger.debug("got collection argument!");
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
