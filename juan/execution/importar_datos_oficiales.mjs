// Convierte los listados oficiales en los CSV que usa el portal (src/main/resources/datos).
// Uso: node execution/importar_datos_oficiales.mjs --municipios <divipola.csv> --instituciones <snies_ies.csv> --salida <carpeta>
// Fuentes: DIVIPOLA (MinSalud, datos.gov.co pqwj-3fi4 / gdxc-w37w) e instituciones de educación superior
// (MEN-SNIES, datos.gov.co n5yy-8nav). Solo instituciones con estado "Activa". Salida: CSV en UTF-8, separador ";".
import { readFileSync, writeFileSync, mkdirSync } from 'node:fs';
import { join } from 'node:path';

// Utilidades mínimas (no se importan de otra carpeta para que el script sea autocontenido)
function responder(success, data = {}, error = null) {
  process.stdout.write(JSON.stringify({ success, data, error }, null, 2) + '\n');
  process.exit(success ? 0 : 1);
}
function argumentos(opciones, obligatorios = []) {
  const v = {};
  const a = process.argv.slice(2);
  for (let i = 0; i < a.length; i++) {
    const k = a[i].replace(/^--/, '');
    if (!(k in opciones)) responder(false, {}, `Argumento desconocido: --${k}`);
    v[k] = a[++i];
  }
  const faltan = obligatorios.filter((k) => v[k] === undefined);
  if (faltan.length) responder(false, {}, `Faltan argumentos obligatorios: ${faltan.map((f) => '--' + f).join(', ')}`);
  return v;
}
const normalizar = (t) => String(t ?? '').normalize('NFD').replace(/[\u0300-\u036f]/g, '').toUpperCase().replace(/[^A-Z0-9]+/g, ' ').trim();

const args = argumentos({ municipios: { type: 'string' }, instituciones: { type: 'string' }, salida: { type: 'string' } },
  ['municipios', 'instituciones', 'salida']);

function leerCSV(ruta) {
  const raw = readFileSync(ruta);
  let texto;
  try { texto = new TextDecoder('utf-8', { fatal: true }).decode(raw); } catch { texto = new TextDecoder('windows-1252').decode(raw); }
  const filas = []; let fila = []; let campo = ''; let comillas = false;
  for (let i = 0; i < texto.length; i++) {
    const c = texto[i];
    if (comillas) {
      if (c === '"' && texto[i + 1] === '"') { campo += '"'; i++; } else if (c === '"') comillas = false; else campo += c;
    } else if (c === '"') comillas = true;
    else if (c === ',') { fila.push(campo); campo = ''; }
    else if (c === '\n') { fila.push(campo.replace(/\r$/, '')); filas.push(fila); fila = []; campo = ''; }
    else campo += c;
  }
  if (campo || fila.length) { fila.push(campo); filas.push(fila); }
  const [cab, ...datos] = filas;
  return datos.filter((f) => f.length > 1).map((f) => Object.fromEntries(cab.map((k, j) => [k.trim(), (f[j] ?? '').trim()])));
}

const titulo = (s) => s.toLowerCase().split(' ').map((w, i) =>
  (i > 0 && ['de', 'del', 'la', 'las', 'los', 'y', 'en'].includes(w)) ? w : w.charAt(0).toUpperCase() + w.slice(1))
  .join(' ').replace(/\bd\.c\./i, 'D.C.');

// Nombre de uso común cuando el oficial es distinto ("Santiago de Cali" -> "Cali")
const ALIAS = { '76001': 'Cali', '11001': 'Bogotá', '05001': 'Medellín', '08001': 'Barranquilla', '13001': 'Cartagena',
  '54001': 'Cúcuta', '68001': 'Bucaramanga', '70001': 'Sincelejo', '47001': 'Santa Marta', '66001': 'Pereira' };

const mun = leerCSV(args.municipios).filter((r) => (r['Tipo: Municipio / Isla / Área no municipalizada'] || 'Municipio') === 'Municipio');
const municipios = mun.map((r) => ({
  codigo: r['Código Municipio'].padStart(5, '0'), departamento: titulo(r['Nombre Departamento']),
  oficial: titulo(r['Nombre Municipio']), municipio: ALIAS[r['Código Municipio']] ?? titulo(r['Nombre Municipio']),
  lat: r['Latitud'].replace(',', '.'), lon: r['longitud'].replace(',', '.'),
})).sort((a, b) => a.departamento.localeCompare(b.departamento, 'es') || a.municipio.localeCompare(b.municipio, 'es'));

// Índice por nombre normalizado (común u oficial) y departamento
const indice = new Map();
for (const m of municipios) for (const n of [m.municipio, m.oficial]) {
  const k = normalizar(n);
  indice.set(k, [...(indice.get(k) ?? []), m]);
}

const ies = leerCSV(args.instituciones).filter((r) => r['Estado'].startsWith('Activa'));
const instituciones = []; const sinMunicipio = [];
for (const r of ies) {
  const candidatos = indice.get(normalizar(r['Municipio Domicilio'])) ?? [];
  const m = candidatos.find((c) => normalizar(c.departamento) === normalizar(r['Departamento Domicilio'])) ?? candidatos[0];
  if (!m) { sinMunicipio.push(`${r['Nombre Institución']} (${r['Municipio Domicilio']})`); continue; }
  const web = /^(na|sin dato)?$/i.test(r['Página Web']) ? '' : r['Página Web'];
  instituciones.push({ nombre: titulo(r['Nombre Institución']), sede: r['Principal/Seccional'], codigo: m.codigo,
    municipio: m.municipio, departamento: m.departamento, caracter: r['Carácter Académico'], web });
}
instituciones.sort((a, b) => a.municipio.localeCompare(b.municipio, 'es') || a.nombre.localeCompare(b.nombre, 'es'));

const csv = (filas, cols) => [cols.join(';'), ...filas.map((f) => cols.map((c) => String(f[c] ?? '').replace(/;/g, ',')).join(';'))].join('\n') + '\n';
mkdirSync(args.salida, { recursive: true });
writeFileSync(join(args.salida, 'municipios.csv'), csv(municipios, ['codigo', 'departamento', 'municipio', 'oficial', 'lat', 'lon']), 'utf8');
writeFileSync(join(args.salida, 'instituciones.csv'), csv(instituciones, ['nombre', 'sede', 'codigo', 'municipio', 'departamento', 'caracter', 'web']), 'utf8');

responder(sinMunicipio.length === 0, {
  municipios: municipios.length, instituciones: instituciones.length,
  municipiosConInstitucion: new Set(instituciones.map((i) => i.codigo)).size, sinMunicipio,
});
