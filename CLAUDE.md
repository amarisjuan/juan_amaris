# Contexto del repositorio

Responder siempre en español. El dueño del proyecto está en Colombia.

**Nada de emojis** en el sitio (ni en plantillas, textos o mapas). Usar los íconos SVG de la marca y
la paleta corporativa (Azul Puerto `#0B4A6F` + Ámbar Arena `#E9A23B`). Ver `juan_amaris/inmobiliaria/MARCA.md`.

## Proyectos
- `juan_amaris/inmobiliaria/` → **proyecto principal**: portal inmobiliario (Spring Boot 3.5, Java 21, Thymeleaf, JPA).
- `juan_amaris/inventario/` → proyecto anterior de inventario (no tocar salvo que se pida).
- `legal/` → espacio de trabajo de **Argenis**, el agente legal (`.claude/agents/argenis.md`).
  Sus reglas propias están en `legal/CLAUDE.md`. Todo lo legal se le delega a él.
- `juan/` → espacio de trabajo de **Juan**, desarrollador y fundador (`.claude/agents/juan.md`).
  Sus reglas propias están en `juan/CLAUDE.md`. Desarrollo del portal y decisiones de producto.
- `jdoor/` → espacio de trabajo de **JDoor**, ciberseguridad senior (`.claude/agents/jdoor.md`).
  Sus reglas propias están en `jdoor/CLAUDE.md`. Revisa la seguridad del portal y reporta hallazgos en
  `coordinacion/seguridad/`; Juan los corrige. Solo prueba en `localhost`.

## Idea de negocio (decidida)
Inmobiliaria propia en Colombia donde **las personas suben sus inmuebles y otras los buscan**.
Nicho fuerte: **vivienda para estudiantes cerca a universidades**.
Monetización prevista: anuncios destacados (sobre todo en temporada de matrículas: ene-feb y jul-ago),
sello de "verificado", comisión por arriendo cerrado, planes para inmobiliarias/constructoras,
alianzas con oficinas de bienestar universitario.

## Lo que ya está hecho (inmobiliaria)
- Registro/login (correo + celular colombiano), contraseñas BCrypt, CSRF.
- **Recuperar contraseña por correo** (`/recuperar`, `/restablecer`, `RecuperacionService`): enlace de un solo uso,
  30 min, máx. 3 por hora, solo se guarda el hash SHA-256, misma respuesta exista o no la cuenta, al cambiarla se cierran
  las demás sesiones (`SesionBloqueada`) y llega un aviso. Correos por **Brevo** (SMTP, `CorreoService`, plantillas en
  `templates/correo/`). Credenciales en `juan_amaris/inmobiliaria/.env` (ver `.env.example`, no se sube); sin ellas el
  enlace sale en la consola.
- **Seguridad del ingreso** (JDoor): máx. 5 intentos fallidos por cuenta y 30 por IP en 15 min (`IntentosIngreso`, en memoria),
  sesión de 30 min, cookie HttpOnly/SameSite=Lax (`APP_COOKIE_SEGURA=true` en producción), contraseñas 8-64 sin las
  comunes de `datos/contrasenas_comunes.txt` (`ValidadorContrasena`). Spring Boot 3.5.16 + Tomcat 10.1.61.
- Publicar, editar, pausar y eliminar inmuebles con hasta 10 fotos (se guardan en `uploads/`).
- Búsqueda pública con filtros (operación, ciudad, tipo, barrio, precio, habitaciones, estrato),
  orden y paginación.
- Detalle con contacto por WhatsApp (+57) y llamada.
- Estudiantes: catálogo de 29 universidades (`config/CatalogoUniversidades.java`, con coordenadas
  aproximadas), universidad cercana, minutos a pie, amoblado, servicios incluidos, página `/estudiantes`.
- Mapas con Leaflet (webjar) + OpenStreetMap: en `/estudiantes`, búsqueda, detalle y formulario
  (clic para ubicar; minutos a pie calculados solos).
- Reglas de la vivienda (convivencia, mascotas, visitas, fumar, horario) con filtros; solo en arriendos.
- Tablero "Busco compañero de cuarto" en `/companeros` (contacto solo con sesión iniciada, máx. 3 por usuario).
- **Privacidad de la ubicación** (decisión 2026-11-06, `coordinacion/consultas/001`): no se publica la dirección
  exacta; el mapa muestra una zona de 300 m guardada (mínimo recomendado por Argenis; no bajar) (`UbicacionPrivadaService`), minutos y distancias redondeados,
  direcciones bloqueadas en los textos (`DetectorDirecciones`) y fotos sin EXIF/GPS (`LimpiadorMetadatos`).
