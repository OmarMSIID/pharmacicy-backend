import type { StockLot } from '../api';
import { dayKey, parseDay } from './format';

export type LotStatus = 'perime' | 'expire' | 'faible' | 'epuise' | 'normal';

const DAY_MS = 86_400_000;

export function daysUntil(day: string, today = new Date()): number {
  const start = parseDay(dayKey(today)).getTime();
  return Math.round((parseDay(day).getTime() - start) / DAY_MS);
}

/** stock-service only sells a lot strictly before its expiry date, so a lot expiring today counts as expired. */
export function lotStatus(lot: StockLot, alertDays: number): LotStatus {
  if (lot.quantite <= 0) return 'epuise';
  const days = daysUntil(lot.datePeremption);
  if (days <= 0) return 'perime';
  if (days <= alertDays) return 'expire';
  if (lot.quantite <= (lot.seuilAlerte ?? 10)) return 'faible';
  return 'normal';
}

export const needsAttention = (status: LotStatus) => status === 'perime' || status === 'expire' || status === 'faible';

const ATTENTION_ORDER: Record<LotStatus, number> = { perime: 0, expire: 1, faible: 2, epuise: 3, normal: 4 };

export function byUrgency(lots: StockLot[], alertDays: number): StockLot[] {
  return [...lots].sort(
    (a, b) =>
      ATTENTION_ORDER[lotStatus(a, alertDays)] - ATTENTION_ORDER[lotStatus(b, alertDays)] ||
      a.datePeremption.localeCompare(b.datePeremption),
  );
}

/** Units that can be sold per medicament, using the same rule as stock-service. */
export function availableByMedicament(lots: StockLot[]): Map<number, number> {
  const available = new Map<number, number>();
  for (const lot of lots) {
    if (daysUntil(lot.datePeremption) > 0) {
      available.set(lot.medicamentId, (available.get(lot.medicamentId) ?? 0) + lot.quantite);
    }
  }
  return available;
}

export function expiryText(day: string): string {
  const days = daysUntil(day);
  if (days < 0) return days === -1 ? 'périmé depuis hier' : `périmé depuis ${-days} j`;
  if (days === 0) return 'périme aujourd’hui';
  if (days === 1) return 'périme demain';
  return `dans ${days} j`;
}
