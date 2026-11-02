import { useEffect, useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { leerError } from '../../api/client';
import { cambiarEstadoEntrega, listarEntregas } from '../../api/entregas';
import EstadoEnvio from '../../components/EstadoEnvio';
import './MisEntregas.css';

/**
 * HU-06 / HU-10: entregas pendientes del repartidor.
 *  ASIGNADO    -> "Iniciar entrega"  (EN_RUTA)
 *  EN_RUTA     -> "Marcar entregada" (ENTREGADO) o "Reportar novedad" (HU-10)
 *  CON_NOVEDAD -> "Retomar entrega"  (EN_RUTA)   o "Marcar entregada"
 */
export default function MisEntregas() {
  const location = useLocation();
  const navigate = useNavigate();
  const [entregas, setEntregas] = useState([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState(null);
  const [mensaje, setMensaje] = useState(location.state?.mensaje ?? null);
  const [procesando, setProcesando] = useState(null); // id del envío que se está actualizando

  async function cargar() {
    try {
      setEntregas(await listarEntregas());
    } catch (err) {
      setError(leerError(err).mensaje);
    } finally {
      setCargando(false);
    }
  }

  useEffect(() => {
    cargar();
    // Limpia el mensaje que llegó desde "Reportar novedad" para que no reaparezca al recargar
    if (location.state?.mensaje) navigate(location.pathname, { replace: true, state: null });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  async function cambiar(entrega, estado, textoExito, detalle) {
    if (estado === 'ENTREGADO' && !window.confirm(`¿Confirmas que el envío ${entrega.codigo} fue entregado?`)) return;

    setProcesando(entrega.id);
    setError(null);
    setMensaje(null);
    try {
      await cambiarEstadoEntrega(entrega.id, estado, detalle);
      setMensaje(`${entrega.codigo}: ${textoExito}`);
      await cargar();
    } catch (err) {
      setError(leerError(err).mensaje);
    } finally {
      setProcesando(null);
    }
  }

  return (
    <section className="entregas">
      <div className="encabezado-pagina">
        <div>
          <h2>Mis entregas</h2>
          <p className="muted">Envíos que tienes asignados y aún no se han entregado.</p>
        </div>
      </div>

      {mensaje && <p className="exito">{mensaje}</p>}
      {error && <p className="error entregas__error">{error}</p>}
      {cargando && <p className="muted">Cargando entregas…</p>}

      {!cargando && !error && entregas.length === 0 && (
        <div className="panel">
          <p className="muted entregas__vacio">No tienes entregas pendientes por ahora.</p>
        </div>
      )}

      <div className="entregas__lista">
        {entregas.map((e) => {
          const ocupado = procesando === e.id;
          return (
            <article key={e.id} className={`entrega entrega--${e.estado.toLowerCase()}`}>
              <header className="entrega__encabezado">
                <span className="codigo">{e.codigo}</span>
                <EstadoEnvio estado={e.estado} />
              </header>

              <dl className="entrega__datos">
                <dt>Destinatario</dt>
                <dd>{e.destinatario}</dd>
                <dt>Dirección</dt>
                <dd>
                  {e.direccionDestino}
                  {e.zonaDestino && <span className="muted"> · {e.zonaDestino}</span>}
                </dd>
              </dl>

              <div className="acciones">
                {e.estado === 'ASIGNADO' && (
                  <button className="btn" disabled={ocupado}
                          onClick={() => cambiar(e, 'EN_RUTA', 'entrega iniciada, ahora está en ruta.', 'Inicio de entrega')}>
                    {ocupado ? 'Actualizando…' : 'Iniciar entrega'}
                  </button>
                )}

                {e.estado === 'CON_NOVEDAD' && (
                  <button className="btn" disabled={ocupado}
                          onClick={() => cambiar(e, 'EN_RUTA', 'la entrega se retomó.', 'Se retoma la entrega')}>
                    {ocupado ? 'Actualizando…' : 'Retomar entrega'}
                  </button>
                )}

                {(e.estado === 'EN_RUTA' || e.estado === 'CON_NOVEDAD') && (
                  <button className="btn btn-exito" disabled={ocupado}
                          onClick={() => cambiar(e, 'ENTREGADO', 'marcado como entregado.', 'Entregado')}>
                    Marcar entregada
                  </button>
                )}

                {e.estado === 'EN_RUTA' && (
                  <Link className="btn-secundario" to={`/repartidor/entregas/${e.id}/novedad`}
                        state={{ codigo: e.codigo, destinatario: e.destinatario, direccion: e.direccionDestino }}>
                    Reportar novedad
                  </Link>
                )}
              </div>
            </article>
          );
        })}
      </div>
    </section>
  );
}
