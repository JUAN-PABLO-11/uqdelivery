# UQDelivery

Sistema de gestión de logística y entregas · Ingeniería de Software · Universidad del Quindío

| Capa | Tecnología |
|---|---|
| Backend | Java 17 · Spring Boot 3.3 · Spring Security + JWT · JPA · Maven |
| Base de datos | MySQL 8 (Docker) · H2 en pruebas |
| Frontend | React 18 · Vite · React Router · Axios |
| Pruebas | JUnit 5 · Mockito · JaCoCo |

---

## 1. Requisitos

- Java 17 o superior
- Maven 3.9 (o el que trae IntelliJ)
- Node.js 20 o superior
- Docker Desktop
- Git

## 2. Levantar el proyecto

```bash
git clone <url-del-repo>
cd uqdelivery

# 1) Base de datos
docker compose up -d

# 2) Backend  ->  http://localhost:8080
cd backend
mvn spring-boot:run

# 3) Frontend ->  http://localhost:5173   (en otra terminal)
cd frontend
cp .env.example .env
npm install
npm run dev
```

La primera vez, JPA crea las tablas y el `DataSeeder` carga datos de prueba.

## 3. Usuarios de prueba

Contraseña de todos: **`Uqdelivery123*`**

| Rol | Correo |
|---|---|
| ADMIN | admin@uqdelivery.com |
| CLIENTE | cliente1@uqdelivery.com · cliente2@uqdelivery.com |
| REPARTIDOR | repartidor1@uqdelivery.com · repartidor2@uqdelivery.com |

También se cargan 6 zonas de Armenia (Circasia está **inactiva**, para probar "fuera de cobertura"), 1 tarifa vigente y 8 envíos en distintos estados.

> Para reiniciar los datos: `docker compose down -v && docker compose up -d`

## 4. Probar la API rápido

```bash
# Login -> copia el "token" de la respuesta
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"correo":"repartidor1@uqdelivery.com","password":"Uqdelivery123*"}'

# Llamar un endpoint protegido
curl http://localhost:8080/api/v1/zonas -H "Authorization: Bearer <token>"
```

## 5. Forma de trabajo con Git (trunk-based / GitHub Flow)

- `main` siempre estable. **Nadie hace push directo a `main`.**
- Una rama por HU, que sale de `main`:

```bash
git checkout main && git pull
git checkout -b feature/HU-04-crear-envio
# ... trabajar ...
git add . && git commit -m "feat(HU-04): crear envío con código de seguimiento"
git pull --rebase origin main      # antes de subir, para evitar conflictos
git push -u origin feature/HU-04-crear-envio
# abrir Pull Request en GitHub -> 1 revisión + CI en verde -> merge
```

- Prefijos de commit: `feat`, `fix`, `test`, `docs`, `refactor`.
- Pull Requests pequeños y frecuentes (idealmente 1 por día).

## 6. Dónde trabaja cada HU (para no pisarnos)

| HU | Backend (paquetes `controller/ service/ dto/`) | Frontend |
|---|---|---|
| HU-01 | `AuthController`, `AuthService` (completar registro y bloqueo) | `pages/auth/`, `api/auth.js` |
| HU-02 | `UsuarioController`, `UsuarioService` → `/api/v1/admin/usuarios` | `pages/admin/Usuarios.jsx`, `api/usuarios.js` |
| HU-04 | `EnvioController`, `EnvioService` → `/api/v1/envios` | `pages/cliente/`, `api/envios.js` |
| HU-05 | `CotizacionService` (+ `haversine`) → `/api/v1/envios/cotizar` | `pages/cliente/NuevoEnvio.jsx`, `api/cotizacion.js` |
| HU-06 | `AsignacionController`, `AsignacionService` → `/api/v1/admin/envios/...` | `pages/admin/AsignarEnvios.jsx`, `pages/repartidor/`, `api/asignaciones.js` |
| HU-10 | `NovedadController`, `NovedadService` → `/api/v1/repartidor/envios/{id}/novedades` | `pages/repartidor/`, `api/novedades.js` |

Todas las **rutas del frontend** y todas las **entidades** ya existen. Si necesitas cambiar una entidad compartida, avísale al equipo antes y hazlo en un PR aparte.

## 7. Reglas del equipo

1. **Estados:** nadie hace `envio.setEstado(...)`. Siempre `estadoService.cambiarEstado(...)`. Reasignar: `estadoService.registrarReasignacion(...)`. Crear: `estadoService.registrarCreacion(...)`.
2. **Errores:** lanza `AppException` desde el service (`new AppException("CODIGO", "mensaje", HttpStatus.XXX)`). El `GlobalExceptionHandler` arma el JSON `{ codigo, mensaje, campos }`.
3. **Usuario que hace la petición:** en el controller, `@AuthenticationPrincipal Usuario actor`.
4. **Nunca devolver entidades** por la API: usar DTOs en `dto/request` y `dto/response` (records).
5. **Validación:** `@Valid` en el controller, Bean Validation en el DTO y `@SinHtml` en todo texto libre. En React, validar igual y mostrar `error.campos` (ver `leerError` en `api/client.js`).
6. **Consultas:** solo Spring Data JPA o `@Query` con parámetros. Prohibido concatenar strings en SQL.
7. **Pruebas:** mínimo 1 prueba del camino feliz y 1 por cada criterio de error de tu HU. Ejemplo en `EstadoServiceTest`.

## 8. Pruebas y cobertura

```bash
cd backend
mvn clean test
# reporte: backend/target/site/jacoco/index.html   (meta: 60-70 %)
```

El CI (GitHub Actions) corre las pruebas del backend y el build del frontend en cada Pull Request.

## 9. Variables de entorno (nube)

| Variable | Ejemplo |
|---|---|
| `SPRING_DATASOURCE_URL` | `jdbc:mysql://host:3306/uqdelivery` |
| `SPRING_DATASOURCE_USERNAME` / `PASSWORD` | credenciales de la BD en la nube |
| `JWT_SECRET` | texto aleatorio de 32+ caracteres |
| `CORS_ORIGINS` | `https://uqdelivery.vercel.app` |
| `VITE_API_URL` (frontend) | `https://uqdelivery-api.onrender.com/api/v1` |

## 10. Documentación

- `docs/GUIA_CASO_00.md`: guía técnica del Caso 00 (DER, máquina de estados, contrato de la API).
