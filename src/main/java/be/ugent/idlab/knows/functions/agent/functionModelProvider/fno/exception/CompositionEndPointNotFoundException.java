package be.ugent.idlab.knows.functions.agent.functionModelProvider.fno.exception;
/**
 * Thrown when fnoc:mapTo of the Function Composition is not found in the mapping description
 */
public class CompositionEndPointNotFoundException extends FunctionCompositionException{
    public CompositionEndPointNotFoundException(String message) {
        super(message);
    }
}
