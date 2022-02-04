package be.ugent.idlab.knows.functions.agent.exceptions.fno;

/**
 * Thrown when parsing the FnO document fails.
 *
 * <p>Copyright 2022 IDLab (Ghent University - imec)</p>
 *
 * @author Gerald Haesendonck
 */
public abstract class FnOException extends Exception {
    public FnOException(String message) {
        super(message);
    }
}
