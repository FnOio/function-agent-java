package be.ugent.idlab.knows.functions.agent;

import be.ugent.idlab.knows.functions.agent.dataType.DataTypeConverter;
import be.ugent.idlab.knows.functions.agent.functionIntantiation.Instantiator;
import be.ugent.idlab.knows.functions.agent.functionModelProvider.fno.exception.FunctionNotFoundException;
import be.ugent.idlab.knows.functions.agent.model.Function;
import be.ugent.idlab.knows.functions.agent.model.Parameter;
import org.apache.jena.rdf.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.*;

import static be.ugent.idlab.knows.functions.agent.functionModelProvider.fno.NAMESPACES.*;

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
        Function f = functionId2Function.get(functionId);
        executeToFile(functionId, arguments, "tmp.txt");
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
            Collection<Object> valueCollection = arguments.get(argumentParameter.getId());
            if (argumentParameter.getTypeConverter().getTypeCategory() == DataTypeConverter.TypeCategory.COLLECTION) {
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

    @Override
    public void executeToFile(String functionId, Arguments arguments, String fileName) throws Exception {
        this.executeToFile(functionId, arguments, fileName, false);
    }

    @Override
    public void executeToFile(String functionId, Arguments arguments, String fileName, boolean debug) throws Exception {
        constructResult(functionId, arguments, fileName, debug);
    }

    private void constructResult(String functionId, Arguments arguments, String filename, boolean debug) throws Exception{
        Model model = ModelFactory.createDefaultModel();
        Function function = functionId2Function.get(functionId);
        Object result = this.execute(functionId, arguments, debug);
        Resource executionResource = model.createResource(FNO+"execution")
                                        .addProperty(ResourceFactory.createProperty(RDF.toString(), "type"), FNO+"Execution");
        if(function.getReturnParameters().isEmpty()){
            executionResource.addLiteral(ResourceFactory.createProperty(DCTERMS.toString(), "description"), "Function has no output");
        }
        else{
            executionResource.addLiteral(ResourceFactory.createProperty(function.getReturnParameters().get(0).getId()), result.toString());
        }
        printModel(model, filename);
    }

    /**
     * Prints out an RDF model
     * If filename is null, print with the logger.
     * @param model the RDF model that needs to be printed
     * @param filename the filename to print it to. If null, default to logger.
     * @throws IOException something goes wrong printing the model
     */
    private void printModel(Model model, String filename) throws IOException {
        if(filename == null){
            StmtIterator it = model.listStatements();
            while(it.hasNext()){
                Statement s = it.nextStatement();
                logger.info("{} {} {}", s.getSubject(), s.getPredicate(), s.getObject());
            }
        }
        else{
            try(PrintWriter pw = new PrintWriter(filename)){
                ResIterator it = model.listSubjects();
                while(it.hasNext()){
                    Resource subject = it.nextResource();
                    pw.println(subject);
                    StmtIterator stmtIterator = subject.listProperties();
                    while(stmtIterator.hasNext()){
                        Statement statement = stmtIterator.nextStatement();
                        pw.println("\t"+statement.getPredicate() + " " + statement.getObject());
                    }
                }
            }
        }
    }
}
