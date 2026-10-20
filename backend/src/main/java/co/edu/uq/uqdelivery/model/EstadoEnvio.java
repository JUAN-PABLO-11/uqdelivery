package co.edu.uq.uqdelivery.model;

/**
 * Estados del envío. CANCELADO se deja desde ya (HU-13, sprint posterior) para no tener
 * que modificar la columna de la BD más adelante.
 */
public enum EstadoEnvio { REGISTRADO, ASIGNADO, EN_RUTA, CON_NOVEDAD, ENTREGADO, CANCELADO }
