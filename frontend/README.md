# PharmaCare frontend

React and TypeScript interface for the pharmacy services, built with Vite. It talks to the API Gateway on `/api` only.

## Pages

| Path | Page | Services used |
| --- | --- | --- |
| `/` | Tableau de bord | ventes, stocks, medicaments |
| `/ventes` | Ventes | ventes |
| `/ventes/nouvelle` | Nouvelle vente (caisse) | medicaments, stocks, ordonnances, ventes |
| `/ventes/:id/ticket` | Ticket imprimable | ventes |
| `/ordonnances` | Ordonnances | ordonnances, medicaments |
| `/medicaments` | Médicaments | medicaments, stocks |
| `/stocks` | Stocks | stocks, medicaments |
| `/parametres` | Paramètres | all, for the status panel |

## Prescriptions

A prescription is created on the Ordonnances page. Its "Délivrer" button opens Nouvelle vente with the prescription linked (`/ventes/nouvelle?ordonnance=<id>`) and the prescribed medicaments in the cart. The sale screen refuses to check out a medicament marked "sur ordonnance" unless the linked prescription lists it. vente-service then marks the prescription as served.

## Run with Docker Compose

From the repository root:

```bash
docker compose up --build
```

The interface is then on `http://localhost:3000`. nginx serves the build and forwards `/api` to the gateway.

## Run in development

Start the backend, then from this folder:

```bash
npm install
npm run dev
```

The interface is on `http://localhost:5173`. Vite forwards `/api` to `http://localhost:8081`. Set `API_URL` to use another gateway address.

## Settings

The Paramètres page saves the user name, the pharmacy details printed on tickets, and the expiry alert window in the browser's local storage, because the backend has no user or settings service yet.
