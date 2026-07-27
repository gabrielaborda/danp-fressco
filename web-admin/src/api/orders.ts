import { api } from './axios';

// GET /admin/pedidos — lista con filtros opcionales (estado, fecha_desde, fecha_hasta, tienda_id)
export const getOrders = (params?: Record<string, any>) =>
  api.get('/admin/pedidos', { params });

// PUT /admin/pedidos/{id}/estado — cambia el estado (confirmado | entregado | cancelado)
// Items del pedido ya vienen incluidos en el PedidoResponse
export const updateOrderStatus = (id: number, estado: string) =>
  api.put(`/admin/pedidos/${id}/estado`, { estado });
