package com.yourorg.api.chains;

import java.util.ArrayList;
import java.util.List;

/**
 * Chain definition for API workflows
 */
public class ChainDefinition {
    private final String name;
    private final List<ChainStep> steps;
    
    public ChainDefinition(String name) {
        this.name = name;
        this.steps = new ArrayList<>();
    }
    
    public ChainDefinition addStep(ChainStep step) {
        this.steps.add(step);
        return this;
    }
    
    public String getName() {
        return name;
    }
    
    public List<ChainStep> getSteps() {
        return new ArrayList<>(steps);
    }
}