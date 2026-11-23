import { useEffect } from 'react';
import { useParams, useSearchParams } from 'react-router-dom';
import { api } from '../api';
import { Ticket } from '../components/Ticket';
import { ErrorNote, LoadingRows } from '../components/ui';
import { useResource } from '../lib/useResource';

export function TicketPage() {
  const { id } = useParams();
  const [params] = useSearchParams();
  const printOnLoad = params.has('imprimer');
  const vente = useResource(() => api.vente(Number(id)), id);

  useEffect(() => {
    if (vente.data && printOnLoad) window.print();
  }, [vente.data, printOnLoad]);

  return (
    <div className="ticket-page">
      {vente.error ? (
        <ErrorNote message={vente.error} onRetry={vente.reload} />
      ) : vente.data ? (
        <Ticket vente={vente.data} />
      ) : (
        <LoadingRows />
      )}
    </div>
  );
}
