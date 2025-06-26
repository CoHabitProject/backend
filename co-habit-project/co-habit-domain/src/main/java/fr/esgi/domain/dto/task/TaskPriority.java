package fr.esgi.domain.dto.task;

public enum TaskPriority {
    LOW("Faible"),
    MEDIUM("Moyenne"),
    HIGH("Élevée"),
    URGENT("Urgent");
    
    private final String frenchLabel;
    
    TaskPriority(String frenchLabel) {
        this.frenchLabel = frenchLabel;
    }
    
    public String getFrenchLabel() {
        return frenchLabel;
    }
}
