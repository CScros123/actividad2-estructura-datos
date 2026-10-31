package es.foco.next.ui;

import android.animation.ValueAnimator;
import android.content.*;
import android.graphics.*;
import android.media.MediaPlayer;
import android.os.*;
import android.view.*;
import android.widget.*;
import es.foco.next.R;

/** Muted local video only while the pause window is visible. Poster is the safe fallback. */
final class PauseVideoView extends FrameLayout implements TextureView.SurfaceTextureListener {
  private final TextureView texture;
  private final PowerManager power;
  private final android.content.SharedPreferences prefs;
  private MediaPlayer player;
  private Surface surface;
  private boolean attached,prepared,failed,screen=true;
  interface MotionFrame {void update(int position,int duration,boolean moving);}
  private MotionFrame motionFrame;
  private int duration;
  void frames(MotionFrame callback){motionFrame=callback;}
  private void notifyFrame(boolean moving){if(motionFrame!=null){int position=0;if(prepared&&player!=null)try{position=player.getCurrentPosition();}catch(IllegalStateException ignored){}motionFrame.update(position,duration,moving);}}
  private Runnable changed;
  private final android.content.SharedPreferences.OnSharedPreferenceChangeListener preferences=(p,key)->sync();
  private final BroadcastReceiver receiver=new BroadcastReceiver(){public void onReceive(Context c,Intent i){if(Intent.ACTION_SCREEN_OFF.equals(i.getAction()))screen=false;else if(Intent.ACTION_SCREEN_ON.equals(i.getAction()))screen=true;sync();}};
  private final android.database.ContentObserver motion=new android.database.ContentObserver(new Handler(Looper.getMainLooper())){public void onChange(boolean self){sync();}};
  PauseVideoView(Context c){
    super(c);power=c.getSystemService(PowerManager.class);prefs=c.getSharedPreferences("foco-ambient",0);
    ImageView poster=new ImageView(c);poster.setImageResource(R.drawable.sanctuary);poster.setScaleType(ImageView.ScaleType.CENTER_CROP);addView(poster,new LayoutParams(-1,-1));
    texture=new TextureView(c);texture.setOpaque(false);texture.setAlpha(1);texture.setSurfaceTextureListener(this);addView(texture,new LayoutParams(-1,-1));
    setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS);
  }
  boolean animationAllowed(){return !prefs.getBoolean("saving",false)&&!prefs.getBoolean("reduce",false)&&ValueAnimator.areAnimatorsEnabled()&&!power.isPowerSaveMode();}
  boolean shouldPlay(){return attached&&screen&&power.isInteractive()&&getWindowVisibility()==VISIBLE&&isShown()&&animationAllowed();}
  boolean hasPlayer(){return player!=null;}
  void changed(Runnable callback){changed=callback;}
  void retry(){failed=false;sync();}
  String status(){return failed?"La animación está en pausa. Toca para reintentar.":!animationAllowed()?"Escena en ahorro de energía":prepared?"Una pausa para volver a ti":"Preparando tu espacio de calma…";}
  private void notifyChanged(){if(changed!=null)changed.run();}
  private void sync(){
    if(!shouldPlay()){release();notifyChanged();return;}
    texture.setAlpha(1);
    if(player!=null||!texture.isAvailable()||failed){notifyChanged();return;}
    try{
      surface=new Surface(texture.getSurfaceTexture());MediaPlayer next=new MediaPlayer();player=next;
      try(android.content.res.AssetFileDescriptor file=getResources().openRawResourceFd(R.raw.sanctuary_motion)){next.setDataSource(file.getFileDescriptor(),file.getStartOffset(),file.getLength());}
      next.setSurface(surface);next.setVolume(0,0);next.setLooping(true);
      next.setOnPreparedListener(p->{if(player!=p)return;if(!shouldPlay()){release();return;}prepared=true;duration=p.getDuration();crop(p.getVideoWidth(),p.getVideoHeight());texture.setAlpha(1);p.start();notifyChanged();});
      next.setOnErrorListener((p,a,b)->{failed=true;release();notifyChanged();return true;});next.prepareAsync();
    }catch(Exception e){failed=true;release();}
    notifyChanged();
  }
  private void crop(int width,int height){if(width<=0||height<=0||texture.getWidth()==0||texture.getHeight()==0)return;float video=width/(float)height,view=texture.getWidth()/(float)texture.getHeight();Matrix m=new Matrix();m.setScale(Math.max(1,video/view),Math.max(1,view/video),texture.getWidth()/2f,texture.getHeight()/2f);texture.setTransform(m);}
  private void release(){notifyFrame(false);prepared=false;texture.setAlpha(0);if(player!=null){MediaPlayer old=player;player=null;old.setOnPreparedListener(null);old.setOnErrorListener(null);try{old.release();}catch(RuntimeException ignored){}}if(surface!=null){surface.release();surface=null;}}
  @Override protected void onAttachedToWindow(){super.onAttachedToWindow();attached=true;prefs.registerOnSharedPreferenceChangeListener(preferences);IntentFilter f=new IntentFilter();f.addAction(Intent.ACTION_SCREEN_OFF);f.addAction(Intent.ACTION_SCREEN_ON);f.addAction(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED);if(Build.VERSION.SDK_INT>=33)getContext().registerReceiver(receiver,f,Context.RECEIVER_NOT_EXPORTED);else getContext().registerReceiver(receiver,f);getContext().getContentResolver().registerContentObserver(android.provider.Settings.Global.getUriFor(android.provider.Settings.Global.ANIMATOR_DURATION_SCALE),false,motion);sync();}
  @Override protected void onDetachedFromWindow(){attached=false;prefs.unregisterOnSharedPreferenceChangeListener(preferences);getContext().unregisterReceiver(receiver);getContext().getContentResolver().unregisterContentObserver(motion);release();super.onDetachedFromWindow();}
  @Override protected void onWindowVisibilityChanged(int v){super.onWindowVisibilityChanged(v);if(power!=null)sync();}
  @Override public void onSurfaceTextureAvailable(SurfaceTexture s,int w,int h){sync();}
  @Override public void onSurfaceTextureSizeChanged(SurfaceTexture s,int w,int h){if(player!=null&&prepared)crop(player.getVideoWidth(),player.getVideoHeight());}
  @Override public boolean onSurfaceTextureDestroyed(SurfaceTexture s){release();return true;}
  @Override public void onSurfaceTextureUpdated(SurfaceTexture s){if(prepared&&player!=null&&attached)notifyFrame(true);}
}
