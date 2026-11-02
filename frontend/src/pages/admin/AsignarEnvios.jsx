import { useEffect, useState } from 'react';
import { leerError } from '../../api/client';
import { asignarEnvio, listarEnviosAdmin, listarRepartidores, reasignarEnvio } from '../../api/asignaciones';
import EstadoEnvio, { NOMBRES_ESTADO } from '../../components/EstadoEnvio';
import Button from '../../components/Button';
import './AsignarEnvios.css';

const HTML = /<[^>]*>|javascript:|on\w+\s*=/i;
const fecha = new Intl.DateTimeFormat('es-CO', { dateStyle: 'medium', timeStyle: 'short' });

/**
 * HU-06: el administrador asigna envíos REGISTRADOS a un repartidor disponible
 * y reasigna envíos ASIGNADOS indicando el motivo (queda en el historial).
 */
export default function AsignarEnvios() {
  const [filtro, setFiltro] = useState('REGISTRADO');
  const [envios, setEnvios] = useState([]);
  const [repartidores, setRepartidores] = useState([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState(null);
  const [mensaje, setMensaje] = useState(null);

  // Envío seleccionado para asignar/reasignar
  const [seleccionado, setSeleccionado] = useState(null);
  const [repartidorId, setRepartidorId] = useState('');
  const [motivo, setMotivo] = useState('');
  const [errores, setErrores] = useState({});
  const [guardando, setGuardando] = useState(false);

  async function cargar() {
    setCargando(true);
    setError(null);
    try {
      const [lista, reps] = await Promise.all([listarEnviosAdmin(filtro), listarRepartidores()]);
      setEnvios(lista);
      setRepartidores(reps);
    } catch (err) {
      setError(leerError(err).mensaje);
    } finally {
      setCargando(false);
    }
  }

  useEffect(() => {
    cargar();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [filtro]);

  function abrir(envio) {
    setSeleccionado(envio);
    setRepartidorId('');
    setMotivo('');
    setErrores({});
    setMensaje(null);
    setError(null);
  }

  function cerrar() {
    setSeleccionado(null);
    setErrores({});
  }

  const esReasignacion = seleccionado?.estado === 'ASIGNADO';
  // Al reasignar no se ofrece el repartidor actual
  const opciones = repartidores.filter((r) => !esReasignacion || r.id !== seleccionado.repartidorId);

  function validar() {
    const e = {};
    if (!repartidorId) e.repartidorId = 'Selecciona un repartidor';
    if (esReasignacion) {
      if (motivo.trim().length < 5) e.motivo = 'El motivo debe tener al menos 5 caracteres';
      else if (HTML.test(motivo)) e.motivo = 'Contiene caracteres no permitidos';
    }
    return e;
  }

  async function confirmar(evento) {
    evento.preventDefault();
    const e = validar();
    setErrores(e);
    if (Object.keys(e).length) return;

    setGuardando(true);
    setError(null);
    try {
      const nombre = repartidores.find((r) => r.id === Number(repartidorId))?.nombre;
      if (esReasignacion) {
        await reasignarEnvio(seleccionado.id, Number(repartidorId), motivo.trim());
        setMensaje(`El envío ${seleccionado.codigo} se reasignó a ${nombre}.`);
      } else {
        await asignarEnvio(seleccionado.id, Number(repartidorId));
        setMensaje(`El envío ${seleccionado.codigo} se asignó a ${nombre}.`);
      }
      cerrar();
      await cargar();
    } catch (err) {
      const { mensaje: msg, campos } = leerError(err);
      if (Object.keys(campos).length) setErrores(campos);
      else setError(msg);
    } finally {
      setGuardando(false);
    }
  }

  return (
    <section className="asignar">
      <div className="encabezado-pagina">
        <div>
          <h2>Asignación de repartidores</h2>
          <p className="muted">Asigna los envíos registrados y reasigna los que aún no han salido a ruta.</p>
        </div>
        <label className="filtro">
          Mostrar
          <select value={filtro} onChange={(e) => { setFiltro(e.target.value); cerrar(); }}>
            <option value="REGISTRADO">Pendientes de asignar</option>
            <option value="ASIGNADO">Asignados</option>
            <option value="">Todos</option>
            {['EN_RUTA', 'CON_NOVEDAD', 'ENTREGADO'].map((e) => (
              <option key={e} value={e}>{NOMBRES_ESTADO[e]}</option>
            ))}
          </select>
        </label>
      </div>

      {mensaje && <p className="exito">{mensaje}</p>}
      {error && <p className="error asignar__error">{error}</p>}

      {seleccionado && (
        <form className="panel asignar__panel" onSubmit={confirmar} noValidate>
          <h3>
            {esReasignacion ? 'Reasignar' : 'Asignar'} envío <span className="codigo">{seleccionado.codigo}</span>
          </h3>
          <p className="muted">
            Para {seleccionado.destinatario} · {seleccionado.direccionDestino}
            {esReasignacion && <> · Repartidor actual: <strong>{seleccionado.repartidor}</strong></>}
          </p>

          {opciones.length === 0 ? (
            <p className="alerta">No hay otros repartidores activos y disponibles.</p>
          ) : (
            <div className="asignar__campos">
              <div className="campo">
                <label htmlFor="repartidor">{esReasignacion ? 'Nuevo repartidor' : 'Repartidor'}</label>
                <select id="repartidor" value={repartidorId} className={errores.repartidorId ? 'invalido' : ''}
                        onChange={(e) => { setRepartidorId(e.target.value); setErrores({ ...errores, repartidorId: undefined }); }}>
                  <option value="">Selecciona…</option>
                  {opciones.map((r) => <option key={r.id} value={r.id}>{r.nombre}</option>)}
                </select>
                {errores.repartidorId && <small className="error">{errores.repartidorId}</small>}
              </div>

              {esReasignacion && (
                <div className="campo">
                  <label htmlFor="motivo">Motivo de la reasignación</label>
                  <input id="motivo" value={motivo} maxLength={255} className={errores.motivo ? 'invalido' : ''}
                         placeholder="Ej: el repartidor no tiene vehículo hoy"
                         onChange={(e) => { setMotivo(e.target.value); setErrores({ ...errores, motivo: undefined }); }} />
                  {errores.motivo && <small className="error">{errores.motivo}</small>}
                </div>
              )}
            </div>
          )}

          <div className="acciones">
            <Button type="submit" cargando={guardando} disabled={opciones.length === 0}>
              {esReasignacion ? 'Confirmar reasignación' : 'Confirmar asignación'}
            </Button>
            <button type="button" className="btn-secundario" onClick={cerrar} disabled={guardando}>Cancelar</button>
          </div>
        </form>
      )}

      {cargando && <p className="muted">Cargando envíos…</p>}

      {!cargando && !error && envios.length === 0 && (
        <p className="muted">No hay envíos para mostrar con este filtro.</p>
      )}

      {!cargando && envios.length > 0 && (
        <div className="tabla-contenedor">
          <table className="tabla">
            <thead>
              <tr>
                <th>Código</th>
                <th>Cliente</th>
                <th>Destino</th>
                <th>Estado</th>
                <th>Repartidor</th>
                <th>Creado</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {envios.map((e) => (
                <tr key={e.id} className={seleccionado?.id === e.id ? 'seleccionada' : ''}>
                  <td className="codigo">{e.codigo}</td>
                  <td>{e.cliente}</td>
                  <td>
                    {e.direccionDestino}
                    {e.zonaDestino && <span className="muted"> · {e.zonaDestino}</span>}
                  </td>
                  <td><EstadoEnvio estado={e.estado} /></td>
                  <td>{e.repartidor ?? <span className="muted">Sin asignar</span>}</td>
                  <td className="asignar__fecha">{e.creadoEn ? fecha.format(new Date(e.creadoEn)) : '—'}</td>
                  <td>
                    {e.estado === 'REGISTRADO' && (
                      <button className="btn btn-chico" onClick={() => abrir(e)}>Asignar</button>
                    )}
                    {e.estado === 'ASIGNADO' && (
                      <button className="btn-secundario btn-chico" onClick={() => abrir(e)}>Reasignar</button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </section>
  );
}
