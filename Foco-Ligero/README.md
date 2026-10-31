# Foco 0.58 — Proyecto final

Aplicación Android nativa de bienestar digital. Paquete `es.foco.next`, versión 58 (`0.58-brisa-viva`). Java 17; Android 8 o posterior (API 26), SDK de compilación y destino 35.

## Funciones

- Presupuesto compartido de **5 minutos de ocio por ventana de 60 minutos**. La ventana comienza al abrir una app observada. Al agotar el presupuesto se bloquean esas apps hasta el final de la ventana, sin ampliaciones dentro de Foco.
- Llamadas, Inicio, Ajustes, WhatsApp y bancos reconocidos quedan disponibles. Se pueden elegir otras apps esenciales.
- Alternativas instaladas de lectura, educación y organización. La pausa muestra sus iconos reales en una órbita: seleccionar, girar con gestos, paginar y abrir. Las redes sociales no se recomiendan como libros.
- Interfaz de día y noche según hora local, terraza, vegetación y hojas flotantes delante del panel, con máscaras para no cubrir textos ni controles.
- Pausa con el vídeo limpio proporcionado por el usuario, local y en bucle. Los iconos interactivos se dibujan encima y se sincronizan con sus fotogramas.
- Música local: Morning Routine (Ghostrifter Official) y Colorful Flowers (Tokyo Music Walker). Durante el bloqueo solicita foco de audio temporal exclusivo, conservando el volumen del teléfono. La respuesta de otras apps depende de Android y de esas apps.
- Botón de ahorro y reducción de movimiento. Los efectos se detienen al salir, apagar la pantalla o activar las preferencias correspondientes. La música no se mantiene al apagar la pantalla.
- Apuntes diarios, historial y exportación TXT; recordatorio diario opcional; exportación CSV de siete días y copia JSON del plan y tiempos. Las notas se exportan por separado.
- Sin cuenta, telemetría, servicio VPN ni filtro DNS propio. Se ofrece abrir el ajuste de DNS privado de Android con el proveedor externo indicado en la interfaz.

## Arquitectura

- `core/src/main/java`: reglas puras Java, presupuesto, ventanas, plazos y clasificación explícita de alternativas.
- `app/src/main/java/es/foco/next/data`: persistencia del plan, tiempos y apuntes; migración de versiones anteriores.
- `app/src/main/java/es/foco/next/runtime`: servicio de Accesibilidad, exclusiones bancarias, llamadas, recordatorios y audio.
- `app/src/main/java/es/foco/next/ui`: actividad principal, pausa, órbita, paisaje, hojas y reproductor del vídeo.
- `app/src/main/res`: recursos visuales y archivos multimedia locales.
- `core/src/test` y `app/src/test`: pruebas JUnit y Robolectric.
- `verification/release-058`: resultados, informes y capturas de prueba de la entrega final.

Accesibilidad observa cambios de ventana para identificar la app activa. No lee mensajes, vídeos, páginas ni el contenido de la pantalla. El motor usa intervalos observados; no calcula productividad ni clasifica vídeos virales individualmente. Los plazos usan reloj monotónico dentro del mismo arranque y respaldo de hora civil después de reiniciar.

## Compilar en Android Studio

1. Instalar Android Studio con JDK 17, Android SDK 35 y Build Tools 35.0.0.
2. Abrir esta carpeta como proyecto Gradle. El wrapper usa Gradle 8.9.
3. Configurar la ruta del SDK desde Android Studio o crear un `local.properties` propio con `sdk.dir=/ruta/al/android-sdk`.
4. Sincronizar dependencias; la primera compilación necesita Internet para descargarlas.

```sh
./gradlew :core:test :app:testDebugUnitTest :app:lintDebug :app:assembleDebug
./gradlew :app:assembleRelease
```

En Windows, usar `gradlew.bat`. La APK debug aparece en `app/build/outputs/apk/debug/`. Sin configuración de firma, release genera una APK sin firmar en `app/build/outputs/apk/release/`.

## Firma y actualizaciones

El proyecto admite estas variables de entorno para firmar release:

- `FOCO_KEYSTORE`: ruta a tu almacén de claves.
- `FOCO_STORE_PASSWORD`: contraseña del almacén.
- `FOCO_KEY_ALIAS`: alias; por defecto `foco`.
- `FOCO_KEY_PASSWORD`: contraseña de la clave; por defecto usa la del almacén.

No guardar contraseñas en Git ni en archivos públicos. **Las claves usadas en las APK entregadas no se incluyen.** Una APK que firmes con una clave distinta no puede actualizar directamente una instalación anterior firmada con la clave de este entorno. Antes de desinstalar, exporta el plan y tiempos, y los apuntes TXT por separado. Conserva tu propia clave para futuras actualizaciones.

## Uso y permisos

Instalar y activar el servicio Foco desde Accesibilidad. Elegir apps de ocio, una intención y una alternativa en Mi plan. Permitir notificaciones si se quieren recordatorios y controles visibles de música. El audio usa el servicio foreground de reproducción ya declarado.

Permisos declarados: estado de red, notificaciones, recepción de reinicio y servicio foreground de reproducción multimedia. No se solicita Internet, micrófono, ubicación, administrador del dispositivo, VPN ni cambio del volumen global.

Android conserva el control de Accesibilidad, DNS, Ajustes y desinstalación. La protección del plan dentro de Foco no impide que su propietario revoque permisos o desinstale la app.

## Datos y exportación

Los datos se almacenan localmente en el espacio privado de Android. El plan y tiempos usan `focus-lite-v1.json`; los apuntes y preferencias se almacenan aparte. No editar el archivo de estado manualmente durante una sesión. La migración conserva el archivo antiguo si existe.

CSV: siete fechas, tiempos observados, aperturas de alternativas y eventos de presupuesto. No contiene mensajes ni contenido de otras apps. La copia del plan no incluye los apuntes: usa también su exportación TXT. Las preferencias visuales, de música y recordatorio tampoco deben asumirse incluidas en la copia del plan.

## Verificación y límites

Entrega 0.58: **89 pruebas aprobadas (32 core y 57 Android), cero errores lint**, firma y archivo de vídeo comprobados dentro del APK. Las capturas Robolectric no prueban vídeo o interacción en hardware real.

Falta medir batería y fluidez y comprobar la respuesta al foco de audio de cada app en un Samsung real. No se promete silenciar apps que ignoren el foco de audio ni un porcentaje de ahorro energético. Suspensiones largas o cierres abruptos pueden perder parte del último intervalo observado; se evita inventar tiempo de uso.

## Recursos e historial

El vídeo y las canciones fueron proporcionados por el usuario; los paisajes se generaron durante el proyecto. Los recursos se incluyen para reproducir la compilación. Antes de redistribuirlos, comprueba las licencias y atribuciones de sus autores. No se establece una licencia de código abierto por incluir este README.

`RELEASE-0.53.md` a `RELEASE-0.58.md` documentan cada cambio reciente. `README-HISTORIAL.md`, `README-0.51.md` y `README-0.52.md` conservan la evolución y detalles anteriores. La versión final no contiene el antiguo sistema de arañas ni la VPN de la rama original.
