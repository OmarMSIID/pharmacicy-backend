import { Plus, Search } from 'lucide-react';
import { useMemo, useState, type FormEvent } from 'react';
import { api, ApiError, type Medicament, type MedicamentInput } from '../api';
import { useToast } from '../components/Toast';
import { Drawer, EmptyNote, ErrorNote, Field, LoadingRows, PageHeader } from '../components/ui';
import { formatAmount, normalize, plural } from '../lib/format';
import { availableByMedicament } from '../lib/stock';
import { useResource } from '../lib/useResource';

const FORMES = ['Comprimé', 'Gélule', 'Sirop', 'Sachet', 'Spray', 'Injectable', 'Pommade', 'Collyre', 'Suppositoire'];

type Editing = Medicament | 'new' | undefined;

function MedicamentForm({
  medicament,
  onSaved,
  onCancel,
}: {
  medicament: Medicament | undefined;
  onSaved: (saved: Medicament) => void;
  onCancel: () => void;
}) {
  const [nom, setNom] = useState(medicament?.nomCommercial ?? '');
  const [dci, setDci] = useState(medicament?.dci ?? '');
  const [dosage, setDosage] = useState(medicament?.dosage ?? '');
  const [forme, setForme] = useState(medicament?.forme ?? '');
  const [prix, setPrix] = useState(medicament ? String(medicament.prixUnitaire).replace('.', ',') : '');
  const [surOrdonnance, setSurOrdonnance] = useState(medicament?.surOrdonnance ?? false);
  const [error, setError] = useState<string>();
  const [saving, setSaving] = useState(false);

  const submit = async (event: FormEvent) => {
    event.preventDefault();
    const price = Number(prix.replace(',', '.').trim());
    if (!nom.trim()) return setError('Indiquez le nom commercial.');
    if (prix.trim() === '' || !Number.isFinite(price) || price < 0) return setError('Indiquez un prix valide.');

    const input: MedicamentInput = {
      nomCommercial: nom.trim(),
      dci: dci.trim() || null,
      dosage: dosage.trim() || null,
      forme: forme.trim() || null,
      prixUnitaire: price,
      surOrdonnance,
    };
    setSaving(true);
    setError(undefined);
    try {
      onSaved(medicament ? await api.updateMedicament(medicament.id, input) : await api.createMedicament(input));
    } catch (cause) {
      setError(cause instanceof ApiError ? cause.message : 'L’enregistrement a échoué.');
      setSaving(false);
    }
  };

  return (
    <form className="form" onSubmit={submit} noValidate>
      <Field label="Nom commercial">
        {(props) => <input {...props} value={nom} maxLength={150} autoFocus onChange={(e) => setNom(e.target.value)} />}
      </Field>
      <Field label="DCI" hint="Dénomination commune internationale">
        {(props) => <input {...props} value={dci} maxLength={150} onChange={(e) => setDci(e.target.value)} />}
      </Field>
      <div className="form-row">
        <Field label="Dosage">
          {(props) => (
            <input
              {...props}
              value={dosage}
              maxLength={50}
              placeholder="500 mg"
              onChange={(e) => setDosage(e.target.value)}
            />
          )}
        </Field>
        <Field label="Forme">
          {(props) => (
            <>
              <input
                {...props}
                list="formes"
                value={forme}
                maxLength={50}
                onChange={(e) => setForme(e.target.value)}
              />
              <datalist id="formes">
                {FORMES.map((option) => (
                  <option key={option} value={option} />
                ))}
              </datalist>
            </>
          )}
        </Field>
      </div>
      <Field label="Prix unitaire (MAD)">
        {(props) => (
          <input
            {...props}
            className="input-number"
            inputMode="decimal"
            value={prix}
            placeholder="0,00"
            onChange={(e) => setPrix(e.target.value)}
          />
        )}
      </Field>
      <label className="checkbox">
        <input type="checkbox" checked={surOrdonnance} onChange={(e) => setSurOrdonnance(e.target.checked)} />
        Délivré uniquement sur ordonnance
      </label>
      {error && (
        <p className="form-error" role="alert">
          {error}
        </p>
      )}
      <div className="form-actions">
        <button type="submit" className="button button-primary" disabled={saving}>
          {saving ? 'Enregistrement…' : 'Enregistrer'}
        </button>
        <button type="button" className="button" onClick={onCancel}>
          Annuler
        </button>
      </div>
    </form>
  );
}

