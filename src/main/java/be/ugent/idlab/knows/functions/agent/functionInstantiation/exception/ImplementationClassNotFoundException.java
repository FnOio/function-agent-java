package be.ugent.idlab.knows.functions.agent.functionInstantiation.exception;

/**
 * Thrown when no class can be found for a given class ID
 *
 * <p>Copyright 2022 IDLab (Ghent University - imec)</p>
 *
 * @author Gerald Haesendonck
 */
public class ImplementationClassNotFoundException extends FunctionInstantiationException {
    public ImplementationClassNotFoundException(String message) {
        super(message);
    }
}
