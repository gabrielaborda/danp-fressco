import { api } from './axios';

// GET /admin/clientes — lista clientes con filtro opcional de estado (activo | suspendido)
export const getClientes = (params?: Record<string, any>) =>
  api.get('/admin/clientes', { params });

export const getCliente = (id: number) =>
  api.get(`/admin/clientes/${id}`);

// PUT /admin/clientes/{id} — actualiza nombre, teléfono o estado (activo | suspendido)
export const updateCliente = (id: number, data: { nombre?: string; telefono?: string; estado?: string }) =>
  api.put(`/admin/clientes/${id}`, data);
