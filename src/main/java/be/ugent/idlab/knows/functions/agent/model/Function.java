package be.ugent.idlab.knows.functions.agent.model;

import java.util.List;

/**
 * <p>Copyright 2022 IDLab (Ghent University - imec)</p>
 *
 * @author Gerald Haesendonck
 */
public class Function {

    // a unique identifier for the function
    private String id;

    // a name for this function
    private final String name;

    // a description for this function
    private final String description;

    // a list of function arguments
    private final List<Parameter> argumentParameters;

    // a list of returned arguments. This should be only 1 in Java
    private final List<Parameter> returnParameters;

    // a function mapping containing a method name mapping and an implementation
    private FunctionMapping functionMapping;

    private boolean isComposite = false;
    private FunctionComposition functionComposition = null;

    public Function(String id, String name, String description, List<Parameter> argumentParameters, List<Parameter> returnParameters) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.argumentParameters = argumentParameters;
        this.returnParameters = returnParameters;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public List<Parameter> getArgumentParameters() {
        return argumentParameters;
    }

    public List<Parameter> getReturnParameters() {
        return returnParameters;
    }

    public FunctionMapping getFunctionMapping() {
        return functionMapping;
    }

    public boolean isComposite() {
        return isComposite;
    }

    public FunctionComposition getFunctionComposition() {
        return functionComposition;
    }

    public Function setId(String id) {
        this.id = id;
        return this;
    }

    public Function setFunctionMapping(FunctionMapping functionMapping) {
        this.functionMapping = functionMapping;
        return this;
    }

    public Function setComposite(boolean composite) {
        isComposite = composite;
        return this;
    }

    public Function setFunctionComposition(FunctionComposition functionComposition) {
        this.functionComposition = functionComposition;
        return this;
    }

    public Function(Function f) {
        this(f.getId(), f.getName(), f.getDescription(), f.getArgumentParameters(), f.getReturnParameters());
        this.functionMapping = f.getFunctionMapping();
        this.isComposite = f.isComposite();
        this.functionComposition = f.getFunctionComposition();
    }
}
