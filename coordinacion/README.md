# Coordinación entre agentes

Carpeta compartida entre **Juan** (desarrollo y fundación, `juan/`), **Argenis** (legal, `legal/`) y
**JDoor** (ciberseguridad, `jdoor/`).

## Cuándo consulta Juan a Argenis
Antes de construir o cambiar algo que toque:
- datos personales (qué se pide, qué se muestra, a quién, cuánto se guarda);
- lo que se publica de un inmueble o de una persona (dirección, ubicación, fotos, contacto);
- cobros, pagos, anuncios destacados o promociones;
- textos legales del portal, contratos o reglas de los anuncios;
- cualquier duda de "¿esto nos puede traer una demanda o una multa?".

## Cómo funciona
1. Juan crea la consulta con `node juan/execution/crear_consulta.mjs` (directiva `juan/directives/consulta_legal.md`).
   Queda un archivo en `coordinacion/consultas/NNN-tema.md`.
2. Argenis responde en ese mismo archivo, sección "Respuesta de Argenis"
   (directiva `legal/directives/responder_consulta.md`): concepto, normas, riesgo y requisitos para desarrollo.
3. El usuario confirma. La decisión final se anota en `coordinacion/DECISIONES.md`.
4. Juan implementa en el portal cumpliendo los requisitos, y Argenis verifica cuando haya un script para hacerlo.

Argenis no es abogado: sus conceptos son orientación y lo importante debe revisarlo un abogado.

## Seguridad (JDoor)
JDoor revisa el portal y deja cada revisión en `coordinacion/seguridad/NNN-tema.md` con los hallazgos por severidad.
Juan corrige; lo que toque datos personales o normas se consulta con Argenis. El usuario confirma la prioridad.
