import { useCallback, useState } from 'react';

let nextToastId = 1;

export default function useToasts() {
  const [toasts, setToasts] = useState([]);

  const removeToast = useCallback((id) => {
    setToasts((current) => current.filter((toast) => toast.id !== id));
  }, []);

  const pushToast = useCallback((message, type = 'info') => {
    const id = nextToastId++;
    setToasts((current) => [...current, { id, message, type }]);
    window.setTimeout(() => removeToast(id), 3500);
  }, [removeToast]);

  return { toasts, pushToast, removeToast };
}
