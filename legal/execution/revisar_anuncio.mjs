// Revisa un anuncio contra reglas legales (discriminación, depósitos, publicidad engañosa, datos personales).
// Uso: node execution/revisar_anuncio.mjs --reglas conocimiento/reglas_anuncios.json (--anuncio datos/anuncio.json | --texto "...") [--salida .tmp/revision.json]
// El anuncio JSON puede traer: titulo, descripcion, otrasReglas, operacion, convivencia, ...
import { argumentos, responder, leerJSON, escribir, normalizar, valorEn } from './lib/comun.mjs';

const args = argumentos({
  reglas: { type: 'string' }, anuncio: { type: 'string' }, texto: { type: 'string' }, salida: { type: 'string' },
}, ['reglas']);
if (!args.anuncio && !args.texto) responder(false, {}, 'Pase --anuncio <archivo.json> o --texto "<texto>"');

const anuncio = args.anuncio ? leerJSON(args.anuncio) : { descripcion: args.texto };
const { reglas } = leerJSON(args.reglas);
const texto = normalizar([anuncio.titulo, anuncio.descripcion, anuncio.otrasReglas].filter(Boolean).join(' \n '));

const hallazgos = [];
for (const r of reglas) {
  if (r.aplica_operacion && anuncio.operacion && r.aplica_operacion !== anuncio.operacion) continue;
  if (r.tipo === 'campo') {
    const v = valorEn(anuncio, r.campo);
    if (r.valores.includes(v)) hallazgos.push({ regla: r.id, nivel: r.nivel, categoria: r.categoria, norma: r.norma, mensaje: r.mensaje, evidencia: `${r.campo} = ${v}` });
    continue;
  }
  for (const p of r.patrones) {
    const m = texto.match(new RegExp(p, 'i'));
    if (m) {
      hallazgos.push({ regla: r.id, nivel: r.nivel, categoria: r.categoria, norma: r.norma, mensaje: r.mensaje, evidencia: m[0] });
      break;
    }
  }
}

const orden = { bloquear: 0, corregir: 1, revisar: 2 };
hallazgos.sort((a, b) => orden[a.nivel] - orden[b.nivel]);
const decision = hallazgos.some((x) => x.nivel === 'bloquear') ? 'bloquear'
  : hallazgos.some((x) => x.nivel === 'corregir') ? 'pedir_correccion'
  : hallazgos.length ? 'revision_humana' : 'aprobado';
const resultado = { decision, total: hallazgos.length, hallazgos };
if (args.salida) escribir(args.salida, JSON.stringify(resultado, null, 2));
responder(true, resultado);
