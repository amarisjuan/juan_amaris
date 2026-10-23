// Utilidades compartidas por los scripts de execution/.
import { readFile, writeFile, mkdir } from "node:fs/promises";
import { dirname } from "node:path";

export function leerArgs(argv = process.argv.slice(2)) {
  const args = {};
  for (let i = 0; i < argv.length; i++) {
    if (!argv[i].startsWith("--")) continue;
    const clave = argv[i].slice(2);
    const siguiente = argv[i + 1];
    if (siguiente === undefined || siguiente.startsWith("--")) args[clave] = true;
    else args[clave] = argv[++i];
  }
  return args;
}

export async function leerJson(ruta) {
  return JSON.parse(await readFile(ruta, "utf8"));
}

export async function escribirJson(ruta, datos) {
  await mkdir(dirname(ruta), { recursive: true });
  await writeFile(ruta, JSON.stringify(datos, null, 2));
}

export function exito(data) {
  process.stdout.write(JSON.stringify({ success: true, data, error: null }) + "\n");
  process.exit(0);
}

export function fallo(error) {
  console.error(error?.stack || error);
  process.stdout.write(JSON.stringify({ success: false, data: null, error: String(error?.message || error) }) + "\n");
  process.exit(1);
}

export function limpiarHtml(html = "") {
  return html.replace(/<[^>]+>/g, " ").replace(/&nbsp;/g, " ").replace(/&amp;/g, "&").replace(/\s+/g, " ").trim();
}

export function normalizarTexto(texto = "") {
  return texto.toLowerCase().normalize("NFD").replace(/[̀-ͯ]/g, "");
}

// true si `termino` aparece como palabra completa en `texto` (sin tildes ni mayusculas).
export function contienePalabra(texto, termino) {
  const t = normalizarTexto(termino).replace(/[.*+?^${}()|[\]\\]/g, "\\$&");
  return new RegExp(`(^|[^a-z0-9])${t}([^a-z0-9]|$)`).test(normalizarTexto(texto));
}
