package be.ugent.idlab.knows.functions.agent;

import be.ugent.idlab.knows.functions.internalfunctions.InternalTestFunctions;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.junit.Assert;
import org.junit.Test;

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

public class GeneratorTest {
    @Test
    public void testCreationWithLink() throws Exception{
        Model model = ModelFactory.createDefaultModel();

        OutputStream outputStream = Files.newOutputStream(Paths.get("testCreationWithLink.ttl"));
        DescriptionGenerator.generateDescription(model, InternalTestFunctions.class.getMethods()[0]);
        RDFDataMgr.write(outputStream, model, Lang.TURTLE);
        outputStream.flush();
        outputStream.close();
        Agent agent = AgentFactory.createFromFnO("testCreationWithLink.ttl");
        Arguments arguments = new Arguments();
        arguments.add("https://w3id.org/function/vocabulary/predicates#arg0", "2");
        arguments.add("https://w3id.org/function/vocabulary/predicates#arg1", "6");
        Object result = agent.execute("https://w3id.org/function/ontology#be.ugent.idlab.knows.functions.internalfunctions.InternalTestFunctions.pow",arguments);
        Assert.assertEquals("2^6 = 64", 64L, result);

    }
}
