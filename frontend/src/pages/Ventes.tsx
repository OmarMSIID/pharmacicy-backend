import { Plus, Printer } from 'lucide-react';
import { useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { api, type Vente } from '../api';
import { openTicketForPrint, Ticket } from '../components/Ticket';
import { Drawer, EmptyNote, ErrorNote, LoadingRows, PageHeader } from '../components/ui';
import { addDays, dayKey, formatDate, formatMoney, formatTime, plural, saleNumber } from '../lib/format';
import { useResource } from '../lib/useResource';

const PERIODS = [
  { id: 'jour', label: 'Aujourd’hui', days: 1 },
  { id: 'semaine', label: '7 jours', days: 7 },
  { id: 'mois', label: '30 jours', days: 30 },
  { id: 'tout', label: 'Tout', days: null },
] as const;

type PeriodId = (typeof PERIODS)[number]['id'];

function summarize(vente: Vente): string {
  const names = vente.lignes.map((ligne) => ligne.nomCommercial ?? `n° ${ligne.medicamentId}`);
  return names.length > 2 ? `${names.slice(0, 2).join(', ')} et ${names.length - 2} autre${names.length > 3 ? 's' : ''}` : names.join(', ');
}

export function Ventes() {
  const ventes = useResource(api.ventes);
  const [period, setPeriod] = useState<PeriodId>('semaine');
  const [selected, setSelected] = useState<Vente>();

  const visible = useMemo(() => {
    const { days } = PERIODS.find((candidate) => candidate.id === period)!;
    if (days === null) return ventes.data ?? [];
    const from = dayKey(addDays(new Date(), -days + 1));
    return (ventes.data ?? []).filter((vente) => dayKey(new Date(vente.dateVente)) >= from);
  }, [ventes.data, period]);

  const periodTotal = visible.reduce((sum, vente) => sum + vente.total, 0);

  return (
    <div className="page">
      <PageHeader
        title="Ventes"
        intro={
          ventes.data
            ? `${plural(visible.length, 'vente')} sur la période, pour ${formatMoney(periodTotal)}.`
            : 'L’historique des ventes de la pharmacie.'
        }
        actions={
          <Link to="/ventes/nouvelle" className="button button-primary">
            <Plus size={16} aria-hidden />
            Nouvelle vente
          </Link>
        }
      />

      <div className="toolbar">
        <div className="segmented" role="group" aria-label="Période">
          {PERIODS.map((candidate) => (
            <button
              type="button"
              key={candidate.id}
              aria-pressed={period === candidate.id}
              onClick={() => setPeriod(candidate.id)}
            >
              {candidate.label}
            </button>
          ))}
        </div>
      </div>

      {ventes.error ? (
        <ErrorNote message={ventes.error} onRetry={ventes.reload} />
      ) : ventes.loading && !ventes.data ? (
        <LoadingRows rows={8} />
      ) : visible.length === 0 ? (
        <EmptyNote title="Aucune vente sur cette période">
          <Link to="/ventes/nouvelle" className="text-link">
            Enregistrer une vente
          </Link>
        </EmptyNote>
      ) : (
        <div className="table-wrap">
          <table className="table table-rows-clickable">
            <thead>
              <tr>
                <th scope="col">N°</th>
                <th scope="col">Date</th>
                <th scope="col">Articles</th>
                <th scope="col" className="num">
                  Qté
                </th>
                <th scope="col" className="num">
                  Montant
                </th>
              </tr>
            </thead>
            <tbody>
              {visible.map((vente) => {
                const date = new Date(vente.dateVente);
                return (
                  <tr key={vente.id} onClick={() => setSelected(vente)}>
                    <td>
                      <button type="button" className="row-link mono" onClick={() => setSelected(vente)}>
                        {saleNumber(vente.id)}
                      </button>
                    </td>
                    <td className="nowrap">
                      {formatDate(date)}
                      <span className="muted">, {formatTime(date)}</span>
                    </td>
                    <td className="cell-clip">{summarize(vente)}</td>
                    <td className="num">{vente.lignes.reduce((sum, ligne) => sum + ligne.quantite, 0)}</td>
                    <td className="num strong">{formatMoney(vente.total)}</td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      )}

      <Drawer
        open={selected !== undefined}
        title={selected ? `Vente n° ${saleNumber(selected.id)}` : 'Vente'}
        onClose={() => setSelected(undefined)}
      >
        {selected && (
          <div className="drawer-ticket">
            <Ticket vente={selected} />
            <button type="button" className="button" onClick={() => openTicketForPrint(selected.id)}>
              <Printer size={16} aria-hidden />
              Imprimer le ticket
            </button>
          </div>
        )}
      </Drawer>
    </div>
  );
}
