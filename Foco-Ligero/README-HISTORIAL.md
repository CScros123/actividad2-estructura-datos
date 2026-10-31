# Foco 0.58 · Brisa viva

Vídeo limpio, hojas visibles y audio temporal durante el bloqueo. [Cambios y pruebas](RELEASE-0.58.md).

# Foco 0.57 · Órbita interactiva

Apps reales con selección, apertura, giro y efectos sincronizados con el vídeo. [Cambios y verificación](RELEASE-0.57.md).

# Foco 0.56 · Pausa luminosa

Bloqueo con el vídeo correcto y efectos en movimiento. [Detalles y límites de verificación](RELEASE-0.56.md).

# Foco 0.55 · Ambiente vivo

Terraza animada, transiciones de interfaz y botón rápido de ahorro. [Cambios y verificación](RELEASE-0.55.md).

# Foco 0.54 · Brisa tranquila

Paisaje ambiental nativo por capas, ajustes de movimiento y ahorro. [Cambios y límites de verificación](RELEASE-0.54.md).

# Foco 0.53 · Día y noche

Foco mantiene el límite compartido de cinco minutos de ocio por ventana de una
hora, las alternativas instaladas, apuntes diarios, recordatorio opcional y registro
semanal. Esta versión añade las dos canciones enviadas por el usuario y paisajes
con tonos claros de día y casi negros de noche, según la hora local.

Consulta [los cambios, funcionamiento y límites de 0.53](RELEASE-0.53.md).
La descripción de las funciones conservadas y de los datos está en
[README-0.52.md](README-0.52.md).

La música se inicia al entrar en Foco o aparecer el bloqueo si no fue silenciada,
respeta llamadas y foco de audio, y se detiene al salir. Su servicio existe solo
durante la reproducción; no es un servicio permanente de vigilancia.

La protección del plan sigue activa dentro de Foco. Esta APK no impide de forma
inviolable desinstalarla, revocar Accesibilidad o cambiar el DNS de Android;
no está aprovisionada como administradora del dispositivo.

JDK 17, SDK 35 y Gradle 8.9:

```sh
./gradlew :core:test :app:testDebugUnitTest :app:lintDebug :app:assembleRelease
```

Verificación y capturas: `verification/release-053`. Las mediciones físicas de batería,
audio y compatibilidad en el Samsung del usuario siguen pendientes.
