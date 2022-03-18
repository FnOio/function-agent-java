package be.ugent.idlab.knows.functions.agent.dataType;

/**
 * <p>Copyright 2022 IDLab (Ghent University - imec)</p>
 *
 * @author Gerald Haesendonck
 */
public class IntegerConverter extends DataTypeConverter<Integer> {
    public IntegerConverter() {
        super(Integer.class, TypeCategory.PRIMITIVE);
    }

    @Override
    public Integer convert(Object value) {
        if (value instanceof Integer) {
            return  (Integer)value;
        } else {
            return Integer.parseInt(value.toString());
        }
    }
}
