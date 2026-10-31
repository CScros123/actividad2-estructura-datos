# Foco Ligero 0.51

Cinco minutos de ocio por ventana de una hora. Una interfaz tranquila que invita
a leer, aprender, crear o descansar. Sin cuentas, telemetría, animación permanente
ni VPN local. Android nativo, Java 17, API mínima 26, destino 35.

## Cómo funciona

En **Mi plan**, elige tus apps de ocio y un siguiente paso concreto, con una app
opcional para empezarlo. El presupuesto de cinco minutos se comparte entre todas
las apps elegidas. La primera apertura inicia una ventana de sesenta minutos:
por ejemplo, de 14:20 a 15:20. Si gastas los cinco minutos a las 14:25, esas apps
quedan bloqueadas hasta las 15:20. Salir de ellas o abrir otra no reinicia la hora.
Al terminar, la próxima apertura comienza una ventana nueva. No es una cuota por
hora de reloj ni una pausa fija de cinco minutos.

Hay un aviso discreto tras cuatro minutos y una pantalla de pausa al agotar cinco.
No hay ampliaciones ni cambios del plan desde Foco mientras dura el bloqueo.
Inicio, Ajustes, Foco, llamadas, WhatsApp y bancos reconocidos permanecen disponibles;
puedes añadir otras apps esenciales. Se detecta Banco General por paquete o nombre
instalado, con las limitaciones habituales de esa heurística. Android conserva el
control del permiso de Accesibilidad y la desinstalación: no es un bloqueo inviolable.
No se usa administración del dispositivo ni se obstaculizan las llamadas.

## Elegir algo agradable

La pantalla principal y la pausa presentan alternativas instaladas para lectura,
aprendizaje y creación. La clasificación usa nombres de paquete conocidos y la
categoría declarada por las apps; no consulta contenido personal. Puedes escoger tu
alternativa manualmente. No se atribuye productividad, aprendizaje ni valor moral
a un tiempo de uso. Descansar sin pantalla también es una salida válida.

El paisaje, las nubes y la hoja son vectores estáticos. «Sendero» es una pequeña
pieza instrumental lo-fi original, generada con síntesis sin muestras de terceros.
Se reproduce solo al pedirlo y se detiene al salir de Foco o perder el foco de audio.
No hay streaming ni servicio de música de fondo. La síntesis reproducible está en
`scripts/make_soundscape.py` (requiere numpy y ffmpeg en el equipo de desarrollo).

## Siete días para revisar tus decisiones

**Hoy → Tu semana, sin juicios → Exportar mis 7 días · CSV** guarda un archivo
que puedes abrir en Excel, Sheets o una app de hojas de cálculo. También se incluye
en la copia de seguridad. El registro se conserva localmente durante hoy y los
seis días anteriores; no se reconstruye retrospectivamente una semana desconocida.

Contiene agregados por fecha y app:

- segundos observados en apps de ocio;
- segundos observados en la alternativa elegida;
- número de aperturas de alternativas desde botones de Foco;
- número de ventanas cuyo presupuesto se agotó.

Una apertura es una elección desde Foco, no una prueba de haber leído o terminado
algo. El tiempo de alternativas distintas a la elegida no se monitoriza. No se
guardan mensajes, búsquedas, direcciones web, vídeos ni consultas DNS. El CSV no
es una cronología exacta ni una medición de cortisol, dopamina o salud cerebral.

## Filtro web sin VPN de Foco

En **Ajustes → Adultos, anuncios y rastreadores**, copia `family.adguard-dns.com`
y abre DNS privado de Android. Debes guardar ese nombre de host en los ajustes
del teléfono. Foco no puede activarlo silenciosamente ni sustituir tus ajustes.

