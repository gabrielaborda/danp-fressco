import { api } from './axios';

// The backend handles manual discounts individually per lot.
// We use this API to set (or overwrite) the manual discount for a lot.
export const updateLotDiscount = (loteId: number, data: { 
    porcentaje?: number; 
    monto_fijo?: number; 
    fecha_inicio: string; 
    fecha_fin: string;
    descripcion?: string;
}) => api.put(`/admin/lotes/${loteId}/descuento`, data);

export const deactivateLotDiscount = (loteId: number) => {
    // To deactivate, we create a discount with 0% that takes precedence or 
    // simply create an inactive one. But the backend disables all active manual discounts 
    // when we PUT a new one. So we can just put 0% starting and ending today.
    return updateLotDiscount(loteId, {
        porcentaje: 0,
        fecha_inicio: new Date().toISOString().split('T')[0],
        fecha_fin: new Date().toISOString().split('T')[0],
        descripcion: "Desactivar descuento"
    });
};

