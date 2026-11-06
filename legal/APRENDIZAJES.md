# Aprendizajes

<!-- Más recientes arriba. Formato y criterios: ver CLAUDE.md, sección "Aprendizajes". -->

- **2026-11-06 — Certificados de Función Pública y SUIN-Juriscol:** desde Node fallan con `UNABLE_TO_VERIFY_LEAF_SIGNATURE` (la cadena de certificados del sitio está incompleta). La compilación oficial de la Alcaldía de Bogotá (`alcaldiabogota.gov.co/sisjur/normas/Norma1.jsp?i=<id>`) usa los mismos id de Función Pública y sí funciona.
  **Por qué importa:** no desactivar la verificación TLS; usar la fuente alternativa. El Decreto 090 de 2018 sigue sin fuente oficial accesible.
- **2026-11-06 — Tildes como entidades HTML en el Senado:** `secretariasenado.gov.co` escribe `d&iacute;as h&aacute;biles`. Sin decodificar entidades, las frases clave "no aparecen" aunque la norma no haya cambiado.
  **Por qué importa:** un `frase_no_encontrada` puede ser un problema de lectura y no un cambio de norma; revisar el HTML antes de concluir.
- **2026-11-06 — Node no usa el proxy del entorno:** el `fetch` nativo ignora `HTTPS_PROXY`; los scripts que salen a internet usan `undici` con `ProxyAgent`. Además, los permisos de red nuevos pueden tardar varios minutos en aplicarse.
  **Por qué importa:** un `error_red` justo después de cambiar la red no significa que el host esté bloqueado; esperar y reintentar una vez.
