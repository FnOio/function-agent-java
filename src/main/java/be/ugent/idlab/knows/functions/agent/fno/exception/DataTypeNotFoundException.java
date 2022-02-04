package be.ugent.idlab.knows.functions.agent.fno.exception;

/**
 * Thrown when a required data type description is not found.
 *
 * <p>Copyright 2022 IDLab (Ghent University - imec)</p>
 *
 * @author Gerald Haesendonck
 */
public class DataTypeNotFoundException extends FnOException {
    public DataTypeNotFoundException(String message) {
        super(message);
    }
}
