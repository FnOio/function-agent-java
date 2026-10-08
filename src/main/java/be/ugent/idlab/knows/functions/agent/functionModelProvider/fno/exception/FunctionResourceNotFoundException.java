package be.ugent.idlab.knows.functions.agent.functionModelProvider.fno.exception;

/**
 * Thrown when a function resource that an FnO description refers to is not found, such as the object of
 * fno:function in an fno:Mapping or the function that a partial application applies.
 *
 * <p>Copyright 2022 IDLab (Ghent University - imec)</p>
 *
 * @author Gerald Haesendonck
 */
public class FunctionResourceNotFoundException extends FnOException {
    public FunctionResourceNotFoundException(String message) {
        super(message);
    }
}
