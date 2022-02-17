package be.ugent.idlab.knows.functions.agent.dataType;

import java.util.Arrays;
import java.util.List;

/**
 * <p>Copyright 2022 IDLab (Ghent University - imec)</p>
 *
 * @author Gerald Haesendonck
 */
public class IntegerConverter implements DataTypeConverter<Integer> {
    @Override
    public Integer convert(Object value) {
        if (value instanceof Integer) {
            return  (Integer)value;
        }
        if (value instanceof String){
            return Integer.parseInt(value.toString());
        }
        return null;
    }

    @Override
    public List<Class<?>> getTypeClasses() {
        return Arrays.asList(Integer.class, int.class);
    }

}
