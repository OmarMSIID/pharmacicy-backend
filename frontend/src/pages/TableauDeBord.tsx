import { ArrowRight, TriangleAlert } from 'lucide-react';
import { useMemo } from 'react';
import { Link } from 'react-router-dom';
import { api, type Vente } from '../api';
import { SalesChart } from '../components/SalesChart';
import { EmptyNote, ErrorNote, LoadingRows, StatusTag } from '../components/ui';
import {
  addDays,
  dayKey,
  formatCount,
  formatAmount,
  formatDate,
  formatLongDay,
  formatMoney,
  formatTime,
  plural,
  saleNumber,
} from '../lib/format';
import { useSettings } from '../lib/settings';
import { availableByMedicament, byUrgency, expiryText, lotStatus, needsAttention } from '../lib/stock';
import { useResource } from '../lib/useResource';

const MAX_ALERTS = 6;

function greeting(): string {
  return new Date().getHours() >= 18 ? 'Bonsoir' : 'Bonjour';
}

const sumOfDay = (ventes: Vente[], key: string) =>
  ventes.filter((vente) => dayKey(new Date(vente.dateVente)) === key);

const total = (ventes: Vente[]) => ventes.reduce((sum, vente) => sum + vente.total, 0);

export function TableauDeBord() {
  const { settings } = useSettings();
  const ventes = useResource(api.ventes);
  const lots = useResource(api.lots);
  const medicaments = useResource(api.medicaments);
  const ordonnances = useResource(api.ordonnances);
  const pendingOrdonnances = ordonnances.data?.filter((ordonnance) => ordonnance.statut === 'EN_ATTENTE').length;

  const names = useMemo(
    () => new Map(medicaments.data?.map((medicament) => [medicament.id, medicament.nomCommercial])),
    [medicaments.data],
  );
  const alerts = useMemo(
    () =>
      byUrgency(
        (lots.data ?? []).filter((lot) => needsAttention(lotStatus(lot, settings.joursAlerte))),
        settings.joursAlerte,
      ),
    [lots.data, settings.joursAlerte],
  );

  const now = new Date();
  const firstName = settings.nom.trim().split(/\s+/)[0];
  const todaySales = sumOfDay(ventes.data ?? [], dayKey(now));
  const yesterdaySales = sumOfDay(ventes.data ?? [], dayKey(addDays(now, -1)));
  const todayTotal = total(todaySales);
  const units = lots.data
    ? [...availableByMedicament(lots.data).values()].reduce((sum, quantity) => sum + quantity, 0)
    : undefined;

  return (
    <div className="page page-wide">
      <header className="dash-head">
        <p className="dash-date">{formatLongDay(now)}</p>
        <h1>
          {greeting()} {firstName}.
        </h1>
      </header>

      <div className="dash-grid">
        <section className="dash-sales" aria-labelledby="recettes-title">
          <h2 id="recettes-title" className="section-label">
            Recettes du jour
          </h2>
          {ventes.error ? (
            <ErrorNote message={ventes.error} onRetry={ventes.reload} />
          ) : ventes.loading && !ventes.data ? (
            <LoadingRows rows={4} />
          ) : (
            <>
              <p className="hero-figure">
                {formatAmount(todayTotal)} <span>MAD</span>
              </p>
              <p className="hero-detail">
                {todaySales.length === 0
                  ? 'Aucune vente pour l’instant aujourd’hui'
                  : `${plural(todaySales.length, 'vente')}, panier moyen ${formatMoney(todayTotal / todaySales.length)}`}
                <span className="hero-detail-muted"> · hier {formatMoney(total(yesterdaySales))}</span>
              </p>
              <SalesChart ventes={ventes.data ?? []} />
            </>
          )}
        </section>

        <section className="dash-watch" aria-labelledby="alertes-title">
          <div className="section-head">
            <h2 id="alertes-title" className="section-label section-label-alert">
              <TriangleAlert size={15} aria-hidden />
              Alertes
            </h2>
            {alerts.length > 0 && <span className="section-count">{alerts.length}</span>}
          </div>
          {lots.error ? (
            <ErrorNote message={lots.error} onRetry={lots.reload} />
          ) : lots.loading && !lots.data ? (
            <LoadingRows />
          ) : alerts.length === 0 ? (
            <EmptyNote title="Rien à signaler">Tous les lots sont en règle.</EmptyNote>
          ) : (
            <ul className="watch-list">
              {alerts.slice(0, MAX_ALERTS).map((lot) => {
                const status = lotStatus(lot, settings.joursAlerte);
                return (
                  <li key={lot.id}>
                    <div>
                      <strong>{names.get(lot.medicamentId) ?? `Médicament n° ${lot.medicamentId}`}</strong>
                      <small>
                        <span className="mono">{lot.numeroLot}</span> ·{' '}
                        {status === 'faible' ? `reste ${lot.quantite}` : expiryText(lot.datePeremption)}
                      </small>
                    </div>
                    <StatusTag status={status} />
                  </li>
                );
              })}
            </ul>
          )}
          <Link to={alerts.length > 0 ? '/stocks?filtre=alertes' : '/stocks'} className="text-link">
            {alerts.length > 0 ? 'Voir toutes les alertes' : 'Voir les stocks'}
            <ArrowRight size={15} aria-hidden />
          </Link>
          <dl className="watch-totals">
            <div>
              <dt>Au catalogue</dt>
              <dd>{medicaments.data ? formatCount(medicaments.data.length) : '–'}</dd>
            </div>
            <div>
              <dt>Unités vendables</dt>
              <dd>{units !== undefined ? formatCount(units) : '–'}</dd>
            </div>
            <div>
              <dt>
                <Link to="/ordonnances" className="stat-link">
                  Ordonnances à délivrer
                </Link>
              </dt>
              <dd>{pendingOrdonnances !== undefined ? formatCount(pendingOrdonnances) : '–'}</dd>
            </div>
          </dl>
        </section>
      </div>

      <section className="dash-recent" aria-labelledby="dernieres-title">
        <div className="section-head">
          <h2 id="dernieres-title" className="section-label">
            Dernières ventes
          </h2>
          <Link to="/ventes" className="text-link">
            Toutes les ventes
            <ArrowRight size={15} aria-hidden />
          </Link>
        </div>
        {ventes.data && ventes.data.length === 0 ? (
          <EmptyNote title="Aucune vente enregistrée">
            <Link to="/ventes/nouvelle" className="text-link">
              Enregistrer la première vente
            </Link>
          </EmptyNote>
        ) : (
          ventes.data && (
            <div className="table-wrap">
              <table className="table">
                <thead>
                  <tr>
                    <th scope="col">N°</th>
                    <th scope="col">Date</th>
                    <th scope="col">Articles</th>
                    <th scope="col" className="num">
                      Montant
                    </th>
                  </tr>
                </thead>
                <tbody>
                  {ventes.data.slice(0, 5).map((vente) => {
                    const date = new Date(vente.dateVente);
                    return (
                      <tr key={vente.id}>
                        <td className="mono muted">{saleNumber(vente.id)}</td>
                        <td className="nowrap">
                          {formatDate(date)}, {formatTime(date)}
                        </td>
                        <td className="cell-clip">
                          {vente.lignes.map((ligne) => ligne.nomCommercial ?? `n° ${ligne.medicamentId}`).join(', ')}
                        </td>
                        <td className="num strong">{formatMoney(vente.total)}</td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          )
        )}
      </section>
    </div>
  );
}
