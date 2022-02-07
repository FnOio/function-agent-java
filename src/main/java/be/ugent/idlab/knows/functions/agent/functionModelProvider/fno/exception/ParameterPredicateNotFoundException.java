package be.ugent.idlab.knows.functions.agent.functionModelProvider.fno.exception;

/**
 * Thrown when the description of the predicate used in an (RML) mapping file is not found
 * for a given parameter.
 *
 * <p>Copyright 2022 IDLab (Ghent University - imec)</p>
 *
 * @author Gerald Haesendonck
 */
public class ParameterPredicateNotFoundException extends FnOException {
    public ParameterPredicateNotFoundException(String message) {
        super(message);
    }
}
