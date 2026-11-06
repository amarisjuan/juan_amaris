# Política de tratamiento de datos personales

**Objetivo:** producir el borrador de la Política de tratamiento y del Aviso de privacidad del portal, con el contenido mínimo que exige el Decreto 1377 de 2013 (art. 13 a 15), listo para revisión de un abogado.

**Entradas:**

| Nombre | Tipo | Origen | Obligatorio |
|---|---|---|---|
| Datos del responsable | JSON | `datos/empresa.json` (lo llena el usuario) | Sí |
| Datos y finalidades del portal | JSON | `datos/portal.json` | Sí |
| Registro de normas | JSON | `conocimiento/normas.json` | Sí |
| Fecha del documento | AAAA-MM-DD | argumento `--fecha` | No (hoy por defecto) |

**Herramientas:** desde la carpeta `legal/`, en este orden:
1. `node execution/verificar_normas.mjs --registro conocimiento/normas.json --salida .tmp/verificacion_normas.json --solo ley1581_art14,ley1581_art15,dec1377_art13,dec1377_art14_15`
2. `node execution/generar_documento.mjs --plantilla plantillas/politica_tratamiento_datos.md --datos datos/empresa.json,datos/portal.json --salida .tmp/politica_tratamiento_datos.md`
3. `node execution/generar_documento.mjs --plantilla plantillas/aviso_privacidad.md --datos datos/empresa.json,datos/portal.json --salida .tmp/aviso_privacidad.md`

**Salidas:** `.tmp/politica_tratamiento_datos.md`, `.tmp/aviso_privacidad.md` (Markdown) y `.tmp/verificacion_normas.json`. En la respuesta: lista de campos `pendientes` y estado de verificación de cada norma.

**Casos extremos:**

| Condición | Qué hacer |
|---|---|
| `generar_documento` devuelve `pendientes` | Entregar el borrador igual, listar los pendientes y pedir al usuario los datos de la empresa. No inventarlos. |
| `generar_documento` falla por `faltantes` | Es un error de plantilla o de datos: corregir el JSON o la plantilla, no la salida. |
| Una norma sale `frase_no_encontrada` | No citarla como vigente; avisar al usuario y revisar si la norma cambió antes de entregar. |
| Normas en `error_red` | Entregar el borrador indicando que la verificación oficial quedó pendiente. |
| El portal empieza a pedir un dato nuevo (p. ej. documento de identidad) | Actualizar `datos/portal.json` (es entrada, no salida) y regenerar. |
| La empresa supera 100.000 UVT en activos | Avisar que debe inscribir la base en el Registro Nacional de Bases de Datos de la SIC (Decreto 090 de 2018). |

**Requiere confirmación:** sí. Antes de copiar la política o el aviso al portal (escribe archivos fuera de `.tmp/`) y antes de publicarlos. La publicación además requiere visto bueno de un abogado.

## Aprendizajes

<!-- append-only, ver reglas en CLAUDE.md -->
