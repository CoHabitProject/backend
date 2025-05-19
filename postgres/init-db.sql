-- Création de l'utilisateur pour co_habit
CREATE USER cohabit WITH PASSWORD 'password';

-- Création de la base de données co_habit
CREATE DATABASE cohabit;

-- Attribution des privilèges
GRANT ALL PRIVILEGES ON DATABASE cohabit TO cohabit;

-- Se connecter à la base cohabit pour définir le propriétaire des schémas
\c cohabit

-- Définir les privilèges pour le schéma public
ALTER SCHEMA public OWNER TO cohabit;
GRANT ALL ON SCHEMA public TO cohabit;