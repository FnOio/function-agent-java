package be.ugent.idlab.knows.functions.agent;

import be.ugent.idlab.knows.functions.agent.model.*;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.rdf.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static be.ugent.idlab.knows.functions.agent.functionModelProvider.fno.NAMESPACES.*;

public class DescriptionGenerator {


    private static final Map<Class<?>, String> datatypeMap = new HashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(DescriptionGenerator.class);


    private static final Property rdfTypeProperty = ResourceFactory.createProperty(RDF.toString(), "type");
    private static final Property fnoNameProperty = ResourceFactory.createProperty(FNO.toString(), "name");
    private static final Property fnoParameterProperty = ResourceFactory.createProperty(FNO.toString(), "expects");
    private static final Property fnoReturnProperty = ResourceFactory.createProperty(FNO.toString(), "returns");
    private static final Property fnoPredicateProperty = ResourceFactory.createProperty(FNO.toString(), "predicate");
    private static final Property fnoRequiredProperty = ResourceFactory.createProperty(FNO.toString(), "required");
    private static final Property fnoTypeProperty = ResourceFactory.createProperty(FNO.toString(), "type");
    private static final Property fnoiClassNameProperty = ResourceFactory.createProperty(FNOI.toString(), "class-name");
    private static final Property fnoFunctionProperty = ResourceFactory.createProperty(FNO.toString(), "function");
    private static final Property fnoImplementationProperty = ResourceFactory.createProperty(FNO.toString(), "implementation");
    private static final Property fnoMethodMappingProperty = ResourceFactory.createProperty(FNO.toString(), "methodMapping");
    private static final Property fnomMethodNameProperty = ResourceFactory.createProperty(FNOM.toString(), "method-name");
    private static final Property dctermsDescriptionProperty = ResourceFactory.createProperty(DCTERMS.toString(), "description");
    private static final String FNOP = "https://w3id.org/function/vocabulary/predicates#";
    /*
     * initialise datatype map
     */
    // TODO add more

    static {
        datatypeMap.put(boolean.class, XSDDatatype.XSDboolean.getURI());
        datatypeMap.put(Boolean.class, XSDDatatype.XSDboolean.getURI());
        datatypeMap.put(String.class, XSDDatatype.XSDstring.getURI());
        datatypeMap.put(int.class, XSDDatatype.XSDint.getURI());
        datatypeMap.put(Integer.class, XSDDatatype.XSDint.getURI());
        datatypeMap.put(long.class, XSDDatatype.XSDinteger.getURI());
        datatypeMap.put(Long.class, XSDDatatype.XSDinteger.getURI());
        datatypeMap.put(float.class, XSDDatatype.XSDdecimal.getURI());
        datatypeMap.put(Float.class, XSDDatatype.XSDdecimal.getURI());
        datatypeMap.put(double.class, XSDDatatype.XSDfloat.getURI());
        datatypeMap.put(Double.class, XSDDatatype.XSDfloat.getURI());
        datatypeMap.put(Object[].class, RDF+"list");
    }

    private static void addParameters(Model model, Method method, Resource function){
        // add parameters
        java.lang.reflect.Parameter[] parameters = method.getParameters(); // parameters
        RDFNode[] rdfArray = new RDFNode[parameters.length];
        Arrays.stream(parameters).map(parameter -> {
            Resource parameterResource = model.createResource(FNO + parameter.getName());
            parameterResource.addProperty(fnoNameProperty, parameter.getName());
            Resource predicateResource = model.createResource(FNOP+parameter.getName());
            parameterResource.addProperty(fnoPredicateProperty,  predicateResource);
            parameterResource.addProperty(fnoRequiredProperty, "true", XSDDatatype.XSDboolean);
            parameterResource.addProperty(fnoTypeProperty, model.createResource(getDatatype(parameter.getType())));
            parameterResource.addProperty(rdfTypeProperty, model.createResource(FNO+"Parameter"));
            return parameterResource;
        }).collect(Collectors.toList()).toArray(rdfArray);
        RDFList parameterList = model.createList(rdfArray);
        function.addProperty(fnoParameterProperty, parameterList);
    }

