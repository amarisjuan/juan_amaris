"""Estrategia: cruce de medias moviles exponenciales (EMA) con filtro RSI.

- Compra cuando la EMA rapida cruza por encima de la lenta y el RSI no esta sobrecomprado.
- Vende cuando la EMA rapida cruza por debajo de la lenta
  (o antes, si se activa el stop loss / take profit).
"""
import pandas as pd

import config


def rsi(close: pd.Series, period: int) -> pd.Series:
    delta = close.diff()
    ganancia = delta.clip(lower=0).ewm(alpha=1 / period, adjust=False).mean()
    perdida = (-delta.clip(upper=0)).ewm(alpha=1 / period, adjust=False).mean()
    rs = ganancia / perdida
    return 100 - 100 / (1 + rs)


def calcular_senales(df: pd.DataFrame) -> pd.DataFrame:
    """Agrega columnas de indicadores y 'senal': 1 = comprar, -1 = vender, 0 = nada."""
    df = df.copy()
    df["ema_rapida"] = df["close"].ewm(span=config.EMA_FAST, adjust=False).mean()
    df["ema_lenta"] = df["close"].ewm(span=config.EMA_SLOW, adjust=False).mean()
    df["rsi"] = rsi(df["close"], config.RSI_PERIOD)

    arriba = df["ema_rapida"] > df["ema_lenta"]
    cruce_arriba = arriba & ~arriba.shift(1, fill_value=False)
    cruce_abajo = ~arriba & arriba.shift(1, fill_value=False)

    df["senal"] = 0
    df.loc[cruce_arriba & (df["rsi"] < config.RSI_MAX), "senal"] = 1
    df.loc[cruce_abajo, "senal"] = -1
    # Sin senales mientras los indicadores se estabilizan
    df.iloc[: config.EMA_SLOW, df.columns.get_loc("senal")] = 0
    return df
