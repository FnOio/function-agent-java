package be.ugent.idlab.knows.functions.agent;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;

import java.util.Collection;
import java.util.Set;

/**
 * Used to pass arguments to a function agent.
 *
 * <p>Copyright 2022 IDLab (Ghent University - imec)</p>
 *
 * @author Gerald Haesendonck
 */
public class Arguments {
    private final MultiValuedMap<String, Object> nameToValueMap = new ArrayListValuedHashMap<>();
    private int count = 0;

    /**
     * Adds an argument name and value to the arguments. Note that parameters with the same name can be added more than
     * once, which translates to a collection of values for that argument.
     * @param name  The name of the argument.
     * @param value The value of the argument.
     * @return  This arguments object with the latest argument added. Use to chain add calls.
     */
    public Arguments add(final String name, final Object value) {
        nameToValueMap.put(name, value);
        ++count;
        return this;
    }

    /**
     * Get the values associated with the name of an argument.
     * @param name  The name of the argument
     * @return      A collection of values for this argument, or an empty collection if the name is unknown.
     */
    public Collection<Object> get(final String name) {
        return nameToValueMap.get(name);
    }

    /**
     * The total number of parameter values. Multiple values for the same argument count.
     * @return  The total number of parameter values.
     */
    public int size() {
        return count;
    }

    /**
     * Gets the current argument names.
     * @return  A set of argument names.
     */
    public Set<String> getArgumentNames() {
        return nameToValueMap.keySet();
    }

    @Override
    public String toString() {
        final StringBuilder str = new StringBuilder();
        nameToValueMap.entries().forEach(entry -> {
            str
                    .append("('")
                    .append(entry.getKey())
                    .append("' -> '")
                    .append(entry.getValue().toString())
                    .append("')");
        });
        return str.toString();
    }
}
