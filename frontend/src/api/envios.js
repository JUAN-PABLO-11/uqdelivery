import api from './client';

/**
 * HU-04: llamadas al backend de envíos.
 * El token lo agrega el cliente `api`. Los errores se leen con leerError() de client.js.
 * La cotización (POST /envios/cotizar) va en api/cotizacion.js (HU-05).
 */

/** Zonas activas para los selects de origen y destino: [{ id, nombre, latitud, longitud }]. */
export const listarZonas = () => api.get('/zonas').then((r) => r.data);

/** Crea el envío. Responde con el envío creado, incluido su código de seguimiento. */
export const crearEnvio = (datos) => api.post('/envios', datos).then((r) => r.data);

/** Envíos del cliente autenticado, del más reciente al más antiguo. */
export const listarEnvios = () => api.get('/envios').then((r) => r.data);

/** Detalle de un envío del cliente (403 si es de otro cliente). */
export const obtenerEnvio = (id) => api.get(`/envios/${id}`).then((r) => r.data);
