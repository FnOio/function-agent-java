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
