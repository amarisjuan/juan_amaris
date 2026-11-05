# Inmobiliaria — portal para publicar y buscar inmuebles en Colombia

Las personas crean una cuenta, **suben sus inmuebles con fotos** (arriendo o venta) y cualquiera puede
**buscar con filtros** y contactar al dueño por **WhatsApp** o llamada.

## Funciones
- Búsqueda pública con filtros: arriendo/venta, ciudad, tipo, barrio o palabra clave, precio mínimo y máximo,
  habitaciones, estrato. También ordena por fecha o precio y muestra los resultados por páginas.
- **Vivienda para estudiantes** (`/estudiantes`): 29 universidades principales de Colombia agrupadas por ciudad.
  Cada inmueble puede indicar la universidad cercana, los minutos a pie y si es amoblado o tiene servicios incluidos.
  Se puede filtrar por esos datos y ordenar por "más cerca a la universidad". Para agregar más universidades,
  edita `CatalogoUniversidades.java`; las nuevas se cargan solas al reiniciar.
- **Mapas** (Leaflet + OpenStreetMap, gratis y sin clave de API; Leaflet viene incluido en la app):
  - `/estudiantes`: mapa de las 29 universidades. Al tocar una, se ven los arriendos cerca.
  - Búsqueda: mapa con los resultados y la universidad elegida.
  - Detalle: el inmueble, la universidad y la distancia en línea recta.
  - Publicar: el dueño hace clic en el mapa (o usa "mi ubicación") y los minutos caminando
    se calculan solos (~80 m/min, ajustado por calles).
  - Las coordenadas de las universidades son aproximadas (campus principal); ajústalas en `CatalogoUniversidades.java`.
- Página de detalle con galería de fotos, características y botones de WhatsApp (+57) y llamada.
- Registro con nombre, correo y celular colombiano, e inicio de sesión.
- Publicar, editar, pausar/activar y eliminar inmuebles propios (hasta 10 fotos JPG/PNG/WEBP de 5 MB c/u).
- Seguridad: solo el dueño (o el ADMIN) puede editar sus inmuebles. Las contraseñas se guardan con BCrypt,
  los formularios llevan protección CSRF y los campos internos (dueño, fotos) no se pueden manipular.

## Cómo ejecutarlo
Requisitos: Java 21.

```bash
./mvnw spring-boot:run
```
Abre http://localhost:8080

- Por defecto usa una base **H2 en archivo** (`./data`), así que no tienes que instalar nada más.
  Las fotos se guardan en `./uploads`.
- Usuario de prueba: `admin@inmobiliaria.co` / `admin123`. Hay 6 inmuebles de ejemplo
  (desactívalos con `app.datos-demo=false`). **Cambia esa contraseña antes de publicar el sitio.**
- Con MySQL: `./mvnw spring-boot:run -Dspring-boot.run.profiles=mysql`
  (ver `application-mysql.properties`).

## Pruebas
```bash
./mvnw test
```

## Próximos pasos sugeridos
1. Mapa con la ubicación (Leaflet + OpenStreetMap).
2. Favoritos y alertas por correo o WhatsApp cuando aparezca un inmueble que coincida con la búsqueda.
3. Planes pagos: anuncios destacados, publicaciones para inmobiliarias y constructoras.
4. Verificación de propietarios y anuncios para generar confianza.
5. Fotos en almacenamiento en la nube (S3/Cloudinary) para producción.
