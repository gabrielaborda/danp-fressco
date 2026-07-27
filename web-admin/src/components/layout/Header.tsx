import { Menu, LogOut } from 'lucide-react';
import { useAuthStore } from '../../store/authStore';
import { ThemeToggle } from '../ui/ThemeToggle';
import { Button } from '../ui/Button';
import { useNavigate } from 'react-router-dom';

export function Header({ onMenuClick }: { onMenuClick: () => void }) {
  const { nombre, rol, logout } = useAuthStore();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <header className="h-16 border-b border-border bg-surface flex items-center justify-between px-4 lg:px-8">
      <div className="flex items-center gap-4">
        <Button 
          variant="ghost" 
          size="sm" 
          className="md:hidden" 
          onClick={onMenuClick}
        >
          <Menu className="h-6 w-6" />
        </Button>
      </div>

      <div className="flex items-center gap-4">
        <ThemeToggle />
        
        <div className="hidden md:flex flex-col items-end">
          <span className="text-sm font-bold text-text-primary">{nombre || 'Administrador'}</span>
          <span className="text-xs text-text-secondary capitalize">{rol || 'admin'}</span>
        </div>
        
        <Button 
          variant="ghost" 
          size="sm"
          onClick={handleLogout}
          className="text-text-secondary hover:text-error"
          title="Cerrar sesión"
        >
          <LogOut className="h-5 w-5" />
        </Button>
      </div>
    </header>
  );
}
