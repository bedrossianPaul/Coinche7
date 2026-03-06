## Coinche7 - Structure Full Stack Containerisée

Ce dépôt contient une base de projet avec :

- Backend : Spring Boot (`backend`)
- Frontend : Vue.js + Vite (`frontend`)
- Base de données : MySQL (`db`)
- Orchestration : Docker Compose

## Arborescence

```
.
├── backend
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/main
│       ├── java/com/example/coinche7
│       │   ├── Coinche7Application.java
│       │   └── controller/HealthController.java
│       └── resources/application.properties
├── frontend
│   ├── Dockerfile
│   ├── nginx.conf
│   ├── package.json
│   ├── vite.config.js
│   ├── index.html
│   └── src
│       ├── App.vue
│       ├── main.js
│       └── style.css
├── docker-compose.yml
└── .env.example
```

## Démarrage

1. Créer un fichier `.env` à partir de `.env.example` (optionnel si les valeurs par défaut conviennent).
2. Lancer la stack :

```bash
docker compose up --build
```

## Services disponibles

- Frontend : http://localhost
- Backend : http://localhost:8080
- Endpoint test backend : http://localhost:8080/api/hello
- MySQL : `localhost:3306`

Depuis le frontend, les appels `/api/*` sont proxyfiés vers le backend via Nginx.
# Coinche7
Projet Application Web - Jeu de coinche en ligne
