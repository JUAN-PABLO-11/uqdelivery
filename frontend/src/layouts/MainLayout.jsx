import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';

/** Menú por rol. Si tu HU necesita una opción nueva en el menú, agrégala aquí. */
const MENU = {
  ADMIN: [
    { to: '/admin/usuarios', label: 'Usuarios' },
    { to: '/admin/asignaciones', label: 'Asignar envíos' },
  ],
  CLIENTE: [
    { to: '/cliente/envios', label: 'Mis envíos' },
    { to: '/cliente/envios/nuevo', label: 'Nuevo envío' },
  ],
  REPARTIDOR: [{ to: '/repartidor/entregas', label: 'Mis entregas' }],
};

export default function MainLayout() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  return (
    <div className="layout">
      <header className="topbar">
        <strong>UQDelivery</strong>
        <nav>
          {MENU[user.rol].map((item) => (
            <NavLink key={item.to} to={item.to} end>
              {item.label}
            </NavLink>
          ))}
        </nav>
        <span className="usuario">
          {user.nombre} · {user.rol}
          <button className="btn btn-link" onClick={() => { logout(); navigate('/login'); }}>
            Salir
          </button>
        </span>
      </header>
      <main className="contenido">
        <Outlet />
      </main>
    </div>
  );
}
