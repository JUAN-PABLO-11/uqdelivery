import axios from 'axios';

/** Cliente HTTP único: agrega el token y, si el backend responde 401, cierra la sesión. */
const api = axios.create({ baseURL: import.meta.env.VITE_API_URL });

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

api.interceptors.response.use(
  (res) => res,
  (err) => {
    const esLogin = err.config?.url?.includes('/auth/login');
    if (err.response?.status === 401 && !esLogin) {
      localStorage.removeItem('token');
      localStorage.removeItem('usuario');
      window.location.href = '/login';
    }
    return Promise.reject(err);
  }
);

/**
 * Convierte un error de Axios al formato común de la API:
 * { codigo, mensaje, campos: { campo: 'mensaje' } }
 */
export function leerError(err) {
  const data = err.response?.data;
  return {
    codigo: data?.codigo ?? 'ERROR',
    mensaje: data?.mensaje ?? 'No fue posible conectar con el servidor',
    campos: data?.campos ?? {},
  };
}

export default api;
