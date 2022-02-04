package be.ugent.idlab.knows.functions.agent.fno.exception;

/**
 * Thrown when a method mapping has no fnom:method-name.
 *
 * <p>Copyright 2022 IDLab (Ghent University - imec)</p>
 *
 * @author Gerald Haesendonck
 */
public class MethodNameNotFoundException extends FnOException {
    public MethodNameNotFoundException(String message) {
        super(message);
    }
}
