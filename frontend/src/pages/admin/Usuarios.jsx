import { useEffect, useState } from 'react';
import './usuarios.css';

import apiError, { leerError } from '../../api/client';
import {
  listarUsuarios,
  crearUsuario,
  actualizarUsuario,
  cambiarRolUsuario,
  cambiarEstadoUsuario,
} from '../../api/usuarios';

import Button from '../../components/Button';
import Input from '../../components/Input';

const formularioInicial = {
  nombre: '',
  correo: '',
  password: '',
  rol: 'CLIENTE',
};

export default function Usuarios() {
  const [usuarios, setUsuarios] = useState([]);
  const [filtroRol, setFiltroRol] = useState('');
  const [formulario, setFormulario] = useState(formularioInicial);
  const [usuarioEditando, setUsuarioEditando] = useState(null);

  const [cargando, setCargando] = useState(false);
  const [guardando, setGuardando] = useState(false);
  const [error, setError] = useState('');
  const [erroresCampos, setErroresCampos] = useState({});
  const [mensaje, setMensaje] = useState('');

  async function cargarUsuarios() {
    setCargando(true);
    setError('');

    try {
      const datos = await listarUsuarios(filtroRol);

      // Permite recibir una lista directa o una lista dentro de "usuarios".
      const lista = Array.isArray(datos) ? datos : datos?.usuarios;
      setUsuarios(Array.isArray(lista) ? lista : []);
    } catch (err) {
      setError(leerError(err).mensaje);
    } finally {
      setCargando(false);
    }
  }

  useEffect(() => {
    cargarUsuarios();
  }, [filtroRol]);

  function manejarCambio(evento) {
    const { name, value } = evento.target;

    setFormulario((actual) => ({
      ...actual,
      [name]: value,
    }));

    setErroresCampos((actual) => ({
      ...actual,
      [name]: '',
    }));
  }

  function limpiarFormulario() {
    setFormulario(formularioInicial);
    setUsuarioEditando(null);
    setErroresCampos({});
  }

  function prepararEdicion(usuario) {
    setUsuarioEditando(usuario);
    setFormulario({
      nombre: usuario.nombre ?? '',
      correo: usuario.correo ?? '',
      password: '',
      rol: usuario.rol ?? 'CLIENTE',
    });

    setErroresCampos({});
    setMensaje('');
    setError('');
  }

  async function manejarEnvio(evento) {
    evento.preventDefault();
    setGuardando(true);
    setError('');
    setMensaje('');
    setErroresCampos({});

    try {
      if (usuarioEditando) {
        await actualizarUsuario(usuarioEditando.id, {
          nombre: formulario.nombre.trim(),
          correo: formulario.correo.trim(),
        });

        // El cambio de rol se realiza por su endpoint específico.
        if (formulario.rol !== usuarioEditando.rol) {
          await cambiarRolUsuario(usuarioEditando.id, formulario.rol);
        }

        setMensaje('Usuario actualizado correctamente.');
      } else {
        await crearUsuario({
          nombre: formulario.nombre.trim(),
          correo: formulario.correo.trim(),
          password: formulario.password,
          rol: formulario.rol,
        });

        setMensaje('Usuario creado correctamente.');
      }

      limpiarFormulario();
      await cargarUsuarios();
    } catch (err) {
      const detalle = leerError(err);
      setError(detalle.mensaje);
      setErroresCampos(detalle.campos ?? {});
    } finally {
      setGuardando(false);
    }
  }

  async function manejarEstado(usuario) {
    const nuevoEstado = !usuario.activo;
    const accion = nuevoEstado ? 'activar' : 'desactivar';

    if (!window.confirm(`¿Deseas ${accion} a ${usuario.nombre}?`)) {
      return;
    }

    setError('');
    setMensaje('');

    try {
      await cambiarEstadoUsuario(usuario.id, nuevoEstado);
      setMensaje(
        nuevoEstado
          ? 'Usuario activado correctamente.'
          : 'Usuario desactivado correctamente.',
      );

      await cargarUsuarios();
    } catch (err) {
      setError(leerError(err).mensaje);
    }
  }

  return (
    <main className="contenido usuarios">
      <header className="usuarios-encabezado">
        <div>
          <h1>Gestión de usuarios y roles</h1>
          <p className="muted">
            Consulta usuarios, registra nuevos usuarios y administra sus roles.
          </p>
        </div>

        <Button type="button" onClick={limpiarFormulario}>
          Nuevo usuario
        </Button>
      </header>

      {mensaje && <p className="usuarios-mensaje">{mensaje}</p>}
      {error && <p className="error usuarios-alerta">{error}</p>}

      <section className="usuarios-panel">
        <h2>{usuarioEditando ? 'Editar usuario' : 'Crear usuario'}</h2>

        <form className="usuarios-formulario" onSubmit={manejarEnvio}>
          <Input
            id="nombre"
            name="nombre"
            label="Nombre"
            value={formulario.nombre}
            onChange={manejarCambio}
            error={erroresCampos.nombre}
            required
          />

          <Input
            id="correo"
            name="correo"
            label="Correo electrónico"
            type="email"
            value={formulario.correo}
            onChange={manejarCambio}
            error={erroresCampos.correo}
            required
          />

          {!usuarioEditando && (
            <Input
              id="password"
              name="password"
              label="Contraseña"
              type="password"
              value={formulario.password}
              onChange={manejarCambio}
              error={erroresCampos.password}
              minLength={8}
              autoComplete="new-password"
              required
            />
          )}

          <div className="campo">
            <label htmlFor="rol">Rol</label>
            <select
              id="rol"
              name="rol"
              value={formulario.rol}
              onChange={manejarCambio}
              required
            >
              <option value="CLIENTE">Cliente</option>
              <option value="REPARTIDOR">Repartidor</option>
              <option value="ADMIN">Administrador</option>
            </select>

            {erroresCampos.rol && (
              <small className="error">{erroresCampos.rol}</small>
            )}
          </div>

          <div className="usuarios-formulario-acciones">
            <Button type="submit" cargando={guardando} disabled={guardando}>
              {usuarioEditando ? 'Guardar cambios' : 'Crear usuario'}
            </Button>

            {usuarioEditando && (
              <Button
                type="button"
                className="usuarios-btn-secundario"
                onClick={limpiarFormulario}
                disabled={guardando}
              >
                Cancelar
              </Button>
            )}
          </div>
        </form>
      </section>

      <section className="usuarios-panel">
        <div className="usuarios-listado-encabezado">
          <h2>Usuarios registrados</h2>

          <div className="usuarios-filtro">
            <label htmlFor="filtroRol">Filtrar por rol</label>
            <select
              id="filtroRol"
              value={filtroRol}
              onChange={(evento) => setFiltroRol(evento.target.value)}
            >
              <option value="">Todos los roles</option>
              <option value="CLIENTE">Cliente</option>
              <option value="REPARTIDOR">Repartidor</option>
              <option value="ADMIN">Administrador</option>
            </select>
          </div>
        </div>

        {cargando ? (
          <p className="muted">Cargando usuarios...</p>
        ) : usuarios.length === 0 ? (
          <p className="muted">No hay usuarios para mostrar.</p>
        ) : (
          <div className="usuarios-tabla-contenedor">
            <table className="usuarios-tabla">
              <thead>
                <tr>
                  <th>Nombre</th>
                  <th>Correo</th>
                  <th>Rol</th>
                  <th>Estado</th>
                  <th>Acciones</th>
                </tr>
              </thead>

              <tbody>
                {usuarios.map((usuario) => (
                  <tr key={usuario.id}>
                    <td>{usuario.nombre}</td>
                    <td>{usuario.correo}</td>
                    <td>
                      <span className="usuarios-rol">{usuario.rol}</span>
                    </td>
                    <td>
                      <span
                        className={
                          usuario.activo
                            ? 'usuarios-estado activo'
                            : 'usuarios-estado inactivo'
                        }
                      >
                        {usuario.activo ? 'Activo' : 'Inactivo'}
                      </span>
                    </td>
                    <td>
                      <div className="usuarios-acciones">
                        <Button
                          type="button"
                          onClick={() => prepararEdicion(usuario)}
                        >
                          Editar
                        </Button>

                        <Button
                          type="button"
                          className="usuarios-btn-secundario"
                          onClick={() => manejarEstado(usuario)}
                        >
                          {usuario.activo ? 'Desactivar' : 'Activar'}
                        </Button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>
    </main>
  );
}