import React from 'react';
import { FiX } from 'react-icons/fi';

export default function AppModal({ title, subtitle, onClose, children, actions, labelledBy }) {
  return (
    <div className="modal" role="dialog" aria-modal="true" aria-labelledby={labelledBy}>
      <div className="modal-content">
        <div className="modal-header">
          <div>
            {subtitle ? <p className="eyebrow">{subtitle}</p> : null}
            <h3 id={labelledBy}>{title}</h3>
          </div>
          <button className="icon-button" type="button" onClick={onClose} aria-label="Close">
            <FiX />
          </button>
        </div>
        {children}
        {actions ? <div className="modal-actions">{actions}</div> : null}
      </div>
    </div>
  );
}
