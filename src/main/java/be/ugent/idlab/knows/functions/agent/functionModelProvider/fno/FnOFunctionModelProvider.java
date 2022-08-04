package be.ugent.idlab.knows.functions.agent.functionModelProvider.fno;

import be.ugent.idlab.knows.functions.agent.dataType.DataTypeConverter;
import be.ugent.idlab.knows.functions.agent.dataType.DataTypeConverterProvider;
import be.ugent.idlab.knows.functions.agent.functionModelProvider.FunctionModelProvider;
import be.ugent.idlab.knows.functions.agent.functionModelProvider.fno.exception.*;
import be.ugent.idlab.knows.functions.agent.model.*;
import be.ugent.idlab.knows.misc.FileFinder;
import org.apache.jena.rdf.model.*;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringReader;
import java.net.URL;
import java.util.*;

import static be.ugent.idlab.knows.functions.agent.functionModelProvider.fno.NAMESPACES.*;

/**
 * A {@link FunctionModelProvider} for functions described using the Function Ontology (<a href="https://fno.io/">FnO</a>).
 * One instance holds functions found in one FnO document.
 *
 * <p>Copyright 2021 IDLab (Ghent University - imec)</p>
 *
 * @author Gerald Haesendonck
 */
public class FnOFunctionModelProvider implements FunctionModelProvider {
    private final static Logger logger = LoggerFactory.getLogger(FnOFunctionModelProvider.class);
    private final Model functionDescriptionTriples = ModelFactory.createDefaultModel();
    private final Map<String, Function> functionId2Functions = new HashMap<>();
    private final Map<String, FunctionMapping> functionId2functionMappings = new HashMap<>();

    private final Map<String, FunctionComposition> functionId2functionCompositions = new HashMap<>();
    private final DataTypeConverterProvider dataTypeConverterProvider;
    private final Map<String, String> location2otherLocationMap;

    private final Map<String, String> parameterURItoPredicate = new HashMap<>();
    // some properties used throughout the parsing process
    private final Property typeProperty = ResourceFactory.createProperty(RDF.toString(), "type");

    /**
     * Creates an instance of {@link FnOFunctionModelProvider}, holding all functions found in the given FnO documents.
     * If parsing fails, no functions will be returned by {@link FunctionModelProvider#getFunctions()}.
     *
     * @param dataTypeConverterProvider     A provider for converting data types used in the FnO descriptions to Java data types.
     * @param fnoDocPaths    One or more documents in RDF describing functions using the Function Ontology (FnO).
     *                       One fnoDocPath can be a path to a
     *                       file, a URL to a file or a String containing FnO triples in Turtle format.
     */
    public FnOFunctionModelProvider(final DataTypeConverterProvider dataTypeConverterProvider, final String... fnoDocPaths) throws FnOException {
        this(dataTypeConverterProvider, Collections.emptyMap(), fnoDocPaths);
    }

    /**
     * Creates an instance of {@link FnOFunctionModelProvider}, holding all functions found in the given FnO documents.
     * If parsing fails, no functions will be returned by {@link FunctionModelProvider#getFunctions()}.
     *
     * @param dataTypeConverterProvider     A provider for converting data types used in the FnO descriptions to Java data types.
     * @param implementationLocationMapping   Overrides implmentation locations such as jar files as defined in the FnO doc with other locations.
     * @param fnoDocPaths    One or more documents in RDF describing functions using the Function Ontology (FnO).
     *                       One fnoDocPath can be a path to a
     *                       file, a URL to a file or a String containing FnO triples in Turtle format.
     */
    public FnOFunctionModelProvider(final DataTypeConverterProvider dataTypeConverterProvider, final Map<String, String> implementationLocationMapping, final String... fnoDocPaths) throws FnOException {
        this.dataTypeConverterProvider = dataTypeConverterProvider;
        this.location2otherLocationMap = implementationLocationMapping;
        parse(fnoDocPaths);
    }

