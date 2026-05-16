import React from 'react';
import AppModal from './AppModal';

export default function ConfirmDialog({
  open,
  title,
  message,
  confirmLabel = 'Confirm',
  cancelLabel = 'Cancel',
  danger = false,
  onCancel,
  onConfirm
}) {
  if (!open) return null;

  return (
    <AppModal
      title={title}
      subtitle="Confirmation"
      labelledBy="confirm-dialog-title"
      onClose={onCancel}
      actions={
        <>
          <button className="btn-secondary" type="button" onClick={onCancel}>
            {cancelLabel}
          </button>
          <button
            className={danger ? 'btn-danger-outline' : 'btn-primary'}
            type="button"
            onClick={onConfirm}
          >
            {confirmLabel}
          </button>
        </>
      }
    >
      <p>{message}</p>
    </AppModal>
  );
}
