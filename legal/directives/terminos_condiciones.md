# Términos y condiciones del portal

**Objetivo:** producir el borrador de los Términos y condiciones del portal como portal de contacto (Ley 1480 de 2011 art. 53 y Ley 2439 de 2024), con reglas de uso, contenido prohibido y responsabilidad, listo para revisión de un abogado.

**Entradas:**

| Nombre | Tipo | Origen | Obligatorio |
|---|---|---|---|
| Datos del responsable | JSON | `datos/empresa.json` | Sí |
| Datos del portal | JSON | `datos/portal.json` | Sí |
| Registro de normas | JSON | `conocimiento/normas.json` | Sí |

**Herramientas:** desde la carpeta `legal/`:
1. `node execution/verificar_normas.mjs --registro conocimiento/normas.json --salida .tmp/verificacion_normas.json --solo ley1480_art53,ley2439_2024,ley1480_art30,ley820_art16,ley1482_1752`
2. `node execution/generar_documento.mjs --plantilla plantillas/terminos_condiciones.md --datos datos/empresa.json,datos/portal.json --salida .tmp/terminos_condiciones.md`

**Salidas:** `.tmp/terminos_condiciones.md` (Markdown) y `.tmp/verificacion_normas.json`. En la respuesta: pendientes y brechas entre lo que dicen los términos y lo que hace el portal.

**Casos extremos:**

| Condición | Qué hacer |
|---|---|
| El portal no pide documento de identidad ni dirección a quien publica | Reportarlo como brecha frente al art. 53 de la Ley 1480 y proponer el cambio en el portal (requiere confirmación). |
| El portal empieza a cobrar (anuncios destacados) | Detenerse: hay que agregar cláusulas de pago, retracto y reversión (Ley 1480 y Ley 2439 de 2024). Pedir ampliar la directiva. |
| Pendientes en datos de la empresa | Entregar el borrador y pedir los datos. No inventarlos. |
| Una norma sale `frase_no_encontrada` | No citarla como vigente hasta revisarla. |

**Requiere confirmación:** sí. Antes de copiar los términos al portal y antes de publicarlos; la publicación requiere visto bueno de un abogado.

## Aprendizajes

<!-- append-only, ver reglas en CLAUDE.md -->
