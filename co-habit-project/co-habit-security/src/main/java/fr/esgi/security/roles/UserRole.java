package fr.esgi.security.roles;

public enum UserRole {
    USR1("Utilisateur standard"),
    USR2("Utilisateur avec droits de gestion"),
    ADM1("Administrateur");

    private final String description;

    UserRole(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

