import { useState, useCallback } from "react";
import { getProducts, updateProduct, createProduct } from "../api/products";
import { DataTable, type Column } from "../components/ui/DataTable";
import { Card } from "../components/ui/Card";
import { Button } from "../components/ui/Button";
import { Modal } from "../components/ui/Modal";
import { useToast } from "../components/ui/ToastProvider";
import { Edit2, Trash2, X, Save, Plus } from "lucide-react";
import { Badge } from "../components/ui/Badge";

const CATEGORIAS = [
    "Frutas",
    "Verduras",
    "Lácteos",
    "Carnes",
    "Bebidas",
    "Abarrotes",
    "Panadería",
    "Limpieza"
];

export default function Productos() {
    const toast = useToast();

    // Search states
    const [searchNombre, setSearchNombre] = useState("");
    const [searchCategoria, setSearchCategoria] = useState("");
    const [refreshKey, setRefreshKey] = useState(0);

    // Modal state
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [selectedProduct, setSelectedProduct] = useState<any>(null);
    const [isSaving, setIsSaving] = useState(false);

    // Form states
    const [formData, setFormData] = useState({
        nombre: "",
        descripcion: "",
        categoria: "",
        precio_base: 0,
        imagen_url: "",
    });

    const fetchProductsData = useCallback(async ({ page, pageSize }: any) => {
        try {
            const params: any = { page, pageSize };

            // Si el backend soporta búsqueda: params.search = searchNombre
            const res: any = await getProducts(params);

            // Filtros locales por si el backend no los aplica
            let filteredData = res.data;

            if (searchNombre) {
                filteredData = filteredData.filter((p: any) =>
                    p.nombre.toLowerCase().includes(searchNombre.toLowerCase())
                );
            }
            if (searchCategoria) {
                filteredData = filteredData.filter((p: any) => p.categoria === searchCategoria);
            }

            // Order newest to oldest (asumiendo id autoincremental si no hay fechas)
            filteredData.sort((a: any, b: any) => b.id - a.id);

            return { data: filteredData, total: filteredData.length };
        } catch (e: any) {
            toast.error(e?.message ?? "Error al cargar los productos");
            return { data: [], total: 0 };
        }
    }, [searchNombre, searchCategoria, refreshKey, toast]);

    const handleCreate = () => {
        setSelectedProduct(null);
        setFormData({
            nombre: "",
            descripcion: "",
            categoria: "",
            precio_base: 0,
            imagen_url: "",
        });
        setIsModalOpen(true);
    };

    const handleEdit = (product: any) => {
        setSelectedProduct(product);
        setFormData({
            nombre: product.nombre || "",
            descripcion: product.descripcion || "",
            categoria: product.categoria || "",
            precio_base: product.precio_base || 0,
            imagen_url: product.imagen_url || "",
        });
        setIsModalOpen(true);
    };

    const handleSave = async (activoOverride?: boolean) => {
        setIsSaving(true);
        try {
            const dataToSave = {
                ...formData,
                tienda_id: 1, // Por requerimiento, tienda 1 por defecto
                // Si pasamos activoOverride, lo usamos, sino se mantiene igual o default true al crear.
                activo: activoOverride !== undefined ? activoOverride : (selectedProduct ? selectedProduct.activo : true)
            };

            if (selectedProduct) {
                await updateProduct(selectedProduct.id, dataToSave);
                if (activoOverride === false) {
                    toast.success("Producto desactivado correctamente");
                } else {
                    toast.success("Producto actualizado correctamente");
                }
            } else {
                if (!formData.nombre || !formData.categoria) {
                    toast.error("El nombre y categoría son requeridos");
                    setIsSaving(false);
                    return;
                }
                await createProduct(dataToSave);
                toast.success("Producto creado correctamente");
            }

            setIsModalOpen(false);
            setRefreshKey(prev => prev + 1);
        } catch (err: any) {
            toast.error(err.response?.data?.detail || "Error al guardar el producto");
        } finally {
            setIsSaving(false);
        }
    };

    const columns: Column<any>[] = [
        { header: "ID", accessor: "id" },
        {
            header: "Imagen",
            accessor: "imagen_url",
            render: (v) => v ? (
                <img src={v} alt="Producto" className="w-10 h-10 rounded-lg object-cover bg-surface-variant border border-border" />
            ) : (
                <div className="w-10 h-10 rounded-lg bg-surface-variant flex items-center justify-center text-xs text-text-secondary border border-border">No img</div>
            )
        },
        { header: "Nombre", accessor: "nombre" },
        { header: "Descripción", accessor: "descripcion", render: (v) => <span className="truncate max-w-[200px] block">{v || '-'}</span> },
        { header: "Categoría", accessor: "categoria", render: (v) => <Badge variant="info">{v}</Badge> },
        { header: "Precio Base", accessor: "precio_base", render: (v) => `S/.${Number(v).toFixed(2)}` },
        { 
            header: "Stock Total", 
            accessor: "stock_total", 
            render: (v) => (
                <Badge variant={v > 0 ? "default" : "error"}>
                    {v > 0 ? `${v} un.` : "Agotado"}
                </Badge>
            )
        },
        {
            header: "Estado",
            accessor: "activo",
            render: (v) => (
                <Badge variant={v ? "success" : "error"}>
                    {v ? "Activo" : "Inactivo"}
                </Badge>
            )
        },
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
                    <h1 className="text-2xl font-bold tracking-tight text-text-primary">Productos</h1>
                    <p className="text-sm text-text-secondary">Gestiona tu catálogo de productos, categorías y precios bases.</p>
                </div>
                <Button
                    onClick={handleCreate}
                    className="flex items-center gap-2 px-5 py-2.5 rounded-xl bg-primary text-surface shadow-md shadow-primary/20 hover:shadow-lg hover:shadow-primary/40 hover:-translate-y-0.5 transition-all duration-300 font-bold text-sm tracking-wide"
                >
                    <Plus className="w-4 h-4" />
                    Nuevo Producto
                </Button>
            </div>

            <Card className="p-4 flex flex-col md:flex-row gap-4 items-end">
                <div className="flex-1 w-full">
                    <label className="text-sm text-text-secondary mb-1 block font-medium">Nombre del Producto</label>
                    <input
                        type="text"
                        value={searchNombre}
                        onChange={e => setSearchNombre(e.target.value)}
                        placeholder="Buscar por nombre..."
                        className="w-full bg-surface border border-border rounded-lg px-3 py-2 outline-none focus:border-primary text-text-primary transition-colors"
                    />
                </div>
                <div className="flex-1 w-full">
                    <label className="text-sm text-text-secondary mb-1 block font-medium">Categoría</label>
                    <select
                        value={searchCategoria}
                        onChange={e => setSearchCategoria(e.target.value)}
                        className="w-full bg-surface border border-border rounded-lg px-3 py-2 outline-none focus:border-primary text-text-primary transition-colors"
                    >
                        <option value="">Todas las categorías</option>
                        {CATEGORIAS.map(c => (
                            <option key={c} value={c}>{c}</option>
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
                    fetchData={fetchProductsData}
                    searchable={false}
                />
            </Card>

            <Modal isOpen={isModalOpen} onClose={() => setIsModalOpen(false)} title={selectedProduct ? "Modificar Producto" : "Crear Nuevo Producto"}>
                <div className="space-y-4">
                    <div className="flex flex-col gap-1">
                        <label className="text-sm text-text-secondary font-medium">Nombre</label>
                        <input
                            type="text"
                            value={formData.nombre}
                            onChange={e => setFormData({ ...formData, nombre: e.target.value })}
                            placeholder="Ej. Manzanas Rojas"
                            className="bg-surface px-3 py-2 rounded-lg border border-border text-text-primary focus:border-primary outline-none transition-colors"
                        />
                    </div>

                    <div className="flex flex-col gap-1">
                        <label className="text-sm text-text-secondary font-medium">Descripción</label>
                        <textarea
                            rows={3}
                            value={formData.descripcion}
                            onChange={e => setFormData({ ...formData, descripcion: e.target.value })}
                            placeholder="Breve descripción del producto..."
                            className="bg-surface px-3 py-2 rounded-lg border border-border text-text-primary focus:border-primary outline-none transition-colors resize-none"
                        />
                    </div>

                    <div className="grid grid-cols-2 gap-4">
                        <div className="flex flex-col gap-1">
                            <label className="text-sm text-text-secondary font-medium">Categoría</label>
                            <select
                                value={formData.categoria}
                                onChange={e => setFormData({ ...formData, categoria: e.target.value })}
                                className="w-full bg-surface border border-border rounded-lg px-3 py-2 outline-none focus:border-primary text-text-primary transition-colors"
                            >
                                <option value="">Selecciona...</option>
                                {CATEGORIAS.map(c => (
                                    <option key={c} value={c}>{c}</option>
                                ))}
                            </select>
                        </div>
                        <div className="flex flex-col gap-1">
                            <label className="text-sm text-text-secondary font-medium">Precio Base</label>
                            <input
                                type="number"
                                step="0.01"
                                value={formData.precio_base}
                                onChange={e => setFormData({ ...formData, precio_base: Number(e.target.value) })}
                                className="bg-surface px-3 py-2 rounded-lg border border-border text-text-primary focus:border-primary outline-none transition-colors"
                            />
                        </div>
                    </div>

                    <div className="flex flex-col gap-1">
                        <label className="text-sm text-text-secondary font-medium">URL de Imagen</label>
                        <input
                            type="url"
                            value={formData.imagen_url}
                            onChange={e => setFormData({ ...formData, imagen_url: e.target.value })}
                            placeholder="https://ejemplo.com/imagen.jpg"
                            className="bg-surface px-3 py-2 rounded-lg border border-border text-text-primary focus:border-primary outline-none transition-colors"
                        />
                    </div>

                    <div className="flex justify-between items-center mt-8 pt-5 border-t border-border">
                        {selectedProduct && selectedProduct.activo ? (
                            <Button
                                variant="danger"
                                isLoading={isSaving}
                                onClick={() => handleSave(false)}
                                className="flex items-center gap-2 px-5 py-2.5 rounded-xl bg-red-500/10 text-red-500 hover:bg-red-500 hover:text-white border border-transparent hover:border-red-600 transition-all duration-300"
                            >
                                <Trash2 className="w-4 h-4" />
                                <span className="font-semibold text-sm">Inactivar</span>
                            </Button>
                        ) : selectedProduct && !selectedProduct.activo ? (
                            <Button
                                variant="outline"
                                isLoading={isSaving}
                                onClick={() => handleSave(true)}
                                className="flex items-center gap-2 px-5 py-2.5 rounded-xl text-primary border-primary hover:bg-primary/10 transition-all duration-300"
                            >
                                <Plus className="w-4 h-4" />
                                <span className="font-semibold text-sm">Activar</span>
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
