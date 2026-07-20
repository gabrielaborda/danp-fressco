import React, { useEffect, useState } from 'react';
import { Card } from '../components/ui/Card';
import { Badge } from '../components/ui/Badge';
import { LineChart, Line, XAxis, YAxis, Tooltip, ResponsiveContainer } from 'recharts';
import { getLots, getExpiringLots } from '../api/lots';
import { getOrders } from '../api/orders';
import { useToast } from '../components/ui/ToastProvider';
import { DataTable, type Column } from '../components/ui/DataTable';

interface Metric {
  label: string;
  value: number | string;
  variant?: 'default' | 'warning' | 'info' | 'success' | 'error';
}

export default function Dashboard() {
  const [metrics, setMetrics] = useState<Metric[]>([]);
  const [salesData, setSalesData] = useState<any[]>([]);
  const [expiringLots, setExpiringLots] = useState<any[]>([]);
  const [totalLots, setTotalLots] = useState(0);
  const toast = useToast();

  // Load metrics and data
  useEffect(() => {
    const fetchAll = async () => {
      try {
        const [lotsRes, ordersRes] = await Promise.all([
          getLots({ page: 1, pageSize: 1000 }), // assume backend returns all
          getOrders({ page: 1, pageSize: 1000 }),
        ]);
        const lots = (lotsRes as any).data;
        const orders = (ordersRes as any).data;
        setTotalLots(lots.length);
        const expiring = await getExpiringLots();
        setExpiringLots((expiring as any).data);

        // Simple sales aggregation for last 7 days (dummy example)
        const salesByDay: Record<string, number> = {};
        const today = new Date();
        for (let i = 0; i < 7; i++) {
          const d = new Date(today);
          d.setDate(d.getDate() - i);
          const key = d.toISOString().split('T')[0];
          salesByDay[key] = 0;
        }
        orders.forEach((o: any) => {
          const date = new Date(o.fecha).toISOString().split('T')[0];
          if (salesByDay[date] !== undefined) salesByDay[date] += o.total;
        });
        const chartData = Object.entries(salesByDay)
          .map(([date, total]) => ({ date, total }))
          .reverse();
        setSalesData(chartData);

        setMetrics([
          { label: 'Productos activos', value: lots.filter((l: any) => l.estado === 'disponible').length },
          { label: 'Lotes próximos a vencer', value: expiring.data.length, variant: 'warning' },
          { label: 'Pedidos del día', value: orders.filter((o: any) => new Date(o.fecha).toDateString() === today.toDateString()).length, variant: 'info' },
          { label: 'Ventas del día', value: orders
            .filter((o: any) => new Date(o.fecha).toDateString() === today.toDateString())
            .reduce((sum: number, o: any) => sum + o.total, 0), variant: 'success' },
        ]);
      } catch (e: any) {
        toast.error(e?.message ?? 'Error cargando el dashboard');
      }
    };
    fetchAll();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const lotColumns: Column<any>[] = [
    { header: 'Producto', accessor: (row) => row.producto_nombre },
    { header: 'Cantidad', accessor: 'cantidad' },
    { header: 'Vencimiento', accessor: 'fecha_vencimiento' },
    { header: 'Descuento', accessor: 'descuento_actual', render: (v) => <Badge variant={v > 0 ? 'warning' : 'default'}>{v}%</Badge> },
  ];

  return (
    <div className="space-y-6">
      {/* Métricas */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        {metrics.map((m, i) => (
          <Card key={i} className="flex flex-col items-center py-6">
            <span className="text-xl font-bold text-primary">{m.value}</span>
            <span className={`mt-1 text-sm ${m.variant ? 'text-' + m.variant : 'text-text-secondary'}`}>{m.label}</span>
          </Card>
        ))}
      </div>

      {/* Gráfico ventas */}
      <Card title="Ventas últimos 7 días">
        <ResponsiveContainer width="100%" height={300}>
          <LineChart data={salesData}>
            <XAxis dataKey="date" />
            <YAxis />
            <Tooltip />
            <Line type="monotone" dataKey="total" stroke="var(--color-primary)" strokeWidth={2} />
          </LineChart>
        </ResponsiveContainer>
      </Card>

      {/* Lotes próximos a vencer */}
      <Card title="Lotes próximos a vencer (5 días)">
        <DataTable
          columns={lotColumns}
          fetchData={async (_) => {
            const res = await getExpiringLots();
            return { data: (res as any).data, total: (res as any).data.length };
          }}
        />
      </Card>
    </div>
  );
}
