package be.ugent.idlab.knows.functions.agent;

import be.ugent.idlab.knows.functions.agent.dataType.DataTypeConverter;
import be.ugent.idlab.knows.functions.agent.dataType.DataTypeConverterException;
import be.ugent.idlab.knows.functions.agent.dataType.DataTypeConverterProvider;
import be.ugent.idlab.knows.functions.agent.exception.MissingRDFSeqIndexException;
import be.ugent.idlab.knows.functions.agent.functionIntantiation.Instantiator;
import be.ugent.idlab.knows.functions.agent.functionIntantiation.exception.MethodNotFoundException;
import be.ugent.idlab.knows.functions.agent.functionModelProvider.fno.exception.FunctionNotFoundException;
import be.ugent.idlab.knows.functions.agent.model.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static be.ugent.idlab.knows.functions.agent.functionModelProvider.fno.NAMESPACES.*;

/**
 * <p>
 * Copyright 2021 IDLab (Ghent University - imec)
 * </p>
 *
 * @author Gerald Haesendonck
 */
public class AgentImpl implements Agent {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final Map<String, Function> functionId2Function;
    private final Instantiator instantiator;

    // pattern for finding rdf:_nnn parameters
    final private Pattern seq_pattern = Pattern.compile(RDF +"_\\d+");

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
     * 
     * @param functionId the function ID
     * @param arguments  the arguments of the function
     * @param debug      debug mode to enable all execution
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

        if (function == null) {
            throw new FunctionNotFoundException("Function with id " + functionId + " not found");
        }

