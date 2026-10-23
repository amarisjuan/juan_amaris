import ccxt
import pandas as pd

import config


def crear_exchange(con_llaves: bool = False):
    clase = getattr(ccxt, config.EXCHANGE)
    opciones = {"enableRateLimit": True}
    if con_llaves:
        opciones.update(apiKey=config.API_KEY, secret=config.API_SECRET)
    return clase(opciones)


def descargar_velas(exchange, limite: int = 1000) -> pd.DataFrame:
    velas = exchange.fetch_ohlcv(config.SYMBOL, config.TIMEFRAME, limit=limite)
    return a_dataframe(velas)


def a_dataframe(velas) -> pd.DataFrame:
    df = pd.DataFrame(velas, columns=["timestamp", "open", "high", "low", "close", "volume"])
    df["fecha"] = pd.to_datetime(df["timestamp"], unit="ms")
    return df


def leer_csv(ruta: str) -> pd.DataFrame:
    """Lee un CSV con columnas timestamp,open,high,low,close,volume."""
    df = pd.read_csv(ruta)
    if "fecha" not in df.columns:
        df["fecha"] = pd.to_datetime(df["timestamp"], unit="ms")
    return df
