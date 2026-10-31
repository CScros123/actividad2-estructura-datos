# Foco · Launcher Bosque

Cambios sobre Foco 0.58, sin tocar la medición ni el bloqueo:

- **Launcher**: `MainActivity` declara `CATEGORY_HOME`. Después de instalar, pulsa Inicio y elige *Foco* como app de inicio predeterminada (Ajustes → Apps → Apps predeterminadas → App de inicio).
- **Pestaña Inicio** (`ui/LauncherHome.java`): reloj, fecha, tiempo de ocio restante, buscador y lista de todas las apps.
  - Las apps de ocio llevan la marca «Ocio» y, durante el bloqueo, salen atenuadas con «Descansando · vuelve en N min».
  - Mantén pulsada una app para marcarla o quitarla de ocio (igual que en Mi plan, respeta el bloqueo) o abrir su información.
  - Atrás siempre vuelve a Inicio.
- **Tema verde oscuro** día y noche (`values/colors.xml`, `values-night/colors.xml`), con el bosque animado y las hojas flotantes de siempre.

Compilar: `./gradlew assembleDebug` (requiere Android SDK 35). Las pruebas de color/pestañas en `CalmVisualTest` se actualizaron al nuevo tema.
