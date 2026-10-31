import 'dart:math' as math;

import 'package:flutter/material.dart';

/// Fondo "Bosque": cielo verde, montañas en capas, niebla, nubes lentas,
/// pinos, hojas que caen y luciérnagas. Todo se dibuja en un solo
/// CustomPainter y se pausa cuando el launcher no está visible.
class NatureBackground extends StatefulWidget {
  const NatureBackground({super.key, this.animate = true});

  final bool animate;

  @override
  State<NatureBackground> createState() => _NatureBackgroundState();
}

class _NatureBackgroundState extends State<NatureBackground>
    with SingleTickerProviderStateMixin, WidgetsBindingObserver {
  // Un ciclo largo: el movimiento es lento y tranquilo.
  static const _cycle = Duration(minutes: 4);
  late final AnimationController _clock =
      AnimationController(vsync: this, duration: _cycle);

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addObserver(this);
    if (widget.animate) _clock.repeat();
  }

  @override
  void didUpdateWidget(covariant NatureBackground old) {
    super.didUpdateWidget(old);
    if (widget.animate && !_clock.isAnimating) _clock.repeat();
    if (!widget.animate) _clock.stop();
  }

  @override
  void didChangeAppLifecycleState(AppLifecycleState state) {
    // Sin animación en segundo plano: ahorra batería.
    if (state == AppLifecycleState.resumed && widget.animate) {
      _clock.repeat();
    } else if (state != AppLifecycleState.resumed) {
      _clock.stop();
    }
  }

  @override
  void dispose() {
    WidgetsBinding.instance.removeObserver(this);
    _clock.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final reduce = MediaQuery.maybeDisableAnimationsOf(context) ?? false;
    return RepaintBoundary(
      child: CustomPaint(
        painter: _ForestPainter(
          _clock,
          moving: widget.animate && !reduce,
        ),
        size: Size.infinite,
      ),
    );
  }
}

class _ForestPainter extends CustomPainter {
  _ForestPainter(this.clock, {required this.moving}) : super(repaint: clock);

  final AnimationController clock;
  final bool moving;

  // Paleta Bosque.
  static const _skyTop = Color(0xFF0A1F17);
  static const _skyMid = Color(0xFF173B2B);
  static const _skyLow = Color(0xFF3F6E52);
  static const _glow = Color(0xFFCFE8C0);
  static const _far = Color(0xFF365F4B);
  static const _mid = Color(0xFF244634);
  static const _near = Color(0xFF173225);
  static const _front = Color(0xFF0D2118);

  Size? _cached;
  late Path _farRidge, _midRidge, _nearRidge, _frontHill, _pines;

  double get _t => moving ? clock.value * 240.0 : 30.0; // segundos

  void _build(Size s) {
    if (_cached == s) return;
    _cached = s;
    _farRidge = _ridge(s, base: .50, amp: .07, seed: 1.3, detail: 5);
    _midRidge = _ridge(s, base: .60, amp: .06, seed: 4.1, detail: 4);
    _nearRidge = _ridge(s, base: .70, amp: .05, seed: 7.7, detail: 3);
    _frontHill = _ridge(s, base: .82, amp: .035, seed: 2.2, detail: 2);
    _pines = Path();
    final rnd = math.Random(42);
    for (var i = 0; i < 26; i++) {
      final x = rnd.nextDouble() * s.width;
      final ground = _heightAt(s, x, base: .82, amp: .035, seed: 2.2, detail: 2);
      final h = s.height * (.05 + rnd.nextDouble() * .06);
      final w = h * .42;
      // Pino de tres pisos.
      for (var k = 0; k < 3; k++) {
        final top = ground - h + k * h * .28;
        final half = w * (.45 + k * .28);
        _pines
          ..moveTo(x, top)
          ..lineTo(x - half, top + h * .42)
          ..lineTo(x + half, top + h * .42)
          ..close();
      }
      _pines.addRect(Rect.fromLTWH(x - w * .06, ground - h * .1, w * .12, h * .14));
    }
  }

  static double _heightAt(Size s, double x,
      {required double base,
      required double amp,
      required double seed,
      required int detail}) {
    final u = x / s.width;
    var y = 0.0;
    for (var k = 1; k <= detail; k++) {
      y += math.sin(u * math.pi * (k * 1.7) + seed * k) / k;
    }
    return s.height * (base - amp * y);
  }

  static Path _ridge(Size s,
      {required double base,
      required double amp,
      required double seed,
      required int detail}) {
    final p = Path()..moveTo(0, s.height);
    for (var x = 0.0; x <= s.width + 8; x += 8) {
      p.lineTo(x, _heightAt(s, x, base: base, amp: amp, seed: seed, detail: detail));
    }
    p
      ..lineTo(s.width, s.height)
      ..close();
    return p;
  }

