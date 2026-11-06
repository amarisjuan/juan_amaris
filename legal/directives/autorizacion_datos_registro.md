# Autorización de datos en el registro

**Objetivo:** comprobar que el registro del portal obtiene una autorización previa, expresa e informada para el tratamiento de datos y que queda prueba de ella (Ley 1581 de 2012 art. 9; Decreto 1377 de 2013 arts. 7 y 8).

**Entradas:**

| Nombre | Tipo | Origen | Obligatorio |
|---|---|---|---|
| Raíz del portal | ruta | argumento `--raiz` (normalmente `../juan_amaris/inmobiliaria`) | Sí |
| Controles a verificar | JSON | `conocimiento/checks_autorizacion.json` | Sí |

**Herramientas:** desde la carpeta `legal/`:
`node execution/verificar_autorizacion_portal.mjs --raiz ../juan_amaris/inmobiliaria --checks conocimiento/checks_autorizacion.json --salida .tmp/verificacion_autorizacion.json`

**Salidas:** `.tmp/verificacion_autorizacion.json` con cada control (`cumple` sí/no, norma). En la respuesta: tabla de controles que no se cumplen y la propuesta de cambio en el portal para cada uno.

**Casos extremos:**

| Condición | Qué hacer |
|---|---|
| Algún control no se cumple | Proponer el cambio concreto en el portal (casilla, campos `fechaAutorizacion` y `versionPolitica`, página de la política). No editar el portal sin confirmación. |
| Ya hay usuarios registrados sin autorización guardada | Avisar: hay que pedirles la autorización al próximo inicio de sesión (Decreto 1377 de 2013, art. 10, datos recolectados antes). |
| Cambió la ruta de un archivo del portal | Corregir la ruta en `checks_autorizacion.json` (dato objetivamente equivocado) y decirlo en la respuesta. |
| La política no está publicada todavía | Ejecutar primero la directiva `politica_tratamiento_datos.md`. |

**Requiere confirmación:** sí. Cualquier cambio al código o a la base de datos del portal para implementar la autorización requiere confirmación explícita del usuario, cambio por cambio.

## Aprendizajes

<!-- append-only, ver reglas en CLAUDE.md -->
