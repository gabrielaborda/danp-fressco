import { useState, useCallback } from "react";
import { Card } from "../components/ui/Card";
import { Modal } from "../components/ui/Modal";
import { Button } from "../components/ui/Button";
import { Badge } from "../components/ui/Badge";
import { DataTable, type Column } from "../components/ui/DataTable";
import { getOrders, updateOrderStatus } from "../api/orders";
import {
    ShoppingCart, ClipboardList, CheckCircle2,
    PackageCheck, XCircle, Calendar, User
} from "lucide-react";
import { toast } from "react-toastify";

// ─── Types ────────────────────────────────────────────────────────────────────
interface PedidoItem {
    id: number;
    lote_id: number;
    cantidad: number;
    precio_unitario_aplicado: number;
    subtotal?: number;
    nombre_producto?: string;
}
interface Pedido {
    id: number;
    cliente_id: number;
    tienda_id: number;
    fecha_pedido: string;
    estado: string;
    total: number;
    notas?: string;
    items: PedidoItem[];
}

// ─── Helpers ──────────────────────────────────────────────────────────────────
const ESTADO_VARIANTS: Record<string, "info" | "warning" | "success" | "error" | "default"> = {
    pendiente: "warning",
    confirmado: "info",
    entregado: "success",
    cancelado: "error",
};
const ESTADO_LABELS: Record<string, string> = {
    pendiente: "Pendiente",
    confirmado: "Confirmado",
    entregado: "Entregado",
    cancelado: "Cancelado",
};
function formatDate(iso: string) {
    return new Date(iso).toLocaleString("es-PE", { dateStyle: "short", timeStyle: "short" });
}

