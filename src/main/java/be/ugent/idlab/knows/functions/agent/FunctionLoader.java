package be.ugent.idlab.knows.functions.agent;

import be.ugent.idlab.knows.functions.model.NAMESPACES;
import org.apache.jena.rdf.model.*;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RiotNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

import static be.ugent.idlab.knows.functions.model.NAMESPACES.*;

/**
 * <p>Copyright 2021 IDLab (Ghent University - imec)</p>
 *
 * @author Gerald Haesendonck
 */
public class FunctionLoader {
    private final static Logger logger = LoggerFactory.getLogger(FunctionLoader.class);
    final Model functionDescriptionTriples = ModelFactory.createDefaultModel();
    final Set<String> loadedFunctionURIs = new HashSet<>();

    public void load(final String fnoDoc) {
        logger.info("Loading function descriptions from {}", fnoDoc);
        try {
            RDFDataMgr.read(functionDescriptionTriples, fnoDoc);
            //System.out.println("functionDescriptionTriples = " + functionDescriptionTriples);
            Property typeProperty = functionDescriptionTriples.getProperty(RDFNS + "type");
            Resource functionObject = ResourceFactory.createResource(FNO + "Function");
            ResIterator functions = functionDescriptionTriples.listSubjectsWithProperty(typeProperty, functionObject);
            while (functions.hasNext()) {
                parseFunction(functions.nextResource());
            }
            System.out.println();
        } catch (RiotNotFoundException rnf) {
            logger.error("Reading function descriptions from {} failed.", fnoDoc, rnf);
        }

    }

    private void parseFunction (final Resource functionResource) {
        String functionURI = functionResource.getURI();
        if (!loadedFunctionURIs.contains(functionURI)) {
            logger.debug("Parsing new function {}", functionURI);

            // get name
            String name = getLiteral(functionResource, FNO + "name");

            // get expected parameters
            parseParameters(functionResource);

            StmtIterator properties = functionResource.listProperties();

        }
    }

    private void parseParameters(final Resource functionResource) {
        logger.debug("Parsing expected parameters of {}", functionResource.getURI());
        Resource expectedResource = functionResource.getPropertyResourceValue(functionDescriptionTriples.getProperty(FNO + "expects"));
        StmtIterator statements = expectedResource.listProperties();
        while (statements.hasNext()) {
            Statement stmt = statements.nextStatement();
            System.out.println("  " + stmt.toString());
        }
        // TODO parse collection
    }

    private String getLiteral(final Resource subject, final String predicateURI) {
        return subject.getProperty(functionDescriptionTriples.getProperty(predicateURI)).getObject().asLiteral().getString();
    }
    
}
