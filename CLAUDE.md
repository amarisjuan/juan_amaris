# Contexto del repositorio

Responder siempre en español. El dueño del proyecto está en Colombia.

## Proyectos
- `juan_amaris/inmobiliaria/` → **proyecto principal**: portal inmobiliario (Spring Boot 3.5, Java 21, Thymeleaf, JPA).
- `juan_amaris/inventario/` → proyecto anterior de inventario (no tocar salvo que se pida).

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
- Base H2 en archivo por defecto (`./data`); perfil `mysql` opcional.
- Datos demo: usuario `admin@inmobiliaria.co` / `admin123` (cambiar antes de producción).

## Comandos
```bash
cd juan_amaris/inmobiliaria
./mvnw spring-boot:run   # http://localhost:8080
./mvnw test              # 9 pruebas con MockMvc
```

## Cuidado (errores ya vistos)
- En `<script th:inline="javascript">` no escribir `[[` en JavaScript normal: Thymeleaf lo toma
  como expresión. Separar con espacio: `[ [a, b], [c, d] ]`.
- Los `@EntityGraph` deben incluir todas las relaciones que use la vista (`open-in-view=false`).
- Rutas públicas nuevas (estáticos, páginas) hay que agregarlas en `config/SeguridadConfig.java`.

## Próximos pasos sugeridos
1. Perfil de estudiante y "busco compañero de cuarto".
2. Reglas de la vivienda (solo mujeres/hombres, mascotas, horarios, visitas).
3. Favoritos y alertas por correo/WhatsApp.
4. Anuncios destacados pagos (pasarela: Wompi, ePayco o Mercado Pago).
5. Verificación de propietarios.
6. Producción: fotos en la nube (Cloudinary/S3), proveedor de mapas (MapTiler/Stadia),
   política de tratamiento de datos (Ley 1581 de 2012), dominio .co y hosting.
