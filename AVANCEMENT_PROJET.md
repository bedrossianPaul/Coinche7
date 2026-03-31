# Avancement du projet Coinche7

_Date de mise à jour : 31/03/2026_

_Autheurs : MOUSSU Eloi, BEDROSSIAN Paul_

## Description du projet

Il s'agit d'une application web de jeu de cartes en ligne, inspirée du jeu de la coinche. 
Le projet est développé en utilisant une architecture full-stack avec un backend Java Spring Boot et un frontend Vue.js, 
le tout orchestré via Docker pour faciliter le développement et le déploiement.

## État d’avancement

Le projet est en place avec une première architecture full-stack fonctionnelle :

- **Infrastructure Docker configurée** avec 3 conteneurs :
  - `frontend` (application Vue en mode développement via Vite)
  - `backend` (API Spring Boot lancée via Maven)
  - `db` (base de données MySQL)
- **Système de connexion implémenté** côté backend avec **authentification par token**.
- **Connexion à la base de données MySQL** opérationnelle, avec des entités de base créées (Utilisateur, Jetons).
- **Endpoints REST de base** pour la gestion des utilisateurs (inscription, connexion).
- **Frontend configuré** pour communiquer avec le backend via Axios, avec une page de connexion fonctionnelle.
- **Première ébauche du front de la table de jeu** réalisée (composants de base pour l’interface de partie).

## Frameworks et technologies utilisés

### Conteneurisation / Orchestration
- **Docker**
- **Docker Compose**

### Backend
- **Java 21**
- **Spring Boot 3.4.3**
- **Spring Web** (API REST)
- **Spring Data JPA** (accès données)
- **MySQL Connector/J**
- **Maven**

### Base de données
- **MySQL 8.4**

### Frontend
- **Vue.js 3**
- **Vite 6**
- **Vue Router 4**
- **Axios**
- **PrimeVue 4** + **PrimeIcons** + **@primeuix/themes**
- **Tailwind CSS 4** (via plugin Vite)

## Résumé

La base du projet est en place : environnement Docker multi-services, backend connecté à la base de données, et une base d’interface front pour la table de jeu. 
Le projet est prêt pour la suite des développements fonctionnels (règles de jeu, logique de manche, interactions temps réel, etc.).
