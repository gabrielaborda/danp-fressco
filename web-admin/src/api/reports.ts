import { api } from './axios';

export const getTopSellingProducts = (limit: number = 5) =>
  api.get('/admin/reportes/productos-mas-vendidos', { params: { limite: limit } });

export const getExpiringLotsReport = (days: number = 10) =>
  api.get('/admin/reportes/lotes-proximos-a-vencer', { params: { dias: days } });

export const getSalesPeriodReport = (params?: Record<string, any>) =>
  api.get('/admin/reportes/ventas-por-periodo', { params });
