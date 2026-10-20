import { createContext, useContext, useState } from 'react';
import * as authApi from '../api/auth';

const AuthContext = createContext(null);

export function rutaInicio(rol) {
  return { ADMIN: '/admin', CLIENTE: '/cliente', REPARTIDOR: '/repartidor' }[rol] ?? '/login';
}

function leerUsuarioGuardado() {
  try {
    return JSON.parse(localStorage.getItem('usuario'));
  } catch {
    return null;
  }
}

export function AuthProvider({ children }) {
  const [user, setUser] = useState(leerUsuarioGuardado);

  async function login(correo, password) {
    const { token, ...datos } = await authApi.login(correo, password);
    localStorage.setItem('token', token);
    localStorage.setItem('usuario', JSON.stringify(datos));
    setUser(datos);
    return datos; // { id, nombre, correo, rol }
  }

  function logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('usuario');
    setUser(null);
  }

  return <AuthContext.Provider value={{ user, login, logout }}>{children}</AuthContext.Provider>;
}

export const useAuth = () => useContext(AuthContext);
