package be.ugent.idlab.knows.functions.agent.functionModelProvider.fno.exception;

/**
 * Thrown when no fno:Mapping is found for a function.
 *
 * <p>Copyright 2022 IDLab (Ghent University - imec)</p>
 *
 * @author Gerald Haesendonck
 */
public class FunctionMappingNotFoundException extends FnOException {

    public FunctionMappingNotFoundException(String message) {
        super(message);
    }
}
