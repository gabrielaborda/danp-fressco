import axios from 'axios';

const api = axios.create({
  baseURL: 'http://127.0.0.1:8000/api/v1',
  headers: {
    'Content-Type': 'application/json',
  },
});

// Interceptor para inyectar el token en cada petición
api.interceptors.request.use(
  (config) => {
    // Obtenemos el token desde localStorage, donde Zustand lo persistirá
    const authStorage = localStorage.getItem('auth-storage');
    if (authStorage) {
      try {
        const { state } = JSON.parse(authStorage);
        if (state && state.token) {
          config.headers.Authorization = `Bearer ${state.token}`;
        }
      } catch (error) {
        console.error("Error parseando el auth-storage", error);
      }
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

export { api };
