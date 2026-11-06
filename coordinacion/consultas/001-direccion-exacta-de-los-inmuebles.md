# Consulta 001: Dirección exacta de los inmuebles

**De:** Juan (desarrollo)  **Para:** Argenis (legal)  **Fecha:** 2026-11-06  **Estado:** decidida

## Pregunta
¿Podemos o debemos mostrar públicamente la dirección exacta y la ubicación exacta en el mapa de los inmuebles publicados? ¿Qué riesgos legales hay si lo hacemos y qué debemos cumplir si no lo hacemos?

## Propuesta de Juan
El fundador decidió no mostrar la dirección exacta (ej. 'Calle 45 # 23-10'). Se mostraría: ciudad, barrio y una zona aproximada en el mapa (círculo de unos 400 m, con el punto desplazado de forma no reversible). La ubicación exacta la sigue viendo solo el dueño al editar. El portal bloquearía que se escriban direcciones en título, descripción y reglas. La dirección se entrega en privado cuando el interesado contacta al dueño. Las distancias a la universidad se calcularían con la ubicación exacta, redondeadas.

## Contexto
Hoy el portal no tiene campo de dirección, pero guarda latitud/longitud exactas y las muestra como pin exacto en los mapas de búsqueda y detalle, a cualquier visitante sin sesión. Los anunciantes son en su mayoría personas naturales (dueños), y muchos inmuebles son habitaciones donde viven estudiantes. Archivos: model/Inmueble.java, model/PuntoMapa.java, templates/inmuebles/detalle.html y buscar.html, static/js/mapas.js.

## Respuesta de Argenis
*Orientación, no asesoría legal. Argenis no es abogado; este concepto es un borrador que debe revisar un abogado antes de tomarlo como definitivo. La decisión la confirma el usuario.*

### Concepto
No estamos obligados a publicar la dirección exacta, y no conviene hacerlo. Cuando el anunciante es una persona natural y en muchos casos vive en el inmueble (habitaciones para estudiantes), la dirección o el pin exacto, junto con su nombre, su celular y sus fotos, permite saber dónde vive una persona identificable. A mi juicio eso es dato personal según la definición del art. 3 lit. c de la Ley 1581 de 2012. La ley no menciona la dirección de forma literal; es una interpretación que debe confirmar el abogado. Si es dato personal, publicarlo abierto en internet choca con el principio de circulación restringida (art. 4 lit. f) y con el de seguridad (art. 4 lit. g). Por eso la situación actual, con el pin exacto visible para cualquiera y sin sesión, se debe corregir. La propuesta de Juan va en la dirección correcta y, con los ajustes de abajo, la considero suficiente.

### Normas
| Norma | Qué dice (resumen) | Verificación |
|---|---|---|
| Ley 1581 de 2012, art. 3 lit. c | Dato personal es cualquier información vinculada o que pueda asociarse a personas naturales determinadas o determinables. | Confirmada en texto oficial (Senado), 2026-11-06. Que la dirección del inmueble encaje aquí es interpretación mía, no texto literal. |
| Ley 1581 de 2012, art. 4 lit. f (acceso y circulación restringida) | Los datos personales, salvo la información pública, no pueden estar disponibles en internet o medios masivos, salvo que el acceso sea técnicamente controlable. | Confirmada en texto oficial, 2026-11-06. |
| Ley 1581 de 2012, art. 4 lit. g (seguridad) y art. 17 lit. d | Deben adoptarse medidas técnicas, humanas y administrativas para evitar consulta, uso o acceso no autorizado o fraudulento. | Confirmada en texto oficial, 2026-11-06. |
| Ley 1581 de 2012, art. 4 lit. b (finalidad) y arts. 8, 9 y 12 | Usar el dato solo para la finalidad informada; autorización previa e informada; informar al titular. | Confirmada en texto oficial, 2026-11-06. |
| Ley 1581 de 2012, art. 23 | Multas de la SIC hasta 2.000 SMMLV. | Confirmada en texto oficial, 2026-11-06. |
| Ley 1480 de 2011, art. 53 (portales de contacto) y Ley 2439 de 2024 | El portal debe exigir a los oferentes datos de identificación y llevar un registro con, al menos, nombre o razón social, documento, dirección física de notificaciones y teléfonos. | Confirmada en texto oficial, 2026-11-06 (frases "portales de contacto" y "direccion fisica de notificaciones"). |
| Ley 1480 de 2011, arts. 29 y 30 | La publicidad obliga y no puede ser engañosa. | En el registro como confirmada en texto oficial (2026-11-06); no la volví a verificar hoy. |

No pude verificar si la SIC tiene conceptos o resoluciones que digan expresamente que la dirección de residencia es dato personal, ni qué tamaño de zona aproximada acepta. No los cito.

### Nivel de riesgo
- **Situación actual (pin exacto público): alto.** Es una exposición continua de dónde vive una persona identificable, en muchos casos de estudiantes. Además del riesgo de sanción, hay un riesgo real para la seguridad de las personas.
- **Con la propuesta de Juan y estos requisitos: bajo.**

