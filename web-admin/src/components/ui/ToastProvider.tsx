import React, { createContext, useContext } from 'react';
import { Toaster, toast } from 'react-hot-toast';

interface ToastContextValue {
  success: (msg: string) => void;
  error: (msg: string) => void;
  info: (msg: string) => void;
}

const ToastContext = createContext<ToastContextValue | null>(null);

export const ToastProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const show = {
    success: (msg: string) => toast.success(msg),
    error: (msg: string) => toast.error(msg),
    info: (msg: string) => toast(msg),
  };

  return (
    <ToastContext.Provider value={show}>
      {children}
      <Toaster
        toastOptions={{
          style: {
            background: 'var(--color-surface-variant)',
            color: 'var(--color-text-primary)',
          },
        }}
      />
    </ToastContext.Provider>
  );
};

export const useToast = (): ToastContextValue => {
  const ctx = useContext(ToastContext);
  if (!ctx) throw new Error('useToast must be used within ToastProvider');
  return ctx;
};
