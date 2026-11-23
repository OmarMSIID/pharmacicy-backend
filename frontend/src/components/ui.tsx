import { X } from 'lucide-react';
import { useEffect, useId, useRef, type ReactNode } from 'react';
import type { LotStatus } from '../lib/stock';

export function PageHeader({ title, intro, actions }: { title: string; intro?: ReactNode; actions?: ReactNode }) {
  return (
    <header className="page-header">
      <div>
        <h1>{title}</h1>
        {intro && <p className="page-intro">{intro}</p>}
      </div>
      {actions && <div className="page-actions">{actions}</div>}
    </header>
  );
}

export function ErrorNote({ message, onRetry }: { message: string; onRetry?: () => void }) {
  return (
    <div className="note note-error" role="alert">
      <p>{message}</p>
      {onRetry && (
        <button type="button" className="button button-small" onClick={onRetry}>
          Réessayer
        </button>
      )}
    </div>
  );
}

export function LoadingRows({ rows = 5 }: { rows?: number }) {
  return (
    <div className="loading-rows" role="status" aria-label="Chargement">
      {Array.from({ length: rows }, (_, index) => (
        <span key={index} style={{ width: `${88 - (index % 3) * 14}%` }} />
      ))}
    </div>
  );
}

export function EmptyNote({ title, children }: { title: string; children?: ReactNode }) {
  return (
    <div className="empty-note">
      <p className="empty-title">{title}</p>
      {children && <div className="empty-body">{children}</div>}
    </div>
  );
}

const STATUS_LABELS: Record<LotStatus, string> = {
  perime: 'Périmé',
  expire: 'Expire bientôt',
  faible: 'Stock faible',
  epuise: 'Épuisé',
  normal: 'En règle',
};

export function StatusTag({ status }: { status: LotStatus }) {
  return <span className={`status status-${status}`}>{STATUS_LABELS[status]}</span>;
}

export function Field({
  label,
  hint,
  children,
}: {
  label: string;
  hint?: string;
  children: (props: { id: string; 'aria-describedby'?: string }) => ReactNode;
}) {
  const id = useId();
  const hintId = `${id}-hint`;
  return (
    <div className="field">
      <label htmlFor={id}>{label}</label>
      {children({ id, 'aria-describedby': hint ? hintId : undefined })}
      {hint && (
        <small id={hintId} className="field-hint">
          {hint}
        </small>
      )}
    </div>
  );
}

export function Drawer({
  open,
  title,
  onClose,
  children,
}: {
  open: boolean;
  title: string;
  onClose: () => void;
  children: ReactNode;
}) {
  const ref = useRef<HTMLDialogElement>(null);

  useEffect(() => {
    const dialog = ref.current;
    if (!dialog) return;
    if (open && !dialog.open) {
      dialog.showModal();
      // showModal focuses the close button first; a form should start on its first field
      dialog.querySelector<HTMLElement>('input, select')?.focus();
    }
    if (!open && dialog.open) dialog.close();
  }, [open]);

  return (
    <dialog
      ref={ref}
      className="drawer"
      aria-label={title}
      onCancel={(event) => {
        event.preventDefault();
        onClose();
      }}
      onClick={(event) => {
        if (event.target === ref.current) onClose();
      }}
    >
      {open && (
        <div className="drawer-body">
          <header className="drawer-header">
            <h2>{title}</h2>
            <button type="button" className="icon-button" onClick={onClose} aria-label="Fermer">
              <X size={18} aria-hidden />
            </button>
          </header>
          {children}
        </div>
      )}
    </dialog>
  );
}
