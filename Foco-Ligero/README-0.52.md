# Foco 0.52 · Mi día

Una app Android nativa y local para reservar cinco minutos de ocio por cada
ventana de una hora y volver a una alternativa elegida: leer, aprender, crear
o descansar. Mantiene la estética verde, el paisaje estático y la música lo-fi
opcional de 0.51. La música se detiene al salir de Foco.

## Qué cambió

### Alternativas que corresponden a su función

Se corrigió un fallo de 0.51: la búsqueda parcial de `ebook` también encontraba
`Facebook`. Se eliminaron las coincidencias parciales y la confianza automática
en CATEGORY_PRODUCTIVITY. Ahora se usa un catálogo explícito de paquetes y
nombres completos conocidos. Las redes sociales quedan fuera de las sugerencias
y del selector de alternativa.

De las apps que mostró el usuario se reconocen, si están instaladas:

- Lectura: Google Play Libros, Nextory, El Libro Total y Biblia.
- Aprendizaje: Duolingo, además de herramientas conocidas como Khan Academy,
  AnkiDroid y Wikipedia si están instaladas.
- Crear y organizar: Keep, Samsung Notes, Calendario, Reminder, AppSheet,
  hojas de cálculo, ChatGPT, Claude y Gemini, entre otras herramientas conocidas.

No se instala ninguna app ni se carga una tienda, feed o recomendación remota.
Se muestran solo apps instaladas y no seleccionadas como ocio o sensibles. La
presencia de una herramienta no demuestra productividad; depende del uso que
el usuario quiera darle. Una alternativa manual no reconocida —por ejemplo,
IQDungeon— aparece en **Tu elección personal**, sin clasificarla automáticamente
como lectura o educación. Una alternativa social antigua se deja sin destino
para elegir otra; no se borran los registros ni se reinicia el presupuesto.

Las tarjetas priorizan notas, calendario y recordatorios dentro de organización.
El catálogo instalado se consulta una vez al entrar en Foco y se reutiliza mientras
la app está abierta; en la pantalla de pausa la caché tiene una validez máxima de
diez minutos. No se sondean las apps instaladas en segundo plano. El selector de
apps reutiliza una sola configuración y un solo detector bancario por apertura.

