function ConfirmDialog({ open, title, message, confirmLabel = 'Confirm', cancelLabel = 'Cancel', danger, busy, onConfirm, onCancel }) {
  if (!open) return null;

  return (
    <div className="confirm-box" role="dialog" aria-modal="true" aria-labelledby="confirm-title">
      {title && (
        <p id="confirm-title" className="confirm-title">
          {title}
        </p>
      )}
      <p>{message}</p>
      <button
        type="button"
        className={danger ? 'btn btn-danger' : 'btn btn-primary'}
        onClick={onConfirm}
        disabled={busy}
      >
        {busy ? 'Please wait...' : confirmLabel}
      </button>
      <button type="button" className="btn btn-secondary" onClick={onCancel} disabled={busy}>
        {cancelLabel}
      </button>
    </div>
  );
}

export default ConfirmDialog;