    /**
     * Returns all parsed functions, or an empty collection if parsing fails.
     * @return  All parsed functions.
     */
    public Map<String, Function> getFunctions() {
        return functionId2Functions;
    }

    /**
     * Parse all functions found in FnO documents. If something goes wrong, no functions are kept.
     * @param fnoDocPaths    The Function Ontology documents containing function descriptions. One fnoDocPath can be a path to a
     *                       file, a URL to a file or a String containing FnO triples in Turtle format.
     */
    private void parse(final String... fnoDocPaths) throws FnOException {

        logger.info("Loading function descriptions from {}", Arrays.toString(fnoDocPaths));
        try {
            readRDFModel(fnoDocPaths);
            parseFunctionMappings();
            parseParameterPredicateMappings();
            parseFunctionCompositions();
            parseFunctions();
            mapFunctionMappingsAndCompositionsToFunctions();
            parseApplies();
        } finally {
            // free up some resources
            functionDescriptionTriples.close();
            functionId2functionMappings.clear();
            functionId2functionCompositions.clear();
            parameterURItoPredicate.clear();
        }
    }

    private void readRDFModel(final String... fnoDocPaths) throws FnODocNotFoundException {
        for (String fnoDocPath : fnoDocPaths) {
            logger.debug("Reading RDF model from {}", fnoDocPath);
            URL fnoDocURL = FileFinder.findFile(fnoDocPath);
            if (fnoDocURL != null) {
                logger.debug("'{}' resolved to '{}'", fnoDocPath, fnoDocURL);
                RDFDataMgr.read(functionDescriptionTriples, fnoDocPath, Lang.TURTLE);
            } else {
                logger.warn("Could not find document; trying to interpret it as direct Turlte input.");
                try {
                    // TODO: at the moment only Turtle is supported
                    RDFDataMgr.read(functionDescriptionTriples, new StringReader(fnoDocPath), "", Lang.TURTLE);
                } catch (Throwable t) {
                    throw new FnODocNotFoundException("Could not process FnO document: " + t.getMessage());
                }
            }
        }
    }

    /**
     * Put parsed function mapping objects into the corresponding function object.
     * @throws FunctionMappingNotFoundException No function mapping is found for a certain function.
     */
    private void mapFunctionMappingsAndCompositionsToFunctions() throws FunctionMappingNotFoundException {
        for (String functionId : functionId2Functions.keySet()) {
            logger.debug("Finding mapping for function {}", functionId);
            if (functionId2functionMappings.containsKey(functionId)) {
                final Function function = functionId2Functions.get(functionId);
                function.setFunctionMapping(functionId2functionMappings.get(functionId));
            } else if (functionId2functionCompositions.containsKey(functionId)) {
                logger.debug("Composition found for " + functionId);
                final Function function = functionId2Functions.get(functionId);
                function.setComposite(true);
                function.setFunctionComposition(functionId2functionCompositions.get(functionId));
            } else {
                throw new FunctionMappingNotFoundException("No '" + FNO + "Mapping' or '"+FNOC+"Composition' found for function '" + functionId + '"');
            }
        }
    }

    /**
     * Searches the FnO document for fno:Mapping resources and converts them to FunctionMapping objects
     * in the internal Function Model
     * @throws FnOException Something goes wrong parsing the method mapping.
     *                      A subclass of FnOException specifies what exactly.
     */
    private void parseFunctionMappings() throws FnOException {
        logger.debug("Parsing function mappings");
        Resource functionMappingObject = ResourceFactory.createResource(FNO + "Mapping");
        ResIterator mappings = functionDescriptionTriples.listSubjectsWithProperty(typeProperty, functionMappingObject);
        while (mappings.hasNext()) {
            parseFunctionMapping(mappings.nextResource());
        }
    }

