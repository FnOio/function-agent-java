package be.ugent.idlab.knows.functions.agent;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * <p>Copyright 2022 IDLab (Ghent University - imec)</p>
 *
 * @author Gerald Haesendonck
 */
public class ArgumentsTest {

    @Test
    public void test() {
        final Arguments arguments = new Arguments()
                .add("one", 1)
                .add("two", 1)
                .add("two", 2);

        // check number of argument values
        assertEquals(3, arguments.size());

        // check argument names
        final Set<String> names = new HashSet<>();
        names.add("one");
        names.add("two");
        assertEquals(names, arguments.getArgumentNames());

        // check value for "one"
        List<Integer> expectedOneValues = Collections.singletonList(1);
        Collection<Object> oneValues = arguments.get("one");
        assertEquals(expectedOneValues, oneValues);

        // check values for "two"
        List<Integer> expectedTwoValues = new ArrayList<>();
        expectedTwoValues.add(1);
        expectedTwoValues.add(2);
        Collection<Object> twoValues = arguments.get("two");
        assertEquals(expectedTwoValues, twoValues);

        Collection<Object> nonExistingValues = arguments.get("nonExistingParameterName");
        assertEquals(0, nonExistingValues.size());
    }
}
