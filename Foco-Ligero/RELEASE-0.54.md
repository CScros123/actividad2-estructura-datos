# Foco 0.54 — Brisa tranquila

La interfaz nativa incorpora un paisaje vectorial Canvas por capas independientes: cielo y nubes lentas, montañas, terraza, hierbas y hojas de borde. No es un vídeo ni una imagen animada como bloque. La fotografía de la terraza del encabezado se conserva como referencia visual; las capas ambientales se dibujan en código detrás de toda la navegación.

El mismo AmbientView permanece al cambiar de pestaña o desplazarse. El parallax solo afecta a las capas decorativas. Las hojas flotantes se limitan a los márgenes para proteger textos y controles. Las tarjetas son translúcidas, sin desenfoque de pantalla completa. Entrada discreta de contenido y respuesta al pulsar botones. El paisaje de pausa usa la misma escena con viento más lento y un velo para mantener legibilidad.

Ajustes > Ambiente visual ofrece naturaleza animada, viento suave/natural/intenso, hojas flotantes, profundidad, ahorro y reducir movimiento. Preferencias locales independientes del plan; se pueden cambiar durante el bloqueo sin modificar el presupuesto. No se añadieron permisos ni dependencias.

## Rendimiento y límites

Un único callback cada 33 ms como máximo por escena visible (30 fps; 66 ms/15 fps en ahorro o pausa). Sin bucle en segundo plano, sin invalidar el panel ni recalcular sus layouts en cada fotograma. Paint y Path reutilizados. Sin carga/decodificación por fotograma ni vídeo, sensores, red o bitmaps nuevos. La animación de la interfaz conserva la frecuencia normal de Android.

Se detiene al pausar la actividad, retirar la ventana, ocultarse, activar ahorro de batería del sistema o desactivar animaciones del sistema. Un observador de la escala de animación y un receptor de ahorro actualizan el estado sin sondeos; ambos se retiran al desmontar. Reducir movimiento y desactivar naturaleza dejan el fondo estático.

Pruebas automatizadas comprueban ciclo de vida, preferencia de movimiento, ahorro del sistema, identidad de la escena entre secciones y continuidad matemática del viento. La suite existente comprueba bloqueo, notas, música y alternativas. Capturas de vistas reales mediante Robolectric, no de un dispositivo físico.

No se ha medido consumo energético, CPU/GPU ni fluidez en un Galaxy S23 real. Para medir: comparar 10 minutos en Hoy con ambiente encendido/apagado con igual brillo y música; revisar Battery Historian/Perfetto y framestats, y comprobar que al salir no quedan callbacks del paisaje. No se promete un porcentaje de ahorro.

La lógica de presupuesto, bloqueo, estadísticas, recordatorios y música sigue siendo la de 0.53.
