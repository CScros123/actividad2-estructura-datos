# Foco 0.53 · Día, noche y tu música

## Canciones proporcionadas

Se integraron completas las dos grabaciones que envió el usuario:

- Morning Routine [Lofi Study Music] — Ghostrifter Official, unos 2 minutos.
- Colorful Flowers — Tokyo Music Walker, unos 4 minutos.

Los MP3 se convirtieron localmente a AAC de 112 kb/s dentro de M4A, sin recortar,
y se conservaron sus metadatos. Los archivos quedan dentro de la APK, sin streaming,
cuentas, permiso de Internet ni lectura del almacenamiento del teléfono. Los créditos
figuran en Ajustes. La grabación «Sendero» de versiones anteriores ya no se reproduce.

Al entrar en Foco se inicia la sesión si no fue silenciada y no hay otra música
reproduciéndose. Al mostrarse el bloqueo se solicita el foco de audio para acompañar
la pausa y desplazar el audio de la app de ocio. Una llamada impide el inicio.

De día empieza Morning Routine y de noche Colorful Flowers; cuando una canción
termina se reproduce la otra. Se mantiene un solo reproductor compartido por las
superficies visibles. La actualización del contador no vuelve a iniciar la pista.
Hay botones de pausa en Foco, en la pantalla de bloqueo y en el aviso de reproducción.
Silenciar se conserva para siguientes visitas hasta que el usuario pulse reproducir.

La música termina al salir de esas superficies, apagar la pantalla, perder el foco
de audio, recibir una llamada o retirar la capa del bloqueo, incluido al abrir un
banco. No se cambian el volumen global ni los colores o el brillo del teléfono.
La ganancia interna del reproductor es suave y el volumen multimedia sigue bajo
el control del usuario. Otro reproductor puede recuperar el foco: Foco no inicia
una lucha de reintentos ni vuelve a interrumpirlo cada minuto.

Para Android 15 se usa un servicio de primer plano de tipo mediaPlayback solamente
mientras esa música se reproduce. Se inicia el estado de primer plano antes de
pedir el foco de audio. Es START_NOT_STICKY y se detiene y libera el reproductor al
quedarse sin pantalla propietaria. No arranca música desde BOOT_COMPLETED ni añade
sondeos de fondo. Android puede impedir el inicio en determinadas condiciones;
en ese caso el bloqueo y el resto de Foco siguen funcionando, y el botón permite
intentar reproducir manualmente. Hace falta validación física en el teléfono.

Referencia: [foco de audio de Android](https://developer.android.com/media/optimize/audio-focus)
y [restricciones de servicios de primer plano](https://developer.android.com/develop/background-work/services/fgs/restrictions-bg-start).

## Paisajes y tonos por hora local

Se crearon dos paisajes estáticos inspirados en las referencias del usuario:
una terraza costera con nubes y luz diurna, y una costa nocturna con luna y luces
cálidas. La versión nocturna combina el paisaje con superficies casi negras,
verdes más profundos y texto tenue legible.

- Día: 06:00–18:59, según la zona horaria del teléfono.
- Noche: 19:00–05:59.

Se aplica a Foco y al bloqueo, independientemente del modo claro/oscuro del sistema.
La app abierta programa solo la próxima transición y escucha cambios puntuales de
hora/zona mientras está visible. Al volver del fondo comprueba el modo una vez.
El bloqueo utiliza su próxima frontera temporal. No hay GPS, consultas de amanecer,
animación del paisaje ni un bucle permanente para comprobar la luz.

Los PNG generados se copiaron a drawable-nodpi y sus originales se conservaron.
Las imágenes se decodifican para las vistas estáticas; no se convierten en vídeo.
Los archivos de audio y las imágenes aumentan el tamaño de la APK. Eso no equivale
a una medición de consumo energético: reproducir música sí usa recursos y puede
silenciarse. No se promete un porcentaje de ahorro.

## Alcance real de la protección

Se conserva la prohibición de ampliar tiempo, cambiar el plan o restaurar una copia
durante un bloqueo. No se ha usado Accesibilidad para ocultar Ajustes o impedir al
usuario revocar permisos del sistema. Las llamadas, Inicio y apps esenciales siguen
accesibles, y la araña no se ha reintroducido.

Una APK normal no puede garantizar que el usuario no desinstale Foco, desactive
Accesibilidad o cambie el DNS privado. Android ofrece controles administrativos
específicos, pero esta instalación no está aprovisionada como propietaria del
dispositivo ni tiene esos privilegios:

- [setUninstallBlocked](https://developer.android.com/reference/android/app/admin/DevicePolicyManager#setUninstallBlocked(android.content.ComponentName,%20java.lang.String,%20boolean))
  está reservado a propietarios/administración delegada con los privilegios indicados.
- [DISALLOW_CONFIG_PRIVATE_DNS](https://developer.android.com/reference/android/os/UserManager.html#DISALLOW_CONFIG_PRIVATE_DNS)
  requiere los roles o permisos de administración especificados por Android.

Esas políticas no equivalen por sí solas a una garantía universal sobre Accesibilidad
o sobre todos los Ajustes. No se configuró administración oculta, root, modo quiosco,
restablecimiento del teléfono ni un permiso que esta APK no tenga. El filtro privado
sigue configurándose manualmente en Android, como en 0.52.

## Datos y actualización

Paquete es.foco.next, código 53, firmado con el certificado de nuestras versiones
0.42–0.52. Puede actualizarse encima de 0.52 sin desinstalar. Se conservan plan,
plazos, notas, recordatorio y registros de siete días; no se reinician presupuestos.
Las fuentes no contienen claves de firma ni datos personales del teléfono.

Se conservan todas las funciones descritas en README-0.52.md. Los permisos nuevos
son FOREGROUND_SERVICE y FOREGROUND_SERVICE_MEDIA_PLAYBACK para la reproducción
acotada. No se añade Internet, GPS, micrófono, VPN o permiso de alarmas exactas.
Los resultados y capturas reales están en verification/release-053.
