# Co'Habit - Backend

## Description

Backend du projet Co'Habit, une application de colocation développée avec Spring Boot utilisant une architecture modulaire hexagonale.

## Architecture

Le projet est structuré en modules Maven suivant une architecture hexagonale :

- **co-habit-application** : Module principal contenant l'application et le point d'entrée
- **co-habit-domain** : Module contenant les entités et règles métier
- **co-habit-persistence** : Module gérant la persistance des données
- **co-habit-rest** : Module exposant les API REST
- **co-habit-security** : Module gérant l'authentification et l'autorisation
- **co-habit-web** : Module gérant l'interface web

## Prérequis

- Java 23
- Maven 3.9+
- Docker et Docker Compose (pour déploiement)

## Installation

### Développement local

1. Cloner le dépôt

   ```
   git clone [URL_DU_DEPOT]
   cd backend
   ```

2. Compiler le projet

   ```
   cd co-habit-project
   mvn clean install
   ```

3. Exécuter l'application
   ```
   cd co-habit-application
   mvn spring-boot:run
   ```

### Docker

Le projet peut être exécuté via Docker Compose :

```
cd backend
docker-compose up -d
```

## Environnement de développement local

Le projet inclut une configuration Docker Compose légère pour le développement local, qui fournit :

- **Keycloak** : Un fournisseur OpenID Connect disponible sur le port 8088
  - Realm "co-habit" préconfiguré avec le client "co-habit-confidential"
  - Identifiants admin : nom d'utilisateur : admin, mot de passe : admin
  - Secret client : "secret"

- **PostgreSQL** : Serveur de base de données sur le port 5432
  - Deux bases de données configurées :
    - `keycloak` : Utilisée par le service Keycloak
    - `cohabit` : Utilisée par l'application
  - Détails de connexion pour l'application :
    - URL : jdbc:postgresql://localhost:5432/cohabit
    - Nom d'utilisateur : cohabit
    - Mot de passe : password

### Démarrer l'environnement

```bash
# Démarrer tous les conteneurs
docker-compose up -d

# Arrêter tous les conteneurs
docker-compose down

# Afficher les logs
docker-compose logs -f
```

## Configuration

Le fichier `application.properties` (ou `application.yml`) se trouve dans le module `co-habit-application/src/main/resources`.

## API Documentation

Une fois l'application démarrée, la documentation de l'API est disponible aux URLs suivantes :

- Swagger UI : http://localhost:8080/swagger-ui/index.html
- API Docs : http://localhost:8080/v3/api-docs

## Sécurité

Le module de sécurité utilise OAuth2 et Keycloak pour l'authentification et l'autorisation.

## Tests

Exécuter les tests unitaires :

```
mvn test
```

## Déploiement

L'application utilise GitHub Actions pour l'intégration et le déploiement continus.

## Contributeurs

- Axel Gallic (Developer)
- Bertrand Renaudin (Principal DevOps)
- Carlos Cerén (Developer)
- Dmitri Chine (Principal developer)

## Licence

[Type de licence]
