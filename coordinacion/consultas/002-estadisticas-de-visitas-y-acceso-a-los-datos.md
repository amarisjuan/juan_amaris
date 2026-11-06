# Consulta 002: Estadísticas de visitas y acceso a los datos

**De:** Juan (desarrollo)  **Para:** Argenis (legal)  **Fecha:** 2026-11-06  **Estado:** abierta

## Pregunta
Ya se construyó (por decisión del usuario, antes de esta consulta) un panel para administradores con estadísticas de visitas, contactos y búsquedas, y el registro privado de anunciantes del art. 53. ¿El diseño cumple la Ley 1581 de 2012? ¿Hace falta aviso de cookies, actualizar la política o el aviso de privacidad, o limitar quién y cómo accede al registro de anunciantes?

## Propuesta de Juan
Estadísticas: solo totales por día (tabla conteos_diarios); no se guarda IP, usuario ni cookies. Visitantes únicos aproximados con huella SHA-256(sal diaria aleatoria en memoria + IP + navegador) que no se guarda en la base y no se puede revertir ni cruzar entre días. Los clics de WhatsApp pasan por /contacto/... que suma 1 y redirige; no se sabe ni se guarda quién hizo clic. Registro art. 53: tipo y número de documento, dirección y ciudad de notificaciones; obligatorio antes de publicar inmuebles; visible solo en /admin/anunciantes para rol ADMIN; nunca en vistas públicas (cubierto con pruebas); toString sin datos personales.

## Contexto
Código: service/ActividadService.java, config/ContadorVisitas.java, controller/ContactoController.java, service/PanelService.java, controller/AdminController.java, model/RegistroAnunciante.java, controller/RegistroAnuncianteController.java, templates/admin/. Pendientes de opinión: tiempo de conservación de conteos y del registro, si los administradores deben firmar acuerdo de confidencialidad, si la búsqueda por texto libre puede contener datos personales.

## Respuesta de Argenis
<!-- Argenis: concepto, normas con artículo, nivel de riesgo, requisitos para desarrollo, qué revisar con abogado. -->

## Decisión del usuario
<!-- Se completa cuando el usuario confirma. Copiar también a coordinacion/DECISIONES.md -->
