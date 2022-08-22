package be.ugent.idlab.knows.functions.internalfunctions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * <p>Copyright 2022 IDLab (Ghent University - imec)</p>
 *
 * This class contains functions that are "in the class path", for testing purposes
 *
 * @author Gerald Haesendonck
 */
public class InternalTestFunctions {

    private static final Logger logger = LoggerFactory.getLogger(InternalTestFunctions.class);

    /**
     * Returns the sum of a and b
     * @param a your lucky number
     * @param b my lucky number
     * @return  our lucky number (a + b)
     */
    public static long sum(long a, long b) {
        return a + b;
    }

    /**
     * Returns the length of a raw list, i.e. without type parameter. This is to check if data conversion works without
     * knowing the type of the elements of the list.
     * @param aRawList  The raw list to return the length of
     * @return  The length of the raw list
     */
    public static long rawListLen(final List aRawList) {
        return (long) aRawList.size();
    }
    /**
     * Returns the sum of a and b
     * @param a your lucky number
     * @param b my lucky number
     * @return  our lucky number (a * b)
     */
    public static long multiply(long a, long b) {
        return a * b;
    }

    /**
     * Returns a^b, with b >= 0
     * @param a base
     * @param b exponent, greater than or equal to 0
     * @return a to the power of b
     */
    public static long pow(long a, long b) {
        // uses exponentation by squaring
        if(b<0){
            throw new IllegalStateException("b must be larger than 0");
        }
        if (b == 0) {
            return 1;
        }
        if (b % 2 == 0) {
            return pow(a * a, b / 2);
        }
        return a * pow(a * a, (b - 1) / 2);
    }

    public static Long writeToFile(Object o, String filename){
        try (FileWriter fileWriter = new FileWriter(filename)){
            fileWriter.write(o.toString());
        }
        catch (IOException ioException){
            logger.error("error occurred while writing to file: {}", ioException.toString());
            return 1L;
        }
        return 0L;
    }

    public static void writeToFileNoReturn(Object o, String filename){
        try (FileWriter fileWriter = new FileWriter(filename)){
            fileWriter.write(o.toString());
        }
        catch (IOException ioException){
            logger.error("error occurred while writing to file: {}", ioException.toString());
        }
    }

    public static Object tee(Object object, String filename){
        writeToFile(object, filename);
        return object;
    }

    public static List<?> makeSeq(List<?> objects){
        return objects;
    }

    public static List<?> makeListFromSeq(List<?> objects){
        return objects;
    }

}