    /**
     * searches the FnO document for fno:predicate and puts them into a map to use later for mappings of function
     * composition.
     */
    private void parseParameterPredicateMappings() {
        logger.debug("Parsing parameter URI to predicate");
        Property parameterPredicateObject = ResourceFactory.createProperty(FNO+"predicate");
        ResIterator parameters = functionDescriptionTriples.listSubjectsWithProperty(parameterPredicateObject);
        while(parameters.hasNext()){
            parseParameterPredicateMapping(parameters.nextResource());
        }
    }

    /**
     * add a parameter predicate to the map.
     * @param parameter the resource that represents a parameter.
     */
    private void parseParameterPredicateMapping(Resource parameter) {
        String predicateURI = getObjectURI(parameter, FNO+"predicate").orElseThrow(() ->
                new IllegalStateException("not possible to get an object without URI")); // can't happen
        parameterURItoPredicate.put(parameter.getURI(), predicateURI);
    }

    /**
     * Searches the FnO document for fnoc:Composition resources and converts them to FunctionComposition objects
     * in the internal Function Model
     * @throws FnOException Something goes wrong parsing the function composition.
     *                      A subclass of FnOException specifies what exactly.
     */
    private void parseFunctionCompositions() throws FnOException{
        logger.debug("Parsing function compositions");
        Resource functionCompositionObject = ResourceFactory.createResource(FNOC+"Composition");
        ResIterator mappings = functionDescriptionTriples.listSubjectsWithProperty(typeProperty, functionCompositionObject);
        while (mappings.hasNext()){
            parseFunctionComposition(mappings.nextResource());
        }
    }

    /**
     * Converts a function mapping (fno:Mapping) to a FunctionMapping object in the internal Function model and kept in
     * an internal cache.
     * @param functionMappingResource   The function mapping resource
     * @throws FnOException             Something goes wrong parsing the method mapping.
     *                                  A subclass of FnOException specifies what exactly.
     */
    private void parseFunctionMapping(final Resource functionMappingResource) throws FnOException {
        logger.debug("Parsing function mapping {}", functionMappingResource.getURI());

        // get the URI of the function resource this mapping belongs to
        String functionURI = getObjectURI(functionMappingResource, FNO + "function")
                .orElseThrow(() -> new FunctionNotFoundException("No function resource found for fno:Mapping '" +
                        functionMappingResource.getURI() + "'"));

        if (!functionId2functionMappings.containsKey(functionURI)) {
            String functionMappingURI = functionMappingResource.getURI();
            logger.debug("Parsing function mapping {} for function {}", functionMappingURI, functionURI);
            MethodMapping methodMapping = parseMethodMapping(functionMappingResource);
            Implementation implementation = parseImplementation(functionMappingResource);
            FunctionMapping functionMapping = new FunctionMapping(functionURI, methodMapping, implementation);
            functionId2functionMappings.put(functionURI, functionMapping);
        } else {
            logger.debug("Function mapping for {} already parsed", functionURI);
        }
    }

    /**
     * Converts a function composition (fnoc:Composition) to a FunctionComposition object in the internal Function
     * model and kept in an internal cache.
     * @param functionCompositionResource   The function composition resource
     * @throws FnOException             Something goes wrong parsing the function composition.
     *                                  A subclass of FnOException specifies what exactly.
     */
    private void parseFunctionComposition(final Resource functionCompositionResource) throws FnOException{
        logger.debug("Parsing function Composition {}", functionCompositionResource.getURI());
        FunctionComposition composition = new FunctionComposition();
        List<Resource> mappings = getObjectResources(functionCompositionResource, FNOC + "composedOf");
        for(Resource r : mappings){
            final CompositionMappingElement point = parseCompositionMapElement(r);
            if(point.getTo().isOutput()){
                composition.setFunctionId(point.getTo().getFunctionId());
            }
            if(!composition.addMapping(point)){
                logger.debug("duplicate mapping rule found for {}", functionCompositionResource.getURI());
            }
        }
        functionId2functionCompositions.put(composition.getFunctionId(), composition);
    }

