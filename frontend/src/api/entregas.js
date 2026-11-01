import api from './client';

/**
 * HU-06 / HU-10: entregas del repartidor autenticado.
 * El reporte de novedades está en api/novedades.js.
 */

/** Entregas pendientes (ASIGNADO, EN_RUTA, CON_NOVEDAD). */
export const listarEntregas = () => api.get('/repartidor/entregas').then((r) => r.data);

/** "Iniciar entrega" / "Retomar" (EN_RUTA) y "Marcar entregado" (ENTREGADO). */
export const cambiarEstadoEntrega = (envioId, estado, detalle) =>
  api.patch(`/repartidor/envios/${envioId}/estado`, { estado, detalle }).then((r) => r.data);
