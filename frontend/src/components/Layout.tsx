import { ClipboardList, LayoutDashboard, Package, Pill, Plus, ReceiptText, Settings as SettingsIcon } from 'lucide-react';
import { Link, NavLink, Outlet, useLocation } from 'react-router-dom';
import { api } from '../api';
import { useSettings } from '../lib/settings';
import { lotStatus, needsAttention } from '../lib/stock';
import { useResource } from '../lib/useResource';

const NAV = [
  { to: '/', label: 'Tableau de bord', icon: LayoutDashboard, end: true },
  { to: '/ventes', label: 'Ventes', icon: ReceiptText, end: false },
  { to: '/ordonnances', label: 'Ordonnances', icon: ClipboardList, end: false },
  { to: '/medicaments', label: 'Médicaments', icon: Pill, end: false },
  { to: '/stocks', label: 'Stocks', icon: Package, end: false },
  { to: '/parametres', label: 'Paramètres', icon: SettingsIcon, end: false },
];

export function Logo() {
  return (
    <svg className="logo-mark" viewBox="0 0 32 32" aria-hidden>
      <path d="M11.5 2h9a1 1 0 0 1 1 1v7.5H29a1 1 0 0 1 1 1v9a1 1 0 0 1-1 1h-7.5V29a1 1 0 0 1-1 1h-9a1 1 0 0 1-1-1v-7.5H3a1 1 0 0 1-1-1v-9a1 1 0 0 1 1-1h7.5V3a1 1 0 0 1 1-1z" />
    </svg>
  );
}

const initials = (name: string) =>
  name
    .split(/\s+/)
    .filter(Boolean)
    .slice(0, 2)
    .map((part) => part[0].toUpperCase())
    .join('');

export function Layout() {
  const { pathname } = useLocation();
  const { settings } = useSettings();
  const lots = useResource(api.lots, pathname);
  const alerts = lots.data?.filter((lot) => needsAttention(lotStatus(lot, settings.joursAlerte))).length ?? 0;
  const ordonnances = useResource(api.ordonnances, pathname);
  const pending = ordonnances.data?.filter((ordonnance) => ordonnance.statut === 'EN_ATTENTE').length ?? 0;

  return (
    <div className="shell">
      <aside className="sidebar">
        <Link to="/" className="brand">
          <Logo />
          <span>{settings.pharmacie || 'PharmaCare'}</span>
        </Link>

        <Link to="/ventes/nouvelle" className="button button-primary sidebar-sale">
          <Plus size={16} aria-hidden />
          Nouvelle vente
        </Link>

        <nav aria-label="Navigation principale">
          <ul className="nav-list">
            {NAV.map(({ to, label, icon: Icon, end }) => (
              <li key={to}>
                <NavLink to={to} end={end} className="nav-link">
                  <Icon size={17} strokeWidth={1.75} aria-hidden />
                  <span>{label}</span>
                  {to === '/stocks' && alerts > 0 && (
                    <span className="nav-count" aria-label={`${alerts} alertes`}>
                      {alerts}
                    </span>
                  )}
                  {to === '/ordonnances' && pending > 0 && (
                    <span className="nav-count nav-count-neutral" aria-label={`${pending} ordonnances en attente`}>
                      {pending}
                    </span>
                  )}
                </NavLink>
              </li>
            ))}
          </ul>
        </nav>

        <Link to="/parametres" className="sidebar-user">
          <span className="avatar" aria-hidden>
            {initials(settings.nom)}
          </span>
          <span className="sidebar-user-text">
            <strong>{settings.nom}</strong>
            <small>{settings.role}</small>
          </span>
        </Link>
      </aside>

      <main className="content">
        <Outlet />
      </main>
    </div>
  );
}
