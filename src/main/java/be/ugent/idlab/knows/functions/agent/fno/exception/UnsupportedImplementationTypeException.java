package be.ugent.idlab.knows.functions.agent.fno.exception;

/**
 * Thrown when a certain implementation type is not supported by this library.
 *
 * <p>Copyright 2022 IDLab (Ghent University - imec)</p>
 *
 * @author Gerald Haesendonck
 */
public class UnsupportedImplementationTypeException extends FnOException {
    public UnsupportedImplementationTypeException(String message) {
        super(message);
    }
}
