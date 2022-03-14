package be.ugent.idlab.knows.functions.agent.functionModelProvider.fno.exception;

/**
 * Thrown when an FnO document could not be found at the given path.
 *
 * <p>Copyright 2022 IDLab (Ghent University - imec)</p>
 *
 * @author Gerald Haesendonck
 */
public class FnODocNotFoundException extends FnOException {

    public FnODocNotFoundException(String message) {
        super(message);
    }
}
