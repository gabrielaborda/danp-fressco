import { api } from './axios';

export const getDiscountRules = (params?: Record<string, any>) =>
  api.get('/admin/descuentos/reglas', { params });

export const createDiscountRule = (data: Record<string, any>) =>
  api.post('/admin/descuentos/reglas', data);

export const updateDiscountRule = (id: number, data: Record<string, any>) =>
  api.put(`/admin/descuentos/reglas/${id}`, data);

export const deleteDiscountRule = (id: number) =>
  api.delete(`/admin/descuentos/reglas/${id}`);

export const getDiscountHistory = (params?: Record<string, any>) =>
  api.get('/admin/descuentos/historial', { params });
