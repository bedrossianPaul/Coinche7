# Coinche7

Projet Application Web - Jeu de coinche en ligne

**Étudiant : Eloi Moussu & Paul Bedrossian**

## Coinche7 - Structure

Ce dépôt contient une base de projet avec :

- Backend : Spring Boot (`backend`)
- Frontend : Vue.js + Vite (`frontend`)
- Base de données : MySQL (`db`)
- Orchestration : Docker Compose


## Démarrage (mode développement avec hot reload)

1. Créer un fichier `.env` à partir de `.env.example`
2. Lancer la stack :

```bash
docker compose up --build
```

Le hot reload est activé :

- Frontend : Vite en mode dev dans le conteneur (`http://localhost:5173`)
- Backend : Spring Boot via `mvn spring-boot:run` + DevTools (`http://localhost:8080`)
- Base MySQL : `localhost:3306`

## Services disponibles

- Frontend : http://localhost:5173
- Backend : http://localhost:8080
- MySQL : `localhost:3306`