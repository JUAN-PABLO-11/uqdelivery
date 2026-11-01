import { useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { useAuth, rutaInicio } from '../../auth/AuthContext';
import { leerError } from '../../api/client';
import { validarLogin } from '../../auth/validaciones';
import Input from '../../components/Input';
import Button from '../../components/Button';

/** HU-01: inicio de sesión. */
export default function Login() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const mensajeExito = location.state?.mensaje;   // viene de Registro

  const [correo, setCorreo] = useState(location.state?.correo ?? '');
  const [password, setPassword] = useState('');
  const [errores, setErrores] = useState({});
  const [error, setError] = useState(null);       // { codigo, mensaje }
  const [cargando, setCargando] = useState(false);

  async function onSubmit(e) {
    e.preventDefault();
    setError(null);

    const encontrados = validarLogin({ correo, password });
    setErrores(encontrados);
    if (Object.keys(encontrados).length) return;

    setCargando(true);
    try {
      const usuario = await login(correo.trim(), password);
      navigate(rutaInicio(usuario.rol), { replace: true });
    } catch (err) {
      const { codigo, mensaje, campos } = leerError(err);
      if (Object.keys(campos).length) setErrores(campos);
      else setError({ codigo, mensaje });
      setPassword('');
    } finally {
      setCargando(false);
    }
  }

  return (
    <div className="centrado">
      <form className="tarjeta" onSubmit={onSubmit} noValidate>
        <h1>UQDelivery</h1>

        {mensajeExito && !error && <p className="exito">{mensajeExito}</p>}

        <Input id="correo" label="Correo" type="email" value={correo} autoComplete="email"
               onChange={(e) => { setCorreo(e.target.value); setErrores({ ...errores, correo: undefined }); }}
               error={errores.correo} />
        <Input id="password" label="Contraseña" type="password" value={password} autoComplete="current-password"
               onChange={(e) => { setPassword(e.target.value); setErrores({ ...errores, password: undefined }); }}
               error={errores.password} />

        {error && (
          <p className={error.codigo === 'CUENTA_BLOQUEADA' ? 'alerta' : 'error'}>{error.mensaje}</p>
        )}

        <Button type="submit" cargando={cargando}>Iniciar sesión</Button>
        <p className="muted">¿No tienes cuenta? <Link to="/registro">Regístrate</Link></p>
      </form>
    </div>
  );
}
