export interface Medicament {
  id: number;
  nomCommercial: string;
  dci: string | null;
  dosage: string | null;
  forme: string | null;
  prixUnitaire: number;
  surOrdonnance: boolean;
}

export type MedicamentInput = Omit<Medicament, 'id'>;

export interface StockLot {
  id: number;
  medicamentId: number;
  numeroLot: string;
  quantite: number;
  datePeremption: string;
  seuilAlerte: number | null;
}

export type StockLotInput = Omit<StockLot, 'id'>;

export interface LigneVente {
  id: number;
  medicamentId: number;
  nomCommercial: string | null;
  quantite: number;
  prixTotal: number;
}

export interface Vente {
  id: number;
  dateVente: string;
  ordonnanceId: number | null;
  total: number;
  lignes: LigneVente[];
  avertissement?: string;
}

export type StatutOrdonnance = 'EN_ATTENTE' | 'SERVIE' | 'ANNULEE';

export interface Ordonnance {
  id: number;
  nomPrescripteur: string | null;
  datePrescription: string;
  statut: StatutOrdonnance;
  medicamentIds: number[];
}

export interface OrdonnanceInput {
  nomPrescripteur: string | null;
  medicamentIds: number[];
}

export class ApiError extends Error {
  constructor(
    public readonly status: number,
    message: string,
    /** The service's own message, in English; shown only where a specific case is recognised. */
    public readonly detail?: string,
  ) {
    super(message);
  }
}

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  let response: Response;
  try {
    response = await fetch(`/api${path}`, {
      ...init,
      headers: init?.body ? { 'Content-Type': 'application/json' } : undefined,
    });
  } catch {
    throw new ApiError(0, 'Le serveur est injoignable. Vérifiez votre connexion.');
  }

  if (!response.ok) {
    let detail: string | undefined;
    try {
      detail = ((await response.json()) as { message?: string }).message;
    } catch {
      // Body is not JSON; the status code is enough
    }
    throw new ApiError(response.status, defaultMessage(response.status), detail);
  }
  if (response.status === 204) {
    return undefined as T;
  }
  return response.json() as Promise<T>;
}

function defaultMessage(status: number): string {
  switch (status) {
    case 400:
      return 'Certaines informations sont incorrectes.';
    case 404:
      return 'Cet élément n’existe plus.';
    case 409:
      return 'Le stock a changé pendant la saisie. Vérifiez les quantités.';
    default:
      return 'Un service ne répond pas pour le moment. Réessayez dans quelques instants.';
  }
}

const json = (method: string, body: unknown): RequestInit => ({ method, body: JSON.stringify(body) });

export const api = {
  medicaments: () => request<Medicament[]>('/medicaments'),
  createMedicament: (input: MedicamentInput) => request<Medicament>('/medicaments', json('POST', input)),
  updateMedicament: (id: number, input: MedicamentInput) =>
    request<Medicament>(`/medicaments/${id}`, json('PUT', input)),
  deleteMedicament: (id: number) => request<void>(`/medicaments/${id}`, { method: 'DELETE' }),

  lots: () => request<StockLot[]>('/stocks'),
  createLot: (input: StockLotInput) => request<StockLot>('/stocks/entree', json('POST', input)),

  ventes: () => request<Vente[]>('/ventes'),
  vente: (id: number) => request<Vente>(`/ventes/${id}`),
  createVente: (lignes: { medicamentId: number; quantite: number }[], ordonnanceId?: number) =>
    request<Vente>('/ventes', json('POST', { ordonnanceId: ordonnanceId ?? null, lignes })),

  ordonnances: () => request<Ordonnance[]>('/ordonnances'),
  createOrdonnance: (input: OrdonnanceInput) => request<Ordonnance>('/ordonnances', json('POST', input)),
};
