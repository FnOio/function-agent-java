package be.ugent.idlab.knows.functions.agent;

import java.util.Map;

/**
 * <p>Copyright 2021 IDLab (Ghent University - imec)</p>
 *
 * @author Gerald Haesendonck
 */
public interface Agent {
    /**
     *
     * @param functionTerm
     * @param args
     */
    Object execute(final String functionTerm, final Map<String, Object> args);
}
