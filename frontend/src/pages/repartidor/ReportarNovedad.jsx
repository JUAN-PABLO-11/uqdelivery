import { useEffect, useState } from 'react';
import { Link, useLocation, useNavigate, useParams } from 'react-router-dom';
import { registrarNovedad } from '../../api/novedades';
import { leerError } from '../../api/client';
import Button from '../../components/Button';
import './ReportarNovedad.css';

const TIPOS_NOVEDAD = [
  { value: 'DIRECCION_INCORRECTA', label: 'Dirección incorrecta' },
  { value: 'DESTINATARIO_AUSENTE', label: 'Destinatario ausente' },
  { value: 'PAQUETE_DANADO', label: 'Paquete dañado' },
  { value: 'OTRO', label: 'Otro motivo' },
];

const MIN = 10;
const MAX = 500;
const HTML = /<[^>]*>|javascript:|on\w+\s*=/i;

/** HU-10: el repartidor reporta una novedad sobre un envío EN_RUTA (pasa a CON_NOVEDAD). */
export default function ReportarNovedad() {
  const { id } = useParams();
  const navigate = useNavigate();
  const envio = useLocation().state; // { codigo, destinatario, direccion } si viene desde "Mis entregas"

  const [tipo, setTipo] = useState('');
  const [descripcion, setDescripcion] = useState('');
  const [coordenadas, setCoordenadas] = useState(null);
  const [gps, setGps] = useState('buscando'); // buscando | ok | no-disponible
  const [cargando, setCargando] = useState(false);
  const [errorGeneral, setErrorGeneral] = useState('');
  const [errores, setErrores] = useState({});

  // Ubicación GPS opcional: si el navegador no la da, la novedad se guarda sin coordenadas
  useEffect(() => {
    if (!('geolocation' in navigator)) {
      setGps('no-disponible');
      return;
    }
    navigator.geolocation.getCurrentPosition(
      (pos) => {
        setCoordenadas({ latitud: pos.coords.latitude, longitud: pos.coords.longitude });
        setGps('ok');
      },
      () => setGps('no-disponible'),
      { enableHighAccuracy: true, timeout: 8000 }
    );
  }, []);

  function validar() {
    const e = {};
    const texto = descripcion.trim();
    if (!tipo) e.tipo = 'Selecciona el tipo de novedad';
    if (texto.length < MIN) e.descripcion = `La descripción debe tener al menos ${MIN} caracteres`;
    else if (texto.length > MAX) e.descripcion = `La descripción no puede superar ${MAX} caracteres`;
    else if (HTML.test(texto)) e.descripcion = 'La descripción no puede contener HTML o scripts';
    return e;
  }

  async function onSubmit(evento) {
    evento.preventDefault();
    setErrorGeneral('');
    const e = validar();
    setErrores(e);
    if (Object.keys(e).length) return;

    setCargando(true);
    try {
      await registrarNovedad(id, {
        tipo,
        descripcion: descripcion.trim(),
        latitud: coordenadas?.latitud ?? null,
        longitud: coordenadas?.longitud ?? null,
      });
      navigate('/repartidor/entregas', {
        replace: true,
        state: { mensaje: `Novedad reportada${envio?.codigo ? ` para ${envio.codigo}` : ''}. El envío quedó "Con novedad".` },
      });
    } catch (err) {
      const { mensaje, campos } = leerError(err);
      if (Object.keys(campos).length) setErrores(campos);
      else setErrorGeneral(mensaje);
    } finally {
      setCargando(false);
    }
  }

  const largo = descripcion.trim().length;

  return (
    <section className="novedad">
      <Link to="/repartidor/entregas" className="novedad__volver">← Mis entregas</Link>

      <form className="panel novedad__form" onSubmit={onSubmit} noValidate>
        <div>
          <h2>Reportar novedad</h2>
          <p className="muted">
            {envio?.codigo ? <>Envío <strong>{envio.codigo}</strong> · {envio.destinatario} · {envio.direccion}</> : <>Envío #{id}</>}
          </p>
        </div>

        {errorGeneral && <p className="error novedad__alerta">{errorGeneral}</p>}

        <div className="campo">
          <label htmlFor="tipo">Tipo de novedad</label>
          <select id="tipo" value={tipo} className={errores.tipo ? 'invalido' : ''}
                  onChange={(e) => { setTipo(e.target.value); setErrores({ ...errores, tipo: undefined }); }}>
            <option value="">Selecciona…</option>
            {TIPOS_NOVEDAD.map((t) => <option key={t.value} value={t.value}>{t.label}</option>)}
          </select>
          {errores.tipo && <small className="error">{errores.tipo}</small>}
        </div>

        <div className="campo">
          <label htmlFor="descripcion">Descripción</label>
          <textarea id="descripcion" rows={4} maxLength={MAX} value={descripcion}
                    className={errores.descripcion ? 'invalido' : ''}
                    placeholder="Describe lo que pasó (mínimo 10 caracteres)"
                    onChange={(e) => { setDescripcion(e.target.value); setErrores({ ...errores, descripcion: undefined }); }} />
          <div className="novedad__ayuda">
            {errores.descripcion ? <small className="error">{errores.descripcion}</small> : <span />}
            <span className="contador">{largo} / {MAX}</span>
          </div>
        </div>

        <p className={`novedad__gps novedad__gps--${gps}`}>
          {gps === 'buscando' && 'Obteniendo ubicación GPS…'}
          {gps === 'ok' && `Ubicación: ${coordenadas.latitud.toFixed(4)}, ${coordenadas.longitud.toFixed(4)}`}
          {gps === 'no-disponible' && 'Ubicación no disponible: la novedad se guardará sin coordenadas.'}
        </p>

        <div className="acciones">
          <Button type="submit" cargando={cargando}>Reportar novedad</Button>
          <Link to="/repartidor/entregas" className="btn-secundario">Cancelar</Link>
        </div>
      </form>
    </section>
  );
}
