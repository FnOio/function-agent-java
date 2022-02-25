package be.ugent.idlab.knows.functions.agent.dataType;

/**
 * Thrown when something goes wrong converting values to data objects.
 *
 * <p>Copyright 2022 IDLab (Ghent University - imec)</p>
 *
 * @author Gerald Haesendonck
 */
public class DataTypeConverterException extends Exception {
    public DataTypeConverterException(String message) {
        super(message);
    }
}
