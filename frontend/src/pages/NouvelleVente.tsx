import { Check, Minus, Plus, Printer, Search, TriangleAlert, X } from 'lucide-react';
import { useEffect, useMemo, useRef, useState, type KeyboardEvent } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { api, ApiError, type Medicament, type Vente } from '../api';
import { openTicketForPrint, Ticket } from '../components/Ticket';
import { ErrorNote, LoadingRows, PageHeader } from '../components/ui';
import { formatAmount, formatMoney, normalize, plural, saleNumber } from '../lib/format';
import { availableByMedicament } from '../lib/stock';
import { useResource } from '../lib/useResource';

interface CartLine {
  medicament: Medicament;
  quantite: number;
  /** Set when the line was filled in from a prescription, so switching prescriptions can replace it. */
  fromOrdonnance?: number;
}

const toCents = (value: number) => Math.round(value * 100);

function describe(medicament: Medicament): string {
  return [medicament.dci, medicament.dosage, medicament.forme].filter(Boolean).join(' · ');
}

function saleError(cause: unknown, withOrdonnance: boolean): string {
  if (!(cause instanceof ApiError)) return 'La vente n’a pas pu être enregistrée.';
  if (cause.status === 409 && cause.detail?.includes('requires a prescription')) {
    return 'Un médicament du panier demande une ordonnance qui le prescrit.';
  }
  const aboutOrdonnance = cause.detail?.toLowerCase().includes('ordonnance') ?? false;
  if (cause.status === 409 && aboutOrdonnance) return 'Cette ordonnance a déjà été servie.';
  if (cause.status === 404 && aboutOrdonnance) return 'Cette ordonnance n’existe plus.';
  if (cause.status === 503 && withOrdonnance) {
    return 'Le service des ordonnances ou du stock ne répond pas. Réessayez dans quelques instants.';
  }
  return cause.message;
}