        if (function.isComposite()) {
            List<Object> values = getParameterValues(functionId, arguments, function);
            return instantiator.getCompositeMethod(functionId, debug).apply(this, values.toArray());
        } else {
            Method method = instantiator.getMethod(functionId);
            if (method != null) {
                List<Object> values = getParameterValues(functionId, arguments, function);
                return method.invoke(null, values.toArray());
            } else {
                throw new MethodNotFoundException("No method found for function " + function.getId() + " (" + function.getName() + ')');
            }
        }
    }

    private List<Object> getParameterValues(String functionId, Arguments arguments, Function function) throws MissingRDFSeqIndexException, DataTypeConverterException {
        final List<Object> valuesInOrder = new ArrayList<>(arguments.size());
        for (Parameter argumentParameter : function.getArgumentParameters()) {
            logger.debug("finding value for parameter {}", argumentParameter.getId());
            Collection<Object> valueCollection = arguments.get(argumentParameter.getId());
            if (argumentParameter.getTypeConverter().getTypeCategory() == DataTypeConverter.TypeCategory.COLLECTION) {
                logger.debug("got collection argument!");
                if((RDF+"_nnn").equals(argumentParameter.getId())){
                    logger.debug("found sequential parameter (_nnn), looking for values");
                    // get the highest available sequence index
                    Optional<Integer> optionalInteger = arguments.getArgumentNames().stream()
                            .filter(name -> seq_pattern.matcher(name).matches())
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
                    valuesInOrder.add(argumentParameter.getTypeConverter().convert(values));
                }
                else{
                    Object convertedValue = argumentParameter.getTypeConverter().convert(valueCollection);
                    valuesInOrder.add(convertedValue);
                }
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
        return valuesInOrder;
    }

    void executeToFile(String functionId, Arguments arguments, String fileName) throws Exception {
        this.executeToFile(functionId, arguments, fileName, false);
    }

    void executeToFile(String functionId, Arguments arguments, String fileName, boolean debug) throws Exception {
        constructResult(functionId, arguments, fileName, debug);
    }

    private void constructResult(String functionId, Arguments arguments, String filename, boolean debug) throws Exception {
        Model model = ModelFactory.createDefaultModel();
        Function function = functionId2Function.get(functionId);
        Object result = this.execute(functionId, arguments, debug);
        Resource executionResource = model.createResource(FNO + "execution").addProperty(ResourceFactory.createProperty(RDF.toString(), "type"), model.createResource(FNO + "Execution"));
        executionResource.addProperty(ResourceFactory.createProperty(FNO.toString(), "executes"), model.createResource(functionId));
        for (Parameter argumentParameter : function.getArgumentParameters()) {
            // TODO this converts everything to string. Probably not what you want
            executionResource.addLiteral(ResourceFactory.createProperty(argumentParameter.getId()), arguments.get(argumentParameter.getId()).toString());
        }
        if (function.getReturnParameters().isEmpty()) {
            executionResource.addLiteral(ResourceFactory.createProperty(DCTERMS.toString(), "description"), "Function has no output");
        } else {
            executionResource.addLiteral(ResourceFactory.createProperty(function.getReturnParameters().get(0).getId()), result.toString());
        }
        printModel(model, filename);
    }

    /**
     * Prints out an RDF model If filename is null, print with the logger.
     * 
     * @param model    the RDF model that needs to be printed
     * @param filename the filename to print it to. If null, default to logger.
     * @throws IOException something goes wrong printing the model
     */
    private void printModel(Model model, String filename) throws IOException {
        OutputStream stream = System.out;
        if (filename != null) {
            stream = Files.newOutputStream(Paths.get(filename));
        }
        RDFDataMgr.write(stream, model, Lang.TURTLE);
        if (filename != null) {
            stream.close();
        }
    }

    String loadFunction(Method javaFunction) {
        DataTypeConverterProvider dataTypeConverterProvider = new DataTypeConverterProvider();
        if (!Modifier.isStatic(javaFunction.getModifiers())) {
            throw new UnsupportedOperationException("Java function needs to be static");
        }
        String name = javaFunction.getName();
        Class<?> clazz = javaFunction.getDeclaringClass();
        String functionId = FNO + "javaFunction." + clazz.getName() + "." + name;

        if (functionId2Function.containsKey(functionId)) { // already a function present, overwrite it
            logger.warn("loadFunction: already found a function with id {}. Overwriting...", functionId);
        }

        List<Parameter> parameters = loadParameters(javaFunction, dataTypeConverterProvider);
        List<Parameter> returnValues = loadReturnValue(javaFunction, dataTypeConverterProvider);
        Function function = new Function(functionId, name, "", parameters, returnValues);

        FunctionMapping functionMapping = loadFunctionMapping(javaFunction, functionId);

        function.setFunctionMapping(functionMapping);

        // add to function list:
        this.functionId2Function.put(functionId, function);
        logger.debug("added Java function to Agent with id: {}", functionId);
        return functionId;
    }

    private List<Parameter> loadParameters(Method javaFunction, DataTypeConverterProvider dataTypeConverterProvider) {
        List<Parameter> parameterList = new ArrayList<>();
        java.lang.reflect.Parameter[] functionParameters = javaFunction.getParameters();
        for (java.lang.reflect.Parameter parameter : functionParameters) {
            String parameterId = FNO + javaFunction.getClass().getName() + javaFunction.getName() + parameter.getName();
            Parameter p = new Parameter(parameter.getName(), parameterId, dataTypeConverterProvider.getDataTypeConverter(parameter.getType().getName()), true);
            parameterList.add(p);
        }
        return parameterList;
    }

    private List<Parameter> loadReturnValue(Method javaFunction, DataTypeConverterProvider dataTypeConverterProvider) {
        Class<?> returnType = javaFunction.getReturnType();
        Parameter returnParameter = new Parameter(returnType.getName() + "Output", "", dataTypeConverterProvider.getDataTypeConverter(returnType.getName()), true);
        List<Parameter> returnValue = new ArrayList<>();
        returnValue.add(returnParameter);
        return returnValue;
    }

    private FunctionMapping loadFunctionMapping(Method javaFunction, String functionId) {
        MethodMapping methodMapping = new MethodMapping("fnom:StringMethodMapping", javaFunction.getName());
        Implementation implementation = new Implementation(javaFunction.getDeclaringClass().getName(), "");
        return new FunctionMapping(functionId, methodMapping, implementation);
    }

    List<String> getParameterPredicates(String functionId) {
        return this.functionId2Function.get(functionId).getArgumentParameters().stream().map(Parameter::getId).collect(Collectors.toList());
    }

    void writeModel(String filename) throws IOException {
        Model model = ModelFactory.createDefaultModel();
        for (Function function : this.functionId2Function.values()) {
            DescriptionGenerator.addFunctionToModel(model, function);
        }
        try (OutputStream outputStream = Files.newOutputStream(Paths.get(filename))) {
            RDFDataMgr.write(outputStream, model, Lang.TURTLE);
        }
    }
}
