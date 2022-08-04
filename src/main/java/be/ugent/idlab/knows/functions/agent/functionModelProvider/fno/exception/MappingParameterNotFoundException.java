package be.ugent.idlab.knows.functions.agent.functionModelProvider.fno.exception;

/**
 * Thrown when the parameter or output predicate of the Function Composition is not found in the mapping description
 */
public class MappingParameterNotFoundException extends FunctionCompositionException {
    public MappingParameterNotFoundException(String message) {
        super(message);
    }
}
