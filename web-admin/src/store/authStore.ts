import { create } from 'zustand';
import { persist } from 'zustand/middleware';

interface AuthState {
  token: string | null;
  rol: string | null;
  nombre: string | null;
  id: string | null;
  isAuthenticated: boolean;
  setAuth: (data: { access_token: string; rol: string; nombre: string; id: string }) => void;
  logout: () => void;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      token: null,
      rol: null,
      nombre: null,
      id: null,
      isAuthenticated: false,
      setAuth: (data) =>
        set({
          token: data.access_token,
          rol: data.rol,
          nombre: data.nombre,
          id: data.id,
          isAuthenticated: true,
        }),
      logout: () =>
        set({
          token: null,
          rol: null,
          nombre: null,
          id: null,
          isAuthenticated: false,
        }),
    }),
    {
      name: 'auth-storage', // Nombre para el key en localStorage
    }
  )
);
