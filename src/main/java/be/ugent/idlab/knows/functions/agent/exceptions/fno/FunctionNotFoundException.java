package be.ugent.idlab.knows.functions.agent.exceptions.fno;

/**
 * Thrown when the object (function resource) for predicate fno:function of a fnoi:Mapping is not found.
 *
 * <p>Copyright 2022 IDLab (Ghent University - imec)</p>
 *
 * @author Gerald Haesendonck
 */
public class FunctionNotFoundException extends FnOException {
    public FunctionNotFoundException(String message) {
        super(message);
    }
}
