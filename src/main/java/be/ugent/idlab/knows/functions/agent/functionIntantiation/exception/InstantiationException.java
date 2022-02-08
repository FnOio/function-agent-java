package be.ugent.idlab.knows.functions.agent.functionIntantiation.exception;

/**
 * Thrown if something goes wrong instantiating the necessary classes to execute a function
 *
 * <p>Copyright 2022 IDLab (Ghent University - imec)</p>
 *
 * @author Gerald Haesendonck
 */
public abstract class InstantiationException extends Exception {
    public InstantiationException(String message) {
        super(message);
    }
}
