package es.foco.next.runtime;
import android.app.*;
import android.content.*;
import android.media.*;
import android.os.IBinder;
import es.foco.next.R;
import es.foco.next.ui.MainActivity;
/** Foreground only while bundled music plays on a visible Foco surface. Never sticky. */
public final class CalmAudioService extends Service {
  private static final String CHANNEL="foco-calm-audio",MUTE="es.foco.next.MUTE_MUSIC";
  private static final int ID=5301;
  private static CalmAudioService instance;
  private MediaPlayer player;
  private AudioFocusRequest focus;
  private AudioManager audio;
  private boolean exclusive;
  private final AudioManager.OnAudioFocusChangeListener focusChanges=change->{if(change<0){CalmAudio.interrupted();release();stopSelf();}};
  static void refreshFocus(){if(instance!=null&&instance.player!=null&&instance.exclusive!=CalmAudio.intervening())if(!instance.requestFocus()){CalmAudio.interrupted();instance.release();instance.stopSelf();}}
  private boolean requestFocus(){
    if(audio==null||audio.getMode()!=AudioManager.MODE_NORMAL)return false;
    if(focus!=null)audio.abandonAudioFocusRequest(focus);
    exclusive=CalmAudio.intervening();focus=new AudioFocusRequest.Builder(exclusive?AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE:AudioManager.AUDIOFOCUS_GAIN).setAudioAttributes(attrs).setOnAudioFocusChangeListener(focusChanges).build();
    return audio.requestAudioFocus(focus)==AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
  }
  private final AudioAttributes attrs=new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build();
  @Override public void onCreate(){super.onCreate();instance=this;audio=getSystemService(AudioManager.class);}
  @Override public int onStartCommand(Intent intent,int flags,int startId){
    if(intent!=null&&MUTE.equals(intent.getAction())){CalmAudio.mute(this);stopSelf();return START_NOT_STICKY;}
    if(!CalmAudio.wanted()){stopSelf();return START_NOT_STICKY;}
    try{
      NotificationChannel channel=new NotificationChannel(CHANNEL,"Música tranquila",NotificationManager.IMPORTANCE_LOW);
      channel.setSound(null,null);channel.enableVibration(false);channel.setShowBadge(false);getSystemService(NotificationManager.class).createNotificationChannel(channel);
      startForeground(ID,notification());
      if(player==null){
        if(!requestFocus()){CalmAudio.interrupted();release();stopSelf();return START_NOT_STICKY;}
        play(CalmAudio.track());
      }
    }catch(RuntimeException e){CalmAudio.interrupted();release();stopSelf();}
    return START_NOT_STICKY;
  }
  private void play(int index){
    if(player!=null)player.release();
    player=MediaPlayer.create(this,index==0?R.raw.morning_routine:R.raw.colorful_flowers,attrs,0);
    if(player==null){CalmAudio.interrupted();stopSelf();return;}
    player.setVolume(.28f,.28f);player.setLooping(false);
    player.setOnCompletionListener(p->{if(CalmAudio.wanted())play((index+1)%2);else{release();stopSelf();}});
    player.setOnErrorListener((p,what,extra)->{CalmAudio.interrupted();release();stopSelf();return true;});
    player.start();CalmAudio.ready(index);getSystemService(NotificationManager.class).notify(ID,notification());
  }
  private Notification notification(){
    PendingIntent open=PendingIntent.getActivity(this,ID,new Intent(this,MainActivity.class),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
    PendingIntent mute=PendingIntent.getService(this,ID,new Intent(this,CalmAudioService.class).setAction(MUTE),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
    return new Notification.Builder(this,CHANNEL).setSmallIcon(R.drawable.leaf).setContentTitle("Foco · un momento tranquilo").setContentText(CalmAudio.title()).setContentIntent(open).setOngoing(true).setOnlyAlertOnce(true).addAction(new Notification.Action.Builder(null,"Silenciar",mute).build()).build();
  }
  static void stopPlayback(){if(instance!=null)instance.release();}
  private void release(){if(player!=null){try{player.release();}catch(RuntimeException ignored){}player=null;}if(audio!=null&&focus!=null){audio.abandonAudioFocusRequest(focus);focus=null;}}
  @Override public void onDestroy(){release();stopForeground(STOP_FOREGROUND_REMOVE);if(instance==this)instance=null;CalmAudio.destroyed();super.onDestroy();}
  @Override public IBinder onBind(Intent i){return null;}
}
