import { useCallback, useEffect, useRef, useState } from 'react';
import { ApiError } from '../api';

export interface Resource<T> {
  data: T | undefined;
  error: string | undefined;
  loading: boolean;
  reload: () => void;
}

/** Loads data on mount and whenever `key` changes. Keeps the last data on screen while reloading. */
export function useResource<T>(load: () => Promise<T>, key?: unknown): Resource<T> {
  const [data, setData] = useState<T>();
  const [error, setError] = useState<string>();
  const [loading, setLoading] = useState(true);
  const [attempt, setAttempt] = useState(0);
  const loadRef = useRef(load);
  loadRef.current = load;

  useEffect(() => {
    let current = true;
    setLoading(true);
    loadRef.current().then(
      (result) => {
        if (!current) return;
        setData(result);
        setError(undefined);
        setLoading(false);
      },
      (cause: unknown) => {
        if (!current) return;
        setError(cause instanceof ApiError ? cause.message : 'Une erreur inattendue est survenue.');
        setLoading(false);
      },
    );
    return () => {
      current = false;
    };
  }, [attempt, key]);

  const reload = useCallback(() => setAttempt((value) => value + 1), []);
  return { data, error, loading, reload };
}
