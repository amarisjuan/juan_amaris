# Responder consulta de desarrollo

**Objetivo:** responder por escrito una consulta de Juan con un concepto legal orientativo, las normas que aplican, el nivel de riesgo y los requisitos concretos para el desarrollo.

**Entradas:**

| Nombre | Tipo | Origen | Obligatorio |
|---|---|---|---|
| Consulta | Markdown | `../coordinacion/consultas/NNN-tema.md` | Sí |
| Registro de normas | JSON | `conocimiento/normas.json` | Sí |
| Decisiones anteriores | Markdown | `../coordinacion/DECISIONES.md` | Sí |

**Herramientas:** si la respuesta se apoya en normas del registro, verificarlas primero:
`node execution/verificar_normas.mjs --registro conocimiento/normas.json --salida .tmp/verificacion_normas.json --solo <ids>`
La respuesta se escribe en la sección "Respuesta de Argenis" del archivo de la consulta.

**Salidas:** la sección "Respuesta de Argenis" completa, con: concepto (2 a 5 frases), normas con artículo y estado de verificación, riesgo (bajo / medio / alto), requisitos para desarrollo en lista verificable, y qué revisar con un abogado. Cambiar **Estado** a `respondida`.

**Casos extremos:**

| Condición | Qué hacer |
|---|---|
| La norma necesaria no está en `normas.json` | Investigarla en fuentes oficiales y proponer agregarla al registro; marcarla como no verificada en la respuesta. |
| El tema excede la orientación general (litigio, sanción en curso, contrato complejo) | Riesgo alto: recomendar abogado y no dar requisitos definitivos. |
| La consulta no tiene información suficiente | Pedir los datos que faltan en la misma sección, sin concluir. |
| Contradice una decisión anterior | Señalarlo explícitamente. |

**Requiere confirmación:** no para responder. La decisión final la confirma el usuario, no Argenis.

## Aprendizajes

<!-- append-only, ver reglas en CLAUDE.md -->
