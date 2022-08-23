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
    }

    public static void generateDescription(Model model, Method method, String filename) throws Exception {
        logger.debug("name: {}", method.getName()); // name

        Property rdfTypeProperty = ResourceFactory.createProperty(RDF.toString(), "type");
        Property fnoNameProperty = ResourceFactory.createProperty(FNO.toString(), "name");
        Property fnoParameterProperty = ResourceFactory.createProperty(FNO.toString(), "expects");
        Property fnoReturnProperty = ResourceFactory.createProperty(FNO.toString(), "returns");
        Property fnoPredicateProperty = ResourceFactory.createProperty(FNO.toString(), "predicate");
        Property fnoRequiredProperty = ResourceFactory.createProperty(FNO.toString(), "required");
        Property fnoTypeProperty = ResourceFactory.createProperty(FNO.toString(), "type");



        Resource function = model.createResource(FNO + method.getDeclaringClass().getName() + "." + method.getName());
        function.addProperty(rdfTypeProperty, FNO + "Function");
        function.addProperty(fnoNameProperty, method.getName());
        function.addProperty(ResourceFactory.createProperty(DCTERMS.toString(), "description"), method.getName());

        // add parameters
        java.lang.reflect.Parameter[] parameters = method.getParameters(); // parameters
        RDFNode[] rdfArray = new RDFNode[parameters.length];
        Arrays.stream(parameters).map(parameter -> {
            Resource parameterResource = model.createResource(FNO + parameter.getName());
            parameterResource.addProperty(fnoNameProperty, parameter.getName());
            parameterResource.addProperty(fnoPredicateProperty,  parameter.getName());
            parameterResource.addProperty(fnoRequiredProperty, "true", XSDDatatype.XSDboolean);
            parameterResource.addProperty(fnoTypeProperty, getDatatype(parameter.getType()).getURI());
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
        returnList[0] = returnTypeResource;

        for (int i = 0; i < exceptionTypes.length; i++){
            Class<?> exceptionType = exceptionTypes[i];
            Resource exceptionResource = model.createResource(FNO+exceptionType.getName()+"Exception");
            exceptionResource.addProperty(fnoNameProperty, exceptionType.getName()+"Exception");
            exceptionResource.addProperty(fnoRequiredProperty, "false");
            returnList[i+1] = exceptionResource;
        }

        RDFList outputList = model.createList(returnList);
        function.addProperty(fnoReturnProperty, outputList);


        int modifiers = method.getModifiers();
        if(Modifier.isStatic(modifiers)){ // static function, we can link implementation.
            Class<?> clazz = method.getDeclaringClass();
            clazz.getPackage().getName();

            Resource classResource = model.createResource(FNO+clazz.getName());
            classResource.addProperty(rdfTypeProperty, FNOI + "JavaClass");
        }

        Class<?> clazz = method.getDeclaringClass();
        clazz.getPackage().getName();
        logger.debug("class name: {}", clazz.getName() );
        method.getModifiers(); // check for static modifier to link implementation
        Modifier.isStatic(0);
    }
    private static XSDDatatype getDatatype(Class<?> clazz){
        return datatypeMap.getOrDefault(clazz, XSDDatatype.XSDanyURI);
    }
}
