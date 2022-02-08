package be.ugent.idlab.knows.functions.agent.functionIntantiation.exception;

/**
 * Thrown when no class can be found for a given class ID
 *
 * <p>Copyright 2022 IDLab (Ghent University - imec)</p>
 *
 * @author Gerald Haesendonck
 */
public class ClassNotFoundException extends InstantiationException {
    public ClassNotFoundException(String message) {
        super(message);
    }
}
