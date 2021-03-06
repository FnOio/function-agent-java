package be.ugent.idlab.knows.functions.agent.functionModelProvider.fno.exception;

/**
 * Thrown when the class of a method mapping is not found.
 * <p>Copyright 2022 IDLab (Ghent University - imec)</p>
 *
 * @author Gerald Haesendonck
 */
public class MethodMappingTypeNotFoundException extends FnOException {
    public MethodMappingTypeNotFoundException(String message) {
        super(message);
    }
}
