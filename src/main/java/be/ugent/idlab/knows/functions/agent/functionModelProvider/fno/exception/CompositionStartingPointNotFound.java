package be.ugent.idlab.knows.functions.agent.functionModelProvider.fno.exception;

/**
 * Thrown when fnoc:mapFrom or fnoc:MapFromTerm of the Function Composition is not found in the mapping description
 */
public class CompositionStartingPointNotFound extends FunctionCompositionException {
    public CompositionStartingPointNotFound(String message) {
        super(message);
    }
}
