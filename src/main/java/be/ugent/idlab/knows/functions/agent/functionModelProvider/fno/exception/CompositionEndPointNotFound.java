package be.ugent.idlab.knows.functions.agent.functionModelProvider.fno.exception;
/**
 * Thrown when fnoc:mapTo of the Function Composition is not found in the mapping description
 */
public class CompositionEndPointNotFound extends FunctionCompositionException{
    public CompositionEndPointNotFound(String message) {
        super(message);
    }
}
