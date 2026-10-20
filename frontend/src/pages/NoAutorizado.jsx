import { Link } from 'react-router-dom';

export default function NoAutorizado() {
  return (
    <div className="centrado">
      <div className="tarjeta">
        <h2>Acceso no autorizado</h2>
        <p>No tienes permiso para ver esta página.</p>
        <Link to="/">Volver al inicio</Link>
      </div>
    </div>
  );
}