    private static void addParameters(Model model, Function function, Resource functionResource){
        List<Parameter> parameterList = function.getArgumentParameters();
        RDFNode[] rdfArray = new RDFNode[parameterList.size()];
        parameterList.stream().map(parameter -> {
           Resource parameterResource = model.createResource(FNO+function.getClass().getName()+parameter.getName());
           parameterResource.addProperty(rdfTypeProperty, model.createResource(FNO+"Parameter"));
           parameterResource.addProperty(fnoNameProperty, parameter.getName());
           Resource predicateResource = model.createResource(parameter.getId());
           parameterResource.addProperty(fnoPredicateProperty, predicateResource);
           parameterResource.addProperty(fnoRequiredProperty, ""+parameter.isRequired());
           parameterResource.addProperty(fnoTypeProperty, model.createResource(getDatatype(parameter.getTypeConverter().getTypeClass())));
           return parameterResource;
        }).collect(Collectors.toList()).toArray(rdfArray);
        RDFList parameters = model.createList(rdfArray);
        functionResource.addProperty(fnoParameterProperty, parameters);
    }

    private static void addReturnTypeAndExceptions(Model model, Method method, Resource function){
        // add return type and exception types
        Class<?> returnType = method.getReturnType();
        Class<?>[] exceptionTypes = method.getExceptionTypes();
        int offset = "void".equals(returnType.getName()) ? 0 : 1;
        RDFNode[] returnList = new RDFNode[exceptionTypes.length + offset];
        if(!"void".equals(returnType.getName())){
            Resource returnTypeResource = model.createResource(FNO+returnType.getName()+"Output");
            returnTypeResource.addProperty(fnoNameProperty, returnType.getName()+"Output");
            returnTypeResource.addProperty(fnoRequiredProperty, "true", XSDDatatype.XSDboolean);
            returnTypeResource.addProperty(rdfTypeProperty, model.createResource(FNO+"Output"));
            returnTypeResource.addProperty(fnoTypeProperty, model.createResource(getDatatype(returnType)));
            returnTypeResource.addProperty(fnoPredicateProperty, model.createResource(FNOP+returnType.getName()+"Output"));
            returnList[0] = returnTypeResource;
        }

        for (int i = 0; i < exceptionTypes.length; i++){
            Class<?> exceptionType = exceptionTypes[i];
            Resource exceptionResource = model.createResource(FNO+exceptionType.getName()+"Exception");
            exceptionResource.addProperty(fnoNameProperty, exceptionType.getName()+"Exception");
            exceptionResource.addProperty(fnoRequiredProperty, "false");
            exceptionResource.addProperty(rdfTypeProperty, model.createResource(FNO+"Output"));
            exceptionResource.addProperty(fnoTypeProperty, model.createResource(getDatatype(exceptionType)));
            exceptionResource.addProperty(fnoPredicateProperty, model.createResource(FNOP+exceptionType.getName()+"Output"));
            returnList[i+offset] = exceptionResource;
        }

        RDFList outputList = model.createList(returnList);
        function.addProperty(fnoReturnProperty, outputList);
    }

    private static void addReturnTypeAndExceptions(Model model, Function function, Resource functionResource){
        List<Parameter> returnParameterList = function.getReturnParameters();
        RDFNode[] rdfArray = new RDFNode[returnParameterList.size()];
        returnParameterList.stream().map(returnType -> {
            Resource returnResource = model.createResource(FNO+function.getClass().getName()+returnType.getName());
            returnResource.addProperty(rdfTypeProperty, model.createResource(FNO+"Output"));
            returnResource.addProperty(fnoNameProperty, returnType.getName());
            Resource predicateResource = model.createResource(returnType.getId());
            returnResource.addProperty(fnoPredicateProperty, predicateResource);
            returnResource.addProperty(fnoRequiredProperty, ""+returnType.isRequired());
            returnResource.addProperty(fnoTypeProperty, model.createResource(getDatatype(returnType.getTypeConverter().getTypeClass())));
            return returnResource;
        }).collect(Collectors.toList()).toArray(rdfArray);
        RDFList parameters = model.createList(rdfArray);
        functionResource.addProperty(fnoReturnProperty, parameters);
    }


