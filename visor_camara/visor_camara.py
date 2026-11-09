#!/usr/bin/env python3
"""
Visor de cámara WiFi (IP camera).

Muestra en una ventana lo que ve tu cámara conectada por WiFi usando
RTSP o HTTP (MJPEG). Funciona con la mayoría de cámaras IP
(Tapo, Hikvision, Dahua, Reolink, Imou, ESP32-CAM, apps tipo "IP Webcam", etc.).

Uso:
    # 1) Buscar cámaras en tu red local
    python visor_camara.py --buscar

    # 2) Conectar con una URL directa
    python visor_camara.py --url rtsp://usuario:clave@192.168.1.50:554/stream1

    # 3) Conectar solo con IP + usuario/clave (prueba rutas comunes)
    python visor_camara.py --ip 192.168.1.50 --usuario admin --clave 1234

Teclas en la ventana:
    q / ESC  -> salir
    s        -> guardar captura (foto .jpg)
    r        -> empezar / detener grabación (.avi)
"""

import argparse
import datetime as dt
import ipaddress
import os
import socket
import sys
import time
from concurrent.futures import ThreadPoolExecutor
from urllib.parse import quote

try:
    import cv2
except ImportError:
    sys.exit("Falta OpenCV. Instálalo con:  pip install opencv-python")

# Rutas más comunes de distintas marcas de cámaras IP.
RUTAS_RTSP = [
    "/stream1",                                  # TP-Link Tapo
    "/stream2",
    "/Streaming/Channels/101",                   # Hikvision / EZVIZ
    "/cam/realmonitor?channel=1&subtype=0",      # Dahua / Imou / Amcrest
    "/h264Preview_01_main",                      # Reolink
    "/live/ch00_0",
    "/live",
    "/h264",
    "/onvif1",                                   # cámaras genéricas chinas (V380, Yoosee)
    "/11",
    "/",
]
RUTAS_HTTP = [
    "/video",                                    # app Android "IP Webcam"
    "/mjpg/video.mjpg",                          # Axis
    "/videostream.cgi",                          # Foscam antiguas
    "/stream",                                   # ESP32-CAM (puerto 81)
    "/video.mjpg",
    "/mjpeg",
]
PUERTOS_CAMARA = [554, 8554, 80, 81, 8080, 8000, 34567]


# --------------------------------------------------------------------------- #
# Búsqueda de cámaras en la red local
# --------------------------------------------------------------------------- #
def ip_local():
    s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    try:
        s.connect(("8.8.8.8", 80))  # no envía datos, solo elige la interfaz
        return s.getsockname()[0]
    except OSError:
        return "192.168.1.100"
    finally:
        s.close()


def puerto_abierto(ip, puerto, timeout=0.4):
    try:
        with socket.create_connection((ip, puerto), timeout=timeout):
            return True
    except OSError:
        return False


def buscar_camaras():
    red = ipaddress.ip_network(ip_local() + "/24", strict=False)
    print(f"Buscando cámaras en {red} (puertos {PUERTOS_CAMARA})...")
    objetivos = [(str(ip), p) for ip in red.hosts() for p in PUERTOS_CAMARA]
    encontrados = {}
    with ThreadPoolExecutor(max_workers=200) as ex:
        for (ip, p), ok in zip(objetivos, ex.map(lambda t: puerto_abierto(*t), objetivos)):
            if ok:
                encontrados.setdefault(ip, []).append(p)
    if not encontrados:
        print("No se encontró ningún dispositivo con puertos de cámara abiertos.")
        return
    print("\nPosibles cámaras:")
    for ip, puertos in sorted(encontrados.items(), key=lambda x: ipaddress.ip_address(x[0])):
        marca = "  <-- RTSP (muy probable cámara)" if 554 in puertos or 8554 in puertos else ""
        print(f"  {ip:15}  puertos: {puertos}{marca}")
    print("\nPrueba:  python visor_camara.py --ip <IP> --usuario <usuario> --clave <clave>")


# --------------------------------------------------------------------------- #
# Conexión al video
# --------------------------------------------------------------------------- #
def abrir(url, timeout_ms=5000):
    os.environ.setdefault("OPENCV_FFMPEG_CAPTURE_OPTIONS", "rtsp_transport;tcp")
    cap = cv2.VideoCapture(url, cv2.CAP_FFMPEG)
    try:
        cap.set(cv2.CAP_PROP_OPEN_TIMEOUT_MSEC, timeout_ms)
        cap.set(cv2.CAP_PROP_BUFFERSIZE, 1)
    except Exception:
        pass
    if cap.isOpened():
        ok, _ = cap.read()
        if ok:
            return cap
    cap.release()
    return None


def urls_candidatas(ip, usuario, clave, puerto=None):
    cred = f"{quote(usuario, safe='')}:{quote(clave, safe='')}@" if usuario else ""
    puertos_rtsp = [puerto] if puerto else [554, 8554]
    puertos_http = [puerto] if puerto else [80, 81, 8080]
    for p in puertos_rtsp:
        if puerto_abierto(ip, p):
            for r in RUTAS_RTSP:
                yield f"rtsp://{cred}{ip}:{p}{r}"
    for p in puertos_http:
        if puerto_abierto(ip, p):
            for r in RUTAS_HTTP:
                yield f"http://{cred}{ip}:{p}{r}"


