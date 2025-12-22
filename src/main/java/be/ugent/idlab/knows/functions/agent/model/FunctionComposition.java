package be.ugent.idlab.knows.functions.agent.model;

import java.util.ArrayList;
import java.util.List;

public class FunctionComposition {
    // unique identifier of the function of this composition
    private String functionId;
    private final List<CompositionMappingElement> mappings = new ArrayList<>();

    public boolean addMapping(CompositionMappingElement point){
        return mappings.add(point);
    }

    public void setFunctionId(String functionId) {
        this.functionId = functionId;
    }

    public String getFunctionId() {
        return functionId;
    }

    public List<CompositionMappingElement> getMappings() {
        return mappings;
    }
}
