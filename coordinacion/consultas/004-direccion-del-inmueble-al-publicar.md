# Consulta 004: Dirección del inmueble al publicar

**De:** Juan (desarrollo)  **Para:** Argenis (legal)  **Fecha:** 2026-11-06  **Estado:** abierta

## Pregunta
Quiero que quien publica pueda indicar la dirección de su inmueble de dos formas: usar su ubicación actual (GPS) o escribirla a mano. ¿Podemos guardar esa dirección de forma privada? ¿Qué debemos cumplir, por ejemplo consentimiento, finalidad, tiempo de conservación y quién puede verla? ¿Puede mostrarse alguna parte de la dirección al público?

## Propuesta de Juan
La dirección se guarda en un campo privado (no publicado). Se usa solo para ubicar el inmueble en el mapa y calcular distancias. El público sigue viendo solo la zona aproximada de 400 m (decisión 001). La dirección no aparece en ninguna página, JSON ni enlace público; solo la ve el dueño al editar y el administrador. Se pide consentimiento explícito en el formulario y se informa la finalidad. Se valida que la dirección sea de Colombia y se geocodifica en el navegador con el servicio de OpenStreetMap.

## Contexto
Campos actuales: latitud/longitud exactas (privadas), zona pública (latitudPublica/longitudPublica), detector de direcciones en textos. Dirección nueva: campo privado 'direccionExacta' en Inmueble. Pendiente de implementar hasta tener respuesta.

## Respuesta de Argenis
<!-- Argenis: concepto, normas con artículo, nivel de riesgo, requisitos para desarrollo, qué revisar con abogado. -->

## Decisión del usuario
<!-- Se completa cuando el usuario confirma. Copiar también a coordinacion/DECISIONES.md -->
