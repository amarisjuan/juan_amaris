// Crea una consulta de Juan para Argenis en la carpeta compartida de coordinación.
// Uso: node execution/crear_consulta.mjs --carpeta ../coordinacion/consultas --tema "Dirección exacta" --pregunta "..." [--propuesta "..."] [--contexto "..."]
// Salida: ruta del archivo creado. No sobreescribe archivos existentes.
import { readdirSync, writeFileSync, mkdirSync, existsSync } from 'node:fs';
import { join } from 'node:path';
import { parseArgs } from 'node:util';

const salir = (success, data = {}, error = null) => {
  process.stdout.write(JSON.stringify({ success, data, error }, null, 2) + '\n');
  process.exit(success ? 0 : 1);
};

let v;
try {
  ({ values: v } = parseArgs({ strict: true, options: {
    carpeta: { type: 'string' }, tema: { type: 'string' }, pregunta: { type: 'string' },
    propuesta: { type: 'string' }, contexto: { type: 'string' }, fecha: { type: 'string' },
  } }));
} catch (e) { salir(false, {}, `Argumentos inválidos: ${e.message}`); }
const faltan = ['carpeta', 'tema', 'pregunta'].filter((k) => !v[k]);
if (faltan.length) salir(false, {}, `Faltan argumentos: ${faltan.map((f) => '--' + f).join(', ')}`);

mkdirSync(v.carpeta, { recursive: true });
const numeros = readdirSync(v.carpeta).map((f) => parseInt(f, 10)).filter(Number.isFinite);
const numero = String((numeros.length ? Math.max(...numeros) : 0) + 1).padStart(3, '0');
const slug = v.tema.toLowerCase().normalize('NFD').replace(/[̀-ͯ]/g, '').replace(/[^a-z0-9]+/g, '-').replace(/^-|-$/g, '').slice(0, 50);
const ruta = join(v.carpeta, `${numero}-${slug}.md`);
if (existsSync(ruta)) salir(false, {}, `Ya existe ${ruta}`);

const fecha = v.fecha ?? new Date().toISOString().slice(0, 10);
writeFileSync(ruta, `# Consulta ${numero}: ${v.tema}

**De:** Juan (desarrollo)  **Para:** Argenis (legal)  **Fecha:** ${fecha}  **Estado:** abierta

## Pregunta
${v.pregunta}

## Propuesta de Juan
${v.propuesta ?? '(sin propuesta)'}

## Contexto
${v.contexto ?? '(sin contexto adicional)'}

## Respuesta de Argenis
<!-- Argenis: concepto, normas con artículo, nivel de riesgo, requisitos para desarrollo, qué revisar con abogado. -->

## Decisión del usuario
<!-- Se completa cuando el usuario confirma. Copiar también a coordinacion/DECISIONES.md -->
`, 'utf8');
salir(true, { ruta, numero, estado: 'abierta' });