  @override
  void paint(Canvas canvas, Size size) {
    if (size.isEmpty) return;
    _build(size);
    final t = _t;
    final paint = Paint()..isAntiAlias = true;
    final w = size.width, h = size.height;

    // Cielo.
    paint.shader = const LinearGradient(
      begin: Alignment.topCenter,
      end: Alignment.bottomCenter,
      colors: [_skyTop, _skyMid, _skyLow],
      stops: [0, .45, .62],
    ).createShader(Offset.zero & size);
    canvas.drawRect(Offset.zero & size, paint);
    paint.shader = null;

    // Luna/sol muy suave detrás de las montañas.
    paint
      ..color = _glow.withAlpha(30)
      ..maskFilter = const MaskFilter.blur(BlurStyle.normal, 40);
    canvas.drawCircle(Offset(w * .72, h * .36), w * .16, paint);
    paint
      ..color = _glow.withAlpha(60)
      ..maskFilter = const MaskFilter.blur(BlurStyle.normal, 8);
    canvas.drawCircle(Offset(w * .72, h * .36), w * .055, paint);

    // Nubes lentas.
    paint.maskFilter = const MaskFilter.blur(BlurStyle.normal, 18);
    for (var i = 0; i < 4; i++) {
      final speed = 3.0 + i * 1.2;
      final x = (i * w * .37 + t * speed) % (w + 260) - 130;
      final y = h * (.12 + i * .07);
      paint.color = Colors.white.withAlpha(16 + i * 4);
      canvas.drawOval(Rect.fromCenter(center: Offset(x, y), width: 180, height: 34), paint);
      canvas.drawOval(Rect.fromCenter(center: Offset(x + 40, y - 12), width: 110, height: 30), paint);
    }
    paint.maskFilter = null;

    // Montañas lejanas → cercanas, con niebla entre capas.
    paint.color = _far;
    canvas.drawPath(_farRidge, paint);
    _mist(canvas, size, h * .55, t, 0);
    paint.color = _mid;
    canvas.drawPath(_midRidge, paint);
    _mist(canvas, size, h * .66, t, 1);
    paint.color = _near;
    canvas.drawPath(_nearRidge, paint);
    paint.color = _front;
    canvas.drawPath(_frontHill, paint);
    canvas.drawPath(_pines, paint);
    canvas.drawRect(Rect.fromLTWH(0, h * .9, w, h * .1), paint);

    if (!moving) return;

    // Luciérnagas: puntos tenues que respiran.
    final rnd = math.Random(7);
    for (var i = 0; i < 12; i++) {
      final bx = rnd.nextDouble() * w, by = h * (.62 + rnd.nextDouble() * .3);
      final ph = rnd.nextDouble() * 6.28;
      final x = bx + math.sin(t * .15 + ph) * 18;
      final y = by + math.cos(t * .11 + ph) * 12;
      final a = (math.sin(t * .6 + ph) + 1) / 2;
      paint
        ..color = const Color(0xFFE6F5A8).withAlpha((a * 150).round())
        ..maskFilter = const MaskFilter.blur(BlurStyle.normal, 4);
      canvas.drawCircle(Offset(x, y), 2.4, paint);
    }
    paint.maskFilter = null;

    // Hojas que caen despacio.
    final leaf = Path()
      ..moveTo(-7, 0)
      ..cubicTo(-2, -9, 7, -7, 9, -3)
      ..cubicTo(6, 6, -3, 7, -7, 0)
      ..close();
    const leafColors = [Color(0xCC6FA866), Color(0xCC8DBF6A), Color(0xCC4E8C5A)];
    for (var i = 0; i < 6; i++) {
      final fall = 9.0 + i % 3 * 3;
      final y = (t * fall + i * (h + 80) / 6) % (h + 80) - 40;
      final x = (i * .618 * w + t * 4) % (w + 40) - 20 + math.sin(t * .3 + i) * 26;
      canvas.save();
      canvas.translate(x, y);
      canvas.rotate(t * .25 + i + math.sin(t * .4 + i) * .6);
      canvas.scale(1 + i % 2 * .3);
      paint.color = leafColors[i % 3];
      canvas.drawPath(leaf, paint);
      canvas.restore();
    }
  }

  void _mist(Canvas canvas, Size size, double y, double t, int layer) {
    final a = 18 + (math.sin(t * .05 + layer) * 8).round();
    final rect = Rect.fromLTWH(0, y - size.height * .05, size.width, size.height * .1);
    final p = Paint()
      ..shader = LinearGradient(
        begin: Alignment.topCenter,
        end: Alignment.bottomCenter,
        colors: [Colors.white.withAlpha(0), Colors.white.withAlpha(a), Colors.white.withAlpha(0)],
      ).createShader(rect);
    canvas.drawRect(rect, p);
  }

  @override
  bool shouldRepaint(covariant _ForestPainter old) => old.moving != moving;
}