Los paquetes de [Nextory](https://play.google.com/store/apps/details?id=com.gtl.nextory)
y [El Libro Total](https://play.google.com/store/apps/details?id=com.syc.librototal.El_Libro_Total)
se comprobaron con sus fichas de distribución. No se copió código de terceros.

### Un apunte para cada día

En **Hoy → Una idea para hoy** puedes escribir y editar hasta 4000 caracteres.
El texto se guarda solo al pulsar **Guardar**. No hay escrituras con cada tecla,
editor enriquecido, sincronización ni supervisión del contenido.

**Mis apuntes · ver y exportar** permite volver a los días anteriores, editar,
eliminar y exportar el cuaderno completo en texto UTF-8. Se pueden conservar hasta
366 fechas; nunca se eliminan apuntes antiguos automáticamente. Si se alcanza el
límite, se pide exportar y eliminar alguno antes de añadir otra fecha.

Los apuntes se almacenan separados del estado del motor y de las preferencias del
recordatorio. Medir el uso y guardar puntos de control no carga ni reescribe los
apuntes. Se pueden escribir durante un bloqueo sin cambiar su vencimiento.

**La copia del plan y tiempos y el CSV semanal no incluyen el texto de los apuntes.**
Para guardar ese contenido, usa **Mis apuntes → Exportar .txt**. La separación
se indica también en los nombres y mensajes de la interfaz.

### Un recordatorio diario tranquilo

En la misma tarjeta, pulsa **Recordatorio diario · apagado**, elige una hora y
permite las notificaciones cuando Android lo solicite. Está apagado inicialmente.
Puedes cambiar la hora o desactivarlo desde ese botón.

Se programa una sola alarma inexacta `AlarmManager.RTC` de Android, con ventana
de quince minutos y sin despertar el dispositivo. No hay servicio, bucle de
consulta ni permiso de alarmas exactas. Android puede retrasarla por reposo o
ahorro de batería; es un recordatorio aproximado, no un despertador.

La notificación es silenciosa y como máximo una al día. No muestra el texto de los
apuntes ni necesita acceso a ellos. Al tocarla abre la sección del día en Foco.
Se recalcula tras reinicio, actualización o cambio de hora/zona horaria, solo si
está activada y hay permiso para notificar. Un evento retrasado de un día anterior
no genera una notificación antigua.

Se siguieron las referencias oficiales de Android sobre
[alarmas inexactas](https://developer.android.com/develop/background-work/services/alarms)
y [permiso de notificaciones](https://developer.android.com/develop/ui/compose/notifications/notification-permission).
Los permisos nuevos son POST_NOTIFICATIONS (solo se pide al activar) y
RECEIVE_BOOT_COMPLETED (para recuperar el aviso opcional). Sigue sin haber
permiso de Internet, VPN, UsageStats, vibración, modificar volumen o ejecutar un
servicio de red en primer plano.

## Lo que se conserva

- Cinco minutos acumulados compartidos entre las apps de ocio elegidas. La primera
  apertura inicia una ventana de sesenta minutos. Si se gastan en cinco minutos,
  esas apps quedan bloqueadas los cincuenta y cinco minutos restantes.
- No hay ampliaciones ni cambios/importación del plan durante un bloqueo.
  Los apuntes y el recordatorio son independientes de esa protección.
- Banco General y otros bancos reconocidos, llamadas, Inicio, Ajustes, Foco y
  WhatsApp siguen fuera de la intervención. La detección bancaria es una heurística
  y existe una lista manual para apps sensibles.
- Registro de siete días de uso observado y elecciones desde Foco, exportable en
  **Hoy → Tu semana, sin juicios → Exportar mis 7 días · CSV**.
- Filtro web opcional mediante el DNS privado de Android. En Ajustes puedes copiar
  `family.adguard-dns.com`. Debes guardarlo en el sistema; Foco no lo activa por sí
  solo. [AdGuard](https://adguard-dns.io/en/public-dns.html) proporciona ese filtro
  externo para adultos, anuncios y rastreadores. No filtra todo el contenido de
  redes sociales y algunos DNS propios pueden evitarlo.

Foco no mide aprendizaje, cortisol, dopamina ni salud cerebral. Los datos son una
referencia para revisar hábitos. Descansar también es una alternativa válida.
Android conserva el control de Accesibilidad y desinstalación; el bloqueo no es
inviolable. Si la VPN antigua era permanente, quita esa asignación y «Bloquear
conexiones sin VPN» al usar esta versión sin VPN local.

## Eficiencia y límites

No se recuperó la animación permanente, el sondeo cada 1,5 segundos ni la VPN de
las versiones anteriores. La intervención depende de cambios de ventana y del
siguiente límite temporal. Solo se actualiza un contador mientras Hoy está visible.
La música utiliza el decodificador solo al pedirla, dentro de Foco. El nuevo aviso
es una operación diaria opcional; escribir notas no produce trabajo de fondo.

Los intervalos no certificados o suspendidos más de 65 segundos se descartan.
Una terminación abrupta puede perder aproximadamente un minuto reciente; no se
reconstruye como uso observado un tiempo desconocido. Los plazos usan reloj
monotónico en el mismo arranque y reloj de pared tras reinicio.

No se han medido porcentajes de ahorro ni batería en un S23 Ultra físico.
Las pruebas automatizadas y la ausencia de trabajo continuo no sustituyen una
medición de siete días. La referencia anterior se conserva en README-0.51.md.

## Actualizar y compilar

Paquete `es.foco.next`, código 52, certificado igual al de nuestras versiones
0.42–0.51. La APK se instala encima de 0.51 sin desinstalar. Se conserva el formato
51 del plan y tiempos, las selecciones, plazos y registros existentes. Los apuntes
y el recordatorio tienen almacenamiento independiente. No se incluye ninguna
clave de firma en las fuentes.

JDK 17, Gradle 8.9, Android SDK 35 y Build Tools 35.0.0:

```sh
./gradlew :core:test :app:testDebugUnitTest :app:lintDebug :app:assembleRelease
```

Robolectric necesita resolver sus dependencias en el equipo de desarrollo.
La firma propia usa las variables FOCO_KEYSTORE, FOCO_STORE_PASSWORD,
FOCO_KEY_ALIAS y FOCO_KEY_PASSWORD. Las capturas y resultados ejecutados están en
`verification/release-052`.
