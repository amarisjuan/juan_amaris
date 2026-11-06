# Revisión legal de un anuncio

**Objetivo:** detectar en un anuncio del portal contenido discriminatorio, exigencias ilegales (depósitos), publicidad engañosa o datos personales expuestos, y decidir si se aprueba, se pide corrección o se bloquea.

**Entradas:**

| Nombre | Tipo | Origen | Obligatorio |
|---|---|---|---|
| Anuncio | JSON (`titulo`, `descripcion`, `otrasReglas`, `operacion`, `convivencia`) o texto | archivo `--anuncio` o argumento `--texto` | Sí |
| Reglas | JSON | `conocimiento/reglas_anuncios.json` | Sí |

**Herramientas:** desde la carpeta `legal/`:
`node execution/revisar_anuncio.mjs --reglas conocimiento/reglas_anuncios.json --anuncio <archivo.json> --salida .tmp/revision_anuncio.json`
(o `--texto "<texto del anuncio>"`)

**Salidas:** `.tmp/revision_anuncio.json` con `decision` (`aprobado`, `revision_humana`, `pedir_correccion`, `bloquear`) y cada hallazgo con su norma y evidencia. En la respuesta: decisión y texto sugerido para corregir.

**Casos extremos:**

| Condición | Qué hacer |
|---|---|
| `decision` = `bloquear` | Recomendar retirar el anuncio y explicar la norma. No ocultarlo en el portal sin confirmación. |
| `decision` = `revision_humana` (p. ej. solo mujeres, sin niños) | No decidir solo: presentar el caso al usuario con el contexto (vivienda compartida o independiente). |
| Falso positivo evidente (p. ej. "no se permiten mascotas" marcado por error) | Ajustar el patrón en `reglas_anuncios.json`, volver a correr y registrar el aprendizaje si se repite. |
| Anuncio en otro idioma | Avisar que las reglas solo cubren español. |
| Muchos anuncios a la vez | Correr el script por cada uno; si es frecuente, proponer un modo por lotes (rediseño, preguntar antes). |

**Requiere confirmación:** sí. Antes de ocultar, editar o eliminar un anuncio en el portal, o de escribirle al anunciante.

## Aprendizajes

<!-- append-only, ver reglas en CLAUDE.md -->
