# Foco 0.58 — Vídeo limpio y brisa visible

Se sustituye el fondo por el archivo limpio 1017479.mp4 (16,08 s), sin los iconos ni el mensaje incrustados del vídeo anterior. Se conserva la escena completa, adaptada a H.264 540×960 y 24 fps, sin sonido. Los iconos, controles y texto de Foco siguen siendo reales y la órbita usa la duración del nuevo vídeo.

Las hojas pasan a una capa de primer plano en todas las secciones, por delante de tarjetas y del paisaje. Hay entre 10 y 22 hojas verdes con nervaduras, giro, caída y desplazamiento lateral, además de trazos de brisa. Se reutilizan Paint y Path y el reloj del paisaje o del vídeo: no se añade ningún temporizador. Las zonas de texto, botones y órbita se excluyen del dibujo mediante una máscara que se calcula al cambiar el layout o desplazarse, no en cada fotograma. La capa no recibe toques. El balanceo de la vegetación de la terraza también es más visible.

El audio de Foco solicita AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE mientras hay una pantalla de bloqueo activa, y devuelve o restaura el foco al salir. Si ya estaba sonando Foco, se ajusta el foco conservando el mismo reproductor. No se cambia el volumen del teléfono ni se añaden permisos. Las llamadas conservan prioridad. Este mecanismo pide a las otras apps que interrumpan su audio; no es un control privilegiado para silenciar individualmente cualquier app que ignore las reglas de Android. No se mantiene reproducción al apagar la pantalla, según la aclaración del usuario.

Se respetan ahorro y reducir movimiento. No se cambia el presupuesto, exclusiones, registros ni notas. El vídeo y los efectos se liberan o detienen al salir; solo el audio solicitado por la superficie visible mantiene su servicio existente.

Pruebas nuevas: hojas en movimiento y zonas de controles transparentes; foco exclusivo durante el bloqueo, retorno al foco normal, mismo reproductor y volumen del sistema intacto. Se conservan las pruebas de reglas, órbita, vídeo, música, notas y recordatorios. Falta medir fluidez, consumo y la respuesta real del audio de las apps de ocio en Samsung.