export function Medicaments() {
  const toast = useToast();
  const medicaments = useResource(api.medicaments);
  const lots = useResource(api.lots);
  const [query, setQuery] = useState('');
  const [editing, setEditing] = useState<Editing>();
  const [deleting, setDeleting] = useState<Medicament>();
  const [deleteError, setDeleteError] = useState<string>();
  const [deleteBusy, setDeleteBusy] = useState(false);

  const available = useMemo(() => availableByMedicament(lots.data ?? []), [lots.data]);

  const visible = useMemo(() => {
    const term = normalize(query.trim());
    return (medicaments.data ?? [])
      .filter(
        (medicament) =>
          !term || normalize(medicament.nomCommercial).includes(term) || normalize(medicament.dci).includes(term),
      )
      .sort((a, b) => a.nomCommercial.localeCompare(b.nomCommercial, 'fr'));
  }, [medicaments.data, query]);

  const closeDelete = () => {
    setDeleting(undefined);
    setDeleteError(undefined);
  };

  const confirmDelete = async () => {
    if (!deleting) return;
    setDeleteBusy(true);
    try {
      await api.deleteMedicament(deleting.id);
      toast(`${deleting.nomCommercial} a été supprimé.`);
      closeDelete();
      medicaments.reload();
    } catch (cause) {
      setDeleteError(
        cause instanceof ApiError && cause.status === 500
          ? 'Ce médicament a des lots ou des ventes enregistrés, il ne peut pas être supprimé.'
          : cause instanceof ApiError
            ? cause.message
            : 'La suppression a échoué.',
      );
    } finally {
      setDeleteBusy(false);
    }
  };

  return (
    <div className="page">
      <PageHeader
        title="Médicaments"
        intro={medicaments.data ? `${plural(medicaments.data.length, 'référence')} au catalogue.` : 'Le catalogue de la pharmacie.'}
        actions={
          <button type="button" className="button button-primary" onClick={() => setEditing('new')}>
            <Plus size={16} aria-hidden />
            Ajouter un médicament
          </button>
        }
      />

      <div className="toolbar">
        <div className="search-box">
          <Search size={16} aria-hidden />
          <input
            type="search"
            value={query}
            onChange={(event) => setQuery(event.target.value)}
            placeholder="Rechercher par nom ou DCI…"
            aria-label="Rechercher un médicament"
          />
        </div>
      </div>

      {medicaments.error ? (
        <ErrorNote message={medicaments.error} onRetry={medicaments.reload} />
      ) : medicaments.loading && !medicaments.data ? (
        <LoadingRows rows={8} />
      ) : visible.length === 0 ? (
        <EmptyNote title={query ? `Aucun résultat pour « ${query} »` : 'Le catalogue est vide'}>
          {!query && 'Ajoutez votre premier médicament pour commencer.'}
        </EmptyNote>
      ) : (
        <div className="table-wrap">
          <table className="table">
            <thead>
              <tr>
                <th scope="col">Nom commercial</th>
                <th scope="col">DCI</th>
                <th scope="col">Dosage</th>
                <th scope="col">Forme</th>
                <th scope="col" className="num">
                  En stock
                </th>
                <th scope="col" className="num">
                  Prix (MAD)
                </th>
                <th scope="col">
                  <span className="sr-only">Actions</span>
                </th>
              </tr>
            </thead>
            <tbody>
              {visible.map((medicament) => {
                const stock = available.get(medicament.id) ?? 0;
                return (
                  <tr key={medicament.id}>
                    <td className="strong">
                      {medicament.nomCommercial}
                      {medicament.surOrdonnance && <span className="rx-tag">Ordonnance</span>}
                    </td>
                    <td className="muted">{medicament.dci ?? '–'}</td>
                    <td className="nowrap">{medicament.dosage ?? '–'}</td>
                    <td>{medicament.forme ?? '–'}</td>
                    <td className={`num${lots.data && stock === 0 ? ' is-out' : ''}`}>
                      {lots.data ? (stock === 0 ? 'Rupture' : stock) : '–'}
                    </td>
                    <td className="num">{formatAmount(medicament.prixUnitaire)}</td>
                    <td className="row-actions">
                      <button type="button" className="quiet-button" onClick={() => setEditing(medicament)}>
                        Modifier
                      </button>
                      <button
                        type="button"
                        className="quiet-button quiet-danger"
                        onClick={() => setDeleting(medicament)}
                      >
                        Supprimer
                      </button>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      )}

      <Drawer
        open={editing !== undefined}
        title={editing === 'new' ? 'Nouveau médicament' : 'Modifier le médicament'}
        onClose={() => setEditing(undefined)}
      >
        {editing !== undefined && (
          <MedicamentForm
            key={editing === 'new' ? 'new' : editing.id}
            medicament={editing === 'new' ? undefined : editing}
            onCancel={() => setEditing(undefined)}
            onSaved={(saved) => {
              toast(`${saved.nomCommercial} a été enregistré.`);
              setEditing(undefined);
              medicaments.reload();
            }}
          />
        )}
      </Drawer>

      <Drawer open={deleting !== undefined} title="Supprimer ce médicament ?" onClose={closeDelete}>
        {deleting && (
          <div className="form">
            <p>
              <strong>{deleting.nomCommercial}</strong> sera retiré du catalogue. Cette action est définitive.
            </p>
            {deleteError && (
              <p className="form-error" role="alert">
                {deleteError}
              </p>
            )}
            <div className="form-actions">
              <button type="button" className="button button-danger" disabled={deleteBusy} onClick={confirmDelete}>
                {deleteBusy ? 'Suppression…' : 'Supprimer'}
              </button>
              <button type="button" className="button" onClick={closeDelete}>
                Annuler
              </button>
            </div>
          </div>
        )}
      </Drawer>
    </div>
  );
}
