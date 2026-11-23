import { Navigate, Route, Routes } from 'react-router-dom';
import { Layout } from './components/Layout';
import { ToastProvider } from './components/Toast';
import { SettingsProvider } from './lib/settings';
import { Medicaments } from './pages/Medicaments';
import { NouvelleVente } from './pages/NouvelleVente';
import { Ordonnances } from './pages/Ordonnances';
import { Parametres } from './pages/Parametres';
import { Stocks } from './pages/Stocks';
import { TableauDeBord } from './pages/TableauDeBord';
import { TicketPage } from './pages/TicketPage';
import { Ventes } from './pages/Ventes';

export function App() {
  return (
    <SettingsProvider>
      <ToastProvider>
        <Routes>
          <Route path="/ventes/:id/ticket" element={<TicketPage />} />
          <Route element={<Layout />}>
            <Route index element={<TableauDeBord />} />
            <Route path="ventes" element={<Ventes />} />
            <Route path="ventes/nouvelle" element={<NouvelleVente />} />
            <Route path="ordonnances" element={<Ordonnances />} />
            <Route path="medicaments" element={<Medicaments />} />
            <Route path="stocks" element={<Stocks />} />
            <Route path="parametres" element={<Parametres />} />
            <Route path="*" element={<Navigate to="/" replace />} />
          </Route>
        </Routes>
      </ToastProvider>
    </SettingsProvider>
  );
}
