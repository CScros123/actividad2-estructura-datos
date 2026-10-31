package es.foco.next.ui;
import static org.junit.Assert.*;
import android.content.Context;
import android.os.PowerManager;
import es.foco.next.R;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;
import org.robolectric.util.ReflectionHelpers;
@RunWith(RobolectricTestRunner.class) @Config(sdk=35) @LooperMode(LooperMode.Mode.PAUSED)
public class AmbientViewTest {
 @Test public void lifecycleMotionPreferenceAndPowerSaverStopFrames(){
  var controller=Robolectric.buildActivity(MainActivity.class).setup().visible();
  var activity=controller.get();var scene=(AmbientView)activity.findViewById(R.id.ambient);
  var prefs=activity.getSharedPreferences("foco-ambient",0);
  try{
   assertTrue(scene.moving());
   prefs.edit().putBoolean("reduce",true).commit();scene.refresh();assertFalse(scene.moving());
   prefs.edit().putBoolean("reduce",false).commit();assertTrue(scene.moving());
   Shadows.shadowOf(activity.getSystemService(PowerManager.class)).setIsPowerSaveMode(true);assertFalse(scene.moving());
   Shadows.shadowOf(activity.getSystemService(PowerManager.class)).setIsPowerSaveMode(false);
   activity.findViewById(R.id.navigation);ReflectionHelpers.setField(activity,"page",2);activity.render();
   assertSame(scene,activity.findViewById(R.id.ambient));
   controller.pause();assertFalse(scene.moving());
  }finally{controller.stop().destroy();prefs.edit().clear().commit();}
 }
 @Test public void quickSavingStopsBothSceneAndUiWithoutChangingPlan(){
  var controller=Robolectric.buildActivity(MainActivity.class).setup().visible();var a=controller.get();
  var scene=(AmbientView)a.findViewById(R.id.ambient);var prefs=a.getSharedPreferences("foco-ambient",0);
  try{
   long used=es.foco.next.data.Repository.get(a).state().usedMillis;
   a.findViewById(R.id.energy_toggle).performClick();Shadows.shadowOf(android.os.Looper.getMainLooper()).idle();
   assertTrue(scene.saving());assertFalse(scene.moving());assertFalse(UiMotion.allowed(a));assertFalse(ReflectionHelpers.<Boolean>getField(scene,"running"));
   assertEquals(used,es.foco.next.data.Repository.get(a).state().usedMillis);
   assertTrue(((android.widget.Button)a.findViewById(R.id.energy_toggle)).getText().toString().contains("activado"));
   a.findViewById(R.id.energy_toggle).performClick();Shadows.shadowOf(android.os.Looper.getMainLooper()).idle();assertTrue(scene.moving());
   controller.pause();assertFalse(ReflectionHelpers.<Boolean>getField(scene,"running"));
  }finally{controller.stop().destroy();prefs.edit().clear().commit();}
 }
 @Test public void savingChoiceSurvivesActivityRecreation(){
  var c=Robolectric.buildActivity(MainActivity.class).setup().visible();
  var p=c.get().getSharedPreferences("foco-ambient",0);
  try{c.get().findViewById(R.id.energy_toggle).performClick();c.recreate();assertFalse(((AmbientView)c.get().findViewById(R.id.ambient)).moving());assertFalse(UiMotion.allowed(c.get()));}
  finally{c.pause().stop().destroy();p.edit().clear().commit();}
 }
 @Test public void windIsContinuousAndBoundedAcrossLongCycles(){double last=AmbientView.wind(0);for(double t=.1;t<600;t+=.1){double now=AmbientView.wind(t);assertTrue(now>=0&&now<=1.01);assertTrue(Math.abs(now-last)<.02);last=now;}}
}
