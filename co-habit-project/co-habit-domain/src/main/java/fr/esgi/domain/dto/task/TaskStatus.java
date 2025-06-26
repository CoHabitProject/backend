package fr.esgi.domain.dto.task;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Statut de la tâche")
public enum TaskStatus {
    PENDING("En attente"),
    IN_PROGRESS("En cours"),
    COMPLETED("Terminée"),
    CANCELLED("Annulée");
    
    private final String value;
    
    TaskStatus(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
}
