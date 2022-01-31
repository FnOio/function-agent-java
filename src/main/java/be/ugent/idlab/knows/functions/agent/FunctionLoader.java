package be.ugent.idlab.knows.functions.agent;

import be.ugent.idlab.knows.functions.model.Parameter;
import be.ugent.idlab.knows.functions.util.JavaUtils;
import org.apache.jena.rdf.model.*;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RiotNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static be.ugent.idlab.knows.functions.model.NAMESPACES.FNO;
import static be.ugent.idlab.knows.functions.model.NAMESPACES.RDF;

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
            Property typeProperty = functionDescriptionTriples.getProperty(RDF.toString(), "type");
            Resource functionObject = ResourceFactory.createResource(FNO + "Function");
            ResIterator functions = functionDescriptionTriples.listSubjectsWithProperty(typeProperty, functionObject);
            while (functions.hasNext()) {
                parseFunction(functions.nextResource());
            }
            System.out.println();
        } catch (RiotNotFoundException rnf) {
            logger.error("Reading function descriptions from {} failed.", fnoDoc, rnf);
        } catch (ClassNotFoundException e) {
            logger.error("Parsing function descriptions from {} failed.", fnoDoc, e);
        }

    }

    private void parseFunction (final Resource functionResource) throws ClassNotFoundException {
        String functionURI = functionResource.getURI();
        if (!loadedFunctionURIs.contains(functionURI)) {
            logger.debug("Parsing new function {}", functionURI);

            // get name
            String name = getLiteralStr(functionResource, FNO + "name");

            // get expected parameters
            List<Parameter> expects = parseParameters(functionResource, true);

            // get outputs
            List<Parameter> returns = parseParameters(functionResource, false);

            StmtIterator properties = functionResource.listProperties();

            // TODO: function mappings

        }
    }

    private List<Parameter> parseParameters(final Resource functionResource, boolean input) throws ClassNotFoundException {
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

    private Parameter parseParameter(final Resource parameterResource) throws ClassNotFoundException {
        String uri = parameterResource.getURI();
        logger.debug("Parsing parameter {}", uri);
        String typeURI = getObjectURI(parameterResource, FNO + "type");
        Class<?> typeClass = JavaUtils.getParamType(typeURI);
        String name = getLiteralStr(parameterResource, FNO + "name");
        String predicateURI = getObjectURI(parameterResource, FNO + "predicate");
        Parameter parameter = new Parameter(uri, name, predicateURI, typeClass);
        if (parameterResource.hasProperty(ResourceFactory.createProperty(FNO + "required"))) {
            boolean isRequired = getLiteralBoolean(parameterResource, FNO + "required");
            parameter.setRequired(isRequired);
        }
        return parameter;
    }

    private Literal getLiteral(final Resource subject, final String predicateURI) {
        return subject.getProperty(functionDescriptionTriples.getProperty(predicateURI)).getObject().asLiteral();
    }

    private String getLiteralStr(final Resource subject, final String predicateURI) {
        return getLiteral(subject, predicateURI).getString();
    }

    private boolean getLiteralBoolean(final Resource subject, final String predicateURI) {
        return getLiteral(subject, predicateURI).getBoolean();
    }

    private String getObjectURI(final Resource subject, final String predicateURI) {
        return subject.getProperty(functionDescriptionTriples.getProperty(predicateURI)).getObject().asResource().getURI();
    }

    private List<Resource> getResourcesFromList(final Resource listResource) {
        List<Resource> resources = new ArrayList<>();

        // only proceed if the list is not exhausted
        if (!listResource.hasURI(RDF + "nil")) {
            // add 'first' resource of list
            Resource firstResource = getObjectResourceFromProperty(listResource, RDF + "first");
            if (firstResource != null) {
                resources.add(firstResource);

                // process the 'rest' of the list
                Resource restResource = getObjectResourceFromProperty(listResource, RDF + "rest");
                if (restResource != null) {
                    resources.addAll(getResourcesFromList(restResource));
                }
            }
        }
        return resources;
    }

    private Resource getObjectResourceFromProperty(final Resource subject, final String property) {
        StmtIterator stmtIter = subject.listProperties(functionDescriptionTriples.getProperty(property));
        if (stmtIter.hasNext()) {
            Statement statement = stmtIter.nextStatement();
            RDFNode objectResource = statement.getObject();
            return objectResource.asResource();
        } else {
            return null;
        }
    }
    
}
