package be.ugent.idlab.knows.functions.agent.dataType;

/**
 * <p>Copyright 2022 IDLab (Ghent University - imec)</p>
 *
 * @author Gerald Haesendonck
 */
public class CharacterConverter extends DataTypeConverter<Character> {

    public CharacterConverter() {
        super(Character.class, TypeCategory.PRIMITIVE);
    }

    @Override
    public Character convert(Object value) throws DataTypeConverterException {
        if (value instanceof Character) {
            return (Character) value;
        } else {
            return value.toString().charAt(0);
        }
    }
}
