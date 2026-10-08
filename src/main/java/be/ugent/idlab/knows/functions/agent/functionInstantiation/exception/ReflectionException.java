package be.ugent.idlab.knows.functions.agent.functionInstantiation.exception;

/**
 * Something goes wrong instantiating classes or methods using reflection
 * <p>Copyright 2022 IDLab (Ghent University - imec)</p>
 *
 * @author Gerald Haesendonck
 */
public class ReflectionException extends FunctionInstantiationException {
    public ReflectionException(String message) {
        super(message);
    }

}
