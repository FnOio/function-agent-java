package be.ugent.idlab.knows.functions.agent.functionInstantiation.exception;

/**
 * Thrown when there is a cycle in the Composition mappings of a function composition
 *
 */
public class CyclicDependencyException extends FunctionInstantiationException{
    public CyclicDependencyException(String message) {
        super(message);
    }
}
