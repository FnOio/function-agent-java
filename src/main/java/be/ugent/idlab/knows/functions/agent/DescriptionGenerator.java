package be.ugent.idlab.knows.functions.agent;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.rdf.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static be.ugent.idlab.knows.functions.agent.functionModelProvider.fno.NAMESPACES.*;

public class DescriptionGenerator {


    private static final Map<Class<?>, XSDDatatype> datatypeMap = new HashMap<>();
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
    private static final String FNOP = "https://w3id.org/function/vocabulary/predicates#";
    /*
     * initialise datatype map
     */
    // TODO add more

    static {
        datatypeMap.put(boolean.class, XSDDatatype.XSDboolean);
        datatypeMap.put(Boolean.class, XSDDatatype.XSDboolean);
        datatypeMap.put(String.class, XSDDatatype.XSDstring);
        datatypeMap.put(int.class, XSDDatatype.XSDint);
        datatypeMap.put(Integer.class, XSDDatatype.XSDint);
        datatypeMap.put(long.class, XSDDatatype.XSDinteger);
        datatypeMap.put(Long.class, XSDDatatype.XSDinteger);
    }

    public static String generateDescription(Model model, Method method) {
        logger.debug("name: {}", method.getName()); // name

        String methodURI = FNO + method.getDeclaringClass().getName() + "." + method.getName();
        Resource function = model.createResource(methodURI);
        function.addProperty(rdfTypeProperty, model.createResource(FNO + "Function"));
        function.addProperty(fnoNameProperty, method.getName());
        function.addProperty(ResourceFactory.createProperty(DCTERMS.toString(), "description"), method.getName());

        // add parameters
        java.lang.reflect.Parameter[] parameters = method.getParameters(); // parameters
        RDFNode[] rdfArray = new RDFNode[parameters.length];
        Arrays.stream(parameters).map(parameter -> {
            Resource parameterResource = model.createResource(FNO + parameter.getName());
            parameterResource.addProperty(fnoNameProperty, parameter.getName());
            Resource predicateResource = model.createResource(FNOP+parameter.getName());
            parameterResource.addProperty(fnoPredicateProperty,  predicateResource);
            parameterResource.addProperty(fnoRequiredProperty, "true", XSDDatatype.XSDboolean);
            parameterResource.addProperty(fnoTypeProperty, model.createResource(getDatatype(parameter.getType()).getURI()));
            parameterResource.addProperty(rdfTypeProperty, model.createResource(FNO+"Parameter"));
            return parameterResource;
        }).collect(Collectors.toList()).toArray(rdfArray);
        RDFList parameterList = model.createList(rdfArray);
        function.addProperty(fnoParameterProperty, parameterList);

        // add return type and exception types
        Class<?> returnType = method.getReturnType();
        Class<?>[] exceptionTypes = method.getExceptionTypes();
        RDFNode[] returnList = new RDFNode[1 + exceptionTypes.length];

        Resource returnTypeResource = model.createResource(FNO+returnType.getName()+"Output");
        returnTypeResource.addProperty(fnoNameProperty, returnType.getName()+"Output");
        returnTypeResource.addProperty(fnoRequiredProperty, "true", XSDDatatype.XSDboolean);
        returnTypeResource.addProperty(rdfTypeProperty, model.createResource(FNO+"Output"));
        returnTypeResource.addProperty(fnoTypeProperty, model.createResource(getDatatype(returnType).getURI()));
        returnTypeResource.addProperty(fnoPredicateProperty, model.createResource(returnType.getName()+"Output"));
        returnList[0] = returnTypeResource;

        for (int i = 0; i < exceptionTypes.length; i++){
            Class<?> exceptionType = exceptionTypes[i];
            Resource exceptionResource = model.createResource(FNO+exceptionType.getName()+"Exception");
            exceptionResource.addProperty(fnoNameProperty, exceptionType.getName()+"Exception");
            exceptionResource.addProperty(fnoRequiredProperty, "false");
            exceptionResource.addProperty(rdfTypeProperty, model.createResource(FNO+"Output"));
            exceptionResource.addProperty(fnoTypeProperty, model.createResource(getDatatype(exceptionType).getURI()));
            returnTypeResource.addProperty(fnoPredicateProperty, model.createResource(exceptionType.getName()+"Output"));
            returnList[i+1] = exceptionResource;
        }

        RDFList outputList = model.createList(returnList);
        function.addProperty(fnoReturnProperty, outputList);


        int modifiers = method.getModifiers();
        if(Modifier.isStatic(modifiers)){ // static function, we can link implementation.
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
        return methodURI;
    }
    private static XSDDatatype getDatatype(Class<?> clazz){
        return datatypeMap.getOrDefault(clazz, XSDDatatype.XSDanyURI);
    }
}