    /**
     * Converts the method mapping (fno:methodMapping) resource of a given function mapping (fno:Mapping)
     * to a MethodMapping objevt in the internal function model.
     * @param functionMappingResource   The fno:Mapping resource to get the method mapping from.
     * @return                          The MethodMapping object for the corresponding fno:methodMapping
     * @throws FnOException             Something goes wrong parsing the method mapping.
     *                                  A subclass of FnOException specifies what exactly.
     */
    private MethodMapping parseMethodMapping(final Resource functionMappingResource) throws FnOException {
        logger.debug("Parsing method mapping for {}", functionMappingResource.getURI());

        // get the method mapping
        Resource methodMappingResource = getObjectResource(functionMappingResource, FNO + "methodMapping")
                .orElseThrow(() -> new MethodMappingNotFoundException("No '" + FNO + "methodMapping' found for fno:Mapping '" +
                        functionMappingResource.getURI() + "'"));

        String methodMappingType = getObjectURI(methodMappingResource, typeProperty.getURI())
                .orElseThrow(() -> new MethodMappingTypeNotFoundException("No type of method mapping found for fno:Mapping '" +
                        functionMappingResource.getURI() + "'"));

        String methodName = getLiteralStr(methodMappingResource, FNOM + "method-name")
                .orElseThrow(() -> new MethodNameNotFoundException("No '" + FNOM + "method-name' found for fno:Mapping '" +
                        functionMappingResource.getURI() + "' "));

        return new MethodMapping(methodMappingType, methodName);
    }

    /**
     * Converts the implementation of a given fno:Mapping to an Implementation in the internal function model.
     * @param functionMappingResource The fno:Mapping to find an implementation for.
     * @return                        An implementation of a function.
     * @throws FnOException           Something goes wrong parsing the implementation.
     *                                A subclass of FnOException specifies what exactly.
     */
    private Implementation parseImplementation(final Resource functionMappingResource) throws FnOException {
        logger.debug("Parsing implementation for {}", functionMappingResource.getURI());
        Optional<Resource> implementationResourceOption = getObjectResource(functionMappingResource, FNO + "implementation");

        // get implementation resource
        final Resource implementationResource = implementationResourceOption
                .orElseThrow(() -> new ImplementationDescriptionNotFoundException("No implementation found for function mapping " + functionMappingResource.getURI()));

        final String implementationUri = implementationResource.getURI();

        // get the implementtion type
        final String implementationType = getObjectURI(implementationResource, typeProperty.getURI())
                .orElseThrow(() -> new ImplementationDescriptionNotFoundException("No implementation type found for implementation resource " + implementationUri));

        // check the implementation type. At this moment only java classes are supported.
        final String supportedImplementationType = FNOI + "JavaClass";
        if (!implementationType.equals(supportedImplementationType)) {
            throw new UnsupportedImplementationTypeException("Only implementation type '" + supportedImplementationType + "' supported. Found '" + implementationType + "'");
        }

        // get the class name (mandatory)
        final String className = getLiteralStr(implementationResource, FNOI + "class-name")
                .orElseThrow(() -> new ClassNameDescriptionNotFoundException("No '" + FNOI + "class-name' found for implementation '" + implementationUri + "'"));

        // get the optional path to an implementation, e.g. a JAR file
        Property downloadPageProperty = ResourceFactory.createProperty(DOAP.toString(), "download-page");
        final String location = getLiteralStr(implementationResource, downloadPageProperty.getURI()).orElse("");
        final String mappedLocation = location2otherLocationMap.getOrDefault(location, location);

        return new Implementation(className, mappedLocation);
    }

