package es.foco.next.core;
import static org.junit.Assert.*;
import org.junit.Test;
public class FocusEngineTest {
 private final AppConfig c=new AppConfig();private final FocusState s=new FocusState();private final FocusEngine engine=new FocusEngine();
 private long time=1000,wall=1_800_000_000_000L;private String day="2026-10-26";private int boot=3;
 public FocusEngineTest(){c.watchedApps.add("feed.one");c.watchedApps.add("feed.two");}
 private FocusEngine.Decision step(String pkg,long delta,boolean interactive,boolean essential,boolean call){
  time+=delta;wall+=delta;FocusEngine.Input in=new FocusEngine.Input();in.pkg=pkg;in.elapsed=time;in.wall=wall;in.boot=boot;in.day=day;in.millisToday=10*60*60_000L;in.interactive=interactive;in.essential=essential;in.inCall=call;return engine.step(c,s,in);
 }
 private FocusEngine.Decision use(int minutes){FocusEngine.Decision d=step("feed.one",0,true,false,false);for(int i=0;i<minutes;i++)d=step("feed.one",60000,true,false,false);return d;}
 @Test public void fiveMinutesAreSharedAndCannotBeExtendedBySwitchingApps(){
  use(3);step("feed.two",30000,true,false,false);step("feed.two",60000,true,false,false);
  var d=step("feed.two",30000,true,false,false);assertTrue(d.blocked);assertEquals(55*60000,d.left);assertEquals(300000,s.usedMillis);assertEquals(2,s.lockedApps.size());
  assertTrue(step("feed.one",1000,true,false,false).blocked);
 }
 @Test public void exitingDoesNotResetHourOrBudget(){use(2);long end=s.window.elapsed;step("reader.app",0,true,false,false);step("feed.two",60000,true,false,false);assertEquals(120000,s.usedMillis);assertEquals(end,s.window.elapsed);}
 @Test public void screenOffTimeDoesNotAccumulate(){use(1);step("feed.one",1000,false,false,false);step("feed.one",60000,false,false,false);assertEquals(61000,s.usedMillis);}
 @Test public void callsPauseObservation(){use(1);step("feed.one",0,true,false,true);step("feed.one",60000,true,false,true);assertEquals(60000,s.usedMillis);}
 @Test public void oneWarningAtFourMinutes(){var d=use(4);assertTrue(d.warning);assertFalse(step("feed.two",0,true,false,false).warning);assertEquals(60000,d.remainingBudget);}
 @Test public void hourExpiresOnceAndStartsFreshOnlyOnNextLeisureUse(){use(5);long end=s.window.elapsed;step("reader.app",0,true,false,false);step("reader.app",end-time,true,false,false);assertFalse(s.window.present());assertEquals(0,s.usedMillis);step("feed.one",60000,true,false,false);assertEquals(0,s.usedMillis);assertEquals(time+FocusEngine.HOUR,s.window.elapsed);}
 @Test public void consumedNearEndBlocksOnlyUntilOriginalHourEnds(){use(1);step("reader.app",0,true,false,false);step("feed.one",50*60000,true,false,false);for(int i=0;i<4;i++)step("feed.one",60000,true,false,false);assertEquals(5*60000,FocusEngine.remaining(s,wall,time,boot));assertEquals(300000,s.usedMillis);}
 @Test public void midnightDoesNotGiveAnotherBudget(){use(5);day="2026-10-27";assertTrue(step("feed.two",1000,true,false,false).blocked);assertEquals(300000,s.usedMillis);assertEquals(0,s.dailyPackageMillis.size());}
 @Test public void monotonicDeadlineIgnoresWallClockChangesOnSameBoot(){use(5);long end=s.window.elapsed;wall-=20*FocusEngine.HOUR;assertTrue(step("feed.one",1000,true,false,false).blocked);assertEquals(end-time,FocusEngine.remaining(s,wall,time,boot));}
 @Test public void rebootRetainsDeadlineButNeverChargesUnknownUse(){use(3);FocusEngine.forget(s);boot=4;time=1;assertFalse(step("feed.one",30000,true,false,false).blocked);assertEquals(180000,s.usedMillis);}
 @Test public void delayedMissingCallbacksNeverInventMinutes(){use(1);step("feed.one",10*60000,true,false,false);assertEquals(60000,s.usedMillis);}
 @Test public void disablingCannotCancelAnExistingLock(){use(5);c.enabled=false;assertTrue(step("feed.two",0,true,false,false).blocked);}
 @Test public void disablingBeforeThresholdStopsMeasurement(){use(2);c.enabled=false;step("feed.one",0,true,false,false);step("feed.one",60000,true,false,false);assertEquals(120000,s.usedMillis);}
 @Test public void essentialAppsRemainOpenEvenWhenExplicitlySelected(){use(5);for(String p:new String[]{"com.whatsapp","com.whatsapp.w4b","com.bgeneral.app","com.android.settings","com.samsung.android.dialer"}){c.watchedApps.add(p);s.lockedApps.add(p);assertFalse(step(p,0,true,false,false).blocked);}}
 @Test public void manualSensitiveAndReaderAreExcluded(){c.sensitiveApps.add("private.app");c.watchedApps.add("private.app");c.returnApp="reader.app";c.watchedApps.add(c.returnApp);assertFalse(FocusEngine.watched(c,"private.app"));assertFalse(FocusEngine.watched(c,"reader.app"));}
 @Test public void blockedTimeIsNotReportedAsConsumption(){use(5);step("feed.one",60000,true,false,false);assertEquals(Long.valueOf(300000),s.dailyPackageMillis.get("feed.one"));}
 @Test public void transientAnchorIsNotReusableAfterForget(){use(1);FocusEngine.forget(s);step("feed.one",30000,true,false,false);assertEquals(60000,s.usedMillis);}
 @Test public void chosenAlternativeIsMeasuredWithoutSpendingLeisureBudget(){c.returnApp="reader.app";step("reader.app",0,true,false,false);step("reader.app",60000,true,false,false);assertEquals(0,s.usedMillis);assertFalse(s.window.present());assertEquals(Long.valueOf(60000),s.weeklyAlternative.get(day).get("reader.app"));}
 @Test public void historyRetainsOnlySevenDays(){for(int i=1;i<=9;i++){time+=24*FocusEngine.HOUR;wall+=24*FocusEngine.HOUR;day="2026-10-"+(i<10?"0":"")+i;step("feed.one",0,true,false,false);step("feed.one",60000,true,false,false);step("reader.app",0,true,false,false);}assertEquals(7,s.weeklyOcio.size());assertFalse(s.weeklyOcio.containsKey("2026-10-01"));assertTrue(s.weeklyOcio.containsKey("2026-10-09"));}
 @Test public void blockIsLoggedOncePerWindow(){use(5);step("feed.two",60000,true,false,false);assertEquals(Long.valueOf(1),s.weeklyBlocks.get(day));}
}
