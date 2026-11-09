# Manual de marca

Guía corta para que el sitio, las redes sociales y los materiales impresos se vean iguales.
Los colores del sitio están en `src/main/resources/static/css/estilos.css` (bloque `:root`): si se cambian ahí,
cambia todo el sitio.

## Concepto
**Confianza + hogar.** El azul transmite seriedad y seguridad (lo que más pide la gente al arrendar o comprar),
y el ámbar aporta calidez y recuerda la arena y el sol del Caribe colombiano.

## Paleta

| Nombre | HEX | Uso |
|---|---|---|
| **Azul Puerto** (principal) | `#0B4A6F` | Botones, enlaces, logo, pines del mapa |
| Azul Noche | `#0A2540` | Títulos, portada, pie de página |
| Azul Puerto oscuro | `#083A57` | Botón al pasar el mouse |
| Azul Bruma | `#E6F0F6` | Fondos suaves, encabezados de tablas |
| **Ámbar Arena** (acento) | `#E9A23B` | Botón "Publicar", insignias, línea del encabezado, universidades en el mapa |
| Ámbar oscuro | `#D18A22` | Acento al pasar el mouse, líneas en el mapa |
| Ámbar texto | `#8A5300` | Texto en ámbar sobre fondo claro (info de estudiantes) |
| Crema | `#FDF3E3` | Fondos de secciones para estudiantes |
| Tinta | `#0F1B2A` | Texto principal |
| Gris pizarra | `#5B6B7C` | Texto secundario |
| Borde | `#DDE5EC` | Bordes y divisiones |
| Fondo | `#F6F8FA` | Fondo general |

Estados: éxito `#1E6B3E`, error `#B42318`, aviso `#7A4E00`, WhatsApp `#107C41`.

### Contraste (accesibilidad, mínimo WCAG AA = 4,5)
| Combinación | Contraste |
|---|---|
| Blanco sobre Azul Puerto | 9,45 |
| Azul Noche sobre Ámbar Arena | 7,17 |
| Ámbar texto sobre blanco | 6,33 |
| Gris pizarra sobre blanco | 5,47 |
| Blanco sobre verde WhatsApp | 5,27 |

**Regla:** nunca texto blanco sobre Ámbar Arena (no se lee). Sobre ámbar siempre va Azul Noche.

## Logo
`src/main/resources/static/img/logo.svg` (elegido el 2026-11-09): **techo sobre un nido**. Cuadrado redondeado
Azul Puerto, techo en Ámbar Arena y un nido en blanco (dos arcos). "Nidal" suena a nido: el techo protege el hogar.
El favicon usa el mismo dibujo. Junto al nombre "Nidal Inmuebles", el logo va a la izquierda.

## Íconos
- **No se usan emojis** en el sitio ni en los materiales de la marca.
- Íconos de línea propios en `static/img/iconos.svg` (24×24, trazo 2, esquinas redondeadas) que toman el
  color del texto. En las plantillas: `<svg th:replace="~{fragmentos/base :: ic('gorro')}"></svg>`.
- Disponibles: `casa`, `gorro`, `personas`, `pin`, `candado`, `si`, `no`, `reloj`, `lista`.

## Tipografía
Por ahora la del sistema (rápida y sin costo). Recomendación cuando haya nombre definitivo:
**Plus Jakarta Sans** (títulos, 700–800) e **Inter** (texto, 400–600), ambas gratuitas.

## Pendiente
- Definir el **nombre** de la inmobiliaria (hoy dice "Inmobiliaria") y registrar el dominio .co.
- Versión del logo en blanco para fondos oscuros y en un solo color para impresión.