### Requisitos para desarrollo
1. **La propuesta de Juan es suficiente** si se cumplen también los puntos 2 a 10.
2. **Zona aproximada:** como orientación, un círculo de 300 a 500 m de radio en ciudad (los 400 m propuestos están bien). En municipios pequeños o zonas rurales, donde 400 m pueden señalar una sola casa, usar 800 m a 1 km. Ninguna norma fija esta cifra; es criterio técnico mío.
3. **Desplazamiento no reversible:** el centro del círculo se calcula una sola vez con un desplazamiento aleatorio y se guarda. No se puede recalcular en cada carga de página, porque al promediar varias cargas se recupera el punto real. El punto real debe quedar dentro del círculo, pero no en su centro. Si el dueño mueve el pin, se calcula un desplazamiento nuevo.
4. **Las coordenadas exactas no deben salir del servidor en ninguna respuesta pública:** ni en el HTML, ni en atributos `data-*`, ni en el JavaScript en línea (`mapas.js`, `detalle.html`, `buscar.html`), ni en JSON, ni en `PuntoMapa`. Revisarlo mirando el código fuente de la página sin sesión. Solo se envían al dueño autenticado en su formulario de edición (`form.html`) y al administrador.
5. **Distancias y minutos a pie:** calcular con la ubicación exacta, pero mostrar redondeado (por ejemplo, de 5 en 5 minutos, o "menos de 10 min"), para que no se pueda ubicar el punto cruzando la distancia a varias universidades.
6. **Fotos:** quitar los metadatos (EXIF, incluida la ubicación GPS) de todas las fotos al subirlas. Un celular suele grabar las coordenadas exactas dentro de la foto. Sugerir al dueño que no publique fotos donde se vea la placa de la dirección.
7. **Direcciones en textos:** validar título, descripción, reglas de la vivienda y los avisos de `/companeros` con patrones de nomenclatura colombiana (Calle, Cl, Carrera, Cra, Kr, Avenida, Av, Diagonal, Dg, Transversal, Tv, seguidos de número y "#", "No." o "N°", más "apto", "torre", "interior", "casa" con número). Si se detecta, rechazar el guardado y explicarle al usuario por qué. No borrar el texto en silencio. Puede haber falsos positivos (por ejemplo, "a 2 cuadras de la Calle 26"); aceptar referencias sin número de placa.
8. **Etiquetas claras (Ley 1480, arts. 29 y 30):** junto al mapa debe decir "Ubicación aproximada" y que la dirección exacta se entrega al contactar al anunciante. El barrio y la ciudad sí deben ser los reales.
9. **Informar al dueño (Ley 1581, arts. 4 lit. b y 12):** en el formulario de publicación, explicar que la ubicación exacta no se publica, que se usa solo para mostrar la zona y calcular distancias, y que el portal la guarda de forma privada. Esto debe quedar también en la política de tratamiento de datos.
10. **Registro del art. 53 de la Ley 1480:** es un deber distinto a la dirección del inmueble. El portal debe pedir a cada anunciante, en privado, su nombre, documento de identidad, dirección física de notificaciones y teléfono, y guardarlo como registro interno. Ese registro nunca se muestra en el sitio. Solo lo ven el administrador y las autoridades que lo pidan formalmente. Debe protegerse igual que las coordenadas: acceso restringido por rol y sin aparecer en vistas públicas ni en registros de errores. Hoy el portal no pide documento ni dirección de notificaciones, así que esto es un pendiente.
11. **Opcional, para que lo decida el usuario:** constructoras o inmobiliarias (personas jurídicas) con sala de ventas u oficina podrían mostrar su dirección comercial, porque en ese caso no es la casa de una persona natural. No es obligatorio y se puede dejar para después.

### Qué revisar con un abogado
- Si la dirección de un inmueble asociada al anunciante se considera dato personal, y si cambia algo que la matrícula inmobiliaria sea pública (art. 4 lit. f habla de "salvo la información pública").
- Si el registro del art. 53 de la Ley 1480 aplica al portal tal como opera hoy (sin cobro por transacción) y qué datos mínimos exige la Ley 2439 de 2024 para portales de contacto.
- Cuánto tiempo guardar las coordenadas exactas y el registro del art. 53 después de que el anuncio se elimine.
- El texto final de la política de tratamiento y del aviso en el formulario de publicación.
- Si se aprueba el punto 11, en qué condiciones se puede mostrar la dirección comercial de una persona jurídica.

No encontré decisiones anteriores en `coordinacion/DECISIONES.md` que este concepto contradiga.

## Decisión del usuario
**2026-11-06 — Confirmada por el usuario (fundador).** No se muestra la dirección exacta: solo ciudad, barrio y zona aproximada en el mapa; la dirección se entrega en privado al contactar al dueño. Se implementan los requisitos 1 a 9. El requisito 10 (registro privado del art. 53) queda como tarea siguiente. El punto 11 (dirección comercial de personas jurídicas) queda sin decidir.

**Implementado el 2026-11-06 por Juan.** Verificación de Argenis: `legal/conocimiento/checks_privacidad_ubicacion.json`, 9 de 9 controles cumplidos. Revisión del código fuente de 12 páginas públicas: 0 coordenadas exactas expuestas. Pendiente: requisito 10 (registro privado del art. 53).
