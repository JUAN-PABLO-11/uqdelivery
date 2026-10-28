import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { listarEnvios } from '../../api/envios';
import { leerError } from '../../api/client';
import './MisEnvios.css';

/** Texto que ve el cliente para cada estado del envío. */
const ESTADOS = {
  REGISTRADO: 'Registrado',
  ASIGNADO: 'Asignado',
  EN_RUTA: 'En ruta',
  CON_NOVEDAD: 'Con novedad',
  ENTREGADO: 'Entregado',
};

const pesos = new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP', maximumFractionDigits: 0 });
const fecha = new Intl.DateTimeFormat('es-CO', { dateStyle: 'medium', timeStyle: 'short' });

/** HU-04: lista simple de los envíos del cliente autenticado. */
export default function MisEnvios() {
  const [envios, setEnvios] = useState([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    listarEnvios()
      .then(setEnvios)
      .catch((err) => setError(leerError(err).mensaje))
      .finally(() => setCargando(false));
  }, []);

  return (
    <section className="mis-envios">
      <div className="mis-envios__encabezado">
        <h2>Mis envíos</h2>
        <Link to="/cliente/envios/nuevo" className="btn">Nuevo envío</Link>
      </div>

      {cargando && <p className="muted">Cargando envíos…</p>}

      {error && <p className="error">{error}</p>}

      {!cargando && !error && envios.length === 0 && (
        <p className="muted">
          Aún no tienes envíos. <Link to="/cliente/envios/nuevo">Crea el primero</Link>.
        </p>
      )}

      {!cargando && !error && envios.length > 0 && (
        <div className="mis-envios__tabla">
          <table>
            <thead>
              <tr>
                <th>Código</th>
                <th>Destino</th>
                <th>Estado</th>
                <th className="numero">Valor</th>
                <th>Fecha</th>
              </tr>
            </thead>
            <tbody>
              {envios.map((e) => (
                <tr key={e.id}>
                  <td className="codigo">{e.codigo}</td>
                  <td>
                    {e.direccionDestino}
                    <span className="muted"> · {e.zonaDestino}</span>
                  </td>
                  <td>
                    <span className={`estado estado--${e.estado.toLowerCase()}`}>
                      {ESTADOS[e.estado] ?? e.estado}
                    </span>
                  </td>
                  <td className="numero">{e.valorTotal != null ? pesos.format(e.valorTotal) : '—'}</td>
                  <td>{e.creadoEn ? fecha.format(new Date(e.creadoEn)) : '—'}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </section>
  );
}
