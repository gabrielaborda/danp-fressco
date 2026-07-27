import { Routes, Route, Navigate } from "react-router-dom";
import LoginPage from "../pages/AuthPages/LoginPage";
import AdminLayout from "../components/layout/AdminLayout";

import Dashboard from "../pages/Dashboard";
import Productos from "../pages/Productos";

import Lotes from "../pages/Lotes";
import Descuentos from "../pages/Descuentos";
import Pedidos from "../pages/Pedidos";
import Usuarios from "../pages/Usuarios";
import Reportes from "../pages/Reportes";
import Perfil from "../pages/Perfil";

export default function AppRouter() {
    return (
        <Routes>
            <Route path="/login" element={<LoginPage />} />
            
            <Route element={<AdminLayout />}>
              <Route path="/" element={<Dashboard />} />
              <Route path="/productos" element={<Productos />} />
              <Route path="/lotes" element={<Lotes />} />
              <Route path="/descuentos" element={<Descuentos />} />
              <Route path="/pedidos" element={<Pedidos />} />
              <Route path="/usuarios" element={<Usuarios />} />
              <Route path="/reportes" element={<Reportes />} />
              <Route path="/perfil" element={<Perfil />} />
            </Route>

            <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
    );
}
