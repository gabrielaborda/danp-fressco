import { useEffect, useState, useCallback } from "react";
import { getLots, updateLot, createLot } from "../api/lots";
import { getProducts } from "../api/products";
import { DataTable, type Column } from "../components/ui/DataTable";
import { Card } from "../components/ui/Card";
import { Button } from "../components/ui/Button";
import { Modal } from "../components/ui/Modal";
import { useToast } from "../components/ui/ToastProvider";
import { Edit2, Trash2, X, Save, Plus } from "lucide-react";

export default function Lotes() {
    const toast = useToast();
    const [products, setProducts] = useState<any[]>([]);

    // Search states
    const [searchLoteId, setSearchLoteId] = useState("");
    const [searchProductId, setSearchProductId] = useState("");
    const [refreshKey, setRefreshKey] = useState(0);

    // Modal state
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [selectedLot, setSelectedLot] = useState<any>(null);
    const [isSaving, setIsSaving] = useState(false);

    // Form states
    const [formData, setFormData] = useState({
        producto_id: "",
        cantidad: 0,
        cantidad_inicial: 0,
        fecha_ingreso: "",
        fecha_vencimiento: "",
        precio_lote: 0,
    });

    useEffect(() => {
        getProducts({ pageSize: 1000 }).then((res: any) => {
            setProducts(res.data);
        }).catch(err => {
            console.error("Error loading products", err);
        });
    }, []);

    const fetchLotsData = useCallback(async ({ page, pageSize }: any) => {
        try {
            const params: any = { page, pageSize };

            const res: any = await getLots(params);

            // Filter inactive out
            let filteredData = res.data.filter((lote: any) => lote.estado !== 'Inactivo');

            // Apply advanced search filters
            if (searchLoteId) {
                filteredData = filteredData.filter((l: any) => l.id.toString().includes(searchLoteId));
            }
            if (searchProductId) {
                filteredData = filteredData.filter((l: any) => l.producto_id.toString() === searchProductId);
            }

            // Order newest to oldest
            filteredData.sort((a: any, b: any) => {
                const dateA = new Date(a.creado_en || a.fecha_ingreso).getTime();
                const dateB = new Date(b.creado_en || b.fecha_ingreso).getTime();
                return dateB - dateA;
            });

            return { data: filteredData, total: filteredData.length };
        } catch (e: any) {
            toast.error(e?.message ?? "Error al cargar los lotes");
            return { data: [], total: 0 };
        }
    }, [searchLoteId, searchProductId, refreshKey, toast]);

    const handleCreate = () => {
        setSelectedLot(null);
        setFormData({
            producto_id: "",
            cantidad: 0,
            cantidad_inicial: 0,
            fecha_ingreso: new Date().toISOString().split('T')[0], // default hoy
            fecha_vencimiento: "",
            precio_lote: 0,
        });
        setIsModalOpen(true);
    };

    const handleEdit = (lot: any) => {
        setSelectedLot(lot);
        setFormData({
            producto_id: lot.producto_id,
            cantidad: lot.cantidad,
            cantidad_inicial: lot.cantidad_inicial,
            fecha_ingreso: lot.fecha_ingreso ? lot.fecha_ingreso.split('T')[0] : "",
            fecha_vencimiento: lot.fecha_vencimiento ? lot.fecha_vencimiento.split('T')[0] : "",
            precio_lote: lot.precio_lote,
        });
        setIsModalOpen(true);
    };

    const handleSave = async (estado?: string) => {
        setIsSaving(true);
        try {
            const dataToUpdate = {
                ...formData,
                ...(estado ? { estado } : {})
            };

            if (selectedLot) {
                await updateLot(selectedLot.id, dataToUpdate);
                toast.success(estado === 'Inactivo' ? 'Lote inactivado' : 'Lote actualizado correctamente');
            } else {
                if (!formData.producto_id) {
                    toast.error("Debes seleccionar un producto");
                    setIsSaving(false);
                    return;
                }
                await createLot(dataToUpdate);
                toast.success('Lote creado correctamente');
            }

            setIsModalOpen(false);
            setRefreshKey(prev => prev + 1);
        } catch (err: any) {
            toast.error(err.response?.data?.detail || "Error al guardar el lote");
        } finally {
            setIsSaving(false);
        }
    };

    const columns: Column<any>[] = [
        { header: "ID", accessor: "id" },
        {
            header: "Producto",
            accessor: (row) => {
                if (row.producto_nombre) return row.producto_nombre;
                if (row.productos?.nombre) return row.productos.nombre;
                const found = products.find(p => p.id === row.producto_id);
                return found ? found.nombre : `Prod #${row.producto_id}`;
            }
        },
        { header: "Cantidad", accessor: "cantidad" },
        { header: "Cant. Inicial", accessor: "cantidad_inicial" },
        { header: "Ingreso", accessor: "fecha_ingreso", render: (v) => v ? new Date(v).toLocaleDateString() : '-' },
        { header: "Vencimiento", accessor: "fecha_vencimiento", render: (v) => v ? new Date(v).toLocaleDateString() : '-' },
        { header: "Precio", accessor: "precio_lote", render: (v) => `S/. ${Number(v).toFixed(2)}` },
        {
            header: "Acciones",
            accessor: "id",
            render: (_, row) => (
                <Button variant="ghost" size="sm" onClick={() => handleEdit(row)}>
                    <Edit2 className="w-4 h-4 text-primary" />
                </Button>
            )
        }
    ];

    return (
        <div className="space-y-6">
            <div className="flex items-center justify-between">
                <div>
                    <h1 className="text-2xl font-bold tracking-tight text-text-primary">Lotes</h1>
                    <p className="text-sm text-text-secondary">Gestiona los lotes de productos y sus fechas de vencimiento.</p>
                </div>
                <Button
                    onClick={handleCreate}
                    className="flex items-center gap-2 px-5 py-2.5 rounded-xl bg-primary text-surface shadow-md shadow-primary/20 hover:shadow-lg hover:shadow-primary/40 hover:-translate-y-0.5 transition-all duration-300 font-bold text-sm tracking-wide"
                >
                    <Plus className="w-4 h-4" />
                    Nuevo Lote
                </Button>
            </div>

            <Card className="p-4 flex flex-col md:flex-row gap-4 items-end">
                <div className="flex-1 w-full">
                    <label className="text-sm text-text-secondary mb-1 block font-medium">Número de Lote (ID)</label>
                    <input
                        type="text"
                        value={searchLoteId}
                        onChange={e => setSearchLoteId(e.target.value)}
                        placeholder="Buscar por ID..."
                        className="w-full bg-surface border border-border rounded-lg px-3 py-2 outline-none focus:border-primary text-text-primary transition-colors"
                    />
                </div>
                <div className="flex-1 w-full">
                    <label className="text-sm text-text-secondary mb-1 block font-medium">Producto</label>
                    <select
                        value={searchProductId}
                        onChange={e => setSearchProductId(e.target.value)}
                        className="w-full bg-surface border border-border rounded-lg px-3 py-2 outline-none focus:border-primary text-text-primary transition-colors"
                    >
                        <option value="">Todos los productos</option>
                        {products.map(p => (
                            <option key={p.id} value={p.id}>{p.nombre}</option>
                        ))}
                    </select>
                </div>
                <Button onClick={() => setRefreshKey(k => k + 1)} className="px-8 rounded-xl">
                    Buscar
                </Button>
            </Card>

            <Card className="p-0 overflow-hidden">
                <DataTable
                    key={refreshKey}
                    columns={columns}
                    fetchData={fetchLotsData}
                    searchable={false}
                />
            </Card>

            <Modal isOpen={isModalOpen} onClose={() => setIsModalOpen(false)} title={selectedLot ? "Modificar Lote" : "Crear Nuevo Lote"}>
                <div className="space-y-4">
                    <div className="flex flex-col gap-1">
                        <label className="text-sm text-text-secondary font-medium">Producto {selectedLot && "(No editable)"}</label>
                        {selectedLot ? (
                            <input
                                disabled
                                value={
                                    selectedLot?.producto_nombre ||
                                    selectedLot?.productos?.nombre ||
                                    (selectedLot && products.find(p => p.id === selectedLot.producto_id)?.nombre) ||
                                    (selectedLot ? `Prod #${selectedLot.producto_id}` : "")
                                }
                                className="bg-surface-variant text-text-secondary px-3 py-2 rounded-lg border border-border cursor-not-allowed"
                            />
                        ) : (
                            <select
                                value={formData.producto_id}
                                onChange={e => setFormData({ ...formData, producto_id: e.target.value })}
                                className="w-full bg-surface border border-border rounded-lg px-3 py-2 outline-none focus:border-primary text-text-primary transition-colors"
                            >
                                <option value="">Selecciona un producto</option>
                                {products.map(p => (
                                    <option key={p.id} value={p.id}>{p.nombre}</option>
                                ))}
                            </select>
                        )}
                    </div>
                    <div className="grid grid-cols-2 gap-4">
                        <div className="flex flex-col gap-1">
                            <label className="text-sm text-text-secondary font-medium">Cantidad Actual</label>
                            <input
                                type="number"
                                value={formData.cantidad}
                                onChange={e => setFormData({ ...formData, cantidad: Number(e.target.value) })}
                                className="bg-surface px-3 py-2 rounded-lg border border-border text-text-primary focus:border-primary outline-none transition-colors"
                            />
                        </div>
                        <div className="flex flex-col gap-1">
                            <label className="text-sm text-text-secondary font-medium">Stock Total (Producto)</label>
                            <input
                                disabled
                                value={
                                    products.find(p => String(p.id) === String(formData.producto_id))?.stock_total || 0
                                }
                                className="bg-surface-variant px-3 py-2 rounded-lg border border-border text-text-secondary outline-none cursor-not-allowed"
                            />
                        </div>
                    </div>
                    <div className="grid grid-cols-2 gap-4">
                        <div className="flex flex-col gap-1">
                            <label className="text-sm text-text-secondary font-medium">Fecha Ingreso</label>
                            <input
                                type="date"
                                value={formData.fecha_ingreso}
                                onChange={e => setFormData({ ...formData, fecha_ingreso: e.target.value })}
                                className="bg-surface px-3 py-2 rounded-lg border border-border text-text-primary focus:border-primary outline-none transition-colors"
                            />
                        </div>
                        <div className="flex flex-col gap-1">
                            <label className="text-sm text-text-secondary font-medium">Fecha Vencimiento</label>
                            <input
                                type="date"
                                value={formData.fecha_vencimiento}
                                onChange={e => setFormData({ ...formData, fecha_vencimiento: e.target.value })}
                                className="bg-surface px-3 py-2 rounded-lg border border-border text-text-primary focus:border-primary outline-none transition-colors"
                            />
                        </div>
                    </div>
                    <div className="flex flex-col gap-1">
                        <label className="text-sm text-text-secondary font-medium">Precio del Lote</label>
                        <input
                            type="number"
                            step="0.01"
                            value={formData.precio_lote}
                            onChange={e => setFormData({ ...formData, precio_lote: Number(e.target.value) })}
                            className="bg-surface px-3 py-2 rounded-lg border border-border text-text-primary focus:border-primary outline-none transition-colors"
                        />
                    </div>
                    <div className="flex justify-between items-center mt-8 pt-5 border-t border-border">
                        {selectedLot ? (
                            <Button
                                variant="danger"
                                isLoading={isSaving}
                                onClick={() => handleSave('Inactivo')}
                                className="flex items-center gap-2 px-5 py-2.5 rounded-xl bg-red-500/10 text-red-500 hover:bg-red-500 hover:text-white border border-transparent hover:border-red-600 transition-all duration-300"
                            >
                                <Trash2 className="w-4 h-4" />
                                <span className="font-semibold text-sm">Inactivar</span>
                            </Button>
                        ) : (
                            <div></div>
                        )}
                        <div className="flex gap-3">
                            <Button
                                variant="ghost"
                                onClick={() => setIsModalOpen(false)}
                                disabled={isSaving}
                                className="flex items-center gap-2 px-5 py-2.5 rounded-xl hover:bg-surface-variant text-text-secondary hover:text-text-primary transition-all duration-300 font-medium text-sm"
                            >
                                <X className="w-4 h-4" />
                                Cancelar
                            </Button>
                            <Button
                                isLoading={isSaving}
                                onClick={() => handleSave()}
                                className="flex items-center gap-2 px-6 py-2.5 rounded-xl bg-primary text-surface shadow-lg shadow-primary/30 hover:shadow-primary/50 hover:-translate-y-0.5 transition-all duration-300 font-bold text-sm tracking-wide"
                            >
                                <Save className="w-4 h-4" />
                                Guardar
                            </Button>
                        </div>
                    </div>
                </div>
            </Modal>
        </div>
    );
}
