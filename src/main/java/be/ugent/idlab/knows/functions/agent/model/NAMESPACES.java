package be.ugent.idlab.knows.functions.agent.model;

/**
 * <p>Copyright 2022 IDLab (Ghent University - imec)</p>
 *
 * @author Gerald Haesendonck
 */
public enum NAMESPACES {
    DCTERMS("http://purl.org/dc/terms"),
    DOAP("http://usefulinc.com/ns/doap#"),
    FNO("https://w3id.org/function/ontology#"),
    FNOI("https://w3id.org/function/vocabulary/implementation#"),
    FNOM("https://w3id.org/function/vocabulary/mapping#"),
    LIB("http://example.com/library#"),
    RDF("http://www.w3.org/1999/02/22-rdf-syntax-ns#"),
    ;
    private final String uri;

    NAMESPACES(String uri) {
        this.uri = uri;
    }


    @Override
    public String toString() {
        return uri;
    }
}
