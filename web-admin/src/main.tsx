import { createRoot } from 'react-dom/client'
import { BrowserRouter } from "react-router-dom";
import './index.css'
import AppRouter from './routes/AppRouter.tsx'
import { useThemeStore } from './store/themeStore.ts';
import { ToastProvider } from './components/ui/ToastProvider';

useThemeStore.getState().initTheme();

createRoot(document.getElementById('root')!).render(
  <BrowserRouter>
    <ToastProvider>
      <AppRouter />
    </ToastProvider>
  </BrowserRouter>
)
