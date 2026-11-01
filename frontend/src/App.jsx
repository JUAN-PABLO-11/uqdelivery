import { Navigate, Route, Routes } from 'react-router-dom';
import ProtectedRoute from './auth/ProtectedRoute';
import { useAuth, rutaInicio } from './auth/AuthContext';
import MainLayout from './layouts/MainLayout';
import Login from './pages/auth/Login';
import Registro from './pages/auth/Registro';
import NoAutorizado from './pages/NoAutorizado';
import Usuarios from './pages/admin/Usuarios';
import AsignarEnvios from './pages/admin/AsignarEnvios';
import MisEnvios from './pages/cliente/MisEnvios';
import NuevoEnvio from './pages/cliente/NuevoEnvio';
import MisEntregas from './pages/repartidor/MisEntregas';
import ReportarNovedad from "./pages/repartidor/ReportarNovedad";

/**
 * TODAS las rutas quedan registradas aquí desde el Caso 00.
 * Cada HU trabaja dentro de su página; así nadie tiene que editar este archivo (menos conflictos).
 */
export default function App() {
  const { user } = useAuth();

  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route path="/registro" element={<Registro />} />
      <Route path="/no-autorizado" element={<NoAutorizado />} />

      <Route path="/admin" element={<ProtectedRoute roles={['ADMIN']}><MainLayout /></ProtectedRoute>}>
        <Route index element={<Navigate to="usuarios" replace />} />
        <Route path="usuarios" element={<Usuarios />} />          {/* HU-02 */}
        <Route path="asignaciones" element={<AsignarEnvios />} /> {/* HU-06 */}
      </Route>

      <Route path="/cliente" element={<ProtectedRoute roles={['CLIENTE']}><MainLayout /></ProtectedRoute>}>
        <Route index element={<Navigate to="envios" replace />} />
        <Route path="envios" element={<MisEnvios />} />           {/* HU-04 */}
        <Route path="envios/nuevo" element={<NuevoEnvio />} />    {/* HU-04 + HU-05 */}
      </Route>

      <Route path="/repartidor" element={<ProtectedRoute roles={['REPARTIDOR']}><MainLayout /></ProtectedRoute>}>
        <Route index element={<Navigate to="entregas" replace />} />
        <Route path="entregas" element={<MisEntregas />} />
          <Route path="entregas/:id/novedad" element={<ReportarNovedad />} />
      </Route>

      <Route path="*" element={<Navigate to={user ? rutaInicio(user.rol) : '/login'} replace />} />
    </Routes>
  );
}