    /**
     * Converts a Composition element (fnoc:composedOF) to a CompositionMappingElement object in the internal Function
     * model.
     * @param compositionMapElementResource   The function composition resource
     * @throws FnOException             Something goes wrong parsing the composition element.
     *                                  A subclass of FnOException specifies what exactly.
     */
    private CompositionMappingElement parseCompositionMapElement(final Resource compositionMapElementResource) throws FnOException{
        final CompositionMappingPoint startingPoint = parseCompositionMapElementStartingpoint(compositionMapElementResource);
        final CompositionMappingPoint endPoint = parseCompositionMapElementEndpoint(compositionMapElementResource);
        return new CompositionMappingElement(startingPoint, endPoint);
    }

    /**
     * Converts a Composition element starting point (fnoc:mapFrom or fnoc:mapFromTerm) to a CompositionMappingPoint
     * object in the internal Function model.
     * @param element   The function composition resource
     * @throws FnOException             Something goes wrong parsing the composition element.
     *                                  A subclass of FnOException specifies what exactly.
     */
    private CompositionMappingPoint parseCompositionMapElementStartingpoint(final Resource element) throws FnOException{

        final Optional<Resource> startingPointResource = getObjectResource(element, FNOC+"mapFrom");
        if(startingPointResource.isPresent()){ // fnoc:mapFrom found
            final Resource startingPointFunction = getObjectResource(startingPointResource.get(), FNOC+"constituentFunction")
                    .orElseThrow(() -> new FunctionCompositionException("No " + FNOC+"constituentFunction present in " + FNOC + "mapFrom"));
            Optional<String> startingPointParameter = getObjectURI(startingPointResource.get(), FNOC+"functionParameter");
            boolean isOutput = false;
            if(!startingPointParameter.isPresent()){
                startingPointParameter = getObjectURI(startingPointResource.get(), FNOC+"functionOutput");
                isOutput = true;
            }
            String predicateURI = parameterURItoPredicate.get(startingPointParameter.orElseThrow(() -> new FunctionCompositionException("starting point: no output predicate found for mapping with constituent function " + startingPointFunction.getURI()){}));
            return new CompositionMappingPoint(startingPointFunction.getURI(), predicateURI, isOutput);
        }
        else{ // try to find a fnoc:mapFromTerm
            final Literal literal = getLiteral(element, FNOC+"mapFromTerm")
                    .orElseThrow(() -> new FunctionCompositionException("No composition starting point found for " + element.getId()){});
            CompositionMappingPoint cmp = new CompositionMappingPoint("", literal.getString(), false);
            cmp.setLiteral(true);
            return cmp;
        }
    }

    /**
     * Converts a Composition element end point (fnoc:mapTo) to a CompositionMappingPoint
     * object in the internal Function model.
     * @param element   The function composition resource
     * @throws FnOException             Something goes wrong parsing the composition element.
     *                                  A subclass of FnOException specifies what exactly.
     */
    private CompositionMappingPoint parseCompositionMapElementEndpoint(final Resource element) throws FnOException{
        final Resource endPointResource = getObjectResource(element, FNOC+"mapTo")
                .orElseThrow( () -> new FunctionCompositionException("No mapping end point found"){});
        final String id = getObjectURI(endPointResource, FNOC+"constituentFunction")
                .orElseThrow( () -> new FunctionCompositionException("No Constituent function found for endpoint"){});
        Optional<String> parameterId = getObjectURI(endPointResource, FNOC+"functionParameter");
        boolean isOutput = false;
        if(!parameterId.isPresent()){
            parameterId = getObjectURI(endPointResource, FNOC+"functionOutput");
            isOutput = true;
        }
        String predicateURI = parameterURItoPredicate.get(parameterId.orElseThrow( () -> new FunctionCompositionException("mapping end point: no parameter found for constituent function "+id){}));
        return new CompositionMappingPoint(id, predicateURI, isOutput);
    }
    /**
     * Searches the FnO document for fnoc:applies resources and adds them to the function list as aliases
     */
    private void parseApplies() {
        logger.debug("PARSING fnoc:applies");
        final Map<String, String> appliesMap = new HashMap<>();
        Property appliesObject = ResourceFactory.createProperty(FNOC + "applies");
        ResIterator applies = functionDescriptionTriples.listSubjectsWithProperty(appliesObject);
        while (applies.hasNext()){
            final Resource alias = applies.nextResource();
            final String original = getObjectURI(alias, FNOC+"applies")
                    .orElseThrow(() -> new IllegalStateException("illegal state, there should be object with applies")); // can't happen
            appliesMap.put(alias.getURI(), original);
        }
        for(String first : appliesMap.keySet()){
            if(!functionId2Functions.containsKey(first)) {
                String key = first;
                while(appliesMap.containsKey(key)){ // fnoc:applies on a fnoc:applies
                    key = appliesMap.get(key);
                }
                functionId2Functions.put(first, functionId2Functions.get(key));
            }
        }
    }

