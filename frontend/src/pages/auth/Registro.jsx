import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import * as authApi from '../../api/auth';
import { leerError } from '../../api/client';
import { REGLAS_PASSWORD, validarRegistro } from '../../auth/validaciones';
import Input from '../../components/Input';
import Button from '../../components/Button';

/** HU-01: registro de clientes. */
export default function Registro() {
  const navigate = useNavigate();
  const [form, setForm] = useState({ nombre: '', correo: '', password: '', confirmar: '' });
  const [errores, setErrores] = useState({});
  const [errorGeneral, setErrorGeneral] = useState(null);
  const [cargando, setCargando] = useState(false);

  const cambiar = (campo) => (e) => {
    setForm({ ...form, [campo]: e.target.value });
    setErrores({ ...errores, [campo]: undefined });
  };

  async function onSubmit(e) {
    e.preventDefault();
    setErrorGeneral(null);

    const encontrados = validarRegistro(form);
    setErrores(encontrados);
    if (Object.keys(encontrados).length) return;

    setCargando(true);
    try {
      await authApi.registro({ nombre: form.nombre.trim(), correo: form.correo.trim(), password: form.password });
      navigate('/login', { replace: true, state: { mensaje: 'Registro exitoso. Ya puedes iniciar sesión.', correo: form.correo.trim() } });
    } catch (err) {
      const { codigo, mensaje, campos } = leerError(err);
      if (codigo === 'CORREO_DUPLICADO') setErrores({ correo: mensaje });
      else if (Object.keys(campos).length) setErrores(campos);
      else setErrorGeneral(mensaje);
    } finally {
      setCargando(false);
    }
  }

  return (
    <div className="centrado">
      <form className="tarjeta" onSubmit={onSubmit} noValidate>
        <h1>Crear cuenta</h1>

        <Input id="nombre" label="Nombre completo" value={form.nombre} onChange={cambiar('nombre')}
               error={errores.nombre} maxLength={100} autoComplete="name" />
        <Input id="correo" label="Correo" type="email" value={form.correo} onChange={cambiar('correo')}
               error={errores.correo} maxLength={150} autoComplete="email" />
        <Input id="password" label="Contraseña" type="password" value={form.password} onChange={cambiar('password')}
               error={errores.password} autoComplete="new-password" />

        <ul className="reglas">
          {REGLAS_PASSWORD.map((r) => (
            <li key={r.id} className={r.cumple(form.password) ? 'ok' : ''}>
              {r.cumple(form.password) ? '✓' : '•'} {r.texto}
            </li>
          ))}
        </ul>

        <Input id="confirmar" label="Confirmar contraseña" type="password" value={form.confirmar}
               onChange={cambiar('confirmar')} error={errores.confirmar} autoComplete="new-password" />

        {errorGeneral && <p className="error">{errorGeneral}</p>}

        <Button type="submit" cargando={cargando}>Registrarme</Button>
        <p className="muted">¿Ya tienes cuenta? <Link to="/login">Inicia sesión</Link></p>
      </form>
    </div>
  );
}
