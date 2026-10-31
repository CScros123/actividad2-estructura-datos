# Foco 0.57 — Órbita interactiva

La pausa conserva exactamente el archivo de vídeo de 0.56 y su reproducción en bucle. Encima se incorpora una órbita de aplicaciones reales con los iconos que Android proporciona. Las imágenes grabadas dentro del vídeo siguen siendo decorativas; las nuevas vistas nativas son las que responden a la interacción.

- Tocar un icono lo selecciona, lo sitúa en el centro y destaca su halo dorado. Un segundo toque sobre el seleccionado, o el botón «Abrir», inicia esa app.
- Arrastrar horizontalmente gira la órbita. Al tocarla se detiene su movimiento automático, manteniendo los objetivos fáciles de alcanzar; al soltar continúa sin cambiar bruscamente de ángulo.
- Se muestran hasta siete apps a la vez, con paginación para recorrer toda la lista instalada de alternativas. La app elegida en Mi plan aparece primero. No se agregan redes sociales ni apps de ocio bloqueadas a las alternativas.
- El centro mantiene su posición mientras las otras apps giran detrás con profundidad, brillo, partículas y un anillo de luz. Nombres y botones nativos permiten seleccionar y abrir sin depender de los efectos.

## Sincronización y consumo

La actualización de los iconos sale de `TextureView.onSurfaceTextureUpdated`, usando la posición real del vídeo y su duración. No hay un segundo temporizador ni un decodificador adicional. La órbita completa una vuelta por ciclo del vídeo; los parámetros geométricos conservan continuidad al volver al inicio. Al arrastrar se añade un desplazamiento independiente, manteniendo el vídeo reproduciéndose normalmente.

Ahorro de Foco, ahorro del sistema, reducir movimiento, pantalla apagada o retirada de la ventana detienen el vídeo y la animación automática. Las apps siguen siendo seleccionables y se pueden abrir con movimiento desactivado. Paint y shader del halo se reutilizan; los iconos de apps se cargan al mostrar su grupo, no en cada fotograma. Se conservan las exclusiones bancarias, reglas de bloqueo, registro semanal, música y permisos.

## Verificación

Pruebas de selección separada de apertura, paginación hasta el último grupo, coordenadas ligadas a la posición del vídeo, estabilidad del icono central, congelación al detenerse y giro mediante gestos. Suite de regresión del motor y de la interfaz; comprobación del APK y del vídeo incorporado.

Robolectric no verifica vídeo, composición y gestos sobre el hardware real del S23. Falta comprobar allí fluidez, precisión táctil y consumo de batería. No se presentan porcentajes de ahorro ni una tasa real de fotogramas medida en el teléfono.
