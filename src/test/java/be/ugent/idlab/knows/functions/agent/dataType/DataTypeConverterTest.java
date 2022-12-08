package be.ugent.idlab.knows.functions.agent.dataType;


import org.junit.jupiter.api.Test;

import java.io.StringWriter;
import java.nio.Buffer;
import java.nio.CharBuffer;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * <p>Copyright 2022 IDLab (Ghent University - imec)</p>
 *
 * @author Gerald Haesendonck
 */
public class DataTypeConverterTest {
    private final DataTypeConverterProvider converterProvider = new DataTypeConverterProvider();

    @Test
    public void testRDFListConverterFromList() throws DataTypeConverterException {
        DataTypeConverter<?> listConverter = converterProvider.getDataTypeConverter("http://www.w3.org/1999/02/22-rdf-syntax-ns#List");
        List<String> testList = Arrays.asList("One", "Two", "Three");
        Object result = listConverter.convert(testList);
        assertEquals(testList, result, "Lists are not the same");
    }

    @Test
    public void testRDFListConverterFromArray() throws DataTypeConverterException {
        DataTypeConverter<?> listConverter = converterProvider.getDataTypeConverter("http://www.w3.org/1999/02/22-rdf-syntax-ns#List");
        String[] testArray = new String[]{"One", "Two", "Three"};
        Object result = listConverter.convert(testArray);
        List<String> testList = Arrays.asList("One", "Two", "Three");
        assertEquals(testList, result, "Arrays are not the same");
    }

    @Test
    public void testAbstractDataTypeConverter() {
        DataTypeConverter<CharBuffer> at = new DataTypeConverter<>(CharBuffer.class, DataTypeConverter.TypeCategory.PRIMITIVE) {
            @Override
            public CharBuffer convert(Object value) {
                return null;
            }
        };
        assertTrue(at.isSubTypeOf(Buffer.class), "Buffer should be a superclass of CharBuffer");

        DataTypeConverter<Appendable> ad = new DataTypeConverter<>(Appendable.class, DataTypeConverter.TypeCategory.PRIMITIVE) {
            @Override
            public Appendable convert(Object value) {
                return null;
            }
        };
        assertTrue(ad.isSuperTypeOf(StringWriter.class), "StringWriter should be a subclass of Appendable");
    }

    @Test
    public void testNumberConverterTypes() {
        DataTypeConverter<Integer> ic = new IntegerConverter();
        assertTrue(ic.isSubTypeOf(int.class));
    }

    @Test
    public void testListArraySubType() {
        DataTypeConverter<?> listConverter = converterProvider.getDataTypeConverter("http://www.w3.org/1999/02/22-rdf-syntax-ns#List");
        assertTrue(listConverter.isSubTypeOf(Integer[].class));
    }

    @Test
    public void testListArraySuperType() {
        DataTypeConverter<?> listConverter = converterProvider.getDataTypeConverter("http://www.w3.org/1999/02/22-rdf-syntax-ns#List");
        assertTrue(listConverter.isSuperTypeOf(Integer[].class));
    }

    @Test
    public void testArrayConverter() throws DataTypeConverterException {
        ArrayConverter arrayConverter = new ArrayConverter();
        arrayConverter.setArgumentTypeConverter(new IntegerConverter());
        Object[] convertedValues = arrayConverter.convert(new Integer[]{1, 5});
        System.out.println("convertedValues = " + convertedValues.getClass().getTypeName());
        assertEquals("java.lang.Integer[]", convertedValues.getClass().getTypeName());
    }

    @Test
    public void testBooleanSupertypes() {
        DataTypeConverter<?> booleanConverter = converterProvider.getDataTypeConverter("boolean");
        assertTrue(booleanConverter.isSubTypeOf(boolean.class));
        assertFalse(booleanConverter.isSubTypeOf(int.class));
    }

    @Test
    public void testCharacterSupertypes() {
        DataTypeConverter<?> characterConverter = converterProvider.getDataTypeConverter("char");
        assertTrue(characterConverter.isSuperTypeOf(char.class));
        assertFalse(characterConverter.isSuperTypeOf(int.class));
    }

    @Test
    public void testByteConverterCompatibility() {
        DataTypeConverter<?> byteConverter = converterProvider.getDataTypeConverter("byte");
        assertTrue(byteConverter.isSubTypeOf(Short.class));
        assertFalse(byteConverter.isSuperTypeOf(Short.class));
        assertTrue(byteConverter.isSubTypeOf(int.class));
        assertFalse(byteConverter.isSuperTypeOf(Integer.class));
    }

    @Test
    public void testShortConverterCompatibility() {
       DataTypeConverter<?> shortConverter = converterProvider.getDataTypeConverter("short");
       assertTrue(shortConverter.isSuperTypeOf(byte.class));
       assertFalse(shortConverter.isSuperTypeOf(int.class));
       assertTrue(shortConverter.isSubTypeOf(int.class));
    }

}
