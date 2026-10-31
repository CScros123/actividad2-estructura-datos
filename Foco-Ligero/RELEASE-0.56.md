# Foco 0.56 — Pausa luminosa

El bloqueo utiliza el segundo vídeo proporcionado (`gemini_generated_video_a46cddda.mp4`), con Jesús, luz de colores, destellos, marco dorado e iconos giratorios. El mar nocturno anterior no se incorpora.

El vídeo sí se reproduce: MediaPlayer local y TextureView, en bucle, sin su pista sonora para conservar el control de la música lo-fi. Se ha optimizado a H.264 540×960, 24 fps y aproximadamente 732 KiB, conservando los diez segundos. El texto, el tiempo restante y los botones de acción son vistas nativas sobre un velo de contraste; las acciones siguen siendo utilizables independientemente del vídeo. Los iconos que aparecen dentro del vídeo forman parte del efecto visual y no son controles nativos interactivos.

Ahorro de Foco, reducir movimiento, ahorro del sistema y animaciones desactivadas liberan el reproductor y muestran un fotograma. El modo normal reproduce la escena. El reproductor solo se prepara cuando la superficie está disponible y la pausa está visible con pantalla encendida. Se libera al retirar la ventana, apagar la pantalla, perder la superficie o recibir un error. Un error conserva el bloqueo y ofrece tocar el estado para reintentar; no hay un ciclo de reintentos.

No se cambia el presupuesto de cinco minutos, ni el fin de ventana, las exclusiones bancarias, notas, datos o permisos. Al actualizar el texto de minutos se reutiliza la ventana y el reproductor. Las alternativas se despliegan con un botón para dejar más espacio al paisaje.

## Verificación

Pruebas de política de movimiento y de no crear un decodificador sin una superficie visible, junto con la suite de bloqueo, música, alternativas, notas y recordatorios. Decodificación completa del archivo mediante ffmpeg y comprobación de empaquetado sin compresión para acceder por descriptor.

Las capturas Robolectric muestran la imagen de reserva, no prueban la reproducción del vídeo en hardware. Se necesita probar reproducción, composición de la ventana, fluidez y consumo en el Samsung real; no se presentan porcentajes de ahorro ni una tasa real de fotogramas medida en el teléfono.
