package be.ugent.idlab.knows.functions.agent.exceptions.fno;

/**
 * Thrown when the parameter name description is not found.
 *
 * <p>Copyright 2022 IDLab (Ghent University - imec)</p>
 *
 * @author Gerald Haesendonck
 */
public class ParameterNameDescriptionNotFoundException extends FnOException {
    public ParameterNameDescriptionNotFoundException(String message) {
        super(message);
    }
}
