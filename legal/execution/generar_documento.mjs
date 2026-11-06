// Genera un documento a partir de una plantilla Markdown y uno o varios archivos JSON de datos.
// Sintaxis de plantilla:
//   {{campo}} o {{campo.sub}}      -> valor
//   {{#si campo}} ... {{/si}}      -> se incluye si el campo es verdadero / no vacío
//   {{#no campo}} ... {{/no}}      -> se incluye si el campo es falso / vacío
// Los valores que empiezan por "PENDIENTE" se marcan como [PENDIENTE: campo] y se reportan.
//
// Uso: node execution/generar_documento.mjs --plantilla plantillas/x.md --datos datos/a.json,datos/b.json --salida .tmp/x.md [--fecha AAAA-MM-DD]
import { argumentos, responder, leerJSON, leerTexto, escribir, valorEn } from './lib/comun.mjs';

const args = argumentos({
  plantilla: { type: 'string' }, datos: { type: 'string' }, salida: { type: 'string' }, fecha: { type: 'string' },
}, ['plantilla', 'datos', 'salida']);

const datos = Object.assign({}, ...args.datos.split(',').map((r) => leerJSON(r.trim())));
datos.fecha_generacion = args.fecha ?? new Date().toISOString().slice(0, 10);

const verdadero = (v) => !(v === undefined || v === null || v === false || v === '' || (Array.isArray(v) && v.length === 0));

let texto = leerTexto(args.plantilla);

// Bloques condicionales (permite anidar distintos campos; se resuelven de adentro hacia afuera)
const bloque = /\{\{#(si|no) ([\w.]+)\}\}([\s\S]*?)\{\{\/\1\}\}/;
let guardas = 0;
while (bloque.test(texto)) {
  texto = texto.replace(bloque, (_, tipo, campo, contenido) => {
    const v = verdadero(valorEn(datos, campo));
    return (tipo === 'si' ? v : !v) ? contenido : '';
  });
  if (++guardas > 1000) responder(false, {}, 'Bloques condicionales mal cerrados en la plantilla');
}

const faltantes = new Set();
const pendientes = new Set();
texto = texto.replace(/\{\{([\w.]+)\}\}/g, (_, campo) => {
  const v = valorEn(datos, campo);
  if (v === undefined || v === null) { faltantes.add(campo); return `[FALTA: ${campo}]`; }
  if (typeof v === 'string' && v.trim().toUpperCase().startsWith('PENDIENTE')) { pendientes.add(campo); return `[PENDIENTE: ${campo}]`; }
  if (Array.isArray(v)) return v.map((x) => `- ${x}`).join('\n');
  if (typeof v === 'number') return v.toLocaleString('es-CO');
  return String(v);
});

if (faltantes.size) {
  responder(false, { faltantes: [...faltantes] }, `Faltan datos para la plantilla: ${[...faltantes].join(', ')}`);
}

texto = texto.replace(/\n{3,}/g, '\n\n');
escribir(args.salida, texto);
responder(true, {
  salida: args.salida,
  pendientes: [...pendientes],
  listo_para_revision_abogado: pendientes.size === 0,
  caracteres: texto.length,
});
