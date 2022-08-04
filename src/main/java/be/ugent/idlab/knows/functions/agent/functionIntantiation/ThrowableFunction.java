package be.ugent.idlab.knows.functions.agent.functionIntantiation;

import be.ugent.idlab.knows.functions.agent.Agent;

/**
 * interface to describe a function that takes an {@link Agent} and a number of {@link Object}s and can throw an {@link Exception}
 */
@FunctionalInterface
public interface ThrowableFunction{
    Object apply(Agent agent, Object[] t) throws Exception;
}
