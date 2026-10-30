/**
 * HU-04: mismas reglas que el backend (CrearEnvioRequest).
 * Validar aquí da respuesta inmediata; el backend vuelve a validar siempre.
 * Los nombres de los campos son los mismos que espera POST /envios.
 */
const HTML = /<[^>]*>|javascript:|on\w+\s*=/i;

const TEXTOS = [
  { campo: 'remitente', obligatorio: 'El remitente es obligatorio', max: 150 },
  { campo: 'destinatario', obligatorio: 'El destinatario es obligatorio', max: 150 },
  { campo: 'direccionOrigen', obligatorio: 'La dirección de origen es obligatoria', max: 255 },
  { campo: 'direccionDestino', obligatorio: 'La dirección de destino es obligatoria', max: 255 },
];

const ZONAS = [
  { campo: 'zonaOrigenId', obligatorio: 'La zona de origen es obligatoria' },
  { campo: 'zonaDestinoId', obligatorio: 'La zona de destino es obligatoria' },
];

const MEDIDAS = ['pesoKg', 'altoCm', 'anchoCm', 'largoCm'];

/** Devuelve { campo: mensaje } con los errores; vacío si todo está bien. */
export function validarEnvio(datos) {
  const errores = {};

  for (const { campo, obligatorio, max } of TEXTOS) {
    const valor = (datos[campo] ?? '').trim();
    if (!valor) errores[campo] = obligatorio;
    else if (valor.length > max) errores[campo] = `Máximo ${max} caracteres`;
    else if (HTML.test(valor)) errores[campo] = 'Contiene caracteres no permitidos';
  }

  for (const { campo, obligatorio } of ZONAS) {
    if (!datos[campo]) errores[campo] = obligatorio;
  }

  for (const campo of MEDIDAS) {
    const valor = Number(datos[campo]);
    if (datos[campo] === '' || datos[campo] == null || !Number.isFinite(valor) || valor <= 0) {
      errores[campo] = 'Debe ser mayor a 0';
    }
  }

  return errores;
}

/** Convierte lo que hay en el formulario (todo texto) en el cuerpo de POST /envios. */
export function aCrearEnvio(datos) {
  return {
    remitente: datos.remitente.trim(),
    destinatario: datos.destinatario.trim(),
    direccionOrigen: datos.direccionOrigen.trim(),
    direccionDestino: datos.direccionDestino.trim(),
    zonaOrigenId: Number(datos.zonaOrigenId),
    zonaDestinoId: Number(datos.zonaDestinoId),
    pesoKg: Number(datos.pesoKg),
    altoCm: Number(datos.altoCm),
    anchoCm: Number(datos.anchoCm),
    largoCm: Number(datos.largoCm),
  };
}
