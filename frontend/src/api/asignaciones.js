import api from './client';

/**
 * HU-06: asignación y reasignación de repartidores (rol ADMIN).
 * Los errores se leen con leerError() de client.js.
 */

/** Envíos para el admin. estado opcional: 'REGISTRADO', 'ASIGNADO', ... */
export const listarEnviosAdmin = (estado) =>
  api.get('/admin/envios', { params: estado ? { estado } : {} }).then((r) => r.data);

/** Repartidores activos y disponibles: [{ id, nombre }] */
export const listarRepartidores = () => api.get('/admin/repartidores').then((r) => r.data);

export const asignarEnvio = (envioId, repartidorId) =>
  api.post(`/admin/envios/${envioId}/asignar`, { repartidorId });

export const reasignarEnvio = (envioId, repartidorId, motivo) =>
  api.post(`/admin/envios/${envioId}/reasignar`, { repartidorId, motivo });
