> **BORRADOR generado el {{fecha_generacion}}. Requiere revisión de un abogado antes de firmarse.**
> No es asesoría legal. Base: Ley 820 de 2003 (régimen de arrendamiento de vivienda urbana).

# Contrato de arrendamiento de vivienda urbana
{{#si es_compartido}}**Modalidad: vivienda compartida** (Ley 820 de 2003, art. 2, literal c).{{/si}}
{{#si es_pension}}**Modalidad: pensión** (Ley 820 de 2003, art. 2, literal d).{{/si}}

**Arrendador:** {{arrendador.nombre}}, identificado con {{arrendador.documento}}, dirección de notificaciones {{arrendador.direccion}}.
**Arrendatario:** {{arrendatario.nombre}}, identificado con {{arrendatario.documento}}, dirección de notificaciones {{arrendatario.direccion}}.

Las partes celebran el presente contrato, que se rige por la Ley 820 de 2003 y por las siguientes cláusulas:

**Primera. Objeto.** El arrendador concede al arrendatario el goce de {{#si es_compartido}}{{inmueble.descripcion_parte}}, en el inmueble ubicado en{{/si}}{{#no es_compartido}}el inmueble ubicado en{{/no}} {{inmueble.direccion}}, {{inmueble.ciudad}}, destinado exclusivamente a vivienda.

**Segunda. Canon.** El canon mensual es de ${{canon}} ({{canon_letras}}), que el arrendatario pagará dentro de {{dia_pago}}, mediante {{forma_pago}}. El canon no supera el uno por ciento (1%) del valor comercial del inmueble o de la parte arrendada (art. 18).

**Tercera. Reajuste.** Cumplidos doce (12) meses de ejecución, el canon podrá reajustarse en una proporción que no supere el ciento por ciento (100%) del IPC del año calendario anterior. El arrendador informará por escrito el monto y la fecha del reajuste (art. 20).

**Cuarta. Término.** El contrato tiene una duración de {{duracion_meses}} meses, contados desde el {{fecha_inicio}}. {{#no es_pension}}Si ninguna de las partes da aviso de terminación en la forma y con la anticipación que exige la ley, se prorrogará en las mismas condiciones y por el mismo término.{{/no}}{{#si es_pension}}Cualquiera de las partes podrá darlo por terminado con aviso de diez (10) días, sin indemnización.{{/si}}

**Quinta. Servicios públicos.** {{#si servicios_incluidos}}El canon incluye los servicios de {{servicios_incluidos}}.{{/si}}{{#no servicios_incluidos}}Los servicios públicos son por cuenta del arrendatario.{{/no}}

**Sexta. Garantía.** Como garantía, el arrendatario presenta {{garantia}}. **No se exige depósito en dinero ni otra caución real**, por estar prohibido en vivienda urbana (art. 16).

**Séptima. Obligaciones del arrendador.** Entregar el inmueble en buen estado de servicio, seguridad y sanidad; mantenerlo así y hacer las reparaciones necesarias; {{#si es_compartido}}mantener en buen estado las zonas y servicios de uso común y velar por el orden interno de la vivienda;{{/si}} y entregar copia del contrato al arrendatario.

**Octava. Obligaciones del arrendatario.** Pagar el canon en la forma y fecha pactadas; cuidar el inmueble{{#si es_compartido}} y las zonas y servicios de uso común{{/si}}; no subarrendar ni ceder el contrato sin autorización escrita del arrendador; respetar las normas de convivencia; y restituir el inmueble al terminar el contrato en el estado en que lo recibió, salvo el deterioro natural.

**Novena. Reglas de convivencia.** {{reglas_convivencia}}

**Décima. Terminación.** El contrato podrá terminarse por mutuo acuerdo o por las causales previstas en la Ley 820 de 2003 (arts. 22 a 24), con los avisos y plazos que esa ley establece.

**Undécima. Inventario.** Las partes firman un inventario del estado del inmueble y sus elementos, que hace parte de este contrato.

**Duodécima. Notificaciones.** Las partes recibirán notificaciones en las direcciones indicadas al inicio.

Firmado en {{inmueble.ciudad}}, el ____ de ____________ de ______.

_____________________________  _____________________________
Arrendador                                    Arrendatario
