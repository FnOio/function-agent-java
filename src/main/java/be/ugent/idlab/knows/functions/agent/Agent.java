package be.ugent.idlab.knows.functions.agent;

/**
 * <p>Copyright 2021 IDLab (Ghent University - imec)</p>
 *
 * @author Gerald Haesendonck
 */
public interface Agent {
    /**
     * Executes the function with a given id and given arguments.
     * @param functionId    The unique identifier of a function.
     * @param arguments     The arguments of the function.
     * @return              The result of executing the function.
     * @throws Exception    Something goes wrong looking up the function or executing it.
     */
    Object execute(final String functionId, final Arguments arguments) throws Exception;

    /**
     * Executes the function with a given id and given arguments in debug mode.
     * @param functionId    The unique identifier of a function.
     * @param arguments     The arguments of the function.
     * @param debug         The flag that indicates debug level. Meaning depends on implementation.
     * @return              The result of executing the function.
     * @throws Exception    Something goes wrong looking up the function or executing it.
     */
    Object execute(final String functionId, final Arguments arguments, boolean debug) throws Exception;

    void executeToFile(final String functionId, final Arguments arguments, final String fileName) throws Exception;

    void executeToFile(final String functionId, final Arguments arguments, final String fileName, boolean debug) throws Exception;
}
