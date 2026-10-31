package es.foco.next.ui;
import static org.junit.Assert.*;
import org.junit.*;import org.junit.runner.RunWith;import org.robolectric.*;import org.robolectric.annotation.*;
import android.content.Context;import android.os.PowerManager;
@RunWith(RobolectricTestRunner.class) @Config(sdk=35) @LooperMode(LooperMode.Mode.PAUSED)
public class PauseVideoViewTest {
 @Test public void motionPolicyHonorsSavingReducedMotionAndSystemPower(){Context c=RuntimeEnvironment.getApplication();var p=c.getSharedPreferences("foco-ambient",0);p.edit().clear().commit();PauseVideoView v=new PauseVideoView(c);assertTrue(v.animationAllowed());assertFalse(v.shouldPlay());assertFalse(v.hasPlayer());p.edit().putBoolean("saving",true).commit();assertFalse(v.animationAllowed());p.edit().putBoolean("saving",false).putBoolean("reduce",true).commit();assertFalse(v.animationAllowed());p.edit().clear().commit();Shadows.shadowOf(c.getSystemService(PowerManager.class)).setIsPowerSaveMode(true);assertFalse(v.animationAllowed());}
 @Test public void constructionAndRetryWithoutVisibleSurfaceNeverStartDecoder(){Context c=RuntimeEnvironment.getApplication();c.getSharedPreferences("foco-ambient",0).edit().clear().commit();PauseVideoView v=new PauseVideoView(c);for(int i=0;i<5;i++)v.retry();assertFalse(v.hasPlayer());assertFalse(v.shouldPlay());}
 @Test public void savingAndWindowRemovalReleaseAnExistingPlayer(){
  var controller=Robolectric.buildActivity(android.app.Activity.class).setup().visible();var a=controller.get();var prefs=a.getSharedPreferences("foco-ambient",0);prefs.edit().clear().commit();
  android.widget.FrameLayout root=new android.widget.FrameLayout(a);a.setContentView(root);PauseVideoView v=new PauseVideoView(a);root.addView(v,new android.widget.FrameLayout.LayoutParams(-1,-1));
  try{org.robolectric.util.ReflectionHelpers.setField(v,"player",new android.media.MediaPlayer());assertTrue(v.hasPlayer());prefs.edit().putBoolean("saving",true).commit();Shadows.shadowOf(android.os.Looper.getMainLooper()).idle();assertFalse(v.hasPlayer());org.robolectric.util.ReflectionHelpers.setField(v,"player",new android.media.MediaPlayer());root.removeView(v);assertFalse(v.hasPlayer());}
  finally{controller.pause().stop().destroy();prefs.edit().clear().commit();}
 }

}
