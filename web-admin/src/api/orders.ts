import { api } from './axios';

export const getOrders = (params?: Record<string, any>) =>
  api.get('/admin/pedidos', { params });

export const getOrder = (id: number) =>
  api.get(`/admin/pedidos/${id}`);

export const updateOrderStatus = (id: number, status: string) =>
  api.patch(`/admin/pedidos/${id}/status`, { status });

export const getOrderItems = (orderId: number) =>
  api.get(`/admin/pedidos/${orderId}/items`);
