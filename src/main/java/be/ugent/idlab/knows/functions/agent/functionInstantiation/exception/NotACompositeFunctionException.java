package be.ugent.idlab.knows.functions.agent.functionInstantiation.exception;

/**
 * Thrown when trying to construct a lambda of a function that is not a composition
 *
 */
public class NotACompositeFunctionException extends FunctionInstantiationException{
    public NotACompositeFunctionException(String message) {
        super(message);
    }
}
