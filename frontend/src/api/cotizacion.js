import api from './client';

/**
 * HU-05: cotiza un envío sin crearlo. Requiere sesión de CLIENTE.
 * Los errores se leen con leerError() de client.js (p. ej. 422 FUERA_DE_COBERTURA).
 *
 * datos: { zonaOrigenId, zonaDestinoId, pesoKg, altoCm, anchoCm, largoCm }
 * Responde: { distanciaKm, tarifaBase, valorDistancia, valorPeso, total }
 */
export const cotizar = (datos) => api.post('/envios/cotizar', datos).then((r) => r.data);
