// Verifica en el código del portal que existan los elementos de la autorización de datos (Ley 1581/2012).
// Uso: node execution/verificar_autorizacion_portal.mjs --raiz ../juan_amaris/inmobiliaria --checks conocimiento/checks_autorizacion.json [--salida .tmp/x.json]
// Solo lee archivos; no modifica el portal.
import { existsSync } from 'node:fs';
import { join } from 'node:path';
import { argumentos, responder, leerJSON, leerTexto, escribir } from './lib/comun.mjs';

const args = argumentos({ raiz: { type: 'string' }, checks: { type: 'string' }, salida: { type: 'string' } }, ['raiz', 'checks']);
const { checks } = leerJSON(args.checks);

// Un control con "requiere" solo puede cumplirse si el control del que depende se cumple.
const resultados = [];
for (const c of checks) {
  const previo = c.requiere && resultados.find((r) => r.id === c.requiere);
  if (previo && !previo.cumple) {
    resultados.push({ id: c.id, cumple: false, descripcion: c.descripcion, norma: c.norma, detalle: `Depende de "${c.requiere}", que no se cumple` });
    continue;
  }
  resultados.push(evaluar(c));
}

function evaluar(c) {
  const ruta = join(args.raiz, c.archivo);
  if (!existsSync(ruta)) return { id: c.id, cumple: false, descripcion: c.descripcion, norma: c.norma, detalle: `No existe ${c.archivo}` };
  const cumple = new RegExp(c.patron, 'is').test(leerTexto(ruta));
  return { id: c.id, cumple, descripcion: c.descripcion, norma: c.norma, archivo: c.archivo };
}

const cumplidos = resultados.filter((r) => r.cumple).length;
const resultado = { cumple_todo: cumplidos === resultados.length, cumplidos, total: resultados.length, resultados };
if (args.salida) escribir(args.salida, JSON.stringify(resultado, null, 2));
responder(true, resultado);
