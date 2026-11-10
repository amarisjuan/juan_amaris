# Índice de directivas

Punto de entrada. Busca aquí la intención de la tarea y abre la directiva correspondiente.
Si ninguna fila corresponde, pregunta al usuario antes de improvisar.

| Intención | Directiva |
|---|---|
| _(todavía no hay directivas aprobadas)_ | |

## Propuestas (pendientes de aprobación del usuario; no existen todavía)

Crear una directiva requiere confirmación explícita del usuario. Estas son las que JDoor propone para empezar:

| Intención | Directiva propuesta | Para qué |
|---|---|---|
| Revisar las dependencias del portal en busca de vulnerabilidades conocidas | `revisar_dependencias.md` | Librerías de Maven y webjars con CVE publicados |
| Revisar la configuración de seguridad del portal | `revisar_configuracion.md` | Spring Security, CSRF, cabeceras, cookies, consola H2, contraseñas por defecto |
| Buscar secretos expuestos en el repositorio | `buscar_secretos.md` | Claves, tokens o contraseñas escritas en el código o en el historial de git |
| Revisar un cambio de código antes de publicarlo | `revisar_cambio.md` | Inyección, control de acceso, subida de archivos, datos personales en vistas públicas |
| Prueba de seguridad del portal en local | `prueba_local.md` | Pruebas automatizadas contra `localhost`, nunca contra servidores de terceros |
| Lista de verificación antes de salir a producción | `checklist_produccion.md` | HTTPS, respaldos, contraseñas, registros, límites de intentos |
