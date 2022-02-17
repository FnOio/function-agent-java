package be.ugent.idlab.knows.functions.agent.dataType;

import java.util.Arrays;
import java.util.List;

/**
 * <p>Copyright 2022 IDLab (Ghent University - imec)</p>
 *
 * @author Gerald Haesendonck
 */
public class StringConverter implements DataTypeConverter<String> {
    @Override
    public String convert(Object value) {
        return value.toString();
    }

    @Override
    public List<Class<?>> getTypeClasses() {
        return Arrays.asList(String.class, CharSequence.class);
    }
}
