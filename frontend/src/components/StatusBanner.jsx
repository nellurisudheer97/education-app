import React from 'react';

export default function StatusBanner({ type = 'info', message, actionLabel, onAction }) {
  if (!message) return null;

  const className = type === 'error' ? 'notice error-notice' : 'notice';

  return (
    <div className={className} role={type === 'error' ? 'alert' : 'status'}>
      <span>{message}</span>
      {actionLabel && onAction && (
        <button type="button" onClick={onAction}>
          {actionLabel}
        </button>
      )}
    </div>
  );
}
