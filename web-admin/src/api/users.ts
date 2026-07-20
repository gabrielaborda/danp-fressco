import { api } from './axios';

export const getUsers = (params?: Record<string, any>) =>
  api.get('/admin/usuarios', { params });

export const updateUserStatus = (id: number, isActive: boolean) =>
  api.patch(`/admin/usuarios/${id}/status`, { is_active: isActive });

export const getUserStats = (id: number) =>
  api.get(`/admin/usuarios/${id}/stats`);
