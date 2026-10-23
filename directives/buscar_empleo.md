# Buscar empleo para una persona

**Objetivo:** entregar a una persona una lista ordenada de ofertas de empleo reales que encajan con su perfil.

**Entradas:**

| Nombre | Tipo | Origen | Obligatorio |
|---|---|---|---|
| Perfil | JSON en `perfiles/<nombre>.json` (formato de `perfiles/ejemplo.json`) | Usuario; si no existe, Eva lo arma preguntando | Sí |
| Término de búsqueda | Texto corto en inglés (ej. `java`, `customer support`) | Campo `busqueda` del perfil o el usuario | Sí |
| Fuentes | Lista separada por comas: `remotive,arbeitnow,jobicy` | Usuario; por defecto todas | No |
| Top | Número de ofertas a mostrar | Usuario; por defecto 20 | No |

Campos del perfil: `nombre`, `ciudad`, `busqueda` (términos), `habilidades`, `excluir` (palabras que descartan una oferta si están en el título), `solo_remoto` (true/false), `idiomas`.

**Herramientas:**

1. Descargar ofertas (una vez por término de búsqueda):
   `node execution/buscar_empleos.mjs --busqueda "java" --salida .tmp/empleos_<nombre>.json`
2. Puntuar contra el perfil:
   `node execution/puntuar_empleos.mjs --perfil perfiles/<nombre>.json --empleos .tmp/empleos_<nombre>.json --top 20`

Para probar sin red: `--archivo <json con respuestas crudas por fuente>` en el paso 1.

**Salidas:**

- `.tmp/empleos_<nombre>.json`: todas las ofertas normalizadas (`id, titulo, empresa, ubicacion, remoto, url, fecha, salario, etiquetas, descripcion, fuente`).
- `.tmp/ranking_<nombre>.json`: las mejores ofertas con `puntaje` y `coincidencias`.
- Al usuario: tabla con título, empresa, ubicación, puntaje y enlace de las 5-10 mejores, más una línea de por qué encaja cada una.

**Casos extremos:**

| Condición | Qué hacer |
|---|---|
| No existe el perfil | Preguntar habilidades, tipo de trabajo, remoto sí/no y exclusiones; crear `perfiles/<nombre>.json`. |
| Ya existe un perfil con ese nombre | No sobrescribir sin confirmación. |
| Una fuente falla y otras no | Seguir con las que respondieron y decir cuál falló (campo `errores`). |
| Todas las fuentes fallan (`success: false`) | Revisar la conexión a internet. En sesiones cloud estas webs están bloqueadas: decirlo, no reintentar en bucle. |
| 0 ofertas seleccionadas | Proponer términos más amplios (ej. `developer` en vez de `spring boot`) o bajar `--minimo`. |
| La persona busca trabajo presencial local (Colombia) | Estas fuentes son remotas e internacionales. Decirlo y sugerir buscar en Computrabajo, elempleo.com, LinkedIn y el Servicio Público de Empleo. |
| Ofertas en inglés | Avisar si el perfil no incluye inglés. |

**Requiere confirmación:** sí.
- Antes de crear o sobrescribir un archivo en `perfiles/` (datos personales).
- Antes de postular, enviar correos o mensajes a empresas en nombre de cualquier persona: Eva nunca postula sola.

## Aprendizajes

<!-- append-only, ver reglas en CLAUDE.md -->
