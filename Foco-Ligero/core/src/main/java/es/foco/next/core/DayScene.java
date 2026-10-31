package es.foco.next.core;
import java.time.*;
/** Local clock only: no GPS or polling. */
public final class DayScene {
  private DayScene(){}
  public static boolean night(long wall,ZoneId zone){int hour=Instant.ofEpochMilli(wall).atZone(zone).getHour();return hour<6||hour>=19;}
  public static long nextBoundary(long wall,ZoneId zone){return Math.min(ReminderTime.next(wall,zone,6*60),ReminderTime.next(wall,zone,19*60));}
}
