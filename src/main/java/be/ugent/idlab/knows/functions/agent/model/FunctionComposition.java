package be.ugent.idlab.knows.functions.agent.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class FunctionComposition {
    // unique identifier of the function of this composition
    private String functionId;
    private final List<CompositionMappingElement> mappings = new ArrayList<>();

    public boolean addMapping(CompositionMappingElement point){
        return mappings.add(point);
    }
}
