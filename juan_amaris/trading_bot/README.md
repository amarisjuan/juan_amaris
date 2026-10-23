# Bot de trading de criptomonedas

Bot en Python que opera criptomonedas (por defecto BTC/USDT en Binance) usando
la libreria [ccxt](https://github.com/ccxt/ccxt), que soporta mas de 100 exchanges.

> ⚠️ **Advertencia:** el trading tiene alto riesgo y puedes perder dinero.
> Ningun bot garantiza ganancias. Usa siempre este orden:
> **1. backtest → 2. paper (simulado) por varias semanas → 3. live con poco dinero.**

## Estrategia
- **Compra** cuando la EMA 9 cruza por encima de la EMA 21 y el RSI esta por debajo de 70.
- **Vende** cuando la EMA 9 cruza por debajo de la EMA 21.
- **Stop loss** de 2% y **take profit** de 4%. Todo se configura en `.env`.

## Instalacion
```bash
cd trading_bot
pip install -r requirements.txt
cp .env.example .env     # y edita tus valores
```

## 1. Backtest (probar con datos historicos)
```bash
python backtest.py              # descarga las ultimas 1000 velas
python backtest.py --csv mis_datos.csv
```
Muestra la rentabilidad, el numero de operaciones, el % de operaciones ganadoras y
la maxima caida. Ajusta los parametros en `.env` y vuelve a probar.

## 2. Paper trading (simulado, sin dinero real)
```bash
python bot.py
```
Con `MODE=paper` (valor por defecto), el bot opera con precios reales pero con
dinero ficticio. El saldo se guarda en `estado_paper.json`.

## 3. Live (dinero real)
1. En Binance crea una API key **solo con permiso de Spot Trading** (sin retiros) y restringida a tu IP.
2. Pon `API_KEY`, `API_SECRET` y `MODE=live` en `.env`.
3. Empieza con un capital pequeno.

Para dejarlo corriendo 24/7 usa un VPS (por ejemplo Hetzner, DigitalOcean o
Oracle Cloud, que tiene un plan gratuito) con `nohup python bot.py &` o un servicio de systemd.

## Archivos
| Archivo | Funcion |
|---|---|
| `estrategia.py` | Indicadores (EMA, RSI) y senales de compra/venta |
| `backtest.py` | Simulacion sobre datos historicos |
| `bot.py` | Bot en vivo (paper o live) |
| `datos.py` | Conexion al exchange y descarga de velas |
| `config.py` | Lee la configuracion de `.env` |
