package es.foco.next.runtime;
import static org.junit.Assert.*;
import android.app.KeyguardManager;import android.content.*;import android.content.pm.*;import android.os.*;import android.provider.Settings;import android.view.accessibility.*;
import es.foco.next.core.*;import es.foco.next.data.Repository;import es.foco.next.ui.FocusOverlay;
import java.time.Duration;import java.util.*;import org.junit.*;import org.junit.runner.RunWith;import org.robolectric.*;import org.robolectric.annotation.*;import org.robolectric.util.ReflectionHelpers;
@RunWith(RobolectricTestRunner.class) @Config(sdk=35) @LooperMode(LooperMode.Mode.PAUSED)
public class LiteRuntimeTest {
 private FocusAccessibilityService s;
 private void window(String pkg){AccessibilityWindowInfo w=AccessibilityWindowInfo.obtain();AccessibilityNodeInfo root=AccessibilityNodeInfo.obtain();root.setPackageName(pkg);Shadows.shadowOf(w).setType(AccessibilityWindowInfo.TYPE_APPLICATION);Shadows.shadowOf(w).setFocused(true);Shadows.shadowOf(w).setRoot(root);Shadows.shadowOf(s).setWindows(Collections.singletonList(w));}
 private void event(String pkg){AccessibilityEvent e=AccessibilityEvent.obtain(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED);e.setPackageName(pkg);s.onAccessibilityEvent(e);}
 private FocusOverlay overlay(){return ReflectionHelpers.getField(s,"overlay");}
 private void start(){
  ReflectionHelpers.callStaticMethod(CalmAudio.class,"resetForTests");
  s=Robolectric.buildService(FocusAccessibilityService.class).create().get();
  s.getSharedPreferences("foco-soundscape",0).edit().clear().commit();
  ReflectionHelpers.callStaticMethod(Repository.class,"resetForTests");s.deleteFile("focus-lite-v1.json");s.deleteFile("focus-state-v1.json");
  Settings.Global.putInt(s.getContentResolver(),Settings.Global.BOOT_COUNT,3);
  Shadows.shadowOf(s.getSystemService(PowerManager.class)).setIsInteractive(true);Shadows.shadowOf(s.getSystemService(KeyguardManager.class)).setKeyguardLocked(false);
  Repository r=Repository.get(s);AppConfig c=r.config();c.watchedApps.add("feed.app");r.saveConfig(c);window("feed.app");ReflectionHelpers.callInstanceMethod(s,"onServiceConnected");
 }
 private void consume(){assertTrue("Service connected",FocusAccessibilityService.connected());assertTrue("Observed window",Repository.get(s).remaining()>0);Shadows.shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMinutes(5).plusMillis(500));assertTrue("Budget: "+Repository.get(s).state().usedMillis,Repository.get(s).locked());assertTrue("Overlay visible, stored budget="+Repository.get(s).state().usedMillis+", logs="+org.robolectric.shadows.ShadowLog.getLogs(),overlay().visible());}
 @After public void stop(){if(s!=null)s.onDestroy();}
 @Test public void normalUseHasNoOverlayAndFiveMinutesCreatesTouchableBlock(){start();assertFalse(overlay().visible());consume();android.view.View view=ReflectionHelpers.getField(overlay(),"view");android.view.WindowManager.LayoutParams p=(android.view.WindowManager.LayoutParams)view.getLayoutParams();assertEquals(0,p.flags&android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);assertEquals(0,p.preferredRefreshRate,0);}
 @Test public void bancoGeneralLabelHidesImmediatelyEvenWhenWindowMetadataLags(){start();consume();PackageInfo info=new PackageInfo();info.packageName="com.example.finance";info.applicationInfo=new ApplicationInfo();info.applicationInfo.packageName=info.packageName;info.applicationInfo.nonLocalizedLabel="Banco General";Shadows.shadowOf(s.getPackageManager()).installPackage(info);event(info.packageName);assertFalse(overlay().visible());}
 @Test public void bankScreenStaysOpenAndReturningToLeisureRestoresBlock(){start();consume();window("com.bgeneral.app");event("com.bgeneral.app");assertFalse(overlay().visible());window("feed.app");event("feed.app");assertTrue(overlay().visible());}
 @Test public void screenOffRemovesOverlayWithoutResettingHour(){start();consume();long end=Repository.get(s).state().window.elapsed;Shadows.shadowOf(s.getSystemService(PowerManager.class)).setIsInteractive(false);s.sendBroadcast(new Intent(Intent.ACTION_SCREEN_OFF));Shadows.shadowOf(Looper.getMainLooper()).idle();assertFalse(overlay().visible());assertEquals(end,Repository.get(s).state().window.elapsed);}
 @Test public void unknownWindowNeverKeepsBlockOverAnotherScreen(){start();consume();Shadows.shadowOf(s).setWindows(Collections.emptyList());event("unknown.app");assertFalse(overlay().visible());}
 @Test public void openingFocoDoesNotResetOrPermitEditingLockedPlan(){start();consume();window(s.getPackageName());event(s.getPackageName());assertFalse(overlay().visible());assertTrue(Repository.get(s).locked());AppConfig c=Repository.get(s).config();c.watchedApps.clear();assertThrows(IllegalStateException.class,()->Repository.get(s).saveConfig(c));}
 @Test public void musicDoesNotRestartOnMinuteRefreshAndStopsWhenBankOpens(){start();consume();org.robolectric.shadows.ShadowMediaPlayer.setMediaInfoProvider(source->new org.robolectric.shadows.ShadowMediaPlayer.MediaInfo(243000,0));Shadows.shadowOf(s.getSystemService(android.media.AudioManager.class)).setNextFocusRequestResponse(android.media.AudioManager.AUDIOFOCUS_REQUEST_GRANTED);var audio=Robolectric.buildService(CalmAudioService.class).create();try{audio.get().onStartCommand(new Intent(),0,1);Object player=ReflectionHelpers.getField(audio.get(),"player");assertNotNull(player);Shadows.shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMinutes(1));assertSame(player,ReflectionHelpers.getField(audio.get(),"player"));assertTrue(CalmAudio.active());window("com.bgeneral.app");event("com.bgeneral.app");assertFalse(overlay().visible());assertFalse(CalmAudio.active());assertFalse(CalmAudio.wanted());}finally{audio.destroy();}}
 @Test public void externallyDetachedOverlayReleasesAudioOwner(){start();consume();assertTrue(CalmAudio.wanted());android.view.View view=ReflectionHelpers.getField(overlay(),"view");s.getSystemService(android.view.WindowManager.class).removeViewImmediate(view);assertFalse(CalmAudio.wanted());}
}
