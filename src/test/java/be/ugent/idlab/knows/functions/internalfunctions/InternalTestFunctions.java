package be.ugent.idlab.knows.functions.internalfunctions;

import java.util.List;

/**
 * <p>Copyright 2022 IDLab (Ghent University - imec)</p>
 *
 * This class contains functions that are "in the class path", for testing purposes
 *
 * @author Gerald Haesendonck
 */
public class InternalTestFunctions {

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
}