    private static void addMapping(Model model, Method method, Resource function){
        Class<?> clazz = method.getDeclaringClass();
        Resource classResource = model.createResource(FNO+clazz.getName());
        classResource.addProperty(rdfTypeProperty, model.createResource(FNOI + "JavaClass"));
        classResource.addProperty(fnoiClassNameProperty, clazz.getName());
        Resource mappingResource = model.createResource(FNO+method.getName()+"Mapping");
        mappingResource.addProperty(rdfTypeProperty, model.createResource(FNO+"Mapping"));
        mappingResource.addProperty(fnoFunctionProperty, function);
        mappingResource.addProperty(fnoImplementationProperty, classResource);
        Resource methodMappingResource = model.createResource();
        methodMappingResource.addProperty(rdfTypeProperty, model.createResource(FNOM+"StringMethodMapping"));
        methodMappingResource.addProperty(fnomMethodNameProperty, method.getName());
        mappingResource.addProperty(fnoMethodMappingProperty, methodMappingResource);
    }

    private static void addMapping(Model model, Function function, Resource functionResource) {
        if (function.isComposite()) {
            return;
        }
        FunctionMapping functionMapping = function.getFunctionMapping();
        MethodMapping methodMapping = functionMapping.getMethodMapping();
        Implementation implementation = functionMapping.getImplementation();
        Resource classResource = model.createResource(FNO + implementation.getClassName());
        classResource.addProperty(rdfTypeProperty, model.createResource(FNOI + "JavaClass"));
        classResource.addProperty(fnoiClassNameProperty, implementation.getClassName());
        Resource mappingResource = model.createResource(FNO + function.getName() + "Mapping");
        mappingResource.addProperty(rdfTypeProperty, model.createResource(FNO + "Mapping"));
        mappingResource.addProperty(fnoFunctionProperty, functionResource);
        mappingResource.addProperty(fnoImplementationProperty, classResource);
        Resource methodMappingResource = model.createResource();
        methodMappingResource.addProperty(rdfTypeProperty, model.createResource(methodMapping.getType()));
        methodMappingResource.addProperty(fnoMethodMappingProperty, model.createResource(methodMapping.getMethodName()));
    }

    private static void addComposition(Model model, Function function, Resource functionResource){
        if(!function.isComposite()){
            return;
        }
        FunctionComposition functionComposition = function.getFunctionComposition();
        List<CompositionMappingElement> functionCompositionMappings = functionComposition.getMappings();
    }

    public static String generateDescription(Model model, Method method) {
        logger.debug("name: {}", method.getName()); // name

        String methodURI = FNO + method.getDeclaringClass().getName() + "." + method.getName();
        Resource function = model.createResource(methodURI);
        function.addProperty(rdfTypeProperty, model.createResource(FNO + "Function"));
        function.addProperty(fnoNameProperty, method.getName());
        function.addProperty(dctermsDescriptionProperty, method.getName());

        addParameters(model, method, function);
        addReturnTypeAndExceptions(model, method, function);

        int modifiers = method.getModifiers();
        if(Modifier.isStatic(modifiers)){ // static function, we can link implementation.
            addMapping(model, method, function);
        }
        return methodURI;
    }

    public static void addFunctionToModel(Model model, Function function){
        String functionURI = function.getId();
        Resource functionResource = model.createResource(functionURI);
        functionResource.addProperty(rdfTypeProperty, model.createResource(FNO+"Function"));
        functionResource.addProperty(fnoNameProperty, function.getName());
        functionResource.addProperty(dctermsDescriptionProperty, function.getDescription());

        addParameters(model, function, functionResource);
        addReturnTypeAndExceptions(model, function, functionResource);
        if(function.isComposite()){
            addComposition(model, function, functionResource);
        }else{
            addMapping(model, function, functionResource);
        }
    }

    private static String getDatatype(Class<?> clazz){
        logger.debug("getting data type for {}", clazz.getName());
        if(!datatypeMap.containsKey(clazz)){
            logger.debug("no entry found, returning default value XSDanyURI");
        }
        return datatypeMap.getOrDefault(clazz, XSDDatatype.XSDanyURI.getURI());
    }
}
