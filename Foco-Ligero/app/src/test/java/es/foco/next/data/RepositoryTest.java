package es.foco.next.data;
import static org.junit.Assert.*;
import android.content.Context;import android.os.SystemClock;import android.provider.Settings;
import es.foco.next.core.*;
import java.nio.file.*;import org.json.*;import org.junit.*;import org.junit.runner.RunWith;import org.robolectric.*;import org.robolectric.annotation.*;
@RunWith(RobolectricTestRunner.class) @Config(sdk=35)
public class RepositoryTest {
 private Context c;
 @Before public void init(){c=RuntimeEnvironment.getApplication();Repository.resetForTests();for(String n:new String[]{"focus-lite-v1.json","focus-state-v1.json"})c.deleteFile(n);Settings.Global.putInt(c.getContentResolver(),Settings.Global.BOOT_COUNT,3);}
 private FocusEngine.Input input(long elapsed){FocusEngine.Input in=new FocusEngine.Input();in.pkg="feed.app";in.wall=System.currentTimeMillis();in.elapsed=elapsed;in.boot=3;in.day="2026-10-26";in.interactive=true;return in;}
 private Repository r(){Repository r=Repository.get(c);AppConfig a=r.config();a.watchedApps.add("feed.app");r.saveConfig(a);return r;}
 @Test public void migrationArchivesOriginalAndDropsLegacyCyclesAndOptions()throws Exception{
  String old="{\"config\":{\"enabled\":true,\"autoSocial\":false,\"watchedApps\":[\"feed.app\"],\"returnApp\":\"reader.app\",\"dnsEnabled\":true,\"strictBrowsers\":true},\"state\":{\"day\":\"2026-10-26\",\"inkUseMillis\":600000,\"dailyPackageMillis\":{\"feed.app\":180000}}}";
  Path p=c.getFileStreamPath("focus-state-v1.json").toPath();Files.write(p,old.getBytes(java.nio.charset.StandardCharsets.UTF_8));Repository r=Repository.get(c);
  assertTrue(r.migrated);assertTrue(r.legacyVpn);assertEquals("reader.app",r.config().returnApp);assertTrue(r.config().watchedApps.contains("feed.app"));assertEquals(Long.valueOf(180000),r.state().dailyPackageMillis.get("feed.app"));assertEquals(0,r.state().usedMillis);assertEquals(old,new String(Files.readAllBytes(p),java.nio.charset.StandardCharsets.UTF_8));assertFalse(r.exportBackup().contains("strictBrowsers"));
 }
 @Test public void checkpointsRetainBudgetAndDeadlineButNotLiveAnchor(){Repository r=r();long t=SystemClock.elapsedRealtime();r.sample(input(t),true);r.sample(input(t+60000),true);String json=r.exportBackup();assertFalse(json.contains("observedElapsed"));Repository.resetForTests();r=Repository.get(c);assertEquals(60000,r.state().usedMillis);assertEquals(-1,r.state().observedElapsed);}
 @Test public void activeLockRejectsConfigAndBackupReplacement(){Repository r=r();String backup=r.exportBackup();long t=SystemClock.elapsedRealtime();r.sample(input(t),true);for(int i=1;i<=5;i++)r.sample(input(t+i*60000),true);assertTrue(r.locked());AppConfig a=r.config();a.enabled=false;
  assertThrows(IllegalStateException.class,()->r.saveConfig(a));assertThrows(IllegalStateException.class,()->r.importBackup(backup));assertTrue(r.config().enabled);}
 @Test public void invalidImportPreservesPriorData(){Repository r=r();String backup=r.exportBackup();assertThrows(IllegalArgumentException.class,()->r.importBackup("{\"format\":51}"));assertEquals(backup,r.exportBackup());}
 @Test public void intentAndSelectedAppsSurviveBackup(){Repository r=r();AppConfig a=r.config();a.intention="Leer sin prisa";a.returnApp="reader.app";r.saveConfig(a);String backup=r.exportBackup();r.importBackup(backup);assertEquals("Leer sin prisa",r.config().intention);assertTrue(r.config().watchedApps.contains("feed.app"));}
 @Test public void corruptOriginalIsPreservedAndNeverSilentlyReset()throws Exception{Path p=c.getFileStreamPath("focus-state-v1.json").toPath();Files.write(p,"broken".getBytes(java.nio.charset.StandardCharsets.UTF_8));assertThrows(IllegalStateException.class,()->Repository.get(c));assertEquals("broken",new String(Files.readAllBytes(p),java.nio.charset.StandardCharsets.UTF_8));}
 @Test public void alternativeCannotAlsoBeOcio(){Repository r=r();AppConfig a=r.config();a.returnApp="feed.app";assertThrows(IllegalArgumentException.class,()->r.saveConfig(a));}
 @Test public void weeklyCsvContainsOnlyObservedAggregatesAndExplicitChoices(){Repository r=r();long t=SystemClock.elapsedRealtime();r.sample(input(t),true);r.sample(input(t+60000),true);r.recordChoice("reader.app");String csv=r.exportWeek();assertTrue(csv.startsWith("fecha,tipo,paquete,app,segundos_observados,veces"));assertTrue(csv.contains("alternativa_abierta_desde_foco"));assertTrue(csv.contains("reader.app"));assertFalse(csv.contains("http"));assertFalse(csv.contains("observedElapsed"));}
}
