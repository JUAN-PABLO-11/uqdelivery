import api from './client';

// HU-01
export const login = (correo, password) =>
  api.post('/auth/login', { correo, password }).then((r) => r.data);

export const registro = ({ nombre, correo, password }) =>
  api.post('/auth/registro', { nombre, correo, password }).then((r) => r.data);
