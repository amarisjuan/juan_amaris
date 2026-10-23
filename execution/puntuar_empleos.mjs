// Ordena ofertas segun su afinidad con el perfil de una persona.
// Uso: node execution/puntuar_empleos.mjs --perfil perfiles/juan.json --empleos .tmp/empleos.json
//        [--salida .tmp/ranking.json] [--top 20] [--minimo 1]
import { basename } from "node:path";
import { leerArgs, leerJson, escribirJson, exito, fallo, normalizarTexto, contienePalabra as contiene } from "./lib.mjs";

const PESO_TITULO = 3;
const PESO_ETIQUETA = 2;
const PESO_DESCRIPCION = 1;

function puntuar(empleo, perfil) {
  const titulo = normalizarTexto(empleo.titulo);
  const etiquetas = normalizarTexto(empleo.etiquetas.join(" | "));
  const descripcion = normalizarTexto(empleo.descripcion);

  const excluidaPor = (perfil.excluir || []).find((p) => contiene(titulo, p));
  if (excluidaPor) return { puntaje: 0, coincidencias: [], descartada: `titulo contiene "${excluidaPor}"` };
  if (perfil.solo_remoto && !empleo.remoto) return { puntaje: 0, coincidencias: [], descartada: "no es remoto" };

  let puntaje = 0;
  const coincidencias = [];
  for (const habilidad of new Set([...(perfil.busqueda || []), ...(perfil.habilidades || [])])) {
    let p = 0;
    if (contiene(titulo, habilidad)) p += PESO_TITULO;
    if (contiene(etiquetas, habilidad)) p += PESO_ETIQUETA;
    if (contiene(descripcion, habilidad)) p += PESO_DESCRIPCION;
    if (p > 0) coincidencias.push(habilidad);
    puntaje += p;
  }
  return { puntaje, coincidencias, descartada: null };
}

async function main() {
  const args = leerArgs();
  if (!args.perfil || !args.empleos) throw new Error("Faltan --perfil y/o --empleos");
  const perfil = await leerJson(args.perfil);
  const empleos = await leerJson(args.empleos);
  const top = Number(args.top || 20);
  const minimo = Number(args.minimo || 1);
  const salida = args.salida || `.tmp/ranking_${basename(args.perfil, ".json")}.json`;

  const evaluados = empleos.map((e) => ({ ...e, ...puntuar(e, perfil) }));
  const descartadas = evaluados.filter((e) => e.descartada).length;
  const ranking = evaluados
    .filter((e) => !e.descartada && e.puntaje >= minimo)
    .sort((a, b) => b.puntaje - a.puntaje)
    .slice(0, top)
    .map(({ descripcion, descartada, ...resto }) => resto);

  await escribirJson(salida, ranking);
  exito({
    salida,
    evaluadas: empleos.length,
    descartadas,
    seleccionadas: ranking.length,
    mejores: ranking.slice(0, 5).map((e) => ({ titulo: e.titulo, empresa: e.empresa, puntaje: e.puntaje, url: e.url })),
  });
}

main().catch(fallo);
