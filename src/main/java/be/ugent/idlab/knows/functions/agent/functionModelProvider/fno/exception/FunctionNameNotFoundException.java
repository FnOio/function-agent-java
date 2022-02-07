package be.ugent.idlab.knows.functions.agent.functionModelProvider.fno.exception;

/**
 * Thrown when the name of a function is not found in the function description.
 *
 * <p>Copyright 2022 IDLab (Ghent University - imec)</p>
 *
 * @author Gerald Haesendonck
 */
public class FunctionNameNotFoundException extends FnOException {
    public FunctionNameNotFoundException(String message) {
        super(message);
    }
}