    /**
     * Searches the FnO document for fno:Function resources and converts them to Function objects in the internal
     * Function model.
     * @throws FnOException Something goes wrong parsing the functions.
     *                      A subclass of FnOException specifies what exactly.
     */
    private void parseFunctions() throws FnOException {
        logger.debug("Parsing functions");
        Resource functionObject = ResourceFactory.createResource(FNO + "Function");
        ResIterator functions = functionDescriptionTriples.listSubjectsWithProperty(typeProperty, functionObject);
        while (functions.hasNext()) {
            parseFunction(functions.nextResource());
        }
    }

    /**
     * Converts a given fno:Function to a Function object in the internal function model and puts it in an internal cache.
     * @param functionResource  The function to convert
     * @throws FnOException     Something goes wrong parsing the function.
     *                          A subclass of FnOException specifies what exactly.
     */
    private void parseFunction (final Resource functionResource) throws FnOException {
        String functionURI = functionResource.getURI();
        if (!functionId2Functions.containsKey(functionURI)) {
            logger.debug("Parsing new function {}", functionURI);

            // get name
            String name = getLiteralStr(functionResource, FNO + "name")
                    .orElseThrow(() -> new FunctionNameNotFoundException("Could not find '" + FNO + "name' for"));

            // get expected parameters
            List<Parameter> expects = parseParameters(functionResource, true);

            // get outputs
            List<Parameter> returns = parseParameters(functionResource, false);

            // get description
            String description = getLiteralStr(functionResource, DCTERMS + "description").orElse("");

            // parse deprecated way of providing classes
            parseLib(functionResource);

            Function function = new Function(functionURI, name, description, expects, returns);
            functionId2Functions.put(functionURI, function);
        }
    }

    /**
     * Converts the parameters for a given fno:Function to a list of Parameter objects in the internal function model.
     * @param functionResource  The fno:Function to parse the parameters from.
     * @param input             If true, this method parses input parameters. If false it parses output parameters.
     * @return                  A list of Parameter objects in the internal function model.
     * @throws FnOException     Something goes wrong parsing the parameters.
     *                          A subclass of FnOException specifies what exactly.
     */
    private List<Parameter> parseParameters(final Resource functionResource, boolean input) throws FnOException {
        logger.debug("Parsing expected parameters of {}", functionResource.getURI());
        String isInputParameter = input ? "expects" : "returns";
        Resource expectedOrReturnedResources = functionResource.getPropertyResourceValue(functionDescriptionTriples.getProperty(FNO.toString(), isInputParameter));
        List<Resource> parameterResourceList = getResourcesFromList(expectedOrReturnedResources);
        List<Parameter> parameters = new ArrayList<>(parameterResourceList.size());
        for (Resource parameterResource : parameterResourceList) {
            parameters.add(parseParameter(parameterResource));
        }
        return parameters;
    }

