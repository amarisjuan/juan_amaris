"""Prueba la estrategia con datos historicos ANTES de arriesgar dinero.

Uso:
    python backtest.py                 # descarga velas del exchange
    python backtest.py --csv datos.csv # usa un archivo propio
"""
import argparse

import config
from datos import crear_exchange, descargar_velas, leer_csv
from estrategia import calcular_senales


def backtest(df):
    df = calcular_senales(df)
    capital = config.CAPITAL
    cantidad = 0.0
    precio_entrada = 0.0
    operaciones = []
    curva = []

    for fila in df.itertuples():
        if cantidad > 0:
            stop = precio_entrada * (1 - config.STOP_LOSS)
            objetivo = precio_entrada * (1 + config.TAKE_PROFIT)
            precio_salida = motivo = None
            if fila.low <= stop:
                precio_salida, motivo = stop, "stop loss"
            elif fila.high >= objetivo:
                precio_salida, motivo = objetivo, "take profit"
            elif fila.senal == -1:
                precio_salida, motivo = fila.close, "senal"
            if precio_salida:
                capital += cantidad * precio_salida * (1 - config.FEE)
                ganancia = (precio_salida / precio_entrada - 1) - 2 * config.FEE
                operaciones.append((fila.fecha, motivo, ganancia))
                cantidad = 0.0
        elif fila.senal == 1:
            invertir = capital * config.POSITION_SIZE
            cantidad = invertir * (1 - config.FEE) / fila.close
            capital -= invertir
            precio_entrada = fila.close
        curva.append(capital + cantidad * fila.close)

    return operaciones, curva


def resumen(operaciones, curva):
    final = curva[-1] if curva else config.CAPITAL
    ganadoras = [o for o in operaciones if o[2] > 0]
    pico, max_caida = 0.0, 0.0
    for valor in curva:
        pico = max(pico, valor)
        max_caida = max(max_caida, 1 - valor / pico)

    print(f"Par: {config.SYMBOL}  Temporalidad: {config.TIMEFRAME}")
    print(f"Capital inicial:   {config.CAPITAL:.2f} USDT")
    print(f"Capital final:     {final:.2f} USDT")
    print(f"Rentabilidad:      {(final / config.CAPITAL - 1) * 100:.2f} %")
    print(f"Operaciones:       {len(operaciones)}")
    if operaciones:
        print(f"% ganadoras:       {len(ganadoras) / len(operaciones) * 100:.1f} %")
    print(f"Maxima caida:      {max_caida * 100:.2f} %")


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--csv", help="archivo CSV con velas historicas")
    parser.add_argument("--velas", type=int, default=1000)
    args = parser.parse_args()

    df = leer_csv(args.csv) if args.csv else descargar_velas(crear_exchange(), args.velas)
    resumen(*backtest(df))