- **Registro privado de anunciantes** (Ley 1480 art. 53): documento y dirección de notificaciones, obligatorio antes
  de publicar (`/mi-registro`); solo visible en `/admin/anunciantes`.
- **Panel del administrador** (`/admin`, rol ADMIN): visitas, visitantes aproximados, contactos (los botones pasan por
  `/contacto/...`), inmuebles más y menos contactados, búsquedas sin resultados; se actualiza solo cada 15 s (`static/js/panel-vivo.js`). Conteos diarios sin IP
  (`ActividadService`). Consulta 002 a Argenis pendiente.
- **Nombre público** "Laura M." (`Usuario.getNombrePublico()`; nunca `nombre` en vistas públicas) y perfil público
  `/anunciantes/{id}`. **Reportes** confidenciales (`/reportar`, umbral 3 cuentas graves en 30 días oculta en revisión),
  **bloqueos** por correo y cédula (`/admin/reportes`, `/admin/bloqueos`), **aviso antiestafa** antes de WhatsApp o llamada
  y página `/seguridad` (consulta 003).
- **Datos oficiales** en `juan_amaris/inmobiliaria/src/main/resources/datos/`: `municipios.csv` (1.103 municipios,
  DIVIPOLA/MinSalud) e `instituciones.csv` (361 instituciones activas, SNIES/MEN, en 82 municipios). Se regeneran con
  `juan/execution/importar_datos_oficiales.mjs`. Al arrancar, `ImportadorUniversidades` agrega las que falten sin duplicar
  las ya catalogadas (`CatalogoUniversidades`). Las nuevas usan el centro del municipio como ubicación (aproximada).
- **Selectores encadenados** (`static/js/geo.js`): primero la ciudad; la universidad solo muestra las de esa ciudad;
  los barrios se sugieren de `barrios` (se aprenden de las publicaciones). La universidad de un inmueble debe estar
  en la misma ciudad (`PublicacionController.validarUbicacion`).
- **Pendiente:** barrios oficiales por ciudad (la fuente OpenStreetMap/Overpass no está permitida en el entorno actual).
- Base H2 en archivo por defecto (`./data`); perfil `mysql` opcional.
- Datos demo: usuario `admin@inmobiliaria.co` / `admin123` (cambiar antes de producción).

## Comandos
```bash
cd juan_amaris/inmobiliaria
./mvnw spring-boot:run   # http://localhost:8080
./mvnw test              # 49 pruebas con MockMvc
```

## Cuidado (errores ya vistos)
- En `<script th:inline="javascript">` no escribir `[[` en JavaScript normal: Thymeleaf lo toma
  como expresión. Separar con espacio: `[ [a, b], [c, d] ]`.
- Los `@EntityGraph` deben incluir todas las relaciones que use la vista (`open-in-view=false`).
- Columnas `boolean` nuevas en entidades existentes necesitan `@ColumnDefault`, si no, la base
  existente no se actualiza (`ddl-auto=update` falla con "NULL not allowed").
- `hibernate.validator.apply_to_ddl=false`: las validaciones no se vuelven NOT NULL en la base (si no, agregar
  columnas a tablas con datos falla). Las cuentas viejas se completan con migraciones (`config/Migracion*`).
- Al modificar un usuario cargado en el controlador, recargarlo dentro de la transacción del servicio (si no, no se guarda).
- **Nunca** enviar `inmueble.getLatitud()/getLongitud()` a una vista pública: usar `PuntoMapa.zona(...)`.
- El serializador de JavaScript de Thymeleaf escapa `/` como `\/` (ej. `\/inmuebles\/3`): tenerlo en cuenta en pruebas.
- Rutas públicas nuevas (estáticos, páginas) hay que agregarlas en `config/SeguridadConfig.java`.

## Próximos pasos sugeridos
1. Favoritos y alertas por correo/WhatsApp.
2. Anuncios destacados pagos (pasarela: Wompi, ePayco o Mercado Pago).
3. Verificación de propietarios.
4. Producción: fotos en la nube (Cloudinary/S3), proveedor de mapas (MapTiler/Stadia),
   política de tratamiento de datos (Ley 1581 de 2012), dominio .co y hosting.
