package be.ugent.idlab.knows.functions.agent.functionModelProvider.fno.exception;

/**
 * Thrown when a data type is not supported
 * <p>Copyright 2022 IDLab (Ghent University - imec)</p>
 *
 * @author Gerald Haesendonck
 */
public class UnsupportedDataTypeException extends FnOException {

    public UnsupportedDataTypeException(String message) {
        super(message);
    }
}
