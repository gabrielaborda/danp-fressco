import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Card } from '../components/ui/Card';
import { Badge } from '../components/ui/Badge';
import { Button } from '../components/ui/Button';
import { AreaChart, Area, XAxis, YAxis, Tooltip, ResponsiveContainer } from 'recharts';
import { getProducts } from '../api/products';
import { getOrders } from '../api/orders';
import { getExpiringLotsReport, getTopSellingProducts } from '../api/reports';
import {
  AlertTriangle,
  PackageX,
  TrendingUp,
  Clock,
  ArrowRight,
  ShoppingBag,
  Flame,
  CheckCircle2,
  DollarSign
} from 'lucide-react';
import { toast } from 'react-toastify';

export default function Dashboard() {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(true);

  // States
  const [expiringLots, setExpiringLots] = useState<any[]>([]);
  const [lowStockProducts, setLowStockProducts] = useState<any[]>([]);
  const [topSellers, setTopSellers] = useState<any[]>([]);
  const [salesChart, setSalesChart] = useState<any[]>([]);

  // Summary Metrics
  const [stats, setStats] = useState({
    ventasHoy: 0,
    pedidosPendientes: 0,
    lotesPorVencerCount: 0,
    productosBajoStockCount: 0,
  });

  useEffect(() => {
    const fetchDashboardData = async () => {
      setLoading(true);
      try {
        const [productsRes, ordersRes, expiringRes, topSellersRes] = await Promise.all([
          getProducts(),
          getOrders(),
          getExpiringLotsReport(10),
          getTopSellingProducts(5),
        ]);

        const products: any[] = productsRes.data;
        const orders: any[] = ordersRes.data;
        const expiring: any[] = expiringRes.data;
        const topProd: any[] = topSellersRes.data;

        // 1. Low stock products (stock <= 5)
        const lowStock = products.filter((p: any) => (p.stock_total ?? 0) <= 5);
        setLowStockProducts(lowStock);

        // 2. Expiring lots
        setExpiringLots(expiring);

        // 3. Top sellers
        setTopSellers(topProd);

        // 4. Pending orders count & Sales today
        const todayStr = new Date().toDateString();
        const pendingOrders = orders.filter((o: any) => o.estado === 'pendiente');
        
        const hoyOrders = orders.filter(
          (o: any) => new Date(o.fecha_pedido).toDateString() === todayStr && o.estado !== 'cancelado'
        );
        const hoyVentasTotal = hoyOrders.reduce((sum: number, o: any) => sum + Number(o.total || 0), 0);

        setStats({
          ventasHoy: hoyVentasTotal,
          pedidosPendientes: pendingOrders.length,
          lotesPorVencerCount: expiring.length,
          productosBajoStockCount: lowStock.length,
        });

        // 5. 7-Day Chart Data
        const daysMap: Record<string, { label: string; total: number }> = {};
        for (let i = 6; i >= 0; i--) {
          const d = new Date();
          d.setDate(d.getDate() - i);
          const key = d.toISOString().split('T')[0];
          const label = d.toLocaleDateString('es-PE', { weekday: 'short', day: 'numeric' });
          daysMap[key] = { label, total: 0 };
        }

        orders.forEach((o: any) => {
          if (o.estado === 'cancelado') return;
          const key = new Date(o.fecha_pedido).toISOString().split('T')[0];
          if (daysMap[key]) {
            daysMap[key].total += Number(o.total || 0);
          }
        });

        setSalesChart(Object.values(daysMap));
      } catch (err: any) {
        console.error(err);
        toast.error('Error al cargar datos del Dashboard');
      } finally {
        setLoading(false);
      }
    };

    fetchDashboardData();
  }, []);

  return (
    <div className="space-y-6">
      {/* Dynamic Header */}
      <div className="flex flex-col md:flex-row justify-between md:items-center gap-4">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-text-primary">Panel de Control</h1>
          <p className="text-sm text-text-secondary">
            Monitoreo en tiempo real de lotes por vencer, alertas de inventario y ventas.
          </p>
        </div>
        <div className="flex gap-2">
          <Button variant="outline" onClick={() => navigate('/descuentos')}>
            <TrendingUp className="w-4 h-4 mr-2" /> Crear Descuento
          </Button>
          <Button onClick={() => navigate('/lotes')}>
            <ShoppingBag className="w-4 h-4 mr-2" /> Nuevo Lote
          </Button>
        </div>
      </div>

      {/* KPI Cards Grid */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        {/* Sales Today */}
        <Card className="p-5 flex items-center gap-4 border-l-4 border-emerald-500">
          <div className="p-3 bg-emerald-500/10 text-emerald-500 rounded-xl">
            <DollarSign className="w-6 h-6" />
          </div>
          <div>
            <p className="text-xs font-medium text-text-secondary">Ventas de Hoy</p>
            <p className="text-2xl font-bold text-text-primary">S/.{stats.ventasHoy.toFixed(2)}</p>
          </div>
        </Card>

        {/* Pending Orders */}
        <Card 
          className="p-5 flex items-center justify-between border-l-4 border-blue-500 cursor-pointer hover:bg-surface-variant transition-colors"
          onClick={() => navigate('/pedidos')}
        >
          <div className="flex items-center gap-4">
            <div className="p-3 bg-blue-500/10 text-blue-500 rounded-xl">
              <Clock className="w-6 h-6" />
            </div>
            <div>
              <p className="text-xs font-medium text-text-secondary">Pedidos Pendientes</p>
              <p className="text-2xl font-bold text-text-primary">{stats.pedidosPendientes}</p>
            </div>
          </div>
          <ArrowRight className="w-4 h-4 text-text-secondary" />
        </Card>

        {/* Expiring Lots Alert */}
        <Card 
          className="p-5 flex items-center justify-between border-l-4 border-amber-500 cursor-pointer hover:bg-surface-variant transition-colors"
          onClick={() => navigate('/lotes')}
        >
          <div className="flex items-center gap-4">
            <div className="p-3 bg-amber-500/10 text-amber-500 rounded-xl">
              <AlertTriangle className="w-6 h-6" />
            </div>
            <div>
              <p className="text-xs font-medium text-text-secondary">Lotes por Vencer</p>
              <p className="text-2xl font-bold text-text-primary">{stats.lotesPorVencerCount}</p>
            </div>
          </div>
          <Badge variant="warning">{stats.lotesPorVencerCount > 0 ? 'Atención' : 'OK'}</Badge>
        </Card>

        {/* Low Stock Alert */}
        <Card 
          className="p-5 flex items-center justify-between border-l-4 border-rose-500 cursor-pointer hover:bg-surface-variant transition-colors"
          onClick={() => navigate('/productos')}
        >
          <div className="flex items-center gap-4">
            <div className="p-3 bg-rose-500/10 text-rose-500 rounded-xl">
              <PackageX className="w-6 h-6" />
            </div>
            <div>
              <p className="text-xs font-medium text-text-secondary">Stock Bajo / Agotado</p>
              <p className="text-2xl font-bold text-text-primary">{stats.productosBajoStockCount}</p>
            </div>
          </div>
          <Badge variant={stats.productosBajoStockCount > 0 ? 'error' : 'success'}>
            {stats.productosBajoStockCount > 0 ? 'Reponer' : 'OK'}
          </Badge>
        </Card>
      </div>

      {/* Main Alert Grids: Expiring Lots & Low Stock */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Section 1: Expiring Lots */}
        <Card className="p-5 flex flex-col">
          <div className="flex justify-between items-center pb-4 mb-4 border-b border-border">
            <div className="flex items-center gap-2">
              <AlertTriangle className="w-5 h-5 text-amber-500" />
              <h2 className="font-semibold text-text-primary">Lotes Próximos a Vencer (10 días)</h2>
            </div>
            <Button variant="ghost" size="sm" onClick={() => navigate('/descuentos')}>
              Descuentos <ArrowRight className="w-3.5 h-3.5 ml-1" />
            </Button>
          </div>

          <div className="space-y-3 flex-1 overflow-y-auto max-h-[320px] pr-1">
            {expiringLots.length === 0 ? (
              <div className="flex flex-col items-center justify-center py-8 text-text-secondary">
                <CheckCircle2 className="w-8 h-8 text-emerald-500 mb-2" />
                <p className="text-sm">¡Excelente! No hay lotes próximos a vencer.</p>
              </div>
            ) : (
              expiringLots.map((item) => (
                <div
                  key={item.lote_id}
                  className="flex items-center justify-between p-3 rounded-xl bg-surface-variant/60 border border-border/50 hover:border-amber-500/40 transition-colors"
                >
                  <div className="space-y-0.5">
                    <p className="font-semibold text-sm text-text-primary">{item.nombre_producto}</p>
                    <p className="text-xs text-text-secondary">
                      Lote #{item.lote_id} • Disponibles: <span className="font-medium text-text-primary">{item.cantidad_disponible} un.</span>
                    </p>
                  </div>
                  <div className="text-right flex flex-col items-end gap-1">
                    <Badge variant={item.dias_restantes <= 3 ? 'error' : 'warning'}>
                      {item.dias_restantes === 0 ? 'Vence Hoy' : `${item.dias_restantes} días`}
                    </Badge>
                    <span className="text-[11px] text-text-secondary">Vence: {item.fecha_vencimiento}</span>
                  </div>
                </div>
              ))
            )}
          </div>
        </Card>

        {/* Section 2: Low Stock Products */}
        <Card className="p-5 flex flex-col">
          <div className="flex justify-between items-center pb-4 mb-4 border-b border-border">
            <div className="flex items-center gap-2">
              <PackageX className="w-5 h-5 text-rose-500" />
              <h2 className="font-semibold text-text-primary">Productos Bajos en Stock (≤ 5 un.)</h2>
            </div>
            <Button variant="ghost" size="sm" onClick={() => navigate('/lotes')}>
              Añadir Lote <ArrowRight className="w-3.5 h-3.5 ml-1" />
            </Button>
          </div>

          <div className="space-y-3 flex-1 overflow-y-auto max-h-[320px] pr-1">
            {lowStockProducts.length === 0 ? (
              <div className="flex flex-col items-center justify-center py-8 text-text-secondary">
                <CheckCircle2 className="w-8 h-8 text-emerald-500 mb-2" />
                <p className="text-sm">Todo el catálogo tiene stock suficiente.</p>
              </div>
            ) : (
              lowStockProducts.map((p) => {
                const stock = p.stock_total ?? 0;
                return (
                  <div
                    key={p.id}
                    className="flex items-center justify-between p-3 rounded-xl bg-surface-variant/60 border border-border/50 hover:border-rose-500/40 transition-colors"
                  >
                    <div className="flex items-center gap-3">
                      {p.imagen_url ? (
                        <img src={p.imagen_url} alt={p.nombre} className="w-9 h-9 rounded-lg object-cover border border-border" />
                      ) : (
                        <div className="w-9 h-9 rounded-lg bg-surface flex items-center justify-center text-xs text-text-secondary border border-border">
                          📦
                        </div>
                      )}
                      <div>
                        <p className="font-semibold text-sm text-text-primary">{p.nombre}</p>
                        <p className="text-xs text-text-secondary">{p.categoria || 'Sin Categoría'}</p>
                      </div>
                    </div>
                    <div className="text-right">
                      <Badge variant={stock === 0 ? 'error' : 'warning'}>
                        {stock === 0 ? 'Agotado' : `${stock} un. restantes`}
                      </Badge>
                    </div>
                  </div>
                );
              })
            )}
          </div>
        </Card>
      </div>

      {/* Bottom Section: Sales Chart & Top Selling Products */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Sales Chart */}
        <Card className="lg:col-span-2 p-5" title="Ingresos de los últimos 7 días">
          <div className="h-[260px] w-full pt-4">
            <ResponsiveContainer width="100%" height="100%">
              <AreaChart data={salesChart}>
                <defs>
                  <linearGradient id="salesGrad" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="var(--color-primary, #6366f1)" stopOpacity={0.4} />
                    <stop offset="95%" stopColor="var(--color-primary, #6366f1)" stopOpacity={0.0} />
                  </linearGradient>
                </defs>
                <XAxis dataKey="label" stroke="#888888" fontSize={12} tickLine={false} axisLine={false} />
                <YAxis stroke="#888888" fontSize={12} tickLine={false} axisLine={false} tickFormatter={(v) => `S/.${v}`} />
                <Tooltip
                  formatter={(value: any) => [`S/.${Number(value).toFixed(2)}`, 'Ventas']}
                  contentStyle={{ backgroundColor: 'var(--color-surface, #1e1e2d)', borderColor: 'var(--color-border, #333)', borderRadius: '10px' }}
                />
                <Area type="monotone" dataKey="total" stroke="var(--color-primary, #6366f1)" strokeWidth={2.5} fillOpacity={1} fill="url(#salesGrad)" />
              </AreaChart>
            </ResponsiveContainer>
          </div>
        </Card>

        {/* Top Selling Products */}
        <Card className="p-5 flex flex-col">
          <div className="flex items-center gap-2 pb-3 mb-3 border-b border-border">
            <Flame className="w-5 h-5 text-amber-500" />
            <h2 className="font-semibold text-text-primary">Productos Más Vendidos</h2>
          </div>
          <div className="space-y-3 flex-1">
            {topSellers.length === 0 ? (
              <p className="text-sm text-text-secondary text-center py-6">Sin registros de ventas aún.</p>
            ) : (
              topSellers.map((item, idx) => (
                <div key={item.producto_id || idx} className="flex items-center justify-between text-sm py-1 border-b border-border/40 last:border-0">
                  <div className="flex items-center gap-2">
                    <span className="font-mono text-xs font-bold text-primary w-4">#{idx + 1}</span>
                    <div>
                      <p className="font-medium text-text-primary line-clamp-1">{item.nombre}</p>
                      <p className="text-[11px] text-text-secondary">{item.unidades_vendidas} un. vendidas</p>
                    </div>
                  </div>
                  <span className="font-bold text-xs text-text-primary">S/.{Number(item.ingresos_totales || 0).toFixed(2)}</span>
                </div>
              ))
            )}
          </div>
        </Card>
      </div>
    </div>
  );
}
