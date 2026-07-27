import { api } from './axios';

export const getProducts = (params?: Record<string, any>) =>
  api.get('/admin/productos', { params });

export const getProduct = (id: number) =>
  api.get(`/admin/productos/${id}`);

export const createProduct = (data: Record<string, any>) =>
  api.post('/admin/productos', data);

export const updateProduct = (id: number, data: Record<string, any>) =>
  api.put(`/admin/productos/${id}`, data);

export const deleteProduct = (id: number) =>
  api.delete(`/admin/productos/${id}`);
