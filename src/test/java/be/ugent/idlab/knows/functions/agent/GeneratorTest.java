package be.ugent.idlab.knows.functions.agent;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.junit.Test;

public class GeneratorTest {
    @Test
    public void testCreation(){
        Model model = ModelFactory.createDefaultModel();
        DescriptionGenerator.generateDescription(model, DescriptionGenerator.class.getMethods()[0]);
        RDFDataMgr.write(System.out, model, Lang.TURTLE);
    }
}
