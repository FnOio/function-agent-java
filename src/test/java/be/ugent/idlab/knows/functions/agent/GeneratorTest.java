package be.ugent.idlab.knows.functions.agent;

import be.ugent.idlab.knows.functions.internalfunctions.InternalTestFunctions;
import org.apache.jena.rdf.model.*;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.junit.Assert;
import org.junit.Test;

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static be.ugent.idlab.knows.functions.agent.functionModelProvider.fno.NAMESPACES.FNO;
import static be.ugent.idlab.knows.functions.agent.functionModelProvider.fno.NAMESPACES.RDF;

public class GeneratorTest {
    @Test
    public void testCreationWithLink() throws Exception{
        Model model = ModelFactory.createDefaultModel();
        OutputStream outputStream = Files.newOutputStream(Paths.get("testCreationWithLink.ttl"));
        String methodURI = DescriptionGenerator.generateDescription(model, InternalTestFunctions.class.getMethod("pow",long.class, long.class ));
        RDFDataMgr.write(outputStream, model, Lang.TURTLE);
        outputStream.flush();
        outputStream.close();
        Agent agent = AgentFactory.createFromFnO("testCreationWithLink.ttl");
        Arguments arguments = new Arguments();
        arguments.add("https://w3id.org/function/vocabulary/predicates#arg0", "2");
        arguments.add("https://w3id.org/function/vocabulary/predicates#arg1", "6");
        Object result = agent.execute(methodURI,arguments);
        Assert.assertEquals("2^6 = 64", 64L, result);
    }

    @Test
    public void testExceptionAsOutput() throws Exception{
        Model model = ModelFactory.createDefaultModel();
        OutputStream outputStream = Files.newOutputStream(Paths.get("testExceptionAsOutput.ttl"));
        String methodURI = DescriptionGenerator.generateDescription(model, InternalTestFunctions.class.getMethod("testExceptionFunction", Long.class));
        RDFDataMgr.write(outputStream, model, Lang.TURTLE);
        outputStream.flush();
        outputStream.close();
        Resource functionResource = model.getResource(methodURI);
        List<?> list = getResourcesFromList(functionResource.getPropertyResourceValue(ResourceFactory.createProperty("https://w3id.org/function/ontology#returns")));
        Assert.assertEquals("expected 2 output values: return type and 1 exception", 2, list.size());
    }

    @Test
    public void testVarArgs() throws Exception{
        Model model = ModelFactory.createDefaultModel();
        OutputStream outputStream = Files.newOutputStream(Paths.get("testVarArgs.ttl"));
        String methodURI = DescriptionGenerator.generateDescription(model, InternalTestFunctions.class.getMethod("testVarargsFunction", Object[].class));
        RDFDataMgr.write(outputStream, model, Lang.TURTLE);
        outputStream.flush();
        outputStream.close();
        Resource parameter = model.getResource("https://w3id.org/function/ontology#arg0");
        Resource type = parameter.getPropertyResourceValue(ResourceFactory.createProperty(FNO.toString(), "type"));
        Assert.assertEquals(type.getURI(), RDF+"list");
    }

    @Test
    public void testVoidReturnFunction() throws Exception{
        Model model = ModelFactory.createDefaultModel();
        OutputStream outputStream = Files.newOutputStream(Paths.get("testVoidReturnFunction.ttl"));
        String methodURI = DescriptionGenerator.generateDescription(model, InternalTestFunctions.class.getMethod("testVoidReturnFunction", Long.class));
        RDFDataMgr.write(outputStream, model, Lang.TURTLE);
        Resource functionResource = model.getResource(methodURI);
        List<?> list = getResourcesFromList(functionResource.getPropertyResourceValue(ResourceFactory.createProperty("https://w3id.org/function/ontology#returns")));
        Assert.assertTrue("expected no output values", list.isEmpty());
    }

    @Test
    public void testNoParameters() throws Exception{
        Model model = ModelFactory.createDefaultModel();
        OutputStream outputStream = Files.newOutputStream(Paths.get("testNoParameterFunction.ttl"));
        String methodURI = DescriptionGenerator.generateDescription(model, InternalTestFunctions.class.getMethod("testNoParameters"));
        RDFDataMgr.write(outputStream, model, Lang.TURTLE);
        Resource functionResource = model.getResource(methodURI);
        List<?> list = getResourcesFromList(functionResource.getPropertyResourceValue(ResourceFactory.createProperty("https://w3id.org/function/ontology#expects")));
        Assert.assertTrue("expected no parameters", list.isEmpty());
    }

    @Test
    public void testMultipleExceptions() throws Exception{
        Model model = ModelFactory.createDefaultModel();
        OutputStream outputStream = Files.newOutputStream(Paths.get("testMultipleExceptionsFunction.ttl"));
        String methodURI = DescriptionGenerator.generateDescription(model, InternalTestFunctions.class.getMethod("testMultipleExceptions"));
        RDFDataMgr.write(outputStream, model, Lang.TURTLE);
        Resource functionResource = model.getResource(methodURI);
        List<?> list = getResourcesFromList(functionResource.getPropertyResourceValue(ResourceFactory.createProperty("https://w3id.org/function/ontology#returns")));
        Assert.assertEquals("expected 2 exceptions", 2, list.size());
    }


    // from FnoFunctionModelProvider
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
    private Optional<Resource> getObjectResource(final Resource subject, final String predicateURI) {
        Property property = ResourceFactory.createProperty(predicateURI);
        Statement statement = subject.getProperty(property);
        if (statement == null) {
            return Optional.empty();
        } else {
            return Optional.of(statement.getObject().asResource());
        }
    }
}
