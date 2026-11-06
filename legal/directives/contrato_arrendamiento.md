# Contrato de arrendamiento modelo

**Objetivo:** validar los datos de un arriendo contra los topes de la Ley 820 de 2003 y generar el borrador del contrato (vivienda, vivienda compartida o pensión) listo para revisión de un abogado.

**Entradas:**

| Nombre | Tipo | Origen | Obligatorio |
|---|---|---|---|
| Datos del contrato | JSON | `datos/<contrato>.json` (ver `datos/contrato_ejemplo.json`) | Sí |
| IPC del año anterior (%) | número | DANE, lo confirma el usuario; argumento `--ipc` | Sí, si se pacta reajuste |
| Registro de normas | JSON | `conocimiento/normas.json` | Sí |

**Herramientas:** desde la carpeta `legal/`, en este orden:
1. `node execution/verificar_normas.mjs --registro conocimiento/normas.json --salida .tmp/verificacion_normas.json --solo ley820_art2,ley820_art5,ley820_art16,ley820_art18,ley820_art20,ley820_art14_17_22_24`
2. `node execution/validar_contrato.mjs --datos datos/<contrato>.json --ipc <IPC> --salida .tmp/validacion_contrato.json`
3. Solo si `valido` es `true`: `node execution/generar_documento.mjs --plantilla plantillas/contrato_arrendamiento.md --datos datos/<contrato>.json --salida .tmp/contrato_<nombre>.md`

**Salidas:** `.tmp/validacion_contrato.json` (hallazgos por nivel) y `.tmp/contrato_<nombre>.md`. En la respuesta: errores, advertencias y pendientes.

**Casos extremos:**

| Condición | Qué hacer |
|---|---|
| `validar_contrato` reporta errores (depósito, canon sobre el tope, reajuste sobre el IPC) | No generar el contrato. Explicar cada error con su artículo y pedir corregir los datos. |
| No se conoce el IPC | Pedirlo al usuario (dato del DANE). No usar uno de memoria. Referencia investigada: 5,10% para reajustes en 2026 (IPC 2025, fuente de prensa). |
| Faltan valor comercial o avalúo catastral | Advertir que el tope del art. 18 no se pudo verificar. |
| Arrendador con más de 5 contratos | Advertir sobre la matrícula de arrendador (art. 28, Decreto 51 de 2004). |
| Arriendo no es de vivienda (local, oficina, bodega) | Detenerse: la Ley 820 no aplica (es arrendamiento comercial). Pedir una directiva aparte. |
| Arrendatario menor de edad | Detenerse y pedir revisión de abogado (capacidad legal). |

**Requiere confirmación:** no para generar el borrador en `.tmp/`. Sí antes de enviarlo a cualquier persona o de firmarlo, y debe revisarlo un abogado.

## Aprendizajes

<!-- append-only, ver reglas en CLAUDE.md -->
