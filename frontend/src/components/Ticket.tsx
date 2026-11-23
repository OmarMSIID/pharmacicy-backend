import type { Vente } from '../api';
import { formatAmount, formatDate, formatMoney, formatTime, saleNumber } from '../lib/format';
import { useSettings } from '../lib/settings';

export function Ticket({ vente }: { vente: Vente }) {
  const { settings } = useSettings();
  const date = new Date(vente.dateVente);

  return (
    <div className="ticket-shadow">
      <article className="ticket">
        <header className="ticket-head">
          <strong>{settings.pharmacie || 'PharmaCare'}</strong>
          {settings.adresse && <span>{settings.adresse}</span>}
          {settings.telephone && <span>Tél. {settings.telephone}</span>}
        </header>

        <dl className="ticket-meta">
          <div>
            <dt>Vente</dt>
            <dd>n° {saleNumber(vente.id)}</dd>
          </div>
          {vente.ordonnanceId !== null && (
            <div>
              <dt>Ordonnance</dt>
              <dd>n° {saleNumber(vente.ordonnanceId)}</dd>
            </div>
          )}
          <div>
            <dt>Date</dt>
            <dd>
              {formatDate(date)}, {formatTime(date)}
            </dd>
          </div>
        </dl>

        <ul className="ticket-lines">
          {vente.lignes.map((ligne) => (
            <li key={ligne.id}>
              <span>{ligne.nomCommercial ?? `Médicament n° ${ligne.medicamentId}`}</span>
              <span className="ticket-line-detail">
                <span>× {ligne.quantite}</span>
                <span>{formatAmount(ligne.prixTotal)}</span>
              </span>
            </li>
          ))}
        </ul>

        <div className="ticket-total">
          <span>Total</span>
          <strong>{formatMoney(vente.total)}</strong>
        </div>
        <p className="ticket-foot">Merci de votre visite</p>
      </article>
    </div>
  );
}

export const openTicketForPrint = (id: number) => window.open(`/ventes/${id}/ticket?imprimer=1`, '_blank');
