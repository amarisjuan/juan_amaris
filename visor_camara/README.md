# Visor de cámara WiFi

Programa en Python para ver en tu computador lo que transmite tu cámara WiFi (cámara IP).
Soporta RTSP y HTTP/MJPEG, así que sirve para la mayoría de marcas (Tapo, Hikvision/EZVIZ,
Dahua/Imou, Reolink, V380/Yoosee, ESP32-CAM, app "IP Webcam" de Android, etc.).

> Debe ejecutarse en un computador conectado **a la misma red WiFi** que la cámara.

## Instalación

```bash
pip install -r requirements.txt
```

## Uso

1. **Buscar la cámara en tu red:**
   ```bash
   python visor_camara.py --buscar
   ```
2. **Conectarte con la IP** (el programa prueba las rutas más comunes):
   ```bash
   python visor_camara.py --ip 192.168.1.50 --usuario admin --clave TU_CLAVE
   ```
3. **O con la URL exacta** si ya la conoces:
   ```bash
   python visor_camara.py --url "rtsp://admin:TU_CLAVE@192.168.1.50:554/stream1"
   ```

### Teclas
| Tecla | Acción |
|-------|--------|
| `q` / `Esc` | Salir |
| `s` | Guardar foto en `capturas/` |
| `r` | Iniciar / detener grabación en `capturas/` |

## Consejos según la marca
- **TP-Link Tapo:** en la app, *Configuración avanzada → Cuenta de la cámara*, crea usuario y clave. URL: `rtsp://usuario:clave@IP:554/stream1`
- **Hikvision / EZVIZ:** `rtsp://admin:clave@IP:554/Streaming/Channels/101` (en EZVIZ la clave es el código de verificación de la etiqueta y hay que activar RTSP/LAN Live View).
- **Dahua / Imou:** `rtsp://admin:clave@IP:554/cam/realmonitor?channel=1&subtype=0`
- **Reolink:** `rtsp://admin:clave@IP:554/h264Preview_01_main`
- **IP Webcam (Android):** `http://IP:8080/video`

Algunas cámaras baratas que solo funcionan por la nube (sin RTSP) no se pueden ver con este programa;
en ese caso revisa si su app permite activar RTSP u ONVIF.
