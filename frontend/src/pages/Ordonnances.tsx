import { Plus, Search } from 'lucide-react';
import { useMemo, useState, type FormEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import { api, ApiError, type Medicament, type Ordonnance, type StatutOrdonnance } from '../api';
import { useToast } from '../components/Toast';
import { Drawer, EmptyNote, ErrorNote, Field, LoadingRows, PageHeader } from '../components/ui';
import { formatCount, formatDate, formatTime, normalize, plural, saleNumber } from '../lib/format';
import { useResource } from '../lib/useResource';

type Filter = 'attente' | 'servies' | 'toutes';

const STATUT_LABELS: Record<StatutOrdonnance, string> = {
  EN_ATTENTE: 'En attente',
  SERVIE: 'Servie',
  ANNULEE: 'Annulée',
};

export function OrdonnanceStatus({ statut }: { statut: StatutOrdonnance }) {
  return <span className={`status status-ord-${statut.toLowerCase()}`}>{STATUT_LABELS[statut]}</span>;
}

function OrdonnanceForm({
  medicaments,
  onSaved,
  onCancel,
}: {
  medicaments: Medicament[];
  onSaved: (ordonnance: Ordonnance) => void;
  onCancel: () => void;
}) {
  const [prescripteur, setPrescripteur] = useState('');
  const [query, setQuery] = useState('');
  const [selected, setSelected] = useState<number[]>([]);
  const [error, setError] = useState<string>();
  const [saving, setSaving] = useState(false);

  const visible = useMemo(() => {
    const term = normalize(query.trim());
    return [...medicaments]
      .filter((m) => !term || normalize(m.nomCommercial).includes(term) || normalize(m.dci).includes(term))
      .sort((a, b) => a.nomCommercial.localeCompare(b.nomCommercial, 'fr'));
  }, [medicaments, query]);

  const needsPrescriber = medicaments.some((m) => selected.includes(m.id) && m.surOrdonnance);

  const toggle = (id: number) =>
    setSelected((ids) => (ids.includes(id) ? ids.filter((value) => value !== id) : [...ids, id]));

  const submit = async (event: FormEvent) => {
    event.preventDefault();
    if (selected.length === 0) return setError('Choisissez au moins un médicament.');
    if (needsPrescriber && !prescripteur.trim()) {
      return setError('Indiquez le prescripteur : un des médicaments est délivré uniquement sur ordonnance.');
    }
    setSaving(true);
    setError(undefined);
    try {
      onSaved(await api.createOrdonnance({ nomPrescripteur: prescripteur.trim() || null, medicamentIds: selected }));
    } catch (cause) {
      setError(cause instanceof ApiError ? cause.message : 'L’enregistrement a échoué.');
      setSaving(false);
    }
  };

  return (
    <form className="form" onSubmit={submit} noValidate>
      <Field
        label="Prescripteur"
        hint={needsPrescriber ? 'Obligatoire pour les médicaments sur ordonnance.' : 'Facultatif si aucun médicament ne l’exige.'}
      >
        {(props) => (
          <input
            {...props}
            value={prescripteur}
            maxLength={100}
            placeholder="Dr …"
            onChange={(e) => setPrescripteur(e.target.value)}
          />
        )}
      </Field>

      <div className="field">
        <span className="field-label">
          Médicaments prescrits{selected.length > 0 && <span className="muted"> · {selected.length} choisi(s)</span>}
        </span>
        <div className="search-box">
          <Search size={16} aria-hidden />
          <input
            type="search"
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            placeholder="Filtrer par nom ou DCI…"
            aria-label="Filtrer les médicaments"
          />
        </div>
        <ul className="pick-list">
          {visible.map((medicament) => (
            <li key={medicament.id}>
              <label className="checkbox pick-item">
                <input
                  type="checkbox"
                  checked={selected.includes(medicament.id)}
                  onChange={() => toggle(medicament.id)}
                />
                <span>
                  {medicament.nomCommercial}
                  {medicament.dosage && <span className="muted"> {medicament.dosage}</span>}
                  {medicament.surOrdonnance && <span className="rx-tag">Ordonnance</span>}
                </span>
              </label>
            </li>
          ))}
          {visible.length === 0 && <li className="muted pick-none">Aucun médicament ne correspond.</li>}
        </ul>
      </div>

      {error && (
        <p className="form-error" role="alert">
          {error}
        </p>
      )}
      <div className="form-actions">
        <button type="submit" className="button button-primary" disabled={saving}>
          {saving ? 'Enregistrement…' : 'Enregistrer l’ordonnance'}
        </button>
        <button type="button" className="button" onClick={onCancel}>
          Annuler
        </button>
      </div>
    </form>
  );
}

export function Ordonnances() {
  const toast = useToast();
  const navigate = useNavigate();
  const ordonnances = useResource(api.ordonnances);
  const medicaments = useResource(api.medicaments);
  const [filter, setFilter] = useState<Filter>('attente');
  const [adding, setAdding] = useState(false);

  const names = useMemo(() => new Map(medicaments.data?.map((m) => [m.id, m.nomCommercial])), [medicaments.data]);
  const all = ordonnances.data ?? [];
  const pending = all.filter((o) => o.statut === 'EN_ATTENTE');
  const served = all.filter((o) => o.statut === 'SERVIE');
  const visible = filter === 'attente' ? pending : filter === 'servies' ? served : all;

  const FILTERS: { id: Filter; label: string; count: number }[] = [
    { id: 'attente', label: 'En attente', count: pending.length },
    { id: 'servies', label: 'Servies', count: served.length },
    { id: 'toutes', label: 'Toutes', count: all.length },
  ];

  return (
    <div className="page">
      <PageHeader
        title="Ordonnances"
        intro={
          ordonnances.data
            ? pending.length === 0
              ? 'Aucune ordonnance en attente de délivrance.'
              : `${plural(pending.length, 'ordonnance')} en attente de délivrance.`
            : 'Les prescriptions à délivrer et déjà servies.'
        }
        actions={
          <button type="button" className="button button-primary" disabled={!medicaments.data} onClick={() => setAdding(true)}>
            <Plus size={16} aria-hidden />
            Nouvelle ordonnance
          </button>
        }
      />

      <div className="toolbar">
        <div className="segmented" role="group" aria-label="Filtrer les ordonnances">
          {FILTERS.map(({ id, label, count }) => (
            <button type="button" key={id} aria-pressed={filter === id} onClick={() => setFilter(id)}>
              {label}
              <span className="segmented-count">{formatCount(count)}</span>
            </button>
          ))}
        </div>
      </div>

      {ordonnances.error ? (
        <ErrorNote message={ordonnances.error} onRetry={ordonnances.reload} />
      ) : ordonnances.loading && !ordonnances.data ? (
        <LoadingRows rows={6} />
      ) : visible.length === 0 ? (
        <EmptyNote title={filter === 'attente' ? 'Rien à délivrer' : 'Aucune ordonnance'}>
          {filter === 'attente' && 'Les nouvelles ordonnances apparaîtront ici.'}
        </EmptyNote>
      ) : (
        <div className="table-wrap">
          <table className="table">
            <thead>
              <tr>
                <th scope="col">N°</th>
                <th scope="col">Date</th>
                <th scope="col">Prescripteur</th>
                <th scope="col">Médicaments</th>
                <th scope="col">Statut</th>
                <th scope="col">
                  <span className="sr-only">Actions</span>
                </th>
              </tr>
            </thead>
            <tbody>
              {visible.map((ordonnance) => {
                const date = new Date(ordonnance.datePrescription);
                return (
                  <tr key={ordonnance.id}>
                    <td className="mono muted">{saleNumber(ordonnance.id)}</td>
                    <td className="nowrap">
                      {formatDate(date)}
                      <span className="muted">, {formatTime(date)}</span>
                    </td>
                    <td>{ordonnance.nomPrescripteur ?? <span className="muted">Non renseigné</span>}</td>
                    <td className="cell-clip">
                      {ordonnance.medicamentIds.map((id) => names.get(id) ?? `n° ${id}`).join(', ')}
                    </td>
                    <td>
                      <OrdonnanceStatus statut={ordonnance.statut} />
                    </td>
                    <td className="row-actions">
                      {ordonnance.statut === 'EN_ATTENTE' && (
                        <button
                          type="button"
                          className="button button-small"
                          onClick={() => navigate(`/ventes/nouvelle?ordonnance=${ordonnance.id}`)}
                        >
                          Délivrer
                        </button>
                      )}
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      )}

      <Drawer open={adding} title="Nouvelle ordonnance" onClose={() => setAdding(false)}>
        {adding && (
          <OrdonnanceForm
            medicaments={medicaments.data ?? []}
            onCancel={() => setAdding(false)}
            onSaved={(ordonnance) => {
              toast(`L’ordonnance n° ${saleNumber(ordonnance.id)} a été enregistrée.`);
              setAdding(false);
              setFilter('attente');
              ordonnances.reload();
            }}
          />
        )}
      </Drawer>
    </div>
  );
}
