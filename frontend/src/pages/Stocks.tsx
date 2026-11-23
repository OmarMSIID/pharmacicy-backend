import { Plus, Search } from 'lucide-react';
import { useMemo, useState, type FormEvent } from 'react';
import { useSearchParams } from 'react-router-dom';
import { api, ApiError, type Medicament, type StockLot } from '../api';
import { useToast } from '../components/Toast';
import { Drawer, EmptyNote, ErrorNote, Field, LoadingRows, PageHeader, StatusTag } from '../components/ui';
import { addDays, dayKey, formatCount, formatDate, normalize, parseDay, plural } from '../lib/format';
import { useSettings } from '../lib/settings';
import { availableByMedicament, byUrgency, expiryText, lotStatus, needsAttention } from '../lib/stock';
import { useResource } from '../lib/useResource';

type Filter = 'tous' | 'alertes' | 'perimes';

function LotForm({
  medicaments,
  onSaved,
  onCancel,
}: {
  medicaments: Medicament[];
  onSaved: (lot: StockLot) => void;
  onCancel: () => void;
}) {
  const [medicamentId, setMedicamentId] = useState('');
  const [numeroLot, setNumeroLot] = useState('');
  const [quantite, setQuantite] = useState('');
  const [datePeremption, setDatePeremption] = useState('');
  const [seuil, setSeuil] = useState('10');
  const [error, setError] = useState<string>();
  const [saving, setSaving] = useState(false);
  const tomorrow = dayKey(addDays(new Date(), 1));

  const submit = async (event: FormEvent) => {
    event.preventDefault();
    const quantity = Number(quantite);
    const threshold = Number(seuil);
    if (!medicamentId) return setError('Choisissez un médicament.');
    if (!numeroLot.trim()) return setError('Indiquez le numéro de lot.');
    if (!Number.isInteger(quantity) || quantity <= 0) return setError('Indiquez une quantité entière et positive.');
    if (!datePeremption) return setError('Indiquez la date de péremption.');
    if (datePeremption < tomorrow) return setError('Ce lot est déjà périmé.');
    if (!Number.isInteger(threshold) || threshold < 0) return setError('Indiquez un seuil d’alerte valide.');

    setSaving(true);
    setError(undefined);
    try {
      onSaved(
        await api.createLot({
          medicamentId: Number(medicamentId),
          numeroLot: numeroLot.trim(),
          quantite: quantity,
          datePeremption,
          seuilAlerte: threshold,
        }),
      );
    } catch (cause) {
      setError(cause instanceof ApiError ? cause.message : 'L’enregistrement a échoué.');
      setSaving(false);
    }
  };

  return (
    <form className="form" onSubmit={submit} noValidate>
      <Field label="Médicament">
        {(props) => (
          <select {...props} value={medicamentId} autoFocus onChange={(e) => setMedicamentId(e.target.value)}>
            <option value="">Choisir…</option>
            {[...medicaments]
              .sort((a, b) => a.nomCommercial.localeCompare(b.nomCommercial, 'fr'))
              .map((medicament) => (
                <option key={medicament.id} value={medicament.id}>
                  {medicament.nomCommercial}
                  {medicament.dosage ? ` ${medicament.dosage}` : ''}
                </option>
              ))}
          </select>
        )}
      </Field>
      <Field label="Numéro de lot">
        {(props) => (
          <input
            {...props}
            className="input-mono"
            value={numeroLot}
            maxLength={50}
            onChange={(e) => setNumeroLot(e.target.value)}
          />
        )}
      </Field>
      <div className="form-row">
        <Field label="Quantité">
          {(props) => (
            <input
              {...props}
              className="input-number"
              inputMode="numeric"
              value={quantite}
              onChange={(e) => setQuantite(e.target.value)}
            />
          )}
        </Field>
        <Field label="Seuil d’alerte">
          {(props) => (
            <input
              {...props}
              className="input-number"
              inputMode="numeric"
              value={seuil}
              onChange={(e) => setSeuil(e.target.value)}
            />
          )}
        </Field>
      </div>
      <Field label="Date de péremption">
        {(props) => (
          <input
            {...props}
            type="date"
            min={tomorrow}
            value={datePeremption}
            onChange={(e) => setDatePeremption(e.target.value)}
          />
        )}
      </Field>
      {error && (
        <p className="form-error" role="alert">
          {error}
        </p>
      )}
      <div className="form-actions">
        <button type="submit" className="button button-primary" disabled={saving}>
          {saving ? 'Enregistrement…' : 'Enregistrer le lot'}
        </button>
        <button type="button" className="button" onClick={onCancel}>
          Annuler
        </button>
      </div>
    </form>
  );
}

