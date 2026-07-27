import { Link, useLocation } from 'react-router-dom';
import { cn } from '../../utils/cn';
import {
  LayoutDashboard,
  Package,
  Layers,
  Tags,
  ShoppingCart,
  Users,
  BarChart2,
  UserCircle
} from 'lucide-react';

const routes = [
  { name: 'Dashboard', path: '/', icon: LayoutDashboard },
  { name: 'Productos', path: '/productos', icon: Package },
  { name: 'Lotes', path: '/lotes', icon: Layers },
  { name: 'Descuentos', path: '/descuentos', icon: Tags },
  { name: 'Pedidos', path: '/pedidos', icon: ShoppingCart },
  { name: 'Usuarios', path: '/usuarios', icon: Users },
];

export function Sidebar({ isOpen, setIsOpen }: { isOpen: boolean, setIsOpen: (val: boolean) => void }) {
  const location = useLocation();

  return (
    <>
      {/* Mobile overlay */}
      {isOpen && (
        <div
          className="fixed inset-0 z-40 bg-black/50 md:hidden"
          onClick={() => setIsOpen(false)}
        />
      )}

      <aside
        className={cn(
          "fixed inset-y-0 left-0 z-50 w-64 transform bg-surface border-r border-border transition-transform duration-300 md:relative md:translate-x-0 flex flex-col",
          isOpen ? "translate-x-0" : "-translate-x-full"
        )}
      >
        <div className="flex h-16 items-center px-6 border-b border-border">
          <span className="text-2xl font-bold text-primary tracking-tight">Fressco</span>
        </div>

        <nav className="flex-1 overflow-y-auto py-4">
          <ul className="space-y-1 px-3">
            {routes.map((route) => {
              const isActive = location.pathname === route.path || (route.path !== '/' && location.pathname.startsWith(route.path));
              return (
                <li key={route.path}>
                  <Link
                    to={route.path}
                    className={cn(
                      "flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-colors",
                      isActive
                        ? "bg-primary-container/20 text-primary"
                        : "text-text-secondary hover:bg-surface-variant hover:text-text-primary"
                    )}
                  >
                    <route.icon className={cn("h-5 w-5", isActive ? "text-primary" : "text-text-secondary")} />
                    {route.name}
                  </Link>
                </li>
              );
            })}
          </ul>
        </nav>

        <div className="p-4 border-t border-border">
          <Link
            to="/"
            className={cn(
              "flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-colors",
              location.pathname === '/perfil'
                ? "bg-primary-container/20 text-primary"
                : "text-text-secondary hover:bg-surface-variant hover:text-text-primary"
            )}
          >
            <UserCircle className="h-5 w-5" />
            Admin
          </Link>
        </div>
      </aside>
    </>
  );
}
