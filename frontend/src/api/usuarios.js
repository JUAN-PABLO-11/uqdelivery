import api from './client';

// Obtener todos los usuarios, con filtro opcional por rol.
export async function listarUsuarios(rol = '') {
  const respuesta = await api.get('/admin/usuarios', {
    params: rol ? { rol } : {},
  });

  return respuesta.data;
}

// Crear un usuario.
export async function crearUsuario(datos) {
  const respuesta = await api.post('/admin/usuarios', datos);
  return respuesta.data;
}

// Actualizar nombre y correo.
export async function actualizarUsuario(id, datos) {
  const respuesta = await api.put(`/admin/usuarios/${id}`, datos);
  return respuesta.data;
}

// Cambiar el rol de un usuario.
export async function cambiarRolUsuario(id, rol) {
  const respuesta = await api.patch(`/admin/usuarios/${id}/rol`, { rol });
  return respuesta.data;
}

// Activar o desactivar un usuario.
export async function cambiarEstadoUsuario(id, activo) {
  const respuesta = await api.patch(`/admin/usuarios/${id}/estado`, {
    activo,
  });

  return respuesta.data;
}