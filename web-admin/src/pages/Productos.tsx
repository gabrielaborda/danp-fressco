import React, { useState } from 'react';
import { DataTable, type Column } from '../components/ui/DataTable';
import { getProducts, createProduct, updateProduct, deleteProduct } from '../api/products';
import { Modal } from '../components/ui/Modal';
import { Button } from '../components/ui/Button';
import InputField from '../components/auth/login/InputField';
import { useToast } from '../components/ui/ToastProvider';
import { useForm } from 'react-hook-form';
import { Badge } from '../components/ui/Badge';

interface Product {
  id: number;
  nombre: string;
  categoria: string;
  tienda: string;
  precio_base: number;
  lotes_activos: number;
  imagen_url?: string;
}

export default function Productos() {
  const toast = useToast();
  const [isModalOpen, setModalOpen] = useState(false);
  const [editingProduct, setEditingProduct] = useState<Product | null>(null);

  const columns: Column<Product>[] = [
    { header: 'Nombre', accessor: 'nombre' },
    { header: 'Categoría', accessor: 'categoria' },
    { header: 'Tienda', accessor: 'tienda' },
    { header: 'Precio base', accessor: (row) => `$${Number(row.precio_base).toFixed(2)}` },
    { header: 'Lotes activos', accessor: 'lotes_activos' },
    {
      header: 'Acciones',
      accessor: (row) => row.id,
      render: (_, row) => (
        <div className="flex gap-2 justify-end">
          <Button variant="outline" size="sm" onClick={() => { setEditingProduct(row as Product); setModalOpen(true); }}>
            Editar
          </Button>
          <Button variant="danger" size="sm" onClick={() => handleDelete(row.id)}>
            Eliminar
          </Button>
        </div>
      ),
    },
  ];

  const handleDelete = async (id: number) => {
    if (!window.confirm('¿Eliminar este producto?')) return;
    try {
      await deleteProduct(id);
      toast.success('Producto eliminado');
    } catch (e: any) {
      toast.error(e?.message ?? 'Error al eliminar');
    }
  };

  // Modal form handling
  const { register, handleSubmit, reset, formState: { errors } } = useForm<Product>();

  const onSubmit = async (data: Product) => {
    try {
      if (editingProduct) {
        await updateProduct(editingProduct.id, data);
        toast.success('Producto actualizado');
      } else {
        await createProduct(data);
        toast.success('Producto creado');
      }
      setModalOpen(false);
      setEditingProduct(null);
      reset();
    } catch (e: any) {
      toast.error(e?.message ?? 'Error al guardar');
    }
  };

  const openCreate = () => {
    setEditingProduct(null);
    reset();
    setModalOpen(true);
  };

  return (
    <div className="space-y-4">
      <div className="flex justify-between items-center">
        <h1 className="text-2xl font-bold text-text-primary">Productos</h1>
        <Button onClick={openCreate}>Nuevo producto</Button>
      </div>

      <DataTable
        columns={columns}
        fetchData={async ({ page, pageSize, search }) => {
          const res = await getProducts({ page, pageSize, search });
          return { data: (res as any).data, total: (res as any).total };
        }}
      />

      <Modal isOpen={isModalOpen} onClose={() => setModalOpen(false)} title={editingProduct ? 'Editar producto' : 'Nuevo producto'}>
        <form onSubmit={handleSubmit(onSubmit)} className="grid gap-4">
          <InputField
            type="text"
            label="Nombre"
            value={''}
            onChange={() => {}}
            icon={<span className='text-sm'>🛒</span>}
            autoComplete="off"
            {...register('nombre', { required: true })}
          />
          {/* Additional fields */}
          <InputField
            type="text"
            label="Categoría"
            value={''}
            onChange={() => {}}
            autoComplete="off"
            {...register('categoria', { required: true })}
          />
          <InputField
            type="text"
            label="Tienda"
            value={''}
            onChange={() => {}}
            autoComplete="off"
            {...register('tienda', { required: true })}
          />
          <InputField
            type="number"
            label="Precio base"
            value={0}
            onChange={() => {}}
            autoComplete="off"
            {...register('precio_base', { required: true, valueAsNumber: true })}
          />
          <InputField
            type="number"
            label="Lotes activos"
            value={0}
            onChange={() => {}}
            autoComplete="off"
            {...register('lotes_activos', { required: true, valueAsNumber: true })}
          />
          <div className="flex justify-end gap-2">
            <Button variant="outline" onClick={() => setModalOpen(false)}>
              Cancelar
            </Button>
            <Button type="submit" isLoading={false}>
              Guardar
            </Button>
          </div>
        </form>
      </Modal>
    </div>
  );
}
