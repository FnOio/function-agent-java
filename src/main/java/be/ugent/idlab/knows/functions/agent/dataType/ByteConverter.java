package be.ugent.idlab.knows.functions.agent.dataType;

/**
 * <p>Copyright 2022 IDLab (Ghent University - imec)</p>
 *
 * @author Gerald Haesendonck
 */
public class ByteConverter extends DataTypeConverter<Byte> {

    public ByteConverter() {
        super(Byte.class, TypeCategory.PRIMITIVE);
    }
    @Override
    public Byte convert(Object value) throws DataTypeConverterException {
        if (value instanceof Byte) {
            return (Byte) value;
        } else {
            return Byte.parseByte(value.toString());
        }
    }
}
