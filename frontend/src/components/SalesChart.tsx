import { useMemo } from 'react';
import type { Vente } from '../api';
import { addDays, dayKey, formatAxisDay, formatCount, formatLongDay, formatMoney, plural } from '../lib/format';

const DAYS = 7;

interface Day {
  key: string;
  date: Date;
  total: number;
  count: number;
}

function buildDays(ventes: Vente[]): Day[] {
  const today = new Date();
  const days: Day[] = Array.from({ length: DAYS }, (_, index) => {
    const date = addDays(today, index - DAYS + 1);
    return { key: dayKey(date), date, total: 0, count: 0 };
  });
  const byKey = new Map(days.map((day) => [day.key, day]));
  for (const vente of ventes) {
    const day = byKey.get(dayKey(new Date(vente.dateVente)));
    if (day) {
      day.total += vente.total;
      day.count += 1;
    }
  }
  return days;
}

/** Rounds up to a value whose half is also a round number, so that the axis stays readable. */
function niceCeiling(value: number): number {
  if (value <= 0) return 100;
  const magnitude = 10 ** Math.floor(Math.log10(value));
  const step = [1, 2, 3, 4, 5, 6, 8, 10].find((candidate) => candidate * magnitude >= value) ?? 10;
  return step * magnitude;
}

export function SalesChart({ ventes }: { ventes: Vente[] }) {
  const days = useMemo(() => buildDays(ventes), [ventes]);
  const ceiling = niceCeiling(Math.max(...days.map((day) => day.total)));
  const ticks = [ceiling, ceiling / 2, 0];

  return (
    <div className="chart" role="group" aria-label={`Recettes des ${DAYS} derniers jours`}>
      <div className="chart-plot">
        {ticks.map((tick) => (
          <div className="chart-grid" key={tick} style={{ bottom: `${(tick / ceiling) * 100}%` }}>
            <span>{formatCount(tick)}</span>
          </div>
        ))}
        <div className="chart-columns">
          {days.map((day, index) => {
            const isToday = index === days.length - 1;
            const height = day.total > 0 ? Math.max((day.total / ceiling) * 100, 1.5) : 0;
            return (
              <div
                className="chart-column"
                key={day.key}
                tabIndex={0}
                role="img"
                aria-label={`${formatLongDay(day.date)} : ${formatMoney(day.total)}, ${plural(day.count, 'vente')}`}
              >
                <div className={`chart-bar${isToday ? ' is-today' : ''}`} style={{ height: `${height}%` }} />
                <div className="chart-tip" style={{ bottom: `calc(${height}% + 8px)` }} aria-hidden>
                  <strong>{formatMoney(day.total)}</strong>
                  <span>{plural(day.count, 'vente')}</span>
                  <span>{formatLongDay(day.date)}</span>
                </div>
              </div>
            );
          })}
        </div>
      </div>
      <div className="chart-axis" aria-hidden>
        {days.map((day, index) => (
          <span key={day.key} className={index === days.length - 1 ? 'is-today' : undefined}>
            {index === days.length - 1 ? (
              <>
                <span className="axis-long">Aujourd’hui</span>
                <span className="axis-short">Auj.</span>
              </>
            ) : (
              formatAxisDay(day.date)
            )}
          </span>
        ))}
      </div>
    </div>
  );
}