export function Stocks() {
  const toast = useToast();
  const { settings } = useSettings();
  const lots = useResource(api.lots);
  const medicaments = useResource(api.medicaments);
  const [params] = useSearchParams();
  const [filter, setFilter] = useState<Filter>(params.get('filtre') === 'alertes' ? 'alertes' : 'tous');
  const [query, setQuery] = useState('');
  const [adding, setAdding] = useState(false);

  const names = useMemo(() => new Map(medicaments.data?.map((m) => [m.id, m.nomCommercial])), [medicaments.data]);
  const nameOf = (lot: StockLot) => names.get(lot.medicamentId) ?? `Médicament n° ${lot.medicamentId}`;
  const statusOf = (lot: StockLot) => lotStatus(lot, settings.joursAlerte);

  const all = lots.data ?? [];
  const attentionCount = all.filter((lot) => needsAttention(statusOf(lot))).length;
  const expiredCount = all.filter((lot) => statusOf(lot) === 'perime').length;
  const units = [...availableByMedicament(all).values()].reduce((sum, quantity) => sum + quantity, 0);

  const visible = useMemo(() => {
    const term = normalize(query.trim());
    return byUrgency(all, settings.joursAlerte).filter((lot) => {
      const status = lotStatus(lot, settings.joursAlerte);
      if (filter === 'alertes' && !needsAttention(status)) return false;
      if (filter === 'perimes' && status !== 'perime') return false;
      return (
        !term ||
        normalize(names.get(lot.medicamentId)).includes(term) ||
        normalize(lot.numeroLot).includes(term)
      );
    });
  }, [all, filter, query, names, settings.joursAlerte]);

  const FILTERS: { id: Filter; label: string; count: number }[] = [
    { id: 'tous', label: 'Tous les lots', count: all.length },
    { id: 'alertes', label: 'Alertes', count: attentionCount },
    { id: 'perimes', label: 'Périmés', count: expiredCount },
  ];

  return (
    <div className="page">
      <PageHeader
        title="Stocks"
        intro={
          lots.data
            ? `${plural(units, 'unité vendable', 'unités vendables')}, réparties sur ${plural(all.length, 'lot')}.`
            : 'Les lots et leurs dates de péremption.'
        }
        actions={
          <button
            type="button"
            className="button button-primary"
            disabled={!medicaments.data}
            onClick={() => setAdding(true)}
          >
            <Plus size={16} aria-hidden />
            Ajouter un lot
          </button>
        }
      />

      <div className="toolbar">
        <div className="segmented" role="group" aria-label="Filtrer les lots">
          {FILTERS.map(({ id, label, count }) => (
            <button type="button" key={id} aria-pressed={filter === id} onClick={() => setFilter(id)}>
              {label}
              <span className="segmented-count">{formatCount(count)}</span>
            </button>
          ))}
        </div>
        <div className="search-box">
          <Search size={16} aria-hidden />
          <input
            type="search"
            value={query}
            onChange={(event) => setQuery(event.target.value)}
            placeholder="Médicament ou n° de lot…"
            aria-label="Rechercher un lot"
          />
        </div>
      </div>

      {lots.error ? (
        <ErrorNote message={lots.error} onRetry={lots.reload} />
      ) : lots.loading && !lots.data ? (
        <LoadingRows rows={8} />
      ) : visible.length === 0 ? (
        <EmptyNote
          title={
            query
              ? `Aucun résultat pour « ${query} »`
              : filter === 'tous'
                ? 'Aucun lot en stock'
                : 'Rien à signaler de ce côté'
          }
        >
          {!query && filter === 'tous' && 'Ajoutez un lot pour commencer le suivi.'}
        </EmptyNote>
      ) : (
        <div className="table-wrap">
          <table className="table">
            <thead>
              <tr>
                <th scope="col">Médicament</th>
                <th scope="col">Lot</th>
                <th scope="col" className="num">
                  Quantité
                </th>
                <th scope="col">Péremption</th>
                <th scope="col">État</th>
              </tr>
            </thead>
            <tbody>
              {visible.map((lot) => {
                const status = statusOf(lot);
                return (
                  <tr key={lot.id} className={status === 'epuise' ? 'row-faded' : undefined}>
                    <td className="strong">{nameOf(lot)}</td>
                    <td className="mono">{lot.numeroLot}</td>
                    <td className="num">{formatCount(lot.quantite)}</td>
                    <td className="nowrap">
                      {formatDate(parseDay(lot.datePeremption))}
                      {(status === 'perime' || status === 'expire') && (
                        <span className="muted"> · {expiryText(lot.datePeremption)}</span>
                      )}
                    </td>
                    <td>
                      <StatusTag status={status} />
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      )}

      <Drawer open={adding} title="Nouveau lot" onClose={() => setAdding(false)}>
        {adding && (
          <LotForm
            medicaments={medicaments.data ?? []}
            onCancel={() => setAdding(false)}
            onSaved={(lot) => {
              toast(`Le lot ${lot.numeroLot} a été ajouté.`);
              setAdding(false);
              lots.reload();
            }}
          />
        )}
      </Drawer>
    </div>
  );
}