[AdGuard documenta](https://adguard-dns.io/en/public-dns.html) este servidor familiar
para adultos, anuncios y rastreadores. Es un proveedor externo que resuelve las
consultas DNS del sistema; no significa que Foco controle o registre tu red.
Afecta a todo el teléfono, incluidos los bancos. No ofrece exclusiones por app
ni elimina todos los anuncios o el contenido dentro de redes sociales. DNS propio,
IP directas y determinados navegadores pueden evitarlo. Si un sitio deja de funcionar,
revisa el DNS privado en Android.

Si la VPN anterior estaba marcada como permanente con «Bloquear conexiones sin VPN»,
quita esa asignación en los ajustes VPN antes de usar esta versión sin servicio VPN.
La app incluye el acceso a esos ajustes cuando detecta la configuración antigua.

## Qué se eliminó y por qué

Se revisaron los documentos y fuentes originales de las versiones 0.31 a 0.50.
El origen ya era medir uso y presentar pausas. Las versiones posteriores incorporaron
múltiples motores visuales, reglas por horario, atenuación, audio de fricción,
continuaciones, VPN, listas de dominios, recuperación de red y registros DNS.

Esta entrega tiene 17 archivos Java de producción frente a 59 en 0.50. Se han
retirado del nuevo proyecto y APK los motores visuales anteriores, el enjambre,
renderizadores animados, el sondeo de 1,5 segundos, la lectura de barras de URL,
reglas/productividad por recompensas, audio de fricción, modificación de volumen,
UsageStatsRecovery, VPN/DNS, actualizaciones de listas, alarmas y receptores de arranque.
Se mantiene el proyecto anterior intacto fuera de esta entrega para revisión.

El tamaño del código por sí solo no demuestra ahorro de batería. El cambio relevante
es eliminar trabajo continuo: solo se reciben cambios de ventana, se programa el
siguiente umbral y se guarda un punto de control una vez por minuto mientras hay uso
observado. En pausa visible se actualiza el texto una vez por minuto sin reescribir
un estado sin cambios. Fuera de apps observadas, y con pantalla apagada, no queda
un ciclo de sondeo. La interfaz actualiza su contador solo mientras está visible.
No hay `Choreographer`, preferencia de 120 Hz ni permisos de Internet, vibración,
notificaciones, modificar audio o ejecutar servicios de red en primer plano.

La música optativa sí utiliza el decodificador mientras suena. Las instrucciones
oficiales de [Android sobre trabajo y batería](https://developer.android.com/develop/background-work/background-tasks/optimize-battery)
y los [tipos de eventos de Accesibilidad](https://developer.android.com/reference/android/accessibilityservice/AccessibilityServiceInfo)
sirvieron de referencia. Ningún código de los repositorios sugeridos se copió.
No había una skill disponible específica para diseño de apps Android; la interfaz
se implementó directamente con componentes nativos.

## Precisión y persistencia

Solo se carga el intervalo que empezó con una app conocida, pantalla interactiva
y sin llamada. Cambiar a un banco retira primero la superposición. Los intervalos
se cierran al cambiar de app, apagar la pantalla o llegar al próximo umbral.
Un intervalo suspendido más de 65 segundos se descarta para no inventar uso.
Tras una terminación abrupta puede perderse hasta aproximadamente un minuto reciente;
no se reconstruye tiempo ausente como si se hubiera observado.

Los plazos usan reloj monotónico en el mismo arranque y reloj de pared tras reinicio.
El cambio de fecha no restablece el presupuesto. Cambiar manualmente el reloj después
de reiniciar o desactivar permisos puede afectar al comportamiento; Foco no controla
el sistema. Pantalla dividida, PiP, particularidades de Samsung y llamadas deben
validarse también físicamente; las pruebas automatizadas no equivalen a esa validación.

## Actualización y datos anteriores

Paquete `es.foco.next`, código 51. La entrega está firmada con el mismo certificado
que nuestras versiones 0.42–0.50; se puede instalar encima sin desinstalar. Una clave
original de una versión anterior distinta puede impedir esa actualización.

El primer inicio migra apps elegidas, excepciones sensibles, alternativa y totales
diarios desde `focus-state-v1.json` y deja ese archivo original intacto. Las redes
conocidas se incorporan si antes estaba activo su seguimiento automático. Los ciclos
visuales y reglas antiguos no se reinterpretan como un nuevo bloqueo: se empieza
un presupuesto nuevo de cinco minutos. Se guarda en `focus-lite-v1.json` con
escritura atómica. Si un archivo no es legible, se conserva y se informa del fallo;
no se borra ni reinicia silenciosamente.

Las copias de Foco Ligero usan formato 51. Exportar está permitido durante un bloqueo;
restaurar una copia o cambiar el plan no. Los cambios se verifican en el repositorio,
no solo ocultando botones. Las copias anteriores se conservan en su proyecto original.

## Compilar

JDK 17, Android SDK 35, Build Tools 35.0.0 y Gradle 8.9 (wrapper incluido):

```sh
./gradlew :core:test :app:testDebugUnitTest :app:lintDebug :app:assembleRelease
```

Robolectric necesita resolver sus dependencias de Android en el equipo de desarrollo.
Para una firma propia pueden usarse las variables `FOCO_KEYSTORE`,
`FOCO_STORE_PASSWORD`, `FOCO_KEY_ALIAS` y `FOCO_KEY_PASSWORD`. Las claves de firma no
forman parte del ZIP de fuentes. Los resultados ejecutados y capturas de la interfaz
real se encuentran en `verification/release-051`.
