import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { crearEnvio, listarZonas } from '../../api/envios';
import { leerError } from '../../api/client';
import { validarEnvio, aCrearEnvio } from './validacionesEnvio';
import Input from '../../components/Input';
import Button from '../../components/Button';
import './NuevoEnvio.css';

const VACIO = {
  remitente: '', destinatario: '',
  direccionOrigen: '', direccionDestino: '',
  zonaOrigenId: '', zonaDestinoId: '',
  pesoKg: '', altoCm: '', anchoCm: '', largoCm: '',
};

const pesos = new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP', maximumFractionDigits: 0 });

/** HU-04: formulario para crear una solicitud de envío. La cotización (HU-05) va en su propio componente. */
export default function NuevoEnvio() {
  const [datos, setDatos] = useState(VACIO);
  const [zonas, setZonas] = useState([]);
  const [errores, setErrores] = useState({});
  const [error, setError] = useState(null);      // mensaje general de la API
  const [cargando, setCargando] = useState(false);
  const [creado, setCreado] = useState(null);    // envío que devolvió el backend

  useEffect(() => {
    listarZonas()
      .then(setZonas)
      .catch((err) => setError(leerError(err).mensaje));
  }, []);

  function cambiar(e) {
    const { name, value } = e.target;
    setDatos({ ...datos, [name]: value });
    setErrores({ ...errores, [name]: undefined });
  }

  async function onSubmit(e) {
    e.preventDefault();
    setError(null);

    const encontrados = validarEnvio(datos);
    setErrores(encontrados);
    if (Object.keys(encontrados).length) return;

    setCargando(true);
    try {
      setCreado(await crearEnvio(aCrearEnvio(datos)));
    } catch (err) {
      const { mensaje, campos } = leerError(err);
      if (Object.keys(campos).length) setErrores(campos);
      else setError(mensaje);
    } finally {
      setCargando(false);
    }
  }

  function otroEnvio() {
    setDatos(VACIO);
    setErrores({});
    setError(null);
    setCreado(null);
  }

  if (creado) {
    return (
      <section className="nuevo-envio">
        <div className="tarjeta nuevo-envio__exito">
          <h2>Envío creado</h2>
          <p className="exito">Tu envío quedó registrado.</p>
          <p className="muted">Código de seguimiento</p>
          <p className="nuevo-envio__codigo">{creado.codigo}</p>
          {creado.valorTotal != null && (
            <p>Valor: <strong>{pesos.format(creado.valorTotal)}</strong></p>
          )}
          <div className="nuevo-envio__acciones">
            <Link to="/cliente/envios" className="btn">Ver mis envíos</Link>
            <button type="button" className="btn btn-secundario" onClick={otroEnvio}>Crear otro envío</button>
          </div>
        </div>
      </section>
    );
  }

  const campo = (name, label, props = {}) => (
    <Input id={name} name={name} label={label} value={datos[name]} onChange={cambiar}
           error={errores[name]} {...props} />
  );

  const selectZona = (name, label) => (
    <div className="campo">
      <label htmlFor={name}>{label}</label>
      <select id={name} name={name} value={datos[name]} onChange={cambiar}
              className={errores[name] ? 'invalido' : ''}>
        <option value="">Selecciona una zona</option>
        {zonas.map((z) => <option key={z.id} value={z.id}>{z.nombre}</option>)}
      </select>
      {errores[name] && <small className="error">{errores[name]}</small>}
    </div>
  );

  const medida = { type: 'number', min: '0', step: 'any', inputMode: 'decimal' };

  return (
    <section className="nuevo-envio">
      <h2>Nuevo envío</h2>
      <p className="muted nuevo-envio__cobertura">Cobertura: Armenia y municipios cercanos (Quindío).</p>

      <form onSubmit={onSubmit} noValidate>
        {error && <p className="error nuevo-envio__mensaje">{error}</p>}

        <fieldset>
          <legend>Origen</legend>
          {campo('remitente', 'Remitente', { maxLength: 150 })}
          {campo('direccionOrigen', 'Dirección de origen', { maxLength: 255 })}
          {selectZona('zonaOrigenId', 'Zona de origen')}
        </fieldset>

        <fieldset>
          <legend>Destino</legend>
          {campo('destinatario', 'Destinatario', { maxLength: 150 })}
          {campo('direccionDestino', 'Dirección de destino', { maxLength: 255 })}
          {selectZona('zonaDestinoId', 'Zona de destino')}
        </fieldset>

        <fieldset className="nuevo-envio__paquete">
          <legend>Paquete</legend>
          {campo('pesoKg', 'Peso (kg)', medida)}
          {campo('altoCm', 'Alto (cm)', medida)}
          {campo('anchoCm', 'Ancho (cm)', medida)}
          {campo('largoCm', 'Largo (cm)', medida)}
        </fieldset>

        {/* HU-05: aquí van el botón "Cotizar" (api/cotizacion.js) y <CotizacionResumen cotizacion={...} />.
            Mientras tanto, el valor final se muestra al crear el envío. */}
        <p className="muted">El valor del envío se calcula con las zonas, el peso y las medidas.</p>

        <Button type="submit" cargando={cargando}>Crear envío</Button>
      </form>
    </section>
  );
}
