# Getting Started

### Reference Documentation
For further reference, please consider the following sections:

* [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)
* [Spring Boot Maven Plugin Reference Guide](https://docs.spring.io/spring-boot/3.4.5/maven-plugin)
* [Create an OCI image](https://docs.spring.io/spring-boot/3.4.5/maven-plugin/build-image.html)
* [Spring Web](https://docs.spring.io/spring-boot/3.4.5/reference/web/servlet.html)

### Guides
The following guides illustrate how to use some features concretely:

* [Building a RESTful Web Service](https://spring.io/guides/gs/rest-service/)
* [Serving Web Content with Spring MVC](https://spring.io/guides/gs/serving-web-content/)
* [Building REST services with Spring](https://spring.io/guides/tutorials/rest/)

## Configuration Keycloak et Docker

### Modifier la durée de validité des tokens

Pour modifier la durée de validité des tokens d'accès et de refresh dans Keycloak :

1. **Éditer le fichier de configuration du realm** :
   - Ouvrir le fichier `keycloak/co-habit-keycloak-realm.json`
   - Modifier les valeurs suivantes :
     - `accessTokenLifespan` : durée en secondes du token d'accès (défaut: 300s = 5min)
     - `ssoSessionIdleTimeout` : durée en secondes du refresh token (défaut: 1800s = 30min)

2. **Exemple de modification** :
   ```json
   "accessTokenLifespan": 1500,        // 25 minutes au lieu de 5
   "ssoSessionIdleTimeout": 9000,      // 150 minutes au lieu de 30
   ```

3. **Redémarrer Keycloak** pour appliquer les changements :
   ```bash
   docker-compose down -v
   docker-compose up -d keycloak-local
   ```

### Rebuilder les volumes Docker

Pour reconstruire complètement les volumes Docker (utile en cas de problème de configuration) :

1. **Arrêter tous les services** :
   ```bash
   docker-compose down
   ```

2. **Supprimer les volumes existants** :
   ```bash
   docker volume rm keycloak_postgres_data
   docker volume rm elasticsearch_data
   ```

3. **Redémarrer les services** (les volumes seront recréés automatiquement) :
   ```bash
   docker-compose up -d
   ```

4. **Alternative : Forcer la reconstruction complète** :
   ```bash
   docker-compose down -v  # Supprime les volumes
   docker-compose up -d --force-recreate
   ```

### Commandes utiles

- **Voir les logs Keycloak** : `docker logs co-habit-local.openid-provider`
- **Voir les logs PostgreSQL** : `docker logs keycloak-postgres-local`
- **Accès admin Keycloak** : http://localhost:8088 (admin/admin)
- **Accès Kibana** : http://localhost:5601

### Maven Parent overrides

Due to Maven's design, elements are inherited from the parent POM to the project POM.
While most of the inheritance is fine, it also inherits unwanted elements like `<license>` and `<developers>` from the parent.
To prevent this, the project POM contains empty overrides for these elements.
If you manually switch to a different parent and actually want the inheritance, you need to remove those overrides.

