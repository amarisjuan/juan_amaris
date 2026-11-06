// Valida los datos de un contrato de arrendamiento de vivienda urbana contra topes de la Ley 820 de 2003.
// Uso: node execution/validar_contrato.mjs --datos datos/contrato.json [--ipc 5.10] [--salida .tmp/validacion.json]
//   --ipc: variación anual del IPC del año anterior certificada por el DANE (porcentaje). Sin él no se valida el reajuste.
import { argumentos, responder, leerJSON, escribir, valorEn } from './lib/comun.mjs';

const args = argumentos({ datos: { type: 'string' }, ipc: { type: 'string' }, salida: { type: 'string' } }, ['datos']);
const c = leerJSON(args.datos);
const hallazgos = [];
const h = (nivel, norma, mensaje) => hallazgos.push({ nivel, norma, mensaje });
const num = (v) => (v === undefined || v === null || v === '' ? null : Number(v));

const obligatorios = ['arrendador.nombre', 'arrendador.documento', 'arrendatario.nombre', 'arrendatario.documento',
  'inmueble.direccion', 'inmueble.ciudad', 'canon', 'fecha_inicio', 'modalidad'];
for (const campo of obligatorios) {
  const v = valorEn(c, campo);
  if (v === undefined || v === null || v === '' || String(v).toUpperCase().startsWith('PENDIENTE')) {
    h('error', 'Ley 820/2003', `Falta el dato obligatorio "${campo}".`);
  }
}

const modalidades = ['vivienda', 'compartido', 'pension'];
if (c.modalidad && !modalidades.includes(c.modalidad)) {
  h('error', 'Ley 820/2003 art. 2', `Modalidad "${c.modalidad}" no válida. Use: ${modalidades.join(', ')}.`);
}

// Art. 16: prohibición de depósitos en dinero y cauciones reales
const deposito = num(c.deposito);
if (deposito && deposito > 0) {
  h('error', 'Ley 820/2003 art. 16', `Se pactó un depósito de $${deposito.toLocaleString('es-CO')}. En vivienda urbana no se pueden exigir depósitos en dinero ni cauciones reales. Use codeudor, póliza o fianza.`);
}

// Art. 18: renta máxima 1% del valor comercial, que no puede superar 2 veces el avalúo catastral
const canon = num(c.canon), comercial = num(c.valor_comercial), catastral = num(c.avaluo_catastral);
if (canon && (comercial || catastral)) {
  const base = Math.min(comercial ?? Infinity, catastral ? 2 * catastral : Infinity);
  const tope = Math.floor(base * 0.01);
  if (canon > tope) {
    h('error', 'Ley 820/2003 art. 18', `El canon ($${canon.toLocaleString('es-CO')}) supera el tope legal de $${tope.toLocaleString('es-CO')} (1% de $${base.toLocaleString('es-CO')}).`);
  } else {
    h('info', 'Ley 820/2003 art. 18', `Canon dentro del tope legal ($${tope.toLocaleString('es-CO')}).`);
  }
  if (!catastral) h('advertencia', 'Ley 820/2003 art. 18', 'Sin avalúo catastral no se puede confirmar que el valor comercial usado no supere 2 veces el catastral.');
} else if (canon) {
  h('advertencia', 'Ley 820/2003 art. 18', 'Sin valor comercial ni avalúo catastral no se puede verificar el tope del canon.');
}

// Art. 20: reajuste anual máximo = IPC del año anterior
const incremento = num(c.incremento_anual_pct);
if (incremento !== null) {
  if (args.ipc === undefined) {
    h('advertencia', 'Ley 820/2003 art. 20', 'No se pasó --ipc: no se puede verificar el tope del reajuste.');
  } else if (incremento > Number(args.ipc)) {
    h('error', 'Ley 820/2003 art. 20', `El reajuste pactado (${incremento}%) supera el IPC del año anterior (${args.ipc}%).`);
  }
}

// Art. 5: término
const meses = num(c.duracion_meses);
if (meses === null) h('info', 'Ley 820/2003 art. 5', 'No se pactó término: se entiende de un (1) año.');
if (c.modalidad === 'pension' && meses !== null && meses >= 12) {
  h('advertencia', 'Ley 820/2003 art. 2 lit. d', 'La pensión se pacta por un término inferior a un año; revise la modalidad.');
}

// Art. 28: matrícula de arrendadores
const contratos = num(c.arrendador?.contratos_vigentes);
if (contratos !== null && contratos > 5) {
  h('advertencia', 'Ley 820/2003 art. 28 y Decreto 51/2004', `El arrendador tiene ${contratos} contratos: puede requerir matrícula de arrendador ante la alcaldía.`);
}

const errores = hallazgos.filter((x) => x.nivel === 'error').length;
const resultado = { valido: errores === 0, errores, advertencias: hallazgos.filter((x) => x.nivel === 'advertencia').length, hallazgos };
if (args.salida) escribir(args.salida, JSON.stringify(resultado, null, 2));
responder(true, resultado);
