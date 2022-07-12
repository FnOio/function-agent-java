package be.ugent.idlab.knows.functions.agent.functionIntantiation.exception;

/**
 * Thrown when trying to construct a lambda of a function that is not a composition
 *
 */
public class NotACompositeFunctionException extends InstantiationException{
    public NotACompositeFunctionException(String message) {
        super(message);
    }
}
