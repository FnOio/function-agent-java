package be.ugent.idlab.knows.functions.agent.functionIntantiation.exception;

/**
 * Thrown when no {@link be.ugent.idlab.knows.functions.agent.model.Function} is found for a given function ID.
 * <p>Copyright 2022 IDLab (Ghent University - imec)</p>
 *
 * @author Gerald Haesendonck
 */
public class FunctionNotFoundException extends InstantiationException {
    public FunctionNotFoundException(String message) {
        super(message);
    }
}
