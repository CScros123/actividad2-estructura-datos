package es.foco.next.ui;

import android.animation.ValueAnimator;
import android.content.*;
import android.graphics.*;
import android.os.*;
import android.view.View;
import android.widget.*;

/** Independent vector layers; no bitmap decoding, layouts or object creation per frame. */
public final class AmbientView extends View {
  private final Paint paint=new Paint(3);
  private final Path path=new Path();
  private final android.content.SharedPreferences prefs;
  private final PowerManager power;
  private boolean active,attached,quiet,running;
  private boolean nature=true,reduce,saving,leaves=true,depth=true;
  private int intensity=1;
  private TerraceView terrace;
  private FloatingLeaves foreground;
  void foreground(FloatingLeaves view){foreground=view;foreground.configure();}
  private Runnable policyChanged;
  private final android.content.SharedPreferences.OnSharedPreferenceChangeListener preference=(p,key)->refresh();
  private float scroll;
  private double sceneTime;
  private final float density;
  private final boolean night;
  private final Runnable frame=()->{if(running){invalidate();if(foreground!=null)foreground.frame(SystemClock.uptimeMillis()/1000.0,true);if(terrace!=null)terrace.tick(SystemClock.uptimeMillis()/1000.0);postDelayed(thisFrame(),interval());}};
  private Runnable thisFrame(){return frame;}
  private final BroadcastReceiver saver=new BroadcastReceiver(){public void onReceive(Context c,Intent i){refresh();}};
  private final android.database.ContentObserver motion=new android.database.ContentObserver(new Handler(Looper.getMainLooper())){public void onChange(boolean self){refresh();}};
  public AmbientView(Context c){this(c,null,false);}
  public AmbientView(Context c,android.util.AttributeSet attrs){this(c,attrs,false);}
  public AmbientView(Context c,boolean quiet){this(c,null,quiet);}
  private AmbientView(Context c,android.util.AttributeSet attrs,boolean quiet){super(c,attrs);night=SceneTheme.night();this.quiet=quiet;density=c.getResources().getDisplayMetrics().density;prefs=c.getSharedPreferences("foco-ambient",0);power=c.getSystemService(PowerManager.class);setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);setClickable(false);}
  public void setActive(boolean value){active=value;refresh();}
  public void setScroll(float value){scroll=value;if(terrace!=null)terrace.viewport();if(!running)invalidate();}
  void detail(TerraceView view){terrace=view;if(terrace!=null)terrace.wind(intensity==0?.8f:intensity==2?2:1.3f);}
  void onPolicyChanged(Runnable callback){policyChanged=callback;}
  boolean saving(){return prefs.getBoolean("saving",false);}
  void toggleSaving(){prefs.edit().putBoolean("saving",!saving()).apply();}
  boolean moving(){return attached&&active&&isShown()&&nature&&!reduce&&!saving&&!power.isPowerSaveMode()&&ValueAnimator.areAnimatorsEnabled();}
  private long interval(){return quiet?66:33;}
  public void refresh(){
    nature=prefs.getBoolean("enabled",true);reduce=prefs.getBoolean("reduce",false);saving=prefs.getBoolean("saving",false);
    leaves=prefs.getBoolean("leaves",true);depth=prefs.getBoolean("depth",true);intensity=prefs.getInt("wind",1);
    removeCallbacks(frame);running=moving();invalidate();if(terrace!=null){terrace.wind(intensity==0?.8f:intensity==2?2:1.3f);terrace.tick(sceneTime);terrace.viewport();}if(running)postDelayed(frame,interval());
    if(foreground!=null){foreground.configure();foreground.frame(sceneTime,false);}
    if(policyChanged!=null)policyChanged.run();
  }
  @Override protected void onAttachedToWindow(){super.onAttachedToWindow();attached=true;prefs.registerOnSharedPreferenceChangeListener(preference);IntentFilter f=new IntentFilter(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED);if(Build.VERSION.SDK_INT>=33)getContext().registerReceiver(saver,f,Context.RECEIVER_NOT_EXPORTED);else getContext().registerReceiver(saver,f);getContext().getContentResolver().registerContentObserver(android.provider.Settings.Global.getUriFor(android.provider.Settings.Global.ANIMATOR_DURATION_SCALE),false,motion);refresh();}
  @Override protected void onDetachedFromWindow(){attached=false;running=false;prefs.unregisterOnSharedPreferenceChangeListener(preference);removeCallbacks(frame);getContext().unregisterReceiver(saver);getContext().getContentResolver().unregisterContentObserver(motion);super.onDetachedFromWindow();}
  @Override protected void onWindowVisibilityChanged(int visibility){super.onWindowVisibilityChanged(visibility);if(prefs!=null)refresh();}
  static double wind(double seconds){return .35+.18*Math.sin(seconds/17)+.12*Math.sin(seconds/7.3)+.35*Math.pow(Math.max(0,Math.sin(seconds/41)),6);}
  private void color(int c){paint.setColor(c);paint.setStyle(Paint.Style.FILL);}
  @Override protected void onDraw(Canvas canvas){
    super.onDraw(canvas);float w=getWidth()/density,h=getHeight()/density;canvas.save();canvas.scale(density,density);
    boolean move=running;double t=sceneTime;if(move){t=SystemClock.uptimeMillis()/1000.0;sceneTime=t;}
    double breeze=wind(t)*(quiet?.45:1)*(intensity==0?.8:intensity==2?2:1.3);
    color(night?0xFF050E13:0xFF0F2419);canvas.drawRect(0,0,w,h,paint);
    // Distant clouds: slow drift, independent silhouettes, no large translucent screen overlay.
    color(night?0x163E6571:0x22CFE8D2);
    for(int i=0;i<5;i++){float x=(float)((i*w/4+t*(.9+i*.14))%(w+130))-65,y=70+i%3*55;canvas.drawOval(x-42,y-11,x+58,y+13,paint);canvas.drawOval(x-15,y-24,x+25,y+8,paint);}
    float par=depth?Math.min(14,scroll/density*.018f):0;
    color(night?0xFF102B2C:0xFF16321F);path.reset();path.moveTo(0,h*.43f-par*.2f);path.cubicTo(w*.3f,h*.22f,w*.42f,h*.54f,w,h*.32f);path.lineTo(w,h);path.lineTo(0,h);path.close();canvas.drawPath(path,paint);
    color(night?0xFF142E27:0xFF1C3E28);path.reset();path.moveTo(0,h*.58f-par*.4f);path.cubicTo(w*.4f,h*.36f,w*.65f,h*.65f,w,h*.47f);path.lineTo(w,h);path.lineTo(0,h);path.close();canvas.drawPath(path,paint);
    // Terrace and grasses stay at the edges, leaving all text and touch targets clear.
    color(night?0xFF172620:0xFF14301F);canvas.drawRect(0,h*.85f,w,h,paint);
    paint.setStrokeWidth(1.3f);paint.setStyle(Paint.Style.STROKE);paint.setColor(night?0x884C775C:0x9970A877);
    for(int i=0;i<30;i++){float x=i<15?i*1.5f:w-(i-15)*1.5f;float len=25+(i*17%63),sway=(float)(Math.sin(t*.7+i*1.7)*breeze*12);path.reset();path.moveTo(x,h);path.quadTo(x+sway,h-len*.6f,x+sway*1.8f,h-len);canvas.drawPath(path,paint);}
    paint.setStyle(Paint.Style.FILL);
    // Nearby leaf clusters. Soft translucent silhouettes rather than a costly full-screen blur.
    for(int i=0;i<16;i++){float x=i<8?8:w-8,y=100+(i%8)*67-par;float sway=(float)(Math.sin(t*.48+i*2.1)*breeze*11);canvas.save();canvas.translate(x+sway,y);canvas.rotate((i<8?-35:35)+(float)Math.sin(t*.43+i)*7);color(night?0x623B694C:0x7A5C9A64);canvas.drawOval(-12,-5,12,5,paint);color(night?0x193B694C:0x225C9A64);canvas.drawOval(-14,-7,14,7,paint);canvas.restore();}
    if(move&&leaves){for(int i=0;i<3;i++){double cycle=t+i*43;float y=(float)((cycle*8)%(h+180))-90;float x=8+(float)((Math.sin(cycle*.18)+1)*6);if(i%2==1)x=w-x;canvas.save();canvas.translate(x,y);canvas.rotate((float)cycle*13);color(night?0x805A8053:0xB08CC07A);canvas.drawOval(-4,-1.5f,4,1.5f,paint);canvas.restore();}}
    canvas.restore();
  }
  static void settings(LinearLayout parent,AmbientView scene){
    LinearLayout box=Design.card(parent,"Ambiente visual");Context c=parent.getContext();android.content.SharedPreferences p=c.getSharedPreferences("foco-ambient",0);
    String[] labels={"Naturaleza animada","Hojas flotantes","Efecto de profundidad","Modo ahorro de batería","Reducir movimiento"};String[] keys={"enabled","leaves","depth","saving","reduce"};
    for(int i=0;i<keys.length;i++){final String key=keys[i];Switch s=new Switch(c);s.setText(labels[i]);s.setTextColor(c.getColor(es.foco.next.R.color.text_primary));s.setMinHeight(Design.dp(c,52));s.setChecked(p.getBoolean(key,i<3));s.setOnCheckedChangeListener((v,on)->{p.edit().putBoolean(key,on).apply();scene.refresh();});box.addView(s);}
    String[] winds={"Suave","Natural","Intensa"};Button b=Design.button(c,"Intensidad del viento · "+winds[p.getInt("wind",1)],false,()->{});b.setOnClickListener(v->new android.app.AlertDialog.Builder(c).setTitle("Intensidad del viento").setSingleChoiceItems(winds,p.getInt("wind",1),(d,n)->{p.edit().putInt("wind",n).apply();b.setText("Intensidad del viento · "+winds[n]);scene.refresh();d.dismiss();}).setNegativeButton("Cancelar",null).show());box.addView(b);
    Design.note(box,"Solo mientras Foco está visible. El botón de ahorro, el ahorro del sistema y reducir movimiento dejan el paisaje quieto. Sin vídeo ni permisos nuevos.");
  }
}
