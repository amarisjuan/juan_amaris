// Descarga ofertas de bolsas de empleo publicas y las normaliza a un formato comun.
// Uso: node execution/buscar_empleos.mjs --busqueda "java" [--fuentes remotive,arbeitnow,jobicy]
//        [--salida .tmp/empleos.json] [--archivo respuestas_crudas.json]
import { leerArgs, escribirJson, leerJson, exito, fallo, limpiarHtml, contienePalabra } from "./lib.mjs";

const MAX_DESCRIPCION = 2000;

const FUENTES = {
  remotive: {
    url: (q) => `https://remotive.com/api/remote-jobs?search=${encodeURIComponent(q)}`,
    normalizar: (crudo) =>
      (crudo.jobs || []).map((j) => ({
        id: `remotive-${j.id}`,
        titulo: j.title,
        empresa: j.company_name,
        ubicacion: j.candidate_required_location || "",
        remoto: true,
        url: j.url,
        fecha: j.publication_date,
        salario: j.salary || "",
        etiquetas: j.tags || [],
        descripcion: limpiarHtml(j.description),
      })),
  },
  arbeitnow: {
    url: () => "https://www.arbeitnow.com/api/job-board-api",
    filtraLocal: true,
    normalizar: (crudo) =>
      (crudo.data || []).map((j) => ({
        id: `arbeitnow-${j.slug}`,
        titulo: j.title,
        empresa: j.company_name,
        ubicacion: j.location || "",
        remoto: Boolean(j.remote),
        url: j.url,
        fecha: j.created_at ? new Date(j.created_at * 1000).toISOString() : "",
        salario: "",
        etiquetas: j.tags || [],
        descripcion: limpiarHtml(j.description),
      })),
  },
  jobicy: {
    url: (q) => `https://jobicy.com/api/v2/remote-jobs?count=50&tag=${encodeURIComponent(q)}`,
    normalizar: (crudo) =>
      (crudo.jobs || []).map((j) => ({
        id: `jobicy-${j.id}`,
        titulo: j.jobTitle,
        empresa: j.companyName,
        ubicacion: j.jobGeo || "",
        remoto: true,
        url: j.url,
        fecha: j.pubDate,
        salario: j.annualSalaryMin ? `${j.annualSalaryMin}-${j.annualSalaryMax} ${j.salaryCurrency || ""}`.trim() : "",
        etiquetas: [].concat(j.jobIndustry || [], j.jobType || []),
        descripcion: limpiarHtml(j.jobDescription || j.jobExcerpt),
      })),
  },
};

async function descargar(nombre, busqueda) {
  const respuesta = await fetch(FUENTES[nombre].url(busqueda), { headers: { "User-Agent": "eva-buscador-empleo" } });
  if (!respuesta.ok) throw new Error(`${nombre} respondio HTTP ${respuesta.status}`);
  return respuesta.json();
}

async function main() {
  const args = leerArgs();
  if (!args.busqueda) throw new Error("Falta --busqueda");
  const busqueda = String(args.busqueda);
  const fuentes = String(args.fuentes || Object.keys(FUENTES).join(",")).split(",").map((f) => f.trim());
  const salida = args.salida || ".tmp/empleos.json";
  const crudos = args.archivo ? await leerJson(args.archivo) : null;

  const empleos = [];
  const errores = {};
  for (const nombre of fuentes) {
    if (!FUENTES[nombre]) {
      errores[nombre] = "fuente desconocida";
      continue;
    }
    try {
      const crudo = crudos ? crudos[nombre] || {} : await descargar(nombre, busqueda);
      let lista = FUENTES[nombre].normalizar(crudo);
      if (FUENTES[nombre].filtraLocal) {
        lista = lista.filter((e) => contienePalabra(`${e.titulo} ${e.etiquetas.join(" ")} ${e.descripcion}`, busqueda));
      }
      empleos.push(...lista.map((e) => ({ ...e, fuente: nombre, descripcion: e.descripcion.slice(0, MAX_DESCRIPCION) })));
    } catch (e) {
      console.error(`[${nombre}]`, e);
      errores[nombre] = e.message;
    }
  }

  if (empleos.length === 0 && Object.keys(errores).length === fuentes.length) {
    throw new Error(`Todas las fuentes fallaron: ${JSON.stringify(errores)}`);
  }

  const unicos = [...new Map(empleos.map((e) => [e.id, e])).values()];
  await escribirJson(salida, unicos);
  exito({ salida, total: unicos.length, por_fuente: Object.fromEntries(fuentes.map((f) => [f, unicos.filter((e) => e.fuente === f).length])), errores });
}

main().catch(fallo);
