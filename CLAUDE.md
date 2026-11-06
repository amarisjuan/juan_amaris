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

## Idea de negocio (decidida)
Inmobiliaria propia en Colombia donde **las personas suben sus inmuebles y otras los buscan**.
Nicho fuerte: **vivienda para estudiantes cerca a universidades**.
Monetización prevista: anuncios destacados (sobre todo en temporada de matrículas: ene-feb y jul-ago),
sello de "verificado", comisión por arriendo cerrado, planes para inmobiliarias/constructoras,
alianzas con oficinas de bienestar universitario.

## Lo que ya está hecho (inmobiliaria)
- Registro/login (correo + celular colombiano), contraseñas BCrypt, CSRF.
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
  exacta; el mapa muestra una zona de 400 m guardada (`UbicacionPrivadaService`), minutos y distancias redondeados,
  direcciones bloqueadas en los textos (`DetectorDirecciones`) y fotos sin EXIF/GPS (`LimpiadorMetadatos`).
- Base H2 en archivo por defecto (`./data`); perfil `mysql` opcional.
- Datos demo: usuario `admin@inmobiliaria.co` / `admin123` (cambiar antes de producción).

## Comandos
```bash
cd juan_amaris/inmobiliaria
./mvnw spring-boot:run   # http://localhost:8080
./mvnw test              # 22 pruebas con MockMvc
```

## Cuidado (errores ya vistos)
- En `<script th:inline="javascript">` no escribir `[[` en JavaScript normal: Thymeleaf lo toma
  como expresión. Separar con espacio: `[ [a, b], [c, d] ]`.
- Los `@EntityGraph` deben incluir todas las relaciones que use la vista (`open-in-view=false`).
- Columnas `boolean` nuevas en entidades existentes necesitan `@ColumnDefault`, si no, la base
  existente no se actualiza (`ddl-auto=update` falla con "NULL not allowed").
- **Nunca** enviar `inmueble.getLatitud()/getLongitud()` a una vista pública: usar `PuntoMapa.zona(...)`.
- El serializador de JavaScript de Thymeleaf escapa `/` como `\/` (ej. `\/inmuebles\/3`): tenerlo en cuenta en pruebas.
- Rutas públicas nuevas (estáticos, páginas) hay que agregarlas en `config/SeguridadConfig.java`.

## Próximos pasos sugeridos
1. Favoritos y alertas por correo/WhatsApp.
2. Anuncios destacados pagos (pasarela: Wompi, ePayco o Mercado Pago).
3. Verificación de propietarios.
4. Producción: fotos en la nube (Cloudinary/S3), proveedor de mapas (MapTiler/Stadia),
   política de tratamiento de datos (Ley 1581 de 2012), dominio .co y hosting.
