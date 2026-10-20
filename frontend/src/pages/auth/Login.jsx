import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth, rutaInicio } from '../../auth/AuthContext';
import { leerError } from '../../api/client';
import Input from '../../components/Input';
import Button from '../../components/Button';

/** Login básico del Caso 00. HU-01 lo mejora (validaciones, bloqueo, mensajes). */
export default function Login() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [correo, setCorreo] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState(null);
  const [cargando, setCargando] = useState(false);

  async function onSubmit(e) {
    e.preventDefault();
    setError(null);
    setCargando(true);
    try {
      const usuario = await login(correo, password);
      navigate(rutaInicio(usuario.rol), { replace: true });
    } catch (err) {
      setError(leerError(err));
    } finally {
      setCargando(false);
    }
  }

  return (
    <div className="centrado">
      <form className="tarjeta" onSubmit={onSubmit} noValidate>
        <h1>UQDelivery</h1>
        <Input id="correo" label="Correo" type="email" value={correo}
               onChange={(e) => setCorreo(e.target.value)} error={error?.campos?.correo} required />
        <Input id="password" label="Contraseña" type="password" value={password}
               onChange={(e) => setPassword(e.target.value)} error={error?.campos?.password} required />
        {error && !Object.keys(error.campos).length && <p className="error">{error.mensaje}</p>}
        <Button type="submit" cargando={cargando}>Iniciar sesión</Button>
        <p className="muted">¿No tienes cuenta? <Link to="/registro">Regístrate</Link></p>
      </form>
    </div>
  );
}
