import api from './client';

// HU-01: agregar aquí registro(), etc.
export const login = (correo, password) =>
  api.post('/auth/login', { correo, password }).then((r) => r.data);
