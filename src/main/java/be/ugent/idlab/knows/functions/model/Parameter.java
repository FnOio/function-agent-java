package be.ugent.idlab.knows.functions.model;

import lombok.Data;

/**
 * <p>Copyright 2022 IDLab (Ghent University - imec)</p>
 *
 * @author Gerald Haesendonck
 */
@Data
public class Parameter {
    private final String uri;
    private final String name;
    private final String predicateUri;
    private final Class<?> type;
    private boolean isRequired = true;
}
