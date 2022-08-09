package be.ugent.idlab.knows.functions.agent.functionModelProvider.fno.exception;

/**
 * Thrown when the constituent function of the Function Composition is not found in the mapping description
 */
public class ConstituentFunctionNotFoundException extends FunctionCompositionException{
    public ConstituentFunctionNotFoundException(String message) {
        super(message);
    }
}
