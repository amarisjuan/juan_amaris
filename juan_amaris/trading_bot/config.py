import os

from dotenv import load_dotenv

load_dotenv()


def _f(name, default):
    return float(os.getenv(name, default))


def _i(name, default):
    return int(os.getenv(name, default))


EXCHANGE = os.getenv("EXCHANGE", "binance")
API_KEY = os.getenv("API_KEY", "")
API_SECRET = os.getenv("API_SECRET", "")
SYMBOL = os.getenv("SYMBOL", "BTC/USDT")
TIMEFRAME = os.getenv("TIMEFRAME", "1h")
MODE = os.getenv("MODE", "paper").lower()

CAPITAL = _f("CAPITAL", 100)
POSITION_SIZE = _f("POSITION_SIZE", 0.95)
STOP_LOSS = _f("STOP_LOSS", 0.02)
TAKE_PROFIT = _f("TAKE_PROFIT", 0.04)
FEE = _f("FEE", 0.001)

EMA_FAST = _i("EMA_FAST", 9)
EMA_SLOW = _i("EMA_SLOW", 21)
RSI_PERIOD = _i("RSI_PERIOD", 14)
RSI_MAX = _f("RSI_MAX", 70)
