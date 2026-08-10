# TSENA — Frontend

React (Vite) + Tailwind CSS + React Router + Axios + Recharts. Sert d'interface pour l'API Spring Boot du projet.

## Developpement

```
cd frontend
npm install
npm run dev
```

Le serveur de dev tourne sur `http://localhost:5173` et proxy les requetes `/auth`, `/admin`, `/ventes`, `/stock`, `/dashboard`, `/mes-sites` vers le backend Spring Boot sur `http://localhost:8080` (voir `vite.config.js`). Lancer le backend (`./gradlew bootRun`) en parallele.

## Rebuild pour Spring Boot

```
cd frontend
npm run build
```

Cette commande vide et regenere `../src/main/resources/static/` (racine du projet Spring Boot). Une fois le build termine, relancer l'application Spring Boot (`./gradlew bootRun`) : elle sert l'API et le frontend sur la meme origine, `http://localhost:8080`.
