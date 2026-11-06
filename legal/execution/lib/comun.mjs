// Utilidades compartidas por los scripts de execution/. No es un script CLI.
import { readFileSync, writeFileSync, mkdirSync } from 'node:fs';
import { dirname } from 'node:path';
import { parseArgs } from 'node:util';

/** Imprime la salida estándar del proyecto y termina el proceso. */
export function responder(success, data = {}, error = null) {
  process.stdout.write(JSON.stringify({ success, data, error }, null, 2) + '\n');
  process.exit(success ? 0 : 1);
}

/** Lee argumentos --nombre valor. `opciones` sigue el formato de node:util parseArgs. */
export function argumentos(opciones, obligatorios = []) {
  let valores;
  try {
    ({ values: valores } = parseArgs({ options: opciones, strict: true }));
  } catch (e) {
    responder(false, {}, `Argumentos inválidos: ${e.message}`);
  }
  const faltan = obligatorios.filter((k) => valores[k] === undefined);
  if (faltan.length) responder(false, {}, `Faltan argumentos obligatorios: ${faltan.map((f) => '--' + f).join(', ')}`);
  return valores;
}

export function leerJSON(ruta) {
  try {
    return JSON.parse(readFileSync(ruta, 'utf8'));
  } catch (e) {
    responder(false, {}, `No se pudo leer el JSON ${ruta}: ${e.message}`);
  }
}

export function leerTexto(ruta) {
  try {
    return readFileSync(ruta, 'utf8');
  } catch (e) {
    responder(false, {}, `No se pudo leer ${ruta}: ${e.message}`);
  }
}

export function escribir(ruta, contenido) {
  mkdirSync(dirname(ruta), { recursive: true });
  writeFileSync(ruta, contenido, 'utf8');
}

/** Minúsculas y sin tildes, para comparar textos de forma robusta. */
export function normalizar(texto) {
  return String(texto ?? '').toLowerCase().normalize('NFD').replace(/[̀-ͯ]/g, '').replace(/\s+/g, ' ');
}

/** Obtiene un valor anidado: valorEn({a:{b:1}}, 'a.b') -> 1 */
export function valorEn(objeto, ruta) {
  return ruta.split('.').reduce((o, k) => (o == null ? undefined : o[k]), objeto);
}
