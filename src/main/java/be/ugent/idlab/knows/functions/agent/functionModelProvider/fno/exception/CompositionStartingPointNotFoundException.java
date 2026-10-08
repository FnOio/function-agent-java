package be.ugent.idlab.knows.functions.agent.functionModelProvider.fno.exception;

/**
 * Thrown when fnoc:mapFrom or fnoc:MapFromTerm of the Function Composition is not found in the mapping description
 */
public class CompositionStartingPointNotFoundException extends FunctionCompositionException {
    public CompositionStartingPointNotFoundException(String message) {
        super(message);
    }
}
