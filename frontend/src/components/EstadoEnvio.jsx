/** Etiqueta de color para el estado de un envío. Reutilizable en todas las pantallas. */
export const NOMBRES_ESTADO = {
  REGISTRADO: 'Registrado',
  ASIGNADO: 'Asignado',
  EN_RUTA: 'En ruta',
  CON_NOVEDAD: 'Con novedad',
  ENTREGADO: 'Entregado',
  CANCELADO: 'Cancelado',
};

export default function EstadoEnvio({ estado }) {
  return <span className={`estado estado--${estado.toLowerCase()}`}>{NOMBRES_ESTADO[estado] ?? estado}</span>;
}
