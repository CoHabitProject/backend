package fr.esgi.domain.dto.task;

public enum TaskStatus {
    TODO("À faire"),
    PENDING("En attente"),
    IN_PROGRESS("En cours"),
    COMPLETED("Terminée"),
    CANCELLED("Annulée");
    
    private final String frenchLabel;
    
    TaskStatus(String frenchLabel) {
        this.frenchLabel = frenchLabel;
    }
    
    public String getFrenchLabel() {
        return frenchLabel;
    }
}
