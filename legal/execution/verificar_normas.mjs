// Verifica en las fuentes oficiales que cada norma del registro siga diciendo lo que creemos (frase clave).
// Uso: node execution/verificar_normas.mjs --registro conocimiento/normas.json --salida .tmp/verificacion_normas.json [--solo id1,id2] [--timeout 30000] [--actualizar-registro]
//   --actualizar-registro: guarda en el registro el estado (confirmado_oficial / revisar_cambio) y la fecha. Escribe fuera de .tmp/: requiere confirmación.
// Usa el proxy de HTTPS_PROXY si existe. Solo hace lecturas (GET).
import { ProxyAgent, fetch as fetchUndici } from 'undici';
import { argumentos, responder, leerJSON, escribir, normalizar } from './lib/comun.mjs';

const args = argumentos({
  registro: { type: 'string' }, salida: { type: 'string' }, solo: { type: 'string' }, timeout: { type: 'string' },
  'actualizar-registro': { type: 'boolean' },
}, ['registro', 'salida']);
const registro = leerJSON(args.registro);
const { normas } = registro;
const filtro = args.solo ? new Set(args.solo.split(',')) : null;
const timeout = Number(args.timeout ?? 30000);
const proxy = process.env.HTTPS_PROXY || process.env.https_proxy;
const dispatcher = proxy ? new ProxyAgent(proxy) : undefined;

// Las páginas oficiales escriben las tildes como entidades HTML (d&iacute;as). Como luego se quitan
// las tildes al normalizar, basta con dejar la letra base.
function decodificarEntidades(t) {
  return t
    .replace(/&#x([0-9a-f]+);/gi, (_, h) => String.fromCodePoint(parseInt(h, 16)))
    .replace(/&#(\d+);/g, (_, d) => String.fromCodePoint(Number(d)))
    .replace(/&([a-z])(acute|grave|circ|tilde|uml);/gi, '$1')
    .replace(/&nbsp;/g, ' ').replace(/&amp;/g, '&').replace(/&quot;/g, '"').replace(/&[a-z]+;/gi, ' ');
}

const cache = new Map();
async function descargar(url) {
  if (!cache.has(url)) {
    cache.set(url, (async () => {
      const r = await fetchUndici(url, { dispatcher, signal: AbortSignal.timeout(timeout) });
      if (!r.ok) throw new Error(`HTTP ${r.status}`);
      const buf = Buffer.from(await r.arrayBuffer());
      // Algunos sitios oficiales usan ISO-8859-1
      const tipo = r.headers.get('content-type') || '';
      const texto = new TextDecoder(/8859-1|latin1|windows-1252/i.test(tipo) ? 'latin1' : 'utf-8').decode(buf);
      return normalizar(decodificarEntidades(texto.replace(/<[^>]+>/g, ' ')));
    })());
  }
  return cache.get(url);
}

const resultados = [];
for (const n of normas) {
  if (filtro && !filtro.has(n.id)) continue;
  if (!n.url_oficial || !n.frase_clave) { resultados.push({ id: n.id, estado: 'sin_fuente_oficial' }); continue; }
  try {
    const texto = await descargar(n.url_oficial);
    resultados.push({ id: n.id, estado: texto.includes(normalizar(n.frase_clave)) ? 'confirmada' : 'frase_no_encontrada', url: n.url_oficial });
  } catch (e) {
    resultados.push({ id: n.id, estado: 'error_red', url: n.url_oficial, detalle: e.cause?.code || e.message });
  }
}

const resumen = resultados.reduce((a, r) => ({ ...a, [r.estado]: (a[r.estado] || 0) + 1 }), {});
const salida = { fecha: new Date().toISOString(), resumen, resultados };
escribir(args.salida, JSON.stringify(salida, null, 2));

if (args['actualizar-registro']) {
  const hoy = salida.fecha.slice(0, 10);
  for (const r of resultados) {
    const n = normas.find((x) => x.id === r.id);
    if (r.estado === 'confirmada') { n.estado = 'confirmado_oficial'; n.verificado = hoy; }
    else if (r.estado === 'frase_no_encontrada') { n.estado = 'revisar_cambio'; n.verificado = hoy; }
  }
  escribir(args.registro, JSON.stringify(registro, null, 2) + '\n');
}
responder(true, { salida: args.salida, resumen, registro_actualizado: Boolean(args['actualizar-registro']) });
