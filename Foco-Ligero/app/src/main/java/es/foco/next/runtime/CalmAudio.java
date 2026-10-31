package es.foco.next.runtime;
import android.content.*;
import android.media.AudioManager;
import java.util.*;
/** One shared session for visible Foco surfaces, with no duplicate players or polling. */
public final class CalmAudio {
  private static final Map<Object,Runnable> owners=new IdentityHashMap<>();
  private static final Set<Object> interventions=Collections.newSetFromMap(new IdentityHashMap<>());
  public static boolean intervening(){return !interventions.isEmpty();}
  private static Context app;
  private static boolean starting,playing,suspended;
  private static int track;
  public static boolean enabled(Context c){return c.getApplicationContext().getSharedPreferences("foco-soundscape",0).getBoolean("enabled",true);}
  public static boolean active(){return playing;}
  public static boolean pending(){return starting;}
  public static int track(){return track;}
  public static String title(){return track==0?"Morning Routine · Ghostrifter Official":"Colorful Flowers · Tokyo Music Walker";}
  public static void enter(Context c,Object owner,Runnable changed,boolean intervention){
    boolean first=owners.isEmpty();app=c.getApplicationContext();owners.put(owner,changed);if(intervention)interventions.add(owner);else interventions.remove(owner);
    if(playing)CalmAudioService.refreshFocus();
    if(first){suspended=false;track=es.foco.next.ui.SceneTheme.night()?1:0;}
    if(!starting&&!playing&&!suspended&&enabled(c)){
      AudioManager audio=c.getSystemService(AudioManager.class);
      if(audio!=null&&audio.getMode()==AudioManager.MODE_NORMAL&&(intervention||!audio.isMusicActive()))start();
    }
  }
  public static void leave(Object owner){owners.remove(owner);interventions.remove(owner);if(!owners.isEmpty())CalmAudioService.refreshFocus();if(owners.isEmpty()){suspended=false;stop();}}
  public static boolean wanted(){return app!=null&&!owners.isEmpty()&&enabled(app)&&!suspended;}
  public static void toggle(Context c){
    app=c.getApplicationContext();
    if(playing||starting){app.getSharedPreferences("foco-soundscape",0).edit().putBoolean("enabled",false).apply();stop();}
    else{app.getSharedPreferences("foco-soundscape",0).edit().putBoolean("enabled",true).apply();suspended=false;start();}
  }
  private static void start(){
    if(!wanted())return;starting=true;
    try{app.startForegroundService(new Intent(app,CalmAudioService.class));}catch(RuntimeException e){starting=false;suspended=true;}
    signal();
  }
  private static void stop(){starting=playing=false;CalmAudioService.stopPlayback();if(app!=null)app.stopService(new Intent(app,CalmAudioService.class));signal();}
  public static void mute(Context c){app=c.getApplicationContext();app.getSharedPreferences("foco-soundscape",0).edit().putBoolean("enabled",false).apply();stop();}
  static void ready(int index){track=index;starting=false;playing=true;signal();}
  static void interrupted(){suspended=true;starting=playing=false;signal();}
  static void destroyed(){starting=playing=false;signal();}
  static void resetForTests(){owners.clear();interventions.clear();stop();app=null;suspended=false;track=0;}
  private static void signal(){for(Runnable listener:new ArrayList<>(owners.values()))listener.run();}
}
