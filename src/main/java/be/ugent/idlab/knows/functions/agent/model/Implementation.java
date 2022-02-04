package be.ugent.idlab.knows.functions.agent.model;

import lombok.Data;

/**
 * <p>Copyright 2022 IDLab (Ghent University - imec)</p>
 *
 * @author Gerald Haesendonck
 */
@Data
public class Implementation {
    private final String implementationUri;
    private final String className;
    private final String downloadPage;
}
