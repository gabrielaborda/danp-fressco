import { api } from './axios';

export const getLots = (params?: Record<string, any>) =>
  api.get('/admin/lotes', { params });

export const getLot = (id: number) =>
  api.get(`/admin/lotes/${id}`);

export const createLot = (data: Record<string, any>) =>
  api.post('/admin/lotes', data);

export const updateLot = (id: number, data: Record<string, any>) =>
  api.put(`/admin/lotes/${id}`, data);

export const deleteLot = (id: number) =>
  api.delete(`/admin/lotes/${id}`);

export const getExpiringLots = (days: number = 5) =>
  api.get('/admin/reportes/lotes-proximos-a-vencer', { params: { dias: days } });
