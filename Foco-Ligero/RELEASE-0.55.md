# Foco 0.55 — Ambiente vivo

La terraza del encabezado ahora dibuja hojas, cielo y reflejos sobre el agua en capas independientes. No desplaza la arquitectura completa. El fondo general refuerza la brisa y la velocidad de las nubes. La escena persiste al cambiar de pestaña.

Hoy, Mi plan y Ajustes tienen entrada de tarjetas al aparecer en pantalla y respuesta al cambiar de sección. Botones y tarjetas de apps alternativas responden al pulsar; los controles y textos no tienen animadores repetitivos. La nueva entrada solo se reproduce una vez por tarjeta creada, no cada vez que vuelve a la pantalla.

Un botón de Ahorro de energía permanece visible en todas las pestañas. Guarda la elección, detiene por completo el temporizador ambiental y desactiva las transiciones de la interfaz. No cambia el bloqueo, música, estadísticas, apuntes ni permisos. El interruptor equivalente en Ajustes permanece sincronizado. El ahorro del sistema y reducir movimiento también detienen el paisaje.

## Revisión y optimización

- Un único temporizador ambiental compartido por el fondo y la terraza, 30 fps como máximo (15 fps para la pantalla de pausa).
- La terraza solo se invalida mientras está visible; al salir del viewport no se redibuja cada fotograma.
- Las opciones visuales se leen al cambiar, no durante cada dibujo.
- Paint, Path y rectángulos reutilizados; sin vídeo, blur de pantalla completa, sensores ni trabajo de red.
- Las transiciones de tarjetas solo afectan alpha y translation, sin pedir layouts por fotograma.
- Se cancelan animaciones y callbacks al pausar y antes de sustituir una sección. Se retiran receptores y listeners al desmontar.
- En ahorro no hay callback ambiental pendiente. La app conserva medición, bloqueo y música.

Las pruebas nuevas comprueban botón rápido, persistencia al recrear la actividad, detención del ciclo y que el botón no cambie el presupuesto. Las pruebas anteriores cubren bloqueo, alternativas, música, notas y recordatorios.

No se han medido consumo energético, CPU/GPU ni frame times en un Galaxy físico. La reducción de trabajo se verifica en código y pruebas de ciclo de vida; no se promete un porcentaje de ahorro. Prueba recomendada: comparar diez minutos en Hoy con igual brillo y música, con ahorro activado y desactivado; revisar Perfetto/Battery Historian y confirmar ausencia de trabajo visual al salir.
