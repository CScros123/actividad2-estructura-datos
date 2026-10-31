import 'dart:math' as math;
import 'dart:ui' as ui;

import 'package:flutter/material.dart';
import 'package:flutter/scheduler.dart';

/// Fondo "Bosque": cielo verde, montañas en capas, niebla, nubes lentas,
/// pinos, hojas que caen y luciérnagas.
///
/// Rendimiento: el paisaje fijo (cielo, montañas, niebla, pinos) se dibuja
/// UNA vez en una imagen; cada fotograma solo pinta esa imagen y unos pocos
/// elementos pequeños, sin desenfoques. Va a 30 fps como máximo y se detiene
/// cuando el launcher no está visible.
class NatureBackground extends StatefulWidget {
  const NatureBackground({super.key, this.animate = true});

  final bool animate;

  @override
  State<NatureBackground> createState() => _NatureBackgroundState();
}

class _NatureBackgroundState extends State<NatureBackground>
    with SingleTickerProviderStateMixin, WidgetsBindingObserver {
  static const _frame = Duration(milliseconds: 33); // ~30 fps
  final ValueNotifier<double> _time = ValueNotifier(30);
  late final Ticker _ticker = createTicker(_onTick);
  Duration _last = Duration.zero;
  final _scene = _SceneCache();

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addObserver(this);
    if (widget.animate) _ticker.start();
  }

  void _onTick(Duration elapsed) {
    if (elapsed - _last < _frame) return;
    _last = elapsed;
    _time.value = 30 + elapsed.inMicroseconds / 1e6;
  }

  void _setRunning(bool run) {
    if (run && !_ticker.isActive) {
      _last = Duration.zero;
      _ticker.start();
    } else if (!run && _ticker.isActive) {
      _ticker.stop();
    }
  }

  @override
  void didUpdateWidget(covariant NatureBackground old) {
    super.didUpdateWidget(old);
    _setRunning(widget.animate);
  }

  @override
  void didChangeAppLifecycleState(AppLifecycleState state) {
    _setRunning(state == AppLifecycleState.resumed && widget.animate);
  }

  @override
  void dispose() {
    WidgetsBinding.instance.removeObserver(this);
    _ticker.dispose();
    _time.dispose();
    _scene.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final reduce = MediaQuery.maybeDisableAnimationsOf(context) ?? false;
    if (reduce) _setRunning(false);
    final ratio = MediaQuery.devicePixelRatioOf(context);
    return RepaintBoundary(
      child: CustomPaint(
        painter: _ForestPainter(_time, _scene, ratio, moving: !reduce && widget.animate),
        size: Size.infinite,
      ),
    );
  }
}

/// Imágenes que se generan una sola vez por tamaño de pantalla.
class _SceneCache {
  Size? size;
  ui.Image? landscape;
  ui.Image? cloud;

  void dispose() {
    landscape?.dispose();
    cloud?.dispose();
  }
}

class _ForestPainter extends CustomPainter {
  _ForestPainter(this.time, this.cache, this.ratio, {required this.moving})
      : super(repaint: time);

  final ValueNotifier<double> time;
  final _SceneCache cache;
  final double ratio;
  final bool moving;

  static const _skyTop = Color(0xFF0A1F17);
  static const _skyMid = Color(0xFF173B2B);
  static const _skyLow = Color(0xFF3F6E52);
  static const _glow = Color(0xFFCFE8C0);
  static const _far = Color(0xFF365F4B);
  static const _mid = Color(0xFF244634);
  static const _near = Color(0xFF173225);
  static const _front = Color(0xFF0D2118);

  static final Path _leaf = Path()
    ..moveTo(-7, 0)
    ..cubicTo(-2, -9, 7, -7, 9, -3)
    ..cubicTo(6, 6, -3, 7, -7, 0)
    ..close();
  static const _leafColors = [Color(0xCC6FA866), Color(0xCC8DBF6A), Color(0xCC4E8C5A)];

  // Posiciones fijas de las luciérnagas (no se recalculan cada fotograma).
  static final List<List<double>> _flies = () {
    final r = math.Random(7);
    return List.generate(10, (_) => [r.nextDouble(), .62 + r.nextDouble() * .3, r.nextDouble() * 6.28]);
  }();

  // ---------- Paisaje fijo, dibujado una sola vez ----------
  static double _heightAt(Size s, double x, double base, double amp, double seed, int detail) {
    final u = x / s.width;
    var y = 0.0;
    for (var k = 1; k <= detail; k++) {
      y += math.sin(u * math.pi * (k * 1.7) + seed * k) / k;
    }
    return s.height * (base - amp * y);
  }

  static Path _ridge(Size s, double base, double amp, double seed, int detail) {
    final p = Path()..moveTo(0, s.height);
    for (var x = 0.0; x <= s.width + 8; x += 8) {
      p.lineTo(x, _heightAt(s, x, base, amp, seed, detail));
    }
    return p
      ..lineTo(s.width, s.height)
      ..close();
  }

  static void _mist(Canvas c, Size s, double y) {
    final rect = Rect.fromLTWH(0, y - s.height * .05, s.width, s.height * .1);
    c.drawRect(
      rect,
      Paint()
        ..shader = LinearGradient(
          begin: Alignment.topCenter,
          end: Alignment.bottomCenter,
          colors: [Colors.white.withAlpha(0), Colors.white.withAlpha(22), Colors.white.withAlpha(0)],
        ).createShader(rect),
    );
  }

