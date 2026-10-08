package be.ugent.idlab.knows.functions.agent.functionInstantiation.exception;

/**
 * Thrown if something goes wrong instantiating the necessary classes to execute a function
 *
 * <p>Copyright 2022 IDLab (Ghent University - imec)</p>
 *
 * @author Gerald Haesendonck
 */
public abstract class FunctionInstantiationException extends Exception {
    public FunctionInstantiationException(String message) {
        super(message);
    }
}
