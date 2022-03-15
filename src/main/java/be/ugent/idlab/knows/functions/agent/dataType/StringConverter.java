package be.ugent.idlab.knows.functions.agent.dataType;

/**
 * <p>Copyright 2022 IDLab (Ghent University - imec)</p>
 *
 * @author Gerald Haesendonck
 */
public class StringConverter extends DataTypeConverter<String> {
    public StringConverter() {
        super(String.class, TypeCategory.PRIMITIVE);
    }

    @Override
    public String convert(Object value) {
        return value.toString();
    }
}
