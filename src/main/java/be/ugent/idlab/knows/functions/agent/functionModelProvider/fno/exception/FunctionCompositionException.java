package be.ugent.idlab.knows.functions.agent.functionModelProvider.fno.exception;

/**
 * Thrown when something goes wrong when parsing a function composition
 *
 */
public class FunctionCompositionException extends FnOException{
    public FunctionCompositionException(String message) {
        super(message);
    }
}
