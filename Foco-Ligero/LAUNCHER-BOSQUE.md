# Foco · Launcher Bosque

Cambios sobre Foco 0.58, sin tocar la medición ni el bloqueo:

- **Launcher**: `MainActivity` declara `CATEGORY_HOME`. Después de instalar, pulsa Inicio y elige *Foco* como app de inicio predeterminada (Ajustes → Apps → Apps predeterminadas → App de inicio).
- **Inicio minimalista**: reloj, fecha, una línea con tu tiempo de ocio y hasta 7 favoritas en texto. Sin barra ni botones.
  - Desliza hacia arriba (o toca «todas las apps») para abrir el cajón: buscador y apps en texto. Desliza hacia abajo para volver.
  - Mantén pulsada una app: añadir/quitar de Inicio, marcar como ocio o ver su información.
  - Mantén pulsado el reloj o un espacio vacío para abrir el menú de Foco: Hoy, Mi plan y Ajustes.
  - Las apps bloqueadas se ven atenuadas con «· descansando». Atrás siempre vuelve a Inicio.
- **Launcher predeterminado**: la primera vez Foco pide a Android ser la pantalla de inicio (y lo ofrece de nuevo en Inicio y en el menú).
- **Tema verde oscuro** día y noche, con menos hojas, más lentas, y fondo fijo para que todo vaya fluido.

Compilar: `./gradlew assembleDebug` (requiere Android SDK 35 y Java 17/21).