export function NouvelleVente() {
  const [params, setParams] = useSearchParams();
  const medicaments = useResource(api.medicaments);
  const lots = useResource(api.lots);
  const ordonnances = useResource(api.ordonnances);
  const [query, setQuery] = useState('');
  const [highlighted, setHighlighted] = useState(0);
  const [cart, setCart] = useState<CartLine[]>([]);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string>();
  const [completed, setCompleted] = useState<Vente>();
  const searchRef = useRef<HTMLInputElement>(null);
  const prefilledFor = useRef<number | null>(null);

  const ordonnanceId = params.has('ordonnance') ? Number(params.get('ordonnance')) : undefined;
  const pending = useMemo(
    () => (ordonnances.data ?? []).filter((ordonnance) => ordonnance.statut === 'EN_ATTENTE'),
    [ordonnances.data],
  );
  const ordonnance = pending.find((candidate) => candidate.id === ordonnanceId);
  const ordonnanceGone = ordonnanceId !== undefined && ordonnances.data !== undefined && !ordonnance;

  const available = useMemo(() => availableByMedicament(lots.data ?? []), [lots.data]);
  const availableOf = (id: number) => available.get(id) ?? 0;

  const linkOrdonnance = (id: number | undefined) => {
    setError(undefined);
    setParams(id === undefined ? {} : { ordonnance: String(id) }, { replace: true });
  };

  // When the linked prescription changes, its lines replace the previous prescription's lines;
  // lines added by hand stay in the cart
  useEffect(() => {
    if (!medicaments.data || !lots.data) return;
    if (ordonnanceId !== undefined && !ordonnance) return; // still loading, or no longer pending
    const target = ordonnance?.id ?? null;
    if (prefilledFor.current === target) return;
    prefilledFor.current = target;
    setCart((lines) => {
      const next = lines.filter((line) => line.fromOrdonnance === undefined);
      for (const id of ordonnance?.medicamentIds ?? []) {
        const medicament = medicaments.data!.find((candidate) => candidate.id === id);
        if (medicament && (available.get(id) ?? 0) > 0 && !next.some((line) => line.medicament.id === id)) {
          next.push({ medicament, quantite: 1, fromOrdonnance: target! });
        }
      }
      return next;
    });
  }, [ordonnanceId, ordonnance, medicaments.data, lots.data, available]);

  const results = useMemo(() => {
    const term = normalize(query.trim());
    return (medicaments.data ?? [])
      .filter(
        (medicament) =>
          !term || normalize(medicament.nomCommercial).includes(term) || normalize(medicament.dci).includes(term),
      )
      .sort(
        (a, b) =>
          Number((available.get(b.id) ?? 0) > 0) - Number((available.get(a.id) ?? 0) > 0) ||
          a.nomCommercial.localeCompare(b.nomCommercial, 'fr'),
      )
      .slice(0, 40);
  }, [medicaments.data, available, query]);

  useEffect(() => setHighlighted(0), [query]);

  useEffect(() => {
    const onKey = (event: globalThis.KeyboardEvent) => {
      const target = event.target as HTMLElement;
      if (event.key === '/' && !['INPUT', 'TEXTAREA', 'SELECT'].includes(target.tagName)) {
        event.preventDefault();
        searchRef.current?.focus();
      }
    };
    window.addEventListener('keydown', onKey);
    return () => window.removeEventListener('keydown', onKey);
  }, []);

  const add = (medicament: Medicament) => {
    const stock = availableOf(medicament.id);
    if (stock <= 0) return;
    setError(undefined);
    setCart((lines) => {
      const existing = lines.find((line) => line.medicament.id === medicament.id);
      if (!existing) return [...lines, { medicament, quantite: 1 }];
      return lines.map((line) =>
        line === existing ? { ...line, quantite: Math.min(line.quantite + 1, stock) } : line,
      );
    });
    setQuery('');
    searchRef.current?.focus();
  };

  const setQuantity = (id: number, quantite: number) => {
    const stock = availableOf(id);
    setCart((lines) =>
      lines.map((line) =>
        line.medicament.id === id
          ? { ...line, quantite: Math.max(1, Math.min(Number.isFinite(quantite) ? quantite : 1, stock || 1)) }
          : line,
      ),
    );
  };

  const remove = (id: number) => setCart((lines) => lines.filter((line) => line.medicament.id !== id));

  const onSearchKey = (event: KeyboardEvent<HTMLInputElement>) => {
    if (event.key === 'ArrowDown') {
      event.preventDefault();
      setHighlighted((index) => Math.min(index + 1, results.length - 1));
    } else if (event.key === 'ArrowUp') {
      event.preventDefault();
      setHighlighted((index) => Math.max(index - 1, 0));
    } else if (event.key === 'Enter' && results[highlighted]) {
      event.preventDefault();
      add(results[highlighted]);
    }
  };

  const totalCents = cart.reduce((sum, line) => sum + toCents(line.medicament.prixUnitaire) * line.quantite, 0);
  const units = cart.reduce((sum, line) => sum + line.quantite, 0);
  const overStock = cart.some((line) => line.quantite > availableOf(line.medicament.id));
  // A prescription-only medicament needs a linked prescription that lists it
  const needsPrescription = cart
    .filter((line) => line.medicament.surOrdonnance && !ordonnance?.medicamentIds.includes(line.medicament.id))
    .map((line) => line.medicament.nomCommercial);

  const checkout = async () => {
    setSubmitting(true);
    setError(undefined);
    try {
      const vente = await api.createVente(
        cart.map((line) => ({ medicamentId: line.medicament.id, quantite: line.quantite })),
        ordonnance?.id,
      );
      setCompleted(vente);
      setCart([]);
      setParams({}, { replace: true });
      lots.reload();
      ordonnances.reload();
    } catch (cause) {
      setError(saleError(cause, ordonnance !== undefined));
      lots.reload();
      ordonnances.reload();
    } finally {
      setSubmitting(false);
    }
  };

  const restart = () => {
    setCompleted(undefined);
    setQuery('');
    window.setTimeout(() => searchRef.current?.focus());
  };

  if (completed) {
    return (
      <div className="page">
        <PageHeader title="Vente enregistrée" intro="Le stock a été mis à jour." />
        <div className="sale-done">
          <Ticket vente={completed} />
          <div className="sale-done-actions">
            <p className="sale-done-check">
              <Check size={18} aria-hidden />
              {formatMoney(completed.total)} encaissés
            </p>
            {completed.ordonnanceId !== null &&
              (completed.avertissement ? (
                <p className="sale-done-warning" role="status">
                  L’ordonnance n° {saleNumber(completed.ordonnanceId)} n’a pas pu être marquée comme servie. Signalez-le
                  au responsable.
                </p>
              ) : (
                <p className="sale-done-note">Ordonnance n° {saleNumber(completed.ordonnanceId)} servie.</p>
              ))}
            <button type="button" className="button button-primary" onClick={restart}>
              <Plus size={16} aria-hidden />
              Nouvelle vente
            </button>
            <button type="button" className="button" onClick={() => openTicketForPrint(completed.id)}>
              <Printer size={16} aria-hidden />
              Imprimer le ticket
            </button>
            <Link to="/ventes" className="text-link">
              Voir toutes les ventes
            </Link>
          </div>
        </div>
      </div>
    );
  }

  const loadError = medicaments.error ?? lots.error;

  return (
    <div className="page page-wide">
      <PageHeader title="Nouvelle vente" />

      <div className="till">
        <section className="till-search" aria-label="Recherche de médicaments">
          <div className="search-box search-box-large">
            <Search size={18} aria-hidden />
            <input
              ref={searchRef}
              type="search"
              autoFocus
              value={query}
              onChange={(event) => setQuery(event.target.value)}
              onKeyDown={onSearchKey}
              placeholder="Nom commercial ou DCI…"
              aria-label="Rechercher un médicament"
              aria-controls="till-results"
            />
            <kbd aria-hidden>/</kbd>
          </div>

          {loadError ? (
            <ErrorNote
              message={loadError}
              onRetry={() => {
                medicaments.reload();
                lots.reload();
              }}
            />
          ) : !medicaments.data || !lots.data ? (
            <LoadingRows rows={8} />
          ) : results.length === 0 ? (
            <p className="till-none">
              {query ? `Aucun médicament ne correspond à « ${query} ».` : 'Le catalogue est vide.'}
            </p>
          ) : (
            <ul className="till-results" id="till-results">
              {results.map((medicament, index) => {
                const stock = availableOf(medicament.id);
                const inCart = cart.find((line) => line.medicament.id === medicament.id)?.quantite ?? 0;
                return (
                  <li key={medicament.id}>
                    <button
                      type="button"
                      className={`till-result${index === highlighted ? ' is-highlighted' : ''}`}
                      disabled={stock <= 0 || inCart >= stock}
                      onClick={() => add(medicament)}
                      onMouseEnter={() => setHighlighted(index)}
                    >
                      <span className="till-result-name">
                        <strong>
                          {medicament.nomCommercial}
                          {medicament.surOrdonnance && <span className="rx-tag">Ordonnance</span>}
                        </strong>
                        <small>{describe(medicament)}</small>
                      </span>
                      <span className={`till-result-stock${stock === 0 ? ' is-out' : ''}`}>
                        {stock === 0 ? 'Rupture' : `${stock} en stock`}
                      </span>
                      <span className="till-result-price">{formatAmount(medicament.prixUnitaire)}</span>
                    </button>
                  </li>
                );
              })}
            </ul>
          )}
        </section>

        <aside className="till-cart" aria-label="Panier">
          <div className="ticket-shadow">
            <div className="ticket ticket-cart">
              <header className="cart-head">
                <strong>Panier</strong>
                <span>{cart.length ? plural(units, 'article') : ''}</span>
              </header>

              <div className="cart-ordonnance">
                <label htmlFor="cart-ordonnance">Ordonnance</label>
                {ordonnances.error ? (
                  <span className="muted">Service des ordonnances indisponible</span>
                ) : (
                  <select
                    id="cart-ordonnance"
                    value={ordonnance?.id ?? ''}
                    disabled={!ordonnances.data}
                    onChange={(event) => linkOrdonnance(event.target.value ? Number(event.target.value) : undefined)}
                  >
                    <option value="">Sans ordonnance</option>
                    {pending.map((candidate) => (
                      <option key={candidate.id} value={candidate.id}>
                        N° {saleNumber(candidate.id)}
                        {candidate.nomPrescripteur ? ` · ${candidate.nomPrescripteur}` : ''}
                      </option>
                    ))}
                  </select>
                )}
              </div>
              {ordonnanceGone && (
                <p className="cart-warning">L’ordonnance n° {saleNumber(ordonnanceId!)} n’est plus en attente.</p>
              )}

              {cart.length === 0 ? (
                <p className="cart-empty">
                  Cherchez un médicament puis appuyez sur <kbd>Entrée</kbd> pour l’ajouter.
                </p>
              ) : (
                <ul className="cart-lines">
                  {cart.map(({ medicament, quantite }) => {
                    const stock = availableOf(medicament.id);
                    return (
                      <li key={medicament.id}>
                        <div className="cart-line-top">
                          <span className="cart-line-name">{medicament.nomCommercial}</span>
                          <button
                            type="button"
                            className="cart-remove"
                            onClick={() => remove(medicament.id)}
                            aria-label={`Retirer ${medicament.nomCommercial}`}
                          >
                            <X size={14} aria-hidden />
                          </button>
                        </div>
                        <div className="cart-line-bottom">
                          <div className="stepper">
                            <button
                              type="button"
                              onClick={() => setQuantity(medicament.id, quantite - 1)}
                              disabled={quantite <= 1}
                              aria-label={`Une boîte de moins de ${medicament.nomCommercial}`}
                            >
                              <Minus size={13} aria-hidden />
                            </button>
                            <input
                              type="number"
                              min={1}
                              max={stock}
                              value={quantite}
                              onChange={(event) => setQuantity(medicament.id, Number(event.target.value))}
                              aria-label={`Quantité de ${medicament.nomCommercial}`}
                            />
                            <button
                              type="button"
                              onClick={() => setQuantity(medicament.id, quantite + 1)}
                              disabled={quantite >= stock}
                              aria-label={`Une boîte de plus de ${medicament.nomCommercial}`}
                            >
                              <Plus size={13} aria-hidden />
                            </button>
                          </div>
                          <span className="cart-line-price">
                            {formatAmount((toCents(medicament.prixUnitaire) * quantite) / 100)}
                          </span>
                        </div>
                        {quantite > stock && <p className="cart-warning">Il ne reste que {stock} en stock.</p>}
                      </li>
                    );
                  })}
                </ul>
              )}

              <div className="ticket-total">
                <span>Total</span>
                <strong>{formatMoney(totalCents / 100)}</strong>
              </div>

              {needsPrescription.length > 0 && (
                <p className="cart-rx" role="status">
                  <TriangleAlert size={15} aria-hidden />
                  <span>
                    {needsPrescription.join(', ')} {needsPrescription.length > 1 ? 'demandent' : 'demande'} une
                    ordonnance qui {needsPrescription.length > 1 ? 'les' : 'le'} prescrit.
                  </span>
                </p>
              )}

              {error && (
                <p className="cart-error" role="alert">
                  {error}
                </p>
              )}

              <button
                type="button"
                className="button button-primary button-block"
                disabled={cart.length === 0 || overStock || needsPrescription.length > 0 || submitting}
                onClick={checkout}
              >
                {submitting ? 'Enregistrement…' : 'Encaisser'}
              </button>
            </div>
          </div>
        </aside>
      </div>
    </div>
  );
}
