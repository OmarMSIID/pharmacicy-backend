const amount = new Intl.NumberFormat('fr-FR', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
const integer = new Intl.NumberFormat('fr-FR');
const shortDate = new Intl.DateTimeFormat('fr-FR', { day: 'numeric', month: 'short', year: 'numeric' });
const time = new Intl.DateTimeFormat('fr-FR', { hour: '2-digit', minute: '2-digit' });
const longDay = new Intl.DateTimeFormat('fr-FR', { weekday: 'long', day: 'numeric', month: 'long' });
const axisDay = new Intl.DateTimeFormat('fr-FR', { weekday: 'short', day: 'numeric' });

export const formatAmount = (value: number) => amount.format(value);
export const formatMoney = (value: number) => `${amount.format(value)} MAD`;
export const formatCount = (value: number) => integer.format(value);
export const formatDate = (value: Date) => shortDate.format(value);
export const formatTime = (value: Date) => time.format(value);
export const formatLongDay = (value: Date) => longDay.format(value);
export const formatAxisDay = (value: Date) => axisDay.format(value);

export const plural = (count: number, singular: string, pluralForm = `${singular}s`) =>
  `${formatCount(count)} ${count > 1 ? pluralForm : singular}`;

export const saleNumber = (id: number) => String(id).padStart(6, '0');

/** Reads a yyyy-mm-dd value as a local date, so it never shifts by a day across time zones. */
export function parseDay(value: string): Date {
  const [year, month, day] = value.split('-').map(Number);
  return new Date(year, month - 1, day);
}

/** Local yyyy-mm-dd key of a date. */
export function dayKey(value: Date): string {
  const month = String(value.getMonth() + 1).padStart(2, '0');
  const day = String(value.getDate()).padStart(2, '0');
  return `${value.getFullYear()}-${month}-${day}`;
}

export function addDays(value: Date, days: number): Date {
  return new Date(value.getFullYear(), value.getMonth(), value.getDate() + days);
}

/** Lowercase without accents, so that "ibuprofene" finds "Ibuprofène". */
export const normalize = (value: string | null | undefined) =>
  (value ?? '')
    .normalize('NFD')
    .replace(/\p{Diacritic}/gu, '')
    .toLowerCase();
