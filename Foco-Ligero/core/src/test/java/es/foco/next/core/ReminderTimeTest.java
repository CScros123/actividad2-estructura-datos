package es.foco.next.core;
import static org.junit.Assert.*;
import java.time.*;import org.junit.Test;
public class ReminderTimeTest {
 private long at(String date,ZoneId zone){return LocalDateTime.parse(date).atZone(zone).toInstant().toEpochMilli();}
 @Test public void beforeSelectedHourUsesTodayAndAtHourUsesTomorrow(){ZoneId z=ZoneId.of("America/Panama");assertEquals(at("2026-10-26T20:00",z),ReminderTime.next(at("2026-10-26T19:30",z),z,1200));assertEquals(at("2026-10-27T20:00",z),ReminderTime.next(at("2026-10-26T20:00",z),z,1200));}
 @Test public void nextOccurrenceUsesLocalTimeAcrossDst(){ZoneId z=ZoneId.of("America/New_York");long now=at("2026-03-07T20:01",z);long next=ReminderTime.next(now,z,1200);assertEquals(at("2026-03-08T20:00",z),next);assertTrue(next-now<24*60*60000L);}
 @Test public void invalidMinuteIsRejected(){assertThrows(IllegalArgumentException.class,()->ReminderTime.next(0,ZoneOffset.UTC,1440));}
}