  ui.Image _paintLandscape(Size s) {
    final rec = ui.PictureRecorder();
    final c = Canvas(rec);
    c.scale(ratio);
    final w = s.width, h = s.height;
    final paint = Paint()..isAntiAlias = true;

    paint.shader = const LinearGradient(
      begin: Alignment.topCenter,
      end: Alignment.bottomCenter,
      colors: [_skyTop, _skyMid, _skyLow],
      stops: [0, .45, .62],
    ).createShader(Offset.zero & s);
    c.drawRect(Offset.zero & s, paint);
    paint.shader = null;

    paint
      ..color = _glow.withAlpha(30)
      ..maskFilter = const MaskFilter.blur(BlurStyle.normal, 40);
    c.drawCircle(Offset(w * .72, h * .36), w * .16, paint);
    paint
      ..color = _glow.withAlpha(60)
      ..maskFilter = const MaskFilter.blur(BlurStyle.normal, 8);
    c.drawCircle(Offset(w * .72, h * .36), w * .055, paint);
    paint.maskFilter = null;

    paint.color = _far;
    c.drawPath(_ridge(s, .50, .07, 1.3, 5), paint);
    _mist(c, s, h * .55);
    paint.color = _mid;
    c.drawPath(_ridge(s, .60, .06, 4.1, 4), paint);
    _mist(c, s, h * .66);
    paint.color = _near;
    c.drawPath(_ridge(s, .70, .05, 7.7, 3), paint);
    paint.color = _front;
    c.drawPath(_ridge(s, .82, .035, 2.2, 2), paint);

    final pines = Path();
    final rnd = math.Random(42);
    for (var i = 0; i < 26; i++) {
      final x = rnd.nextDouble() * w;
      final ground = _heightAt(s, x, .82, .035, 2.2, 2);
      final ph = h * (.05 + rnd.nextDouble() * .06);
      final pw = ph * .42;
      for (var k = 0; k < 3; k++) {
        final top = ground - ph + k * ph * .28;
        final half = pw * (.45 + k * .28);
        pines
          ..moveTo(x, top)
          ..lineTo(x - half, top + ph * .42)
          ..lineTo(x + half, top + ph * .42)
          ..close();
      }
      pines.addRect(Rect.fromLTWH(x - pw * .06, ground - ph * .1, pw * .12, ph * .14));
    }
    c.drawPath(pines, paint);
    c.drawRect(Rect.fromLTWH(0, h * .9, w, h * .1), paint);

    return rec.endRecording().toImageSync((w * ratio).ceil(), (h * ratio).ceil());
  }

  ui.Image _paintCloud() {
    // Una nube suave pre-desenfocada; luego solo se copia y desplaza.
    final rec = ui.PictureRecorder();
    final c = Canvas(rec);
    final p = Paint()
      ..color = Colors.white
      ..maskFilter = const MaskFilter.blur(BlurStyle.normal, 16);
    c.drawOval(const Rect.fromLTWH(40, 40, 180, 34), p);
    c.drawOval(const Rect.fromLTWH(95, 26, 110, 30), p);
    return rec.endRecording().toImageSync(260, 110);
  }

  void _ensure(Size s) {
    if (cache.size == s && cache.landscape != null) return;
    cache.landscape?.dispose();
    cache.size = s;
    cache.landscape = _paintLandscape(s);
    cache.cloud ??= _paintCloud();
  }

  // ---------- Cada fotograma: imagen + pocos elementos ----------
  @override
  void paint(Canvas canvas, Size size) {
    if (size.isEmpty) return;
    _ensure(size);
    final w = size.width, h = size.height;
    final t = time.value;
    final paint = Paint()..isAntiAlias = true;

    canvas.drawImageRect(
      cache.landscape!,
      Rect.fromLTWH(0, 0, cache.landscape!.width.toDouble(), cache.landscape!.height.toDouble()),
      Offset.zero & size,
      Paint()..filterQuality = FilterQuality.low,
    );

    // Nubes: la misma imagen, desplazándose muy despacio, detrás de las montañas
    // lejanas no hace falta: van en el cielo, arriba.
    final cloud = cache.cloud!;
    for (var i = 0; i < 4; i++) {
      final x = (i * w * .37 + t * (3.0 + i * 1.2)) % (w + 260) - 260;
      final y = h * (.10 + i * .07) - 55;
      paint.color = Color.fromARGB(18 + i * 5, 255, 255, 255);
      canvas.drawImage(cloud, Offset(x, y), paint);
    }
    paint.color = const Color(0xFFFFFFFF);

    if (!moving) return;

    // Luciérnagas: halo + núcleo, sin desenfoque en tiempo real.
    for (final f in _flies) {
      final x = f[0] * w + math.sin(t * .15 + f[2]) * 18;
      final y = f[1] * h + math.cos(t * .11 + f[2]) * 12;
      final a = (math.sin(t * .6 + f[2]) + 1) / 2;
      paint.color = Color.fromARGB((a * 40).round(), 230, 245, 168);
      canvas.drawCircle(Offset(x, y), 6, paint);
      paint.color = Color.fromARGB((a * 190).round(), 236, 250, 190);
      canvas.drawCircle(Offset(x, y), 1.8, paint);
    }

    // Hojas que caen despacio.
    for (var i = 0; i < 6; i++) {
      final fall = 9.0 + i % 3 * 3;
      final y = (t * fall + i * (h + 80) / 6) % (h + 80) - 40;
      final x = (i * .618 * w + t * 4) % (w + 40) - 20 + math.sin(t * .3 + i) * 26;
      canvas.save();
      canvas.translate(x, y);
      canvas.rotate(t * .25 + i + math.sin(t * .4 + i) * .6);
      canvas.scale(1 + i % 2 * .3);
      paint.color = _leafColors[i % 3];
      canvas.drawPath(_leaf, paint);
      canvas.restore();
    }
  }

  @override
  bool shouldRepaint(covariant _ForestPainter old) =>
      old.moving != moving || old.ratio != ratio || old.cache != cache;
}
