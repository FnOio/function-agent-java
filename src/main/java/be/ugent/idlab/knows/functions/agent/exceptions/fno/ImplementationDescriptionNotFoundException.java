package be.ugent.idlab.knows.functions.agent.exceptions.fno;

/**
 * Thrown when the description of an implementation lacks in an FnO document.
 *
 * <p>Copyright 2022 IDLab (Ghent University - imec)</p>
 *
 * @author Gerald Haesendonck
 */
public class ImplementationDescriptionNotFoundException extends FnOException {
    public ImplementationDescriptionNotFoundException(String message) {
        super(message);
    }
}
