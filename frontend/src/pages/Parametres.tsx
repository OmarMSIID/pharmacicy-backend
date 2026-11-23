import { useEffect, useState, type FormEvent } from 'react';
import { useToast } from '../components/Toast';
import { Field, PageHeader } from '../components/ui';
import { useSettings } from '../lib/settings';

const SERVICES = [
  { label: 'Médicaments', path: '/api/medicaments' },
  { label: 'Stocks', path: '/api/stocks' },
  { label: 'Ordonnances', path: '/api/ordonnances' },
  { label: 'Ventes', path: '/api/ventes' },
];

type ServiceState = 'checking' | 'up' | 'down';

function ServiceStatus() {
  const [states, setStates] = useState<Record<string, ServiceState>>({});

  useEffect(() => {
    let current = true;
    for (const service of SERVICES) {
      fetch(service.path).then(
        (response) => current && setStates((all) => ({ ...all, [service.path]: response.ok ? 'up' : 'down' })),
        () => current && setStates((all) => ({ ...all, [service.path]: 'down' })),
      );
    }
    return () => {
      current = false;
    };
  }, []);

  return (
    <ul className="service-list">
      {SERVICES.map((service) => {
        const state = states[service.path] ?? 'checking';
        return (
          <li key={service.path}>
            <span>{service.label}</span>
            <span className={`service-state is-${state}`}>
              {state === 'up' ? 'Disponible' : state === 'down' ? 'Indisponible' : 'Vérification…'}
            </span>
          </li>
        );
      })}
    </ul>
  );
}

export function Parametres() {
  const toast = useToast();
  const { settings, saveSettings } = useSettings();
  const [draft, setDraft] = useState(settings);
  const [error, setError] = useState<string>();

  const update = (field: keyof typeof draft, value: string) => setDraft((current) => ({ ...current, [field]: value }));

  const submit = (event: FormEvent) => {
    event.preventDefault();
    const days = Number(draft.joursAlerte);
    if (!draft.nom.trim()) return setError('Indiquez votre nom.');
    if (!Number.isInteger(days) || days < 1 || days > 365) {
      return setError('Le délai d’alerte doit être compris entre 1 et 365 jours.');
    }
    setError(undefined);
    saveSettings({ ...draft, nom: draft.nom.trim(), joursAlerte: days });
    toast('Les paramètres ont été enregistrés.');
  };

  return (
    <div className="page page-narrow">
      <PageHeader title="Paramètres" intro="Ces réglages sont enregistrés sur cet ordinateur." />

      <form className="settings" onSubmit={submit} noValidate>
        <section className="settings-group">
          <div className="settings-copy">
            <h2>Votre profil</h2>
            <p>Votre nom apparaît dans le menu et sur le tableau de bord.</p>
          </div>
          <div className="settings-fields">
            <Field label="Nom complet">
              {(props) => <input {...props} value={draft.nom} onChange={(e) => update('nom', e.target.value)} />}
            </Field>
            <Field label="Rôle">
              {(props) => (
                <select {...props} value={draft.role} onChange={(e) => update('role', e.target.value)}>
                  <option>Administrateur</option>
                  <option>Pharmacien</option>
                  <option>Préparateur</option>
                </select>
              )}
            </Field>
          </div>
        </section>

        <section className="settings-group">
          <div className="settings-copy">
            <h2>La pharmacie</h2>
            <p>Ces informations sont imprimées en haut des tickets de vente.</p>
          </div>
          <div className="settings-fields">
            <Field label="Nom de la pharmacie">
              {(props) => (
                <input {...props} value={draft.pharmacie} onChange={(e) => update('pharmacie', e.target.value)} />
              )}
            </Field>
            <Field label="Adresse">
              {(props) => <input {...props} value={draft.adresse} onChange={(e) => update('adresse', e.target.value)} />}
            </Field>
            <Field label="Téléphone">
              {(props) => (
                <input
                  {...props}
                  type="tel"
                  value={draft.telephone}
                  onChange={(e) => update('telephone', e.target.value)}
                />
              )}
            </Field>
          </div>
        </section>

        <section className="settings-group">
          <div className="settings-copy">
            <h2>Alertes</h2>
            <p>À partir de quand un lot proche de sa péremption doit vous être signalé.</p>
          </div>
          <div className="settings-fields">
            <Field label="Délai d’alerte (jours)">
              {(props) => (
                <input
                  {...props}
                  className="input-number input-short"
                  inputMode="numeric"
                  value={draft.joursAlerte}
                  onChange={(e) => update('joursAlerte', e.target.value)}
                />
              )}
            </Field>
          </div>
        </section>

        <div className="settings-save">
          {error && (
            <p className="form-error" role="alert">
              {error}
            </p>
          )}
          <button type="submit" className="button button-primary">
            Enregistrer
          </button>
        </div>
      </form>

      <section className="settings-group settings-services">
        <div className="settings-copy">
          <h2>État des services</h2>
          <p>Vérifié à l’ouverture de cette page.</p>
        </div>
        <div className="settings-fields">
          <ServiceStatus />
        </div>
      </section>
    </div>
  );
}
