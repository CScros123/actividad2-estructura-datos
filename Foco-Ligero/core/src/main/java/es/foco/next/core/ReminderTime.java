package es.foco.next.core;
import java.time.*;

/** One future local occurrence; never a fixed 24-hour delay across timezone/DST changes. */
public final class ReminderTime {
  private ReminderTime(){}
  public static long next(long now,ZoneId zone,int minute){
    if(minute<0||minute>=1440)throw new IllegalArgumentException("Hora no válida");
    ZonedDateTime current=Instant.ofEpochMilli(now).atZone(zone);
    ZonedDateTime next=current.toLocalDate().atTime(minute/60,minute%60).atZone(zone);
    if(next.toInstant().toEpochMilli()<=now)next=current.toLocalDate().plusDays(1).atTime(minute/60,minute%60).atZone(zone);
    return next.toInstant().toEpochMilli();
  }
}
