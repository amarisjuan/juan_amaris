# Consulta legal a Argenis

**Objetivo:** antes de construir algo con posible riesgo legal, dejar por escrito la pregunta a Argenis para que responda con normas y requisitos, y desarrollar solo con esa respuesta y la confirmación del usuario.

**Entradas:**

| Nombre | Tipo | Origen | Obligatorio |
|---|---|---|---|
| Tema | texto corto | Juan | Sí |
| Pregunta | texto | Juan | Sí |
| Propuesta técnica | texto | Juan | No (recomendado) |
| Contexto (archivos, pantallas afectadas) | texto | Juan | No |

**Herramientas:** desde la carpeta `juan/`:
`node execution/crear_consulta.mjs --carpeta ../coordinacion/consultas --tema "<tema>" --pregunta "<pregunta>" --propuesta "<propuesta>" --contexto "<contexto>"`

**Salidas:** archivo `coordinacion/consultas/NNN-tema.md` (persistente, no va a `.tmp/` porque es el registro de la consulta). La respuesta la escribe Argenis en ese archivo.

**Casos extremos:**

| Condición | Qué hacer |
|---|---|
| No está claro si el tema tiene riesgo legal | Consultar igual. Ver la lista de "Cuándo consulta Juan" en `coordinacion/README.md`. |
| Argenis responde "riesgo alto" o "revisar con abogado" | No implementar. Informar al usuario y esperar su decisión. |
| La respuesta de Argenis contradice una decisión anterior de `coordinacion/DECISIONES.md` | Señalarlo al usuario; no elegir por cuenta propia. |
| Ya existe una consulta sobre el mismo tema | Leerla primero; crear una nueva solo si la pregunta es distinta. |

**Requiere confirmación:** sí. Después de la respuesta de Argenis, el usuario debe confirmar la decisión antes de implementarla en el portal.

## Aprendizajes

<!-- append-only, ver reglas en CLAUDE.md -->