    /**
     * Converts a fno:Parameter to a Parameter object in the internal function model.
     * @param parameterResource The resource representing an fno:Parameter.
     * @return                  A Parameter in the internal function model.
     * @throws FnOException     Something goes wrong parsing parameterResource.
     *                          A subclass of FnOException specifies what exactly.
     */
    private Parameter parseParameter(final Resource parameterResource) throws FnOException {
        String uri = parameterResource.getURI();
        logger.debug("Parsing parameter {}", uri);

        // get the name of the parameter
        String name = getLiteralStr(parameterResource, FNO + "name")
                .orElseThrow(() -> new ParameterNameDescriptionNotFoundException("No '" + FNO + "name' found for parameter '" + uri + "'"));

        // get the type of the parameter
        String typeUri = getObjectURI(parameterResource, FNO + "type")
                .orElseThrow(() -> new DataTypeNotFoundException("No data type description found for parameter '" + uri + '"'));

        // get the predicate as used in e.g. an RML mapping file.
        String predicateUri = getObjectURI(parameterResource, FNO + "predicate")
                .orElseThrow(() -> new ParameterPredicateNotFoundException("No predicate description found for parameter '" + uri + "'"));

        // see if this parameter is required.
        // This is actually not used at the moment and supposed to be true.
        ResourceFactory.createProperty(FNO + "required");
        boolean isRequired = getLiteralBoolean(parameterResource, FNO + "required").orElse(true);
        final DataTypeConverter<?> typeConverter = dataTypeConverterProvider.getDataTypeConverter(typeUri);

        return new Parameter(name, predicateUri, typeConverter, isRequired);
    }

    /**
     * Parses an implementation the deprecated way, using a http://example.com/library# predicate in a function mapping.
     * This is only to be backward compatible.
     * @param functionResource  The fno:Function to parse the parameters from.
     * @throws FnOException     Something goes wrong parsing parameterResource.
     *                          A subclass of FnOException specifies what exactly.
     */
    private void parseLib(final Resource functionResource) throws FnOException {
        Optional<Resource> libResourceOption = getObjectResource(functionResource, LIB + "providedBy");
        final String functionUri = functionResource.getURI();
        if (functionId2functionMappings.containsKey(functionUri)) {
            logger.debug("Function mapping for {} already parsed", functionUri);
            return;
        }

        if (libResourceOption.isPresent()) {
            logger.warn("Using {}providedBy is deprecated. Using the implementation vocabulary (https://fno.io/vocabulary/implementation/index-en.html) is recommended", LIB);

            final Resource libResource = libResourceOption.get();

            // get jar file name, if any
            final String location = getLiteralStr(libResource, LIB + "localLibrary").orElse("");
            final String mappedLocation = location2otherLocationMap.getOrDefault(location, location);

            // get class
            final String className = getLiteralStr(libResource, LIB + "class")
                    .orElseThrow(() -> new ClassNameDescriptionNotFoundException("No '" + LIB
                    + "class' found for '" + libResource.getURI() + "' for function '" + functionUri + "'"));

            // get method
            final String method = getLiteralStr(libResource, LIB + "method")
                    .orElseThrow(() -> new MethodMappingNotFoundException("No '" + LIB
                            + "method' found for '" + libResource.getURI() + "' for function '" + functionUri + "'"));

            final MethodMapping methodMapping = new MethodMapping(FNOM + "StringMethodMapping", method);
            final Implementation implementation = new Implementation(className, mappedLocation);
            final FunctionMapping functionMapping = new FunctionMapping(functionUri, methodMapping, implementation);
            functionId2functionMappings.put(functionUri, functionMapping);
        }
    }


    /////////////////////////////////////////////////////////
    // Jena helper functions. Might move to separate class //
    /////////////////////////////////////////////////////////

