import React, { useState, useCallback } from "react";
import { Card } from "../components/ui/Card";
import { DataTable, type Column } from "../components/ui/DataTable";
import { Button } from "../components/ui/Button";
import InputField from "../components/auth/login/InputField";
import { getLots } from "../api/lots";
import { getProducts } from "../api/products";
import { updateLotDiscount, deactivateLotDiscount } from "../api/discounts";
import { Percent, CheckSquare, Square, CheckCircle2, Tag, Calendar } from "lucide-react";
import { Badge } from "../components/ui/Badge";
import { toast } from "react-toastify";

export default function Descuentos() {
    const [selectedLots, setSelectedLots] = useState<Set<number>>(new Set());
    const [products, setProducts] = useState<any[]>([]);
    
    // Form state
    const [porcentaje, setPorcentaje] = useState<string>("10");
    const [fechaInicio, setFechaInicio] = useState<string>(new Date().toISOString().split("T")[0]);
    
    // Default end date to 1 week from now
    const nextWeek = new Date();
    nextWeek.setDate(nextWeek.getDate() + 7);
    const [fechaFin, setFechaFin] = useState<string>(nextWeek.toISOString().split("T")[0]);
    const [descripcion, setDescripcion] = useState<string>("Descuento de Temporada");

    const [isProcessing, setIsProcessing] = useState(false);
    const [refreshKey, setRefreshKey] = useState(0);

    React.useEffect(() => {
        getProducts().then(res => setProducts(res.data)).catch(console.error);
    }, []);

    const handleToggleLot = (id: number) => {
        const newSet = new Set(selectedLots);
        if (newSet.has(id)) {
            newSet.delete(id);
        } else {
            newSet.add(id);
        }
        setSelectedLots(newSet);
    };

    const handleApplyDiscounts = async () => {
        if (selectedLots.size === 0) return toast.warn("Selecciona al menos un lote.");
        setIsProcessing(true);
        try {
            const promises = Array.from(selectedLots).map(loteId => 
                updateLotDiscount(loteId, {
                    porcentaje: parseFloat(porcentaje),
                    fecha_inicio: fechaInicio,
                    fecha_fin: fechaFin,
                    descripcion: descripcion
                })
            );
            await Promise.all(promises);
            setSelectedLots(new Set());
            setRefreshKey(k => k + 1);
            toast.success("Descuentos aplicados exitosamente.");
        } catch (error: any) {
            console.error(error);
            toast.error(error.response?.data?.detail || "Error al aplicar descuentos.");
        } finally {
            setIsProcessing(false);
        }
    };

    const handleRemoveDiscounts = async () => {
        if (selectedLots.size === 0) return toast.warn("Selecciona al menos un lote.");
        if (!confirm("¿Seguro que deseas desactivar los descuentos de los lotes seleccionados?")) return;
        setIsProcessing(true);
        try {
            const promises = Array.from(selectedLots).map(loteId => deactivateLotDiscount(loteId));
            await Promise.all(promises);
            setSelectedLots(new Set());
            setRefreshKey(k => k + 1);
            toast.success("Descuentos desactivados exitosamente.");
        } catch (error: any) {
            console.error(error);
            toast.error(error.response?.data?.detail || "Error al desactivar descuentos.");
        } finally {
            setIsProcessing(false);
        }
    };

    const columns: Column<any>[] = [
        {
            header: "Seleccionar",
            accessor: "id",
            render: (id: number) => (
                <button 
                    onClick={() => handleToggleLot(id)}
                    className="text-primary hover:text-primary-hover focus:outline-none"
                >
                    {selectedLots.has(id) ? (
                        <CheckSquare className="w-5 h-5" />
                    ) : (
                        <Square className="w-5 h-5 text-text-secondary" />
                    )}
                </button>
            )
        },
        { 
            header: "Producto", 
            accessor: "producto_id", 
            render: (v: any, row: any) => {
                const nombre = row.producto_nombre || row.productos?.nombre || row.producto?.nombre || products.find(p => p.id === v)?.nombre;
                return nombre || `Prod #${v}`;
            }
        },
        { header: "Cant.", accessor: "cantidad" },
        { header: "Precio Base", accessor: "precio_lote", render: (v: any) => `$${Number(v).toFixed(2)}` },
        { header: "Vencimiento", accessor: "fecha_vencimiento" },
        { 
            header: "Descuento Activo", 
            accessor: "tiene_descuento_manual",
            render: (tiene: boolean) => tiene ? (
                <Badge variant="success" className="flex items-center gap-1 w-max">
                    <CheckCircle2 className="w-3 h-3" /> Sí
                </Badge>
            ) : (
                <span className="text-text-secondary text-xs">-</span>
            )
        }
    ];

    const fetchLotsData = useCallback(async (params: any) => {
        // We reuse the lotes fetch logic
        const res = await getLots(params);
        return { data: res.data, total: res.headers?.['x-total-count'] || res.data.length };
    }, [refreshKey]);

    return (
        <div className="space-y-6">
            <div className="flex items-center justify-between">
                <div>
                    <h1 className="text-2xl font-bold tracking-tight text-text-primary">Gestión de Descuentos</h1>
                    <p className="text-sm text-text-secondary">Asigna o retira descuentos masivamente a tus lotes.</p>
                </div>
            </div>

            <Card className="p-6">
                <div className="flex items-center gap-3 mb-6 pb-4 border-b border-border">
                    <Percent className="h-6 w-6 text-primary" />
                    <h2 className="text-lg font-semibold text-text-primary">Configurar Campaña</h2>
                </div>
                
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
                    <InputField
                        type="text"
                        icon={<Tag className="w-4 h-4" />}
                        label="Descripción / Nombre"
                        value={descripcion}
                        onChange={(e) => setDescripcion(e.target.value)}
                        placeholder="Ej: Oferta de Verano"
                    />
                    <InputField
                        type="number"
                        icon={<Percent className="w-4 h-4" />}
                        label="Porcentaje (%)"
                        value={porcentaje}
                        onChange={(e) => setPorcentaje(e.target.value)}
                    />
                    <InputField
                        type="date"
                        icon={<Calendar className="w-4 h-4" />}
                        label="Fecha de Inicio"
                        value={fechaInicio}
                        onChange={(e) => setFechaInicio(e.target.value)}
                    />
                    <InputField
                        type="date"
                        icon={<Calendar className="w-4 h-4" />}
                        label="Fecha de Fin"
                        value={fechaFin}
                        onChange={(e) => setFechaFin(e.target.value)}
                    />
                </div>
            </Card>

            <Card className="p-0 overflow-hidden">
                <div className="p-4 border-b border-border bg-surface flex justify-between items-center">
                    <div className="flex items-center gap-2">
                        <Badge variant="info">{selectedLots.size} seleccionados</Badge>
                        <span className="text-sm text-text-secondary">
                            Selecciona los lotes en la tabla y aplica una acción.
                        </span>
                    </div>
                    <div className="flex gap-2">
                        <Button 
                            variant="danger" 
                            disabled={selectedLots.size === 0 || isProcessing}
                            onClick={handleRemoveDiscounts}
                            isLoading={isProcessing}
                        >
                            Quitar Descuento
                        </Button>
                        <Button 
                            variant="primary" 
                            disabled={selectedLots.size === 0 || isProcessing}
                            onClick={handleApplyDiscounts}
                            isLoading={isProcessing}
                        >
                            Aplicar Descuento
                        </Button>
                    </div>
                </div>
                
                <div className="p-4">
                    <DataTable
                        columns={columns}
                        fetchData={fetchLotsData}
                        pageSizeOptions={[10, 20, 50, 100]}
                        searchable={true}
                    />
                </div>
            </Card>
        </div>
    );
}
