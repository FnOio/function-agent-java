package be.ugent.idlab.knows.functions.agent.dataType;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>Copyright 2022 IDLab (Ghent University - imec)</p>
 *
 * @author Gerald Haesendonck
 */
public class DefaultDataTypeConverter extends DataTypeConverter<Object> {
    private final Logger logger = LoggerFactory.getLogger(DefaultDataTypeConverter.class);

    protected DefaultDataTypeConverter() {
        super(Object.class, TypeCategory.OBJECT);
    }

    @Override
    public Object convert(Object value) {
        logger.warn("No DataTypeConverter found for class '{}'; passing the object through", value.getClass().getName());
        return value;
    }
}
