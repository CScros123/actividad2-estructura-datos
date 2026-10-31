package es.foco.next.core;
import static org.junit.Assert.*;
import java.time.*;import org.junit.Test;
public class DaySceneTest {
 private final ZoneId zone=ZoneId.of("America/Panama");
 private long at(String local){return LocalDateTime.parse(local).atZone(zone).toInstant().toEpochMilli();}
 @Test public void exactDayAndNightBoundaries(){assertTrue(DayScene.night(at("2026-10-26T05:59"),zone));assertFalse(DayScene.night(at("2026-10-26T06:00"),zone));assertFalse(DayScene.night(at("2026-10-26T18:59"),zone));assertTrue(DayScene.night(at("2026-10-26T19:00"),zone));}
 @Test public void schedulesOnlyNextTransition(){assertEquals(at("2026-10-26T19:00"),DayScene.nextBoundary(at("2026-10-26T12:00"),zone));assertEquals(at("2026-10-27T06:00"),DayScene.nextBoundary(at("2026-10-26T19:00"),zone));assertEquals(at("2026-10-26T06:00"),DayScene.nextBoundary(at("2026-10-26T00:00"),zone));}
 @Test public void usesLocalTimezoneNotUtc(){long instant=Instant.parse("2026-10-26T09:00:00Z").toEpochMilli();assertTrue(DayScene.night(instant,zone));assertFalse(DayScene.night(instant,ZoneOffset.UTC));}
}
