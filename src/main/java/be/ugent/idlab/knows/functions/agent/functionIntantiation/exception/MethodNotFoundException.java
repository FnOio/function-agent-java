package be.ugent.idlab.knows.functions.agent.functionIntantiation.exception;

/**
 * Thrown when a method is not found in a certain class.
 *
 * <p>Copyright 2022 IDLab (Ghent University - imec)</p>
 *
 * @author Gerald Haesendonck
 */
public class MethodNotFoundException extends ReflectionException {

    public MethodNotFoundException(String message) {
        super(message);
    }
}
