import { useState, useCallback } from "react";
import { Card } from "../components/ui/Card";
import { Modal } from "../components/ui/Modal";
import { Button } from "../components/ui/Button";
import { Badge } from "../components/ui/Badge";
import { DataTable, type Column } from "../components/ui/DataTable";
import { getClientes, updateCliente } from "../api/users";
import { Users as UsersIcon, UserCheck, UserX, Calendar, Mail, Phone } from "lucide-react";
import { toast } from "react-toastify";

// ─── Types ────────────────────────────────────────────────────────────────────
interface Cliente {
    id: number;
    nombre: string;
    email: string;
    rol: string;
    telefono?: string;
    fecha_registro: string;
    estado: string; // activo | suspendido
}

function formatDate(iso: string) {
    return new Date(iso).toLocaleDateString("es-PE", { dateStyle: "medium" });
}

// ─── Component ────────────────────────────────────────────────────────────────
export default function Usuarios() {
    const [selectedCliente, setSelectedCliente] = useState<Cliente | null>(null);
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [isUpdating, setIsUpdating] = useState(false);
    const [refreshKey, setRefreshKey] = useState(0);

    const [filterEstado, setFilterEstado] = useState("");
    const [activeFilter, setActiveFilter] = useState("");

    const handleSearch = () => { setActiveFilter(filterEstado); setRefreshKey(k => k + 1); };
    const handleClear = () => { setFilterEstado(""); setActiveFilter(""); setRefreshKey(k => k + 1); };

    const handleOpen = (cliente: Cliente) => { setSelectedCliente(cliente); setIsModalOpen(true); };

    const handleToggleEstado = async () => {
        if (!selectedCliente) return;
        const nuevoEstado = selectedCliente.estado === "activo" ? "suspendido" : "activo";
        setIsUpdating(true);
        try {
            await updateCliente(selectedCliente.id, { estado: nuevoEstado });
            toast.success(`Cliente "${selectedCliente.nombre}" → ${nuevoEstado === "activo" ? "Activado" : "Suspendido"}`);
            setSelectedCliente(prev => prev ? { ...prev, estado: nuevoEstado } : null);
            setRefreshKey(k => k + 1);
        } catch (err: any) {
            toast.error(err.response?.data?.detail || "Error al actualizar el cliente");
        } finally {
            setIsUpdating(false);
        }
    };

    const fetchClientes = useCallback(async (_params: any) => {
        const qp: Record<string, any> = {};
        if (activeFilter) qp.estado = activeFilter;
        const res = await getClientes(qp);
        const data: Cliente[] = res.data;
        return { data, total: data.length };
    }, [refreshKey]);

    const columns: Column<Cliente>[] = [
        {
            header: "Nombre",
            accessor: "nombre",
            render: (v) => <span className="font-medium text-text-primary">{v}</span>
        },
        {
            header: "Email",
            accessor: "email",
            render: (v) => (
                <div className="flex items-center gap-1.5 text-xs text-text-secondary">
                    <Mail className="w-3 h-3" />{v}
                </div>
            )
        },
        {
            header: "Teléfono",
            accessor: "telefono",
            render: (v) => v ? (
                <div className="flex items-center gap-1.5 text-xs text-text-secondary">
                    <Phone className="w-3 h-3" />{v}
                </div>
            ) : <span className="text-xs text-text-secondary">—</span>
        },
        {
            header: "Registro",
            accessor: "fecha_registro",
            render: (v) => (
                <div className="flex items-center gap-1.5 text-xs text-text-secondary">
                    <Calendar className="w-3 h-3" />{formatDate(v)}
                </div>
            )
        },
        {
            header: "Estado",
            accessor: "estado",
            render: (v) => (
                <Badge variant={v === "activo" ? "success" : "error"}>
                    {v === "activo" ? "Activo" : "Suspendido"}
                </Badge>
            )
        },
        {
            header: "Acciones",
            accessor: "id",
            render: (_, row) => (
                <Button variant="ghost" size="sm" onClick={() => handleOpen(row)}>
                    <UsersIcon className="w-4 h-4 text-primary" />
                </Button>
            )
        },
    ];

    const esActivo = selectedCliente?.estado === "activo";

    return (
        <div className="space-y-6">
            <div className="flex items-center justify-between">
                <div>
                    <h1 className="text-2xl font-bold tracking-tight text-text-primary">Usuarios</h1>
                    <p className="text-sm text-text-secondary">Gestiona los clientes registrados desde la app Android.</p>
                </div>
            </div>

            {/* Filters */}
            <Card className="p-4">
                <div className="flex items-center gap-2 mb-3">
                    <UsersIcon className="h-4 w-4 text-primary" />
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
                            <option value="activo">Activo</option>
                            <option value="suspendido">Suspendido</option>
                        </select>
                    </div>
                    <Button onClick={handleSearch} className="px-6">Filtrar</Button>
                    <Button variant="outline" onClick={handleClear} className="px-4">Limpiar</Button>
                </div>
            </Card>

            <Card className="p-0 overflow-hidden">
                <DataTable key={refreshKey} columns={columns} fetchData={fetchClientes} searchable={false} pageSizeOptions={[10, 25, 50]} />
            </Card>

            {/* Detail Modal */}
            {selectedCliente && (
                <Modal isOpen={isModalOpen} onClose={() => setIsModalOpen(false)} title="Detalle de Cliente">
                    <div className="space-y-5">
                        {/* Avatar + name */}
                        <div className="flex items-center gap-4 p-4 bg-surface-variant rounded-xl">
                            <div className="w-14 h-14 rounded-full bg-gradient-to-br from-primary to-indigo-600 flex items-center justify-center text-white text-2xl font-bold shadow-md">
                                {selectedCliente.nombre.charAt(0).toUpperCase()}
                            </div>
                            <div>
                                <p className="font-bold text-text-primary text-lg">{selectedCliente.nombre}</p>
                                <p className="text-xs text-text-secondary">{selectedCliente.email}</p>
                            </div>
                        </div>

                        {/* Info grid */}
                        <div className="grid grid-cols-2 gap-3 text-sm">
                            <div className="bg-surface-variant rounded-lg p-3">
                                <p className="text-xs text-text-secondary mb-1">Teléfono</p>
                                <p className="font-medium text-text-primary">{selectedCliente.telefono || "—"}</p>
                            </div>
                            <div className="bg-surface-variant rounded-lg p-3">
                                <p className="text-xs text-text-secondary mb-1">Registro</p>
                                <p className="font-medium text-text-primary">{formatDate(selectedCliente.fecha_registro)}</p>
                            </div>
                            <div className="bg-surface-variant rounded-lg p-3">
                                <p className="text-xs text-text-secondary mb-1">Rol</p>
                                <p className="font-medium text-text-primary capitalize">{selectedCliente.rol}</p>
                            </div>
                            <div className="bg-surface-variant rounded-lg p-3">
                                <p className="text-xs text-text-secondary mb-1">Estado</p>
                                <Badge variant={esActivo ? "success" : "error"}>
                                    {esActivo ? "Activo" : "Suspendido"}
                                </Badge>
                            </div>
                        </div>

                        {/* Actions */}
                        <div className="flex justify-between items-center pt-3 border-t border-border">
                            {esActivo ? (
                                <button disabled={isUpdating} onClick={handleToggleEstado}
                                    className="group flex items-center gap-2 px-4 py-2 rounded-xl font-semibold text-sm
                                        bg-gradient-to-br from-amber-500 to-orange-600 text-white shadow-md shadow-amber-500/30
                                        hover:shadow-amber-500/50 hover:from-amber-400 hover:to-orange-500
                                        disabled:opacity-50 disabled:cursor-not-allowed transition-all duration-200">
                                    <UserX className="w-4 h-4 group-hover:scale-110 transition-transform" />
                                    Suspender
                                </button>
                            ) : (
                                <button disabled={isUpdating} onClick={handleToggleEstado}
                                    className="group flex items-center gap-2 px-4 py-2 rounded-xl font-semibold text-sm
                                        bg-gradient-to-br from-emerald-500 to-green-600 text-white shadow-md shadow-emerald-500/30
                                        hover:shadow-emerald-500/50 hover:from-emerald-400 hover:to-green-500
                                        disabled:opacity-50 disabled:cursor-not-allowed transition-all duration-200">
                                    <UserCheck className="w-4 h-4 group-hover:scale-110 transition-transform" />
                                    Reactivar
                                </button>
                            )}
                            <Button variant="outline" onClick={() => setIsModalOpen(false)} disabled={isUpdating}>
                                Cerrar
                            </Button>
                        </div>
                    </div>
                </Modal>
            )}
        </div>
    );
}