    /**
     * Returns the object as a literal for a given subject and predicate, if any such triple exists.
     * @param subject       The subject of a triple.
     * @param predicateURI  The predicate of a triple.
     * @return              The object of the triple as a literal, or none if the object is not a
     *                      literal or if no such triple exists.
     */
    private Optional<Literal> getLiteral(final Resource subject, final String predicateURI) {
        Property property = ResourceFactory.createProperty(predicateURI);
        Statement statement = subject.getProperty(property);
        if (statement == null) {
            return Optional.empty();
        } else {
            return Optional.of(statement.getObject().asLiteral());
        }
    }

    /**
     * Returns the object as a string for a given subject and predicate, if any such triple exists.
     * @param subject       The subject of a triple.
     * @param predicateURI  The predicate of a triple.
     * @return              The string value of the object of the triple, or none if the object is not a
     *                      string literal or if no such triple exists.
     */
    private Optional<String> getLiteralStr(final Resource subject, final String predicateURI) {
        Optional<Literal> objectLiteralResult = getLiteral(subject, predicateURI);
        return objectLiteralResult.map(Literal::getString);
    }

    /**
     * Returns the object as a boolean for a given subject and predicate, if any such triple exists.
     * @param subject       The subject of a triple.
     * @param predicateURI  The predicate of a triple.
     * @return              The boolean value of the object of the triple, or none if the object is not a
     *                      boolean literal or if no such triple exists.
     */
    private Optional<Boolean> getLiteralBoolean(final Resource subject, final String predicateURI) {
        Optional<Literal> objectLiteralResult = getLiteral(subject, predicateURI);
        return objectLiteralResult.map(Literal::getBoolean);
    }

    /**
     * Returns the URI of the object for a given subject and predicate, if any such triple exists.
     * @param subject       The subject of a triple.
     * @param predicateURI  The predicate of a triple.
     * @return              The URI of the object of the triple, or none if no such triple exists.
     */
    private Optional<String> getObjectURI(final Resource subject, final String predicateURI) {
        Optional<Resource> objectResourceOption = getObjectResource(subject, predicateURI);
        return objectResourceOption.map(Resource::getURI);
    }

    /**
     * Returns the object as a resource, given a subject and a predicate.
     * @param subject       The subject of a triple.
     * @param predicateURI  The predicate of a triple.
     * @return              The object belonging to this triple, or none of no such triple exists.
     */
    private Optional<Resource> getObjectResource(final Resource subject, final String predicateURI) {
        Property property = ResourceFactory.createProperty(predicateURI);
        Statement statement = subject.getProperty(property);
        if (statement == null) {
            return Optional.empty();
        } else {
            return Optional.of(statement.getObject().asResource());
        }
    }

    /**
     * Returns a list of resources, given a subject and a predicate
     * @param subject       The subject of a triple
     * @param predicateURI  The predicate of a triple
     * @return              The list of objects belonging to this triple, or an empty list if none exist
     */
    private List<Resource> getObjectResources(final Resource subject, final String predicateURI){
        Property property = ResourceFactory.createProperty(predicateURI);
        StmtIterator it = subject.listProperties(property);
        List<Resource> list = new ArrayList<>();
        it.filterDrop(Objects::isNull);
        it.forEach((Statement s)-> list.add(s.getObject().asResource()));
        return list;
    }

    /**
     * Parses an RDF list of resources and returns these resources in a Java List.
     * @param listResource  The RDF list resource.
     * @return              The resources in this list.
     */
    private List<Resource> getResourcesFromList(final Resource listResource) {
        List<Resource> resources = new ArrayList<>();

        // only proceed if the list is not exhausted
        if (!listResource.hasURI(RDF + "nil")) {
            // add 'first' resource of list
            Optional<Resource> firstResource = getObjectResource(listResource, RDF + "first");
            if (firstResource.isPresent()) {
                resources.add(firstResource.get());

                // process the 'rest' of the list
                Optional<Resource> restResource = getObjectResource(listResource, RDF + "rest");
                restResource.ifPresent(resource -> resources.addAll(getResourcesFromList(resource)));
            }
        }
        return resources;
    }

}