// ─── Component ────────────────────────────────────────────────────────────────
export default function Pedidos() {
    const [selectedOrder, setSelectedOrder] = useState<Pedido | null>(null);
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [isUpdating, setIsUpdating] = useState(false);
    const [refreshKey, setRefreshKey] = useState(0);

    const [filterEstado, setFilterEstado] = useState("");
    const [filterDesde, setFilterDesde] = useState("");
    const [filterHasta, setFilterHasta] = useState("");
    const [activeFilters, setActiveFilters] = useState({ estado: "", desde: "", hasta: "" });

    const handleSearch = () => {
        setActiveFilters({ estado: filterEstado, desde: filterDesde, hasta: filterHasta });
        setRefreshKey(k => k + 1);
    };

    const handleClear = () => {
        setFilterEstado(""); setFilterDesde(""); setFilterHasta("");
        setActiveFilters({ estado: "", desde: "", hasta: "" });
        setRefreshKey(k => k + 1);
    };

    const handleOpenOrder = (order: Pedido) => { setSelectedOrder(order); setIsModalOpen(true); };

    const handleChangeStatus = async (nuevoEstado: string) => {
        if (!selectedOrder) return;
        setIsUpdating(true);
        try {
            await updateOrderStatus(selectedOrder.id, nuevoEstado);
            toast.success(`Pedido #${selectedOrder.id} → "${ESTADO_LABELS[nuevoEstado]}"`);
            setSelectedOrder(prev => prev ? { ...prev, estado: nuevoEstado } : null);
            setRefreshKey(k => k + 1);
        } catch (err: any) {
            toast.error(err.response?.data?.detail || "Error al actualizar el estado");
        } finally {
            setIsUpdating(false);
        }
    };

    const fetchOrders = useCallback(async (_params: any) => {
        const qp: Record<string, any> = {};
        if (activeFilters.estado) qp.estado = activeFilters.estado;
        if (activeFilters.desde) qp.fecha_desde = activeFilters.desde;
        if (activeFilters.hasta) qp.fecha_hasta = activeFilters.hasta;
        const res = await getOrders(qp);
        const data: Pedido[] = res.data;
        return { data, total: data.length };
    }, [refreshKey]);

    const columns: Column<Pedido>[] = [
        {
            header: "ID",
            accessor: "id",
            render: (v) => <span className="font-mono text-xs font-semibold text-text-secondary">#{v}</span>
        },
        {
            header: "Cliente",
            accessor: "cliente_id",
            render: (v) => (
                <div className="flex items-center gap-1.5 text-sm">
                    <User className="w-3.5 h-3.5 text-text-secondary" />
                    <span>Cliente #{v}</span>
                </div>
            )
        },
        {
            header: "Fecha",
            accessor: "fecha_pedido",
            render: (v) => (
                <div className="flex items-center gap-1.5 text-xs text-text-secondary">
                    <Calendar className="w-3 h-3" />
                    {formatDate(v)}
                </div>
            )
        },
        {
            header: "Estado",
            accessor: "estado",
            render: (v) => <Badge variant={ESTADO_VARIANTS[v] ?? "default"}>{ESTADO_LABELS[v] ?? v}</Badge>
        },
        {
            header: "Ítems",
            accessor: "items",
            render: (v) => <span className="text-sm text-text-secondary">{v?.length ?? 0} ítem(s)</span>
        },
        {
            header: "Total",
            accessor: "total",
            render: (v) => <span className="font-bold text-text-primary">S/.{Number(v).toFixed(2)}</span>
        },
        {
            header: "Ver",
            accessor: "id",
            render: (_, row) => (
                <Button variant="ghost" size="sm" onClick={() => handleOpenOrder(row)}>
                    <ClipboardList className="w-4 h-4 text-primary" />
                </Button>
            )
        },
    ];

    const estado = selectedOrder?.estado;
    const canConfirm = estado === "pendiente";
    const canDeliver = estado === "confirmado";
    const canCancel = estado === "pendiente" || estado === "confirmado";

    return (
        <div className="space-y-6">
            <div className="flex items-center justify-between">
                <div>
                    <h1 className="text-2xl font-bold tracking-tight text-text-primary">Pedidos</h1>
                    <p className="text-sm text-text-secondary">Gestiona y cambia el estado de los pedidos de tus clientes.</p>
                </div>
            </div>

            {/* Filters */}
            <Card className="p-4">
                <div className="flex items-center gap-2 mb-3">
                    <ShoppingCart className="h-4 w-4 text-primary" />
                    <span className="text-sm font-semibold text-text-primary">Filtros</span>
                </div>
                <div className="flex flex-wrap gap-3 items-end">
                    <div className="flex flex-col gap-1">
                        <label className="text-xs text-text-secondary font-medium">Estado</label>
                        <select
                            value={filterEstado}
                            onChange={e => setFilterEstado(e.target.value)}
                            className="bg-surface border border-border rounded-lg px-3 py-2 text-sm text-text-primary outline-none focus:border-primary transition-colors"
                        >
                            <option value="">Todos</option>
                            <option value="pendiente">Pendiente</option>
                            <option value="confirmado">Confirmado</option>
                            <option value="entregado">Entregado</option>
                            <option value="cancelado">Cancelado</option>
                        </select>
                    </div>
                    <div className="flex flex-col gap-1">
                        <label className="text-xs text-text-secondary font-medium">Desde</label>
                        <input type="date" value={filterDesde} onChange={e => setFilterDesde(e.target.value)}
                            className="bg-surface border border-border rounded-lg px-3 py-2 text-sm text-text-primary outline-none focus:border-primary transition-colors" />
                    </div>
                    <div className="flex flex-col gap-1">
                        <label className="text-xs text-text-secondary font-medium">Hasta</label>
                        <input type="date" value={filterHasta} onChange={e => setFilterHasta(e.target.value)}
                            className="bg-surface border border-border rounded-lg px-3 py-2 text-sm text-text-primary outline-none focus:border-primary transition-colors" />
                    </div>
                    <Button onClick={handleSearch} className="px-6">Buscar</Button>
                    <Button variant="outline" onClick={handleClear} className="px-4">Limpiar</Button>
                </div>
            </Card>

            <Card className="p-0 overflow-hidden">
                <DataTable key={refreshKey} columns={columns} fetchData={fetchOrders} searchable={false} pageSizeOptions={[10, 25, 50]} />
            </Card>

            {/* Detail Modal */}
            {selectedOrder && (
                <Modal isOpen={isModalOpen} onClose={() => setIsModalOpen(false)} title={`Pedido #${selectedOrder.id}`}>
                    <div className="space-y-4">
                        <div className="grid grid-cols-2 gap-3 text-sm">
                            <div className="bg-surface-variant rounded-lg p-3">
                                <p className="text-xs text-text-secondary mb-1">Cliente ID</p>
                                <p className="font-semibold text-text-primary">#{selectedOrder.cliente_id}</p>
                            </div>
                            <div className="bg-surface-variant rounded-lg p-3">
                                <p className="text-xs text-text-secondary mb-1">Fecha</p>
                                <p className="font-semibold text-text-primary">{formatDate(selectedOrder.fecha_pedido)}</p>
                            </div>
                            <div className="bg-surface-variant rounded-lg p-3">
                                <p className="text-xs text-text-secondary mb-1">Estado</p>
                                <Badge variant={ESTADO_VARIANTS[selectedOrder.estado] ?? "default"}>
                                    {ESTADO_LABELS[selectedOrder.estado] ?? selectedOrder.estado}
                                </Badge>
                            </div>
                            <div className="bg-surface-variant rounded-lg p-3">
                                <p className="text-xs text-text-secondary mb-1">Total</p>
                                <p className="font-bold text-lg text-text-primary">S/.{Number(selectedOrder.total).toFixed(2)}</p>
                            </div>
                        </div>

                        {selectedOrder.notas && (
                            <div className="bg-surface-variant rounded-lg p-3 text-sm">
                                <p className="text-xs text-text-secondary mb-1">Notas</p>
                                <p className="text-text-primary">{selectedOrder.notas}</p>
                            </div>
                        )}

                        {/* Items */}
                        <div>
                            <p className="text-xs font-semibold text-text-secondary uppercase tracking-wider mb-2">Ítems del pedido</p>
                            <div className="rounded-lg border border-border overflow-hidden">
                                <table className="min-w-full text-sm">
                                    <thead className="bg-surface-variant">
                                        <tr>
                                            <th className="px-3 py-2 text-left text-xs font-medium text-text-secondary">Producto</th>
                                            <th className="px-3 py-2 text-center text-xs font-medium text-text-secondary">Cant.</th>
                                            <th className="px-3 py-2 text-right text-xs font-medium text-text-secondary">Precio unit.</th>
                                            <th className="px-3 py-2 text-right text-xs font-medium text-text-secondary">Subtotal</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        {selectedOrder.items.map((item, i) => (
                                            <tr key={item.id} className={i % 2 === 0 ? "bg-surface" : "bg-surface-variant"}>
                                                <td className="px-3 py-2 text-text-primary">{item.nombre_producto || `Lote #${item.lote_id}`}</td>
                                                <td className="px-3 py-2 text-center">{item.cantidad}</td>
                                                <td className="px-3 py-2 text-right">S/.{Number(item.precio_unitario_aplicado).toFixed(2)}</td>
                                                <td className="px-3 py-2 text-right font-semibold">
                                                    S/.{(item.subtotal ?? Number(item.precio_unitario_aplicado) * item.cantidad).toFixed(2)}
                                                </td>
                                            </tr>
                                        ))}
                                    </tbody>
                                </table>
                            </div>
                        </div>

                        {/* Actions */}
                        <div className="flex justify-between items-center pt-3 border-t border-border">
                            {canCancel ? (
                                <button disabled={isUpdating} onClick={() => handleChangeStatus("cancelado")}
                                    className="group flex items-center gap-2 px-4 py-2 rounded-xl font-semibold text-sm
                                        bg-gradient-to-br from-red-500 to-rose-600 text-white shadow-md shadow-red-500/30
                                        hover:shadow-red-500/50 hover:from-red-400 hover:to-rose-500
                                        disabled:opacity-50 disabled:cursor-not-allowed transition-all duration-200">
                                    <XCircle className="w-4 h-4 group-hover:scale-110 transition-transform" />
                                    Cancelar
                                </button>
                            ) : <div />}
                            <div className="flex gap-2">
                                <Button variant="outline" onClick={() => setIsModalOpen(false)} disabled={isUpdating}>Cerrar</Button>
                                {canConfirm && (
                                    <button disabled={isUpdating} onClick={() => handleChangeStatus("confirmado")}
                                        className="group flex items-center gap-2 px-4 py-2 rounded-xl font-semibold text-sm
                                            bg-gradient-to-br from-blue-500 to-indigo-600 text-white shadow-md shadow-blue-500/30
                                            hover:shadow-blue-500/50 hover:from-blue-400 hover:to-indigo-500
                                            disabled:opacity-50 disabled:cursor-not-allowed transition-all duration-200">
                                        <CheckCircle2 className="w-4 h-4 group-hover:scale-110 transition-transform" />
                                        Confirmar
                                    </button>
                                )}
                                {canDeliver && (
                                    <button disabled={isUpdating} onClick={() => handleChangeStatus("entregado")}
                                        className="group flex items-center gap-2 px-4 py-2 rounded-xl font-semibold text-sm
                                            bg-gradient-to-br from-emerald-500 to-green-600 text-white shadow-md shadow-emerald-500/30
                                            hover:shadow-emerald-500/50 hover:from-emerald-400 hover:to-green-500
                                            disabled:opacity-50 disabled:cursor-not-allowed transition-all duration-200">
                                        <PackageCheck className="w-4 h-4 group-hover:scale-110 transition-transform" />
                                        Marcar Entregado
                                    </button>
                                )}
                            </div>
                        </div>
                    </div>
                </Modal>
            )}
        </div>
    );
}

