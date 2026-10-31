package es.foco.next.runtime;

import android.accessibilityservice.AccessibilityService;
import android.app.KeyguardManager;
import android.content.*;
import android.content.pm.ResolveInfo;
import android.media.AudioManager;
import android.os.*;
import android.view.accessibility.AccessibilityEvent;
import es.foco.next.core.*;
import es.foco.next.data.Repository;
import es.foco.next.ui.FocusOverlay;
import java.time.*;
import java.util.*;

/** Only window events and one upcoming boundary. No content events, polling, animation or VPN. */
public final class FocusAccessibilityService extends AccessibilityService implements Repository.Listener {
  private static FocusAccessibilityService instance;
  private final Handler main=new Handler(Looper.getMainLooper());
  private Repository repository;
  private BankAppGuard bank;
  private CallMonitor calls;
  private FocusOverlay overlay;
  private String foreground="";
  private final Set<String> homes=new HashSet<>();
  private AudioManager.OnModeChangedListener audioListener;
  private boolean registered,closed;
  private final Runnable boundary=()->evaluate(false);
  public static boolean connected(){return instance!=null&&!instance.closed;}
  public static void checkpoint(){if(connected())instance.evaluate(true);}
  public static void appOpened(){if(connected())instance.observe(instance.getPackageName());}
  @Override protected void onServiceConnected(){
    super.onServiceConnected();closed=false;
    try{
      repository=Repository.get(this);bank=new BankAppGuard(this);calls=new CallMonitor(this);
      overlay=new FocusOverlay(this);repository.addListener(this);instance=this;
      for(ResolveInfo r:getPackageManager().queryIntentActivities(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME),0))
        if(r.activityInfo!=null)homes.add(r.activityInfo.packageName);
      IntentFilter f=new IntentFilter();f.addAction(Intent.ACTION_SCREEN_OFF);f.addAction(Intent.ACTION_SCREEN_ON);
      f.addAction(Intent.ACTION_USER_PRESENT);f.addAction(Intent.ACTION_TIME_CHANGED);f.addAction(Intent.ACTION_TIMEZONE_CHANGED);
      if(Build.VERSION.SDK_INT>=33)registerReceiver(screen,f,Context.RECEIVER_NOT_EXPORTED);else registerReceiver(screen,f);
      registered=true;
      try{if(Build.VERSION.SDK_INT>=31&&calls.audio!=null){audioListener=mode->evaluate(true);calls.audio.addOnModeChangedListener(getMainExecutor(),audioListener);}}catch(RuntimeException e){audioListener=null;}
      observe(ForegroundWindows.current(this,false));
    }catch(RuntimeException e){android.util.Log.e("Foco","No se pudo iniciar la medición",e);if(overlay!=null)overlay.dismiss();closed=true;}
  }
  @Override public void onAccessibilityEvent(AccessibilityEvent e){
    if(closed||repository==null||e==null)return;
    int type=e.getEventType();
    if(type!=AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED&&type!=AccessibilityEvent.TYPE_WINDOWS_CHANGED)return;
    String pkg=e.getPackageName()==null?"":e.getPackageName().toString();
    // Hide first on a bank event, before even querying focused-window metadata.
    if(bank.hide(repository.config(),pkg)){overlay.dismiss();observe(pkg);return;}
    String current=ForegroundWindows.current(this,overlay.visible());
    if(current.isEmpty()){
      // Own overlay events aren't evidence that the underlying app has changed.
      if(pkg.equals(getPackageName())&&overlay.visible())return;
      observe("");return;
    }
    if(!current.equals(foreground))observe(current);
  }
  private void observe(String pkg){foreground=pkg;evaluate(true);}
  private boolean awake(){
    PowerManager p=getSystemService(PowerManager.class);KeyguardManager k=getSystemService(KeyguardManager.class);
    return p!=null&&p.isInteractive()&&k!=null&&!k.isKeyguardLocked();
  }
  private boolean essential(AppConfig c,String pkg){return FocusEngine.essential(c,pkg)||homes.contains(pkg)||calls.packages.contains(pkg)||bank.hide(c,pkg);}
  private void evaluate(boolean force){
    if(closed||repository==null||overlay==null)return;
    main.removeCallbacks(boundary);
    try{
      AppConfig c=repository.config();
      String verified=ForegroundWindows.current(this,overlay.visible());
      // A timer can never extend a stale foreground observation onto another app.
      if(!verified.equals(foreground)&&!bank.hide(c,foreground))foreground=verified;
      long wall=System.currentTimeMillis();ZonedDateTime local=Instant.ofEpochMilli(wall).atZone(ZoneId.systemDefault());
      FocusEngine.Input in=new FocusEngine.Input();in.pkg=foreground;in.wall=wall;in.elapsed=SystemClock.elapsedRealtime();in.boot=Repository.boot(this);
      in.day=local.toLocalDate().toString();in.millisToday=wall-local.toLocalDate().atStartOfDay(local.getZone()).toInstant().toEpochMilli();
      in.interactive=awake()&&!foreground.isEmpty();in.essential=essential(c,foreground);in.inCall=calls.active(foreground);
      FocusEngine.Decision d=repository.sample(in,force);
      if(!in.interactive||in.essential||in.inCall)overlay.dismiss();
      else if(d.blocked)overlay.block(c,d.left);
      else if(d.warning)overlay.warn();
      else if(!overlay.warningVisible())overlay.dismiss();
      long next=d.next;
      if(d.blocked)next=Math.min(es.foco.next.ui.SceneTheme.untilBoundary(),Math.min(next,Math.max(1,d.left%60_000==0?60_000:d.left%60_000)));
      if(next>0)main.postDelayed(boundary,next);
    }catch(RuntimeException e){android.util.Log.e("Foco","No se pudo actualizar la medición",e);overlay.dismiss();}
  }
  @Override public void changed(){evaluate(true);}
  private final BroadcastReceiver screen=new BroadcastReceiver(){
    @Override public void onReceive(Context c,Intent i){evaluate(true);}
  };
  @Override public void onInterrupt(){if(!closed)evaluate(true);}
  @Override public void onDestroy(){
    if(!closed)evaluate(true);
    closed=true;main.removeCallbacksAndMessages(null);
    if(repository!=null){repository.removeListener(this);try{repository.forget();}catch(RuntimeException ignored){}}
    if(overlay!=null)overlay.dismiss();
    if(registered)try{unregisterReceiver(screen);}catch(RuntimeException ignored){}
    if(Build.VERSION.SDK_INT>=31&&audioListener!=null&&calls!=null&&calls.audio!=null)
      try{calls.audio.removeOnModeChangedListener(audioListener);}catch(RuntimeException ignored){}
    if(instance==this)instance=null;super.onDestroy();
  }
}
