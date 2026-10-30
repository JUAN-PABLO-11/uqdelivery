import client from './client';

/**
 * Reporta una novedad sobre un envío asignado en ruta (HU-10).
 * @param {number|string} envioId ID del envío
 * @param {Object} novedadData Objeto con tipo, descripcion, latitud, longitud
 */
export const registrarNovedad = async (envioId, novedadData) => {
    const response = await client.post(`/repartidor/envios/${envioId}/novedades`, novedadData);
    return response.data;
};