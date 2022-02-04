package be.ugent.idlab.knows.functions.agent.fno.exception;

/**
 * Thrown when the class name description is not found in the implementation description.
 *
 * <p>Copyright 2022 IDLab (Ghent University - imec)</p>
 *
 * @author Gerald Haesendonck
 */
public class ClassNameDescriptionNotFoundException extends FnOException {
    public ClassNameDescriptionNotFoundException(String message) {
        super(message);
    }
}
