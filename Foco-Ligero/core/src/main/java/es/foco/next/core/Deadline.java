package es.foco.next.core;

/** Monotonic deadline when on the same boot, wall fallback after reboot. */
public final class Deadline {
  public long wall = 0, elapsed = 0;
  public int boot = -1;

  public boolean present() {
    return wall > 0 || elapsed > 0;
  }

  public void start(long wallNow, long elapsedNow, int bootNow, long duration) {
    long bounded = Math.max(0, duration);
    wall = add(wallNow, bounded);
    elapsed = add(elapsedNow, bounded);
    boot = bootNow;
  }

  public long remaining(long wallNow, long elapsedNow, int bootNow, long maximum) {
    if (!present()) return 0;
    long end = boot >= 0 && boot == bootNow ? elapsed : wall;
    long now = boot >= 0 && boot == bootNow ? elapsedNow : wallNow;
    if (end <= now) return 0;
    long delta = end - now;
    if (delta < 0) delta = Long.MAX_VALUE;
    return Math.max(0, Math.min(Math.max(0, maximum), delta));
  }

  public void clear() {
    wall = elapsed = 0;
    boot = -1;
  }

  private static long add(long value, long delta) {
    if (delta > 0 && value > Long.MAX_VALUE - delta) return Long.MAX_VALUE;
    return value + delta;
  }
}
