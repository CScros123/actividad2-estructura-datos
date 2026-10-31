package es.foco.next.ui;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.content.Context;
import es.foco.next.R;
import android.graphics.PixelFormat;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.view.*;
import android.widget.*;
import es.foco.next.core.AppConfig;

/** Touchable pause with a visible-only local scene; focus rules remain in the engine. */
public final class FocusOverlay {
  private final AccessibilityService service;
  private final WindowManager wm;
  private final Handler main=new Handler(Looper.getMainLooper());
  private View view;
  private TextView remaining;
  private boolean warning;
  private long shownMinutes=-1;
  private boolean shownNight;
  private final QuietMusic music;
  private Button musicButton;
  private TextView musicCaption;
  private final Runnable hide=this::dismiss;
  public FocusOverlay(AccessibilityService s){service=s;wm=s.getSystemService(WindowManager.class);music=new QuietMusic(s,this::refreshMusicControl,true);}
  public boolean visible(){return view!=null;}
  public boolean warningVisible(){return warning&&view!=null;}
  public void block(AppConfig c,long left){
    long minutes=Math.max(1,(left+59_999)/60_000);
    if(view!=null&&!warning&&shownNight==SceneTheme.night()){if(shownMinutes!=minutes){remaining.setText("Podrás volver en "+minutes+" min");shownMinutes=minutes;}return;}
    dismiss();
    android.content.res.Configuration dark=new android.content.res.Configuration(service.getResources().getConfiguration());dark.uiMode=(dark.uiMode&~android.content.res.Configuration.UI_MODE_NIGHT_MASK)|android.content.res.Configuration.UI_MODE_NIGHT_YES;
    Context scene=new android.view.ContextThemeWrapper(service.createConfigurationContext(dark),R.style.AppTheme);shownNight=SceneTheme.night();
    ScrollView scroll=new ScrollView(scene);scroll.setFillViewport(true);
    GradientDrawable bg=new GradientDrawable();bg.setColor(scene.getColor(R.color.surface));scroll.setBackground(bg);
    LinearLayout box=Design.column(scene);box.setGravity(Gravity.CENTER);box.setPadding(Design.dp(service,28),Design.dp(service,48),Design.dp(service,28),Design.dp(service,32));scroll.addView(box);
    TextView heading=label(box,"ENCUENTRA TU FOCO.\nVUELVE A TU PAZ.",25,0xFFFFE7A3);
    heading.setTypeface(android.graphics.Typeface.create("serif",android.graphics.Typeface.NORMAL));heading.setShadowLayer(Design.dp(scene,10),0,0,0xFFFFC95B);
    GradientDrawable plaque=new GradientDrawable();plaque.setColor(0xDC17120C);plaque.setCornerRadius(Design.dp(scene,18));plaque.setStroke(Design.dp(scene,1),0xFFE5BF70);heading.setBackground(plaque);heading.setPadding(Design.dp(scene,12),Design.dp(scene,15),Design.dp(scene,12),Design.dp(scene,15));
    Design.gap(box,110);
    OrbitApps orbit=new OrbitApps(scene,Alternatives.installed(scene,c),c.returnApp,entry->{
      Intent app=service.getPackageManager().getLaunchIntentForPackage(entry.pkg);
      if(app!=null){es.foco.next.data.Repository.get(service).recordChoice(entry.pkg);try{service.startActivity(app.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));}catch(RuntimeException ignored){Toast.makeText(scene,"No se pudo abrir esa app",Toast.LENGTH_SHORT).show();}}
    });box.addView(orbit,new LinearLayout.LayoutParams(-1,-2));
    label(box,"Tu tiempo vuelve a ser tuyo",26,0xFFFFE7A3);
    label(box,"Ya usaste tus 5 minutos de ocio de esta hora.",17,scene.getColor(R.color.text_secondary));
    remaining=label(box,"Podrás volver en "+minutes+" min",18,0xFFE5BF70);shownMinutes=minutes;
    Design.gap(box,16);label(box,c.intention.trim().isEmpty()?"Elige un siguiente paso pequeño":c.intention,23,scene.getColor(R.color.text_primary));
    musicButton=action(scene,"Música tranquila",false,()->{music.toggle();refreshMusicControl();});box.addView(musicButton);
    musicCaption=label(box,music.caption(),13,scene.getColor(R.color.text_secondary));
    LinearLayout more=Design.column(scene);more.setVisibility(View.GONE);Alternatives.gallery(more,c,true,()->{});
    box.addView(action(scene,"Otras alternativas",false,()->more.setVisibility(more.getVisibility()==View.VISIBLE?View.GONE:View.VISIBLE)));box.addView(more);
    box.addView(action(scene,"Descansar · ir a Inicio",false,this::home));
    box.addView(action(scene,"Llamadas",false,()->{
      try{service.startActivity(new Intent(Intent.ACTION_DIAL).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));}catch(RuntimeException e){home();}
    }));
    label(box,"No necesitas hacerlo todo. Empieza por cinco minutos.",14,scene.getColor(R.color.text_secondary));
    FrameLayout environment=new FrameLayout(scene);
    FloatingLeaves floating=new FloatingLeaves(scene);
    PauseVideoView cinematic=new PauseVideoView(scene);cinematic.frames((position,duration,moving)->{orbit.videoFrame(position,duration,moving);floating.frame(android.os.SystemClock.uptimeMillis()/1000.0,moving);});environment.addView(cinematic,new FrameLayout.LayoutParams(-1,-1));
    View shade=new View(scene);shade.setBackground(new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,new int[]{0x55000000,0x00000000,0xED09090A}));environment.addView(shade,new FrameLayout.LayoutParams(-1,-1));
    TextView status=Design.text(scene,cinematic.status(),12,false);status.setGravity(Gravity.CENTER);status.setOnClickListener(v->cinematic.retry());box.addView(status);
    Button saving=action(scene,"Ahorro de energía",false,()->{var prefs=scene.getSharedPreferences("foco-ambient",0);prefs.edit().putBoolean("saving",!prefs.getBoolean("saving",false)).apply();});box.addView(saving);
    cinematic.changed(()->{orbit.motion(cinematic.shouldPlay());floating.configure();status.setText(cinematic.status());saving.setText(scene.getSharedPreferences("foco-ambient",0).getBoolean("saving",false)?"Ahorro de energía · activado":"Ahorro de energía · desactivado");});
    scroll.setBackgroundColor(0);environment.addView(scroll,new FrameLayout.LayoutParams(-1,-1));
    environment.addView(floating,new FrameLayout.LayoutParams(-1,-1));floating.protect(scroll);scroll.setOnScrollChangeListener((v,x,y,ox,oy)->floating.rebuildMask());floating.post(floating::rebuildMask);
    view=environment;
    view.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener(){
      @Override public void onViewAttachedToWindow(View v){}
      @Override public void onViewDetachedFromWindow(View v){music.stop();}
    });
    WindowManager.LayoutParams p=params(-1,-1,WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
    p.gravity=Gravity.FILL;wm.addView(view,p);music.enter();refreshMusicControl();
  }
  public void warn(){
    dismiss();warning=true;
    TextView text=new TextView(service);text.setText("Foco · te queda 1 minuto de ocio. ¿Qué quieres hacer después?");text.setTextColor(0xFFF6F7FB);text.setTextSize(16);int pad=Design.dp(service,16);text.setPadding(pad,pad,pad,pad);
    GradientDrawable bg=new GradientDrawable();bg.setColor(0xFF284533);bg.setCornerRadius(pad);text.setBackground(bg);view=text;
    WindowManager.LayoutParams p=params(-1,-2,WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    p.gravity=Gravity.TOP;p.y=Design.dp(service,48);wm.addView(view,p);main.postDelayed(hide,3500);
  }
  private Button action(Context context,String text,boolean primary,Runnable run){
    Button b=Design.button(context,text,primary,run);GradientDrawable bg=new GradientDrawable();bg.setColor(primary?0xED493517:0xDE121416);bg.setCornerRadius(Design.dp(context,18));bg.setStroke(Design.dp(context,1),primary?0xFFFFD783:0x997E683E);b.setBackground(bg);b.setTextColor(primary?0xFFFFE6AA:0xFFE4D0A1);return b;
  }
  private TextView label(LinearLayout box,String s,int size,int color){TextView t=Design.text(box.getContext(),s,size,true);t.setTextColor(color);t.setGravity(Gravity.CENTER);box.addView(t);return t;}
  private void refreshMusicControl(){if(musicButton!=null)musicButton.setText(music.button());if(musicCaption!=null)musicCaption.setText(music.caption());}
  private WindowManager.LayoutParams params(int w,int h,int flags){return new WindowManager.LayoutParams(w,h,WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,flags,PixelFormat.TRANSLUCENT);}
  private void home(){service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME);}
  public void dismiss(){music.stop();musicButton=null;musicCaption=null;main.removeCallbacks(hide);if(view!=null){try{wm.removeViewImmediate(view);}catch(RuntimeException ignored){}view=null;}warning=false;remaining=null;shownMinutes=-1;}
}
