package be.ugent.idlab.knows.functions.agent.dataType;

import be.ugent.idlab.knows.functions.agent.functionModelProvider.fno.exception.UnsupportedDataTypeException;
import org.junit.Test;

import java.io.StringWriter;
import java.nio.Buffer;
import java.nio.CharBuffer;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * <p>Copyright 2022 IDLab (Ghent University - imec)</p>
 *
 * @author Gerald Haesendonck
 */
public class DataTypeConverterTest {
    private final DataTypeConverterProvider converterProvider = new DataTypeConverterProvider();

    @Test
    public void testRDFListConverterFromList() throws UnsupportedDataTypeException, DataTypeConverterException {
        DataTypeConverter<?> listConverter = converterProvider.getDataTypeConverter("http://www.w3.org/1999/02/22-rdf-syntax-ns#List");
        List<String> testList = List.of("One", "Two", "Three");
        Object result = listConverter.convert(testList);
        assertEquals("Lists are not the same", testList, result);
    }

    @Test
    public void testRDFListConverterFromArray() throws UnsupportedDataTypeException, DataTypeConverterException {
        DataTypeConverter<?> listConverter = converterProvider.getDataTypeConverter("http://www.w3.org/1999/02/22-rdf-syntax-ns#List");
        String[] testArray = new String[]{"One", "Two", "Three"};
        Object result = listConverter.convert(testArray);
        List<String> testList = List.of("One", "Two", "Three");
        assertEquals("Arrays are not the same", testList, result);
    }

    @Test
    public void testAbstractDataTypeConverter() {
        DataTypeConverter<CharBuffer> at = new DataTypeConverter<>(CharBuffer.class) {
            @Override
            public CharBuffer convert(Object value) throws DataTypeConverterException {
                return null;
            }
        };
        assertTrue("Buffer should be a superclass of CharBuffer", at.isSubTypeOf(Buffer.class));

        DataTypeConverter<Appendable> ad = new DataTypeConverter<>(Appendable.class) {
            @Override
            public Appendable convert(Object value) throws DataTypeConverterException {
                return null;
            }
        };
        assertTrue("StringWriter should be a subclass of Appendable", ad.isSuperTypeOf(StringWriter.class));
    }

    @Test
    public void testNumberConverterTypes() {
        DataTypeConverter<Integer> ic = new IntegerConverter();
        assertTrue(ic.isSubTypeOf(int.class));
    }
}
