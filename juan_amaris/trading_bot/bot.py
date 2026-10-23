"""Bot de trading. Por defecto corre en modo PAPER (simulado, sin dinero real).

Uso:
    python bot.py            # modo definido en .env (paper por defecto)
    python bot.py --una-vez  # revisa el mercado una sola vez y termina
"""
import argparse
import json
import os
import time
from datetime import datetime

import config
from datos import crear_exchange, descargar_velas
from estrategia import calcular_senales

ARCHIVO_ESTADO = "estado_paper.json"
SEGUNDOS = {"m": 60, "h": 3600, "d": 86400}


def log(msg):
    print(f"[{datetime.now():%Y-%m-%d %H:%M:%S}] {msg}", flush=True)


def cargar_estado():
    if os.path.exists(ARCHIVO_ESTADO):
        with open(ARCHIVO_ESTADO) as f:
            return json.load(f)
    return {"usdt": config.CAPITAL, "cantidad": 0.0, "precio_entrada": 0.0}


def guardar_estado(estado):
    with open(ARCHIVO_ESTADO, "w") as f:
        json.dump(estado, f, indent=2)


def comprar(exchange, estado, precio):
    invertir = estado["usdt"] * config.POSITION_SIZE
    if config.MODE == "live":
        orden = exchange.create_market_buy_order(config.SYMBOL, invertir / precio)
        precio = orden.get("average") or precio
        estado["cantidad"] = orden.get("filled") or invertir / precio
    else:
        estado["cantidad"] = invertir * (1 - config.FEE) / precio
    estado["usdt"] -= invertir
    estado["precio_entrada"] = precio
    log(f"COMPRA {estado['cantidad']:.6f} a {precio:.2f}")


def vender(exchange, estado, precio, motivo):
    if config.MODE == "live":
        orden = exchange.create_market_sell_order(config.SYMBOL, estado["cantidad"])
        precio = orden.get("average") or precio
    estado["usdt"] += estado["cantidad"] * precio * (1 - config.FEE)
    resultado = (precio / estado["precio_entrada"] - 1) * 100
    log(f"VENTA ({motivo}) a {precio:.2f}  resultado: {resultado:+.2f} %")
    estado["cantidad"] = 0.0
    estado["precio_entrada"] = 0.0


def revisar(exchange, estado):
    df = calcular_senales(descargar_velas(exchange, 200))
    # Se usa la ultima vela CERRADA; la ultima fila aun se esta formando
    vela = df.iloc[-2]
    precio = exchange.fetch_ticker(config.SYMBOL)["last"]

    if estado["cantidad"] > 0:
        entrada = estado["precio_entrada"]
        if precio <= entrada * (1 - config.STOP_LOSS):
            vender(exchange, estado, precio, "stop loss")
        elif precio >= entrada * (1 + config.TAKE_PROFIT):
            vender(exchange, estado, precio, "take profit")
        elif vela.senal == -1:
            vender(exchange, estado, precio, "senal")
    elif vela.senal == 1:
        comprar(exchange, estado, precio)

    total = estado["usdt"] + estado["cantidad"] * precio
    log(f"Precio {precio:.2f} | RSI {vela.rsi:.1f} | Total {total:.2f} USDT")
    guardar_estado(estado)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--una-vez", action="store_true")
    args = parser.parse_args()

    if config.MODE == "live":
        if not config.API_KEY or not config.API_SECRET:
            raise SystemExit("Modo live requiere API_KEY y API_SECRET en el .env")
        log("ATENCION: modo LIVE, se usara dinero real.")
    else:
        log("Modo PAPER: operaciones simuladas, sin dinero real.")

    exchange = crear_exchange(con_llaves=config.MODE == "live")
    estado = cargar_estado()
    espera = int(config.TIMEFRAME[:-1]) * SEGUNDOS[config.TIMEFRAME[-1]]

    while True:
        try:
            revisar(exchange, estado)
        except Exception as e:
            log(f"Error: {e}")
        if args.una_vez:
            break
        time.sleep(min(espera, 300))


if __name__ == "__main__":
    main()
