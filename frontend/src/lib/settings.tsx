import { createContext, useCallback, useContext, useMemo, useState, type ReactNode } from 'react';

export interface Settings {
  nom: string;
  role: string;
  pharmacie: string;
  adresse: string;
  telephone: string;
  joursAlerte: number;
}

const DEFAULTS: Settings = {
  nom: 'Mohamed Louhabi',
  role: 'Administrateur',
  pharmacie: 'PharmaCare',
  adresse: '',
  telephone: '',
  joursAlerte: 30,
};

const STORAGE_KEY = 'pharmacare.settings';

function readSettings(): Settings {
  try {
    const stored = localStorage.getItem(STORAGE_KEY);
    return stored ? { ...DEFAULTS, ...(JSON.parse(stored) as Partial<Settings>) } : DEFAULTS;
  } catch {
    return DEFAULTS;
  }
}

interface SettingsContextValue {
  settings: Settings;
  saveSettings: (settings: Settings) => void;
}

const SettingsContext = createContext<SettingsContextValue | null>(null);

export function SettingsProvider({ children }: { children: ReactNode }) {
  const [settings, setSettings] = useState(readSettings);

  const saveSettings = useCallback((next: Settings) => {
    setSettings(next);
    try {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(next));
    } catch {
      // Storage can be blocked; the values still apply until the page is closed
    }
  }, []);

  const value = useMemo(() => ({ settings, saveSettings }), [settings, saveSettings]);
  return <SettingsContext.Provider value={value}>{children}</SettingsContext.Provider>;
}

export function useSettings(): SettingsContextValue {
  const value = useContext(SettingsContext);
  if (!value) throw new Error('useSettings must be used inside SettingsProvider');
  return value;
}