def ocultar_clave(url):
    if "@" in url and "://" in url:
        esquema, resto = url.split("://", 1)
        cred, host = resto.rsplit("@", 1)
        return f"{esquema}://{cred.split(':')[0]}:****@{host}"
    return url


def detectar_url(ip, usuario, clave, puerto=None):
    for url in urls_candidatas(ip, usuario, clave, puerto):
        print(f"Probando {ocultar_clave(url)} ...", end=" ", flush=True)
        cap = abrir(url, timeout_ms=3000)
        if cap:
            print("¡OK!")
            return url, cap
        print("no")
    return None, None


# --------------------------------------------------------------------------- #
# Visor
# --------------------------------------------------------------------------- #
def ver(url, cap=None, carpeta="capturas"):
    os.makedirs(carpeta, exist_ok=True)
    cap = cap or abrir(url)
    if cap is None:
        sys.exit(f"No se pudo conectar a {ocultar_clave(url)}")

    ventana = "Camara WiFi  (q=salir  s=foto  r=grabar)"
    cv2.namedWindow(ventana, cv2.WINDOW_NORMAL)
    grabador, fallos, t0, frames, fps = None, 0, time.time(), 0, 0.0
    print(f"Conectado a {ocultar_clave(url)}")

    while True:
        ok, frame = cap.read()
        if not ok:
            fallos += 1
            if fallos > 30:
                print("Se perdió la señal. Reconectando...")
                cap.release()
                time.sleep(2)
                cap = abrir(url) or cap
                fallos = 0
            continue
        fallos = 0

        frames += 1
        if time.time() - t0 >= 1:
            fps, frames, t0 = frames / (time.time() - t0), 0, time.time()

        if grabador is not None:
            grabador.write(frame)

        vista = frame.copy()
        texto = dt.datetime.now().strftime("%Y-%m-%d %H:%M:%S") + f"  {fps:.1f} fps"
        if grabador is not None:
            texto += "  [REC]"
            cv2.circle(vista, (vista.shape[1] - 25, 25), 10, (0, 0, 255), -1)
        cv2.putText(vista, texto, (10, 30), cv2.FONT_HERSHEY_SIMPLEX, 0.8, (0, 0, 0), 4)
        cv2.putText(vista, texto, (10, 30), cv2.FONT_HERSHEY_SIMPLEX, 0.8, (255, 255, 255), 2)
        cv2.imshow(ventana, vista)

        tecla = cv2.waitKey(1) & 0xFF
        sello = dt.datetime.now().strftime("%Y%m%d_%H%M%S")
        if tecla in (ord("q"), 27):
            break
        if tecla == ord("s"):
            ruta = os.path.join(carpeta, f"foto_{sello}.jpg")
            cv2.imwrite(ruta, frame)
            print(f"Foto guardada: {ruta}")
        if tecla == ord("r"):
            if grabador is None:
                ruta = os.path.join(carpeta, f"video_{sello}.avi")
                h, w = frame.shape[:2]
                grabador = cv2.VideoWriter(ruta, cv2.VideoWriter_fourcc(*"XVID"),
                                           max(fps, 10.0), (w, h))
                print(f"Grabando en {ruta} ...")
            else:
                grabador.release()
                grabador = None
                print("Grabación detenida.")
        if cv2.getWindowProperty(ventana, cv2.WND_PROP_VISIBLE) < 1:
            break

    if grabador is not None:
        grabador.release()
    cap.release()
    cv2.destroyAllWindows()


def main():
    ap = argparse.ArgumentParser(description="Visor para cámaras WiFi (RTSP / HTTP).")
    ap.add_argument("--url", help="URL completa del video (rtsp://... o http://...)")
    ap.add_argument("--ip", help="IP de la cámara (prueba rutas comunes automáticamente)")
    ap.add_argument("--puerto", type=int, help="Puerto (opcional, con --ip)")
    ap.add_argument("--usuario", default="", help="Usuario de la cámara")
    ap.add_argument("--clave", default="", help="Contraseña de la cámara")
    ap.add_argument("--buscar", action="store_true", help="Buscar cámaras en la red local")
    ap.add_argument("--carpeta", default="capturas", help="Dónde guardar fotos y videos")
    a = ap.parse_args()

    if a.buscar:
        buscar_camaras()
    elif a.url:
        ver(a.url, carpeta=a.carpeta)
    elif a.ip:
        url, cap = detectar_url(a.ip, a.usuario, a.clave, a.puerto)
        if not url:
            sys.exit("No se encontró el video. Revisa usuario/clave o busca la URL "
                     "RTSP en el manual / app de tu cámara y usa --url.")
        print(f"\nURL encontrada (guárdala para la próxima vez):\n  {ocultar_clave(url)}\n")
        ver(url, cap, carpeta=a.carpeta)
    else:
        ap.print_help()


if __name__ == "__main__":
    main()
