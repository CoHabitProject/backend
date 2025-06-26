package fr.esgi.domain.dto.task;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Priorité de la tâche")
public enum TaskPriority {
    LOW("Faible"),
    MEDIUM("Moyenne"),
    HIGH("Élevée"),
    URGENT("Urgente");
    
    private final String value;
    
    TaskPriority(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
}
