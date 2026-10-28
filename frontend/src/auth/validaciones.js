/**
 * HU-01: mismas reglas que el backend (RegistroRequest / LoginRequest).
 * Validar aquí da respuesta inmediata; el backend vuelve a validar siempre.
 */
const CORREO = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
const HTML = /<[^>]*>|javascript:|on\w+\s*=/i;

export const REGLAS_PASSWORD = [
  { id: 'largo', texto: 'Mínimo 8 caracteres', cumple: (p) => p.length >= 8 },
  { id: 'mayus', texto: 'Una letra mayúscula', cumple: (p) => /[A-Z]/.test(p) },
  { id: 'numero', texto: 'Un número', cumple: (p) => /\d/.test(p) },
  { id: 'simbolo', texto: 'Un símbolo (ej: * # @ !)', cumple: (p) => /[^A-Za-z0-9]/.test(p) },
];

export function validarCorreo(correo) {
  if (!correo.trim()) return 'El correo es obligatorio';
  if (!CORREO.test(correo.trim())) return 'Correo inválido';
  if (correo.trim().length > 150) return 'El correo no puede superar 150 caracteres';
  return null;
}

export function validarLogin({ correo, password }) {
  const errores = {};
  const e = validarCorreo(correo);
  if (e) errores.correo = e;
  if (!password) errores.password = 'La contraseña es obligatoria';
  return errores;
}

export function validarRegistro({ nombre, correo, password, confirmar }) {
  const errores = {};

  if (!nombre.trim()) errores.nombre = 'El nombre es obligatorio';
  else if (nombre.trim().length > 100) errores.nombre = 'El nombre no puede superar 100 caracteres';
  else if (HTML.test(nombre)) errores.nombre = 'Contiene caracteres no permitidos';

  const e = validarCorreo(correo);
  if (e) errores.correo = e;

  if (!password) errores.password = 'La contraseña es obligatoria';
  else if (REGLAS_PASSWORD.some((r) => !r.cumple(password)))
    errores.password = 'La contraseña no cumple los requisitos';

  if (confirmar !== password) errores.confirmar = 'Las contraseñas no coinciden';

  return errores;
}
