package es.foco.next.runtime;

import android.Manifest;
import android.app.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.os.Build;
import es.foco.next.R;
import es.foco.next.core.ReminderTime;
import es.foco.next.ui.MainActivity;
import java.time.*;

/** A single optional non-wakeup inexact alarm; no service, polling or note access. */
public final class DailyReminder extends BroadcastReceiver {
  private static final String ACTION="es.foco.next.DAILY_REMINDER",CHANNEL="daily-small-step";
  private static final int REQUEST=5201,NOTIFICATION=5202;
  private static SharedPreferences prefs(Context c){return c.getSharedPreferences("foco-daily-reminder",Context.MODE_PRIVATE);}
  public static boolean enabled(Context c){return prefs(c).getBoolean("enabled",false);}
  public static int minute(Context c){return prefs(c).getInt("minute",20*60);}
  public static boolean allowed(Context c){
    if(Build.VERSION.SDK_INT>=33&&c.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)return false;
    NotificationManager n=c.getSystemService(NotificationManager.class);
    if(n==null||!n.areNotificationsEnabled())return false;
    NotificationChannel channel=n.getNotificationChannel(CHANNEL);
    return channel==null||channel.getImportance()!=NotificationManager.IMPORTANCE_NONE;
  }
  public static void configure(Context c,boolean on,int minute){
    if(minute<0||minute>=1440)throw new IllegalArgumentException("Hora no válida");
    if(!prefs(c).edit().putBoolean("enabled",on).putInt("minute",minute).putLong("next",0).commit())throw new IllegalStateException("No se pudo guardar el recordatorio.");
    if(on){
      NotificationChannel channel=new NotificationChannel(CHANNEL,"Un pequeño paso diario",NotificationManager.IMPORTANCE_LOW);
      channel.setDescription("Un aviso tranquilo para leer, aprender o apuntar una idea.");channel.setShowBadge(false);channel.setSound(null,null);channel.enableVibration(false);
      c.getSystemService(NotificationManager.class).createNotificationChannel(channel);
    }
    schedule(c,true);
  }
  private static PendingIntent operation(Context c,long when){
    String day=Instant.ofEpochMilli(when).atZone(ZoneId.systemDefault()).toLocalDate().toString();
    Intent i=new Intent(c,DailyReminder.class).setAction(ACTION).putExtra("day",day);
    return PendingIntent.getBroadcast(c,REQUEST,i,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
  }
  public static void ensure(Context c){schedule(c,false);}
  private static void schedule(Context c,boolean reset){
    if(!reset&&!enabled(c)&&!prefs(c).contains("next"))return;
    AlarmManager alarms=c.getSystemService(AlarmManager.class);if(alarms==null)return;
    if(!enabled(c)||!allowed(c)){
      PendingIntent existing=PendingIntent.getBroadcast(c,REQUEST,new Intent(c,DailyReminder.class).setAction(ACTION),PendingIntent.FLAG_NO_CREATE|PendingIntent.FLAG_IMMUTABLE);
      if(existing!=null){alarms.cancel(existing);existing.cancel();}
      if(prefs(c).contains("next"))prefs(c).edit().remove("next").apply();
      if(!enabled(c))c.getSystemService(NotificationManager.class).cancel(NOTIFICATION);
      return;
    }
    long now=System.currentTimeMillis(),next=prefs(c).getLong("next",0);
    if(reset||next<=now)next=ReminderTime.next(now,ZoneId.systemDefault(),minute(c));
    alarms.setWindow(AlarmManager.RTC,next,15*60_000L,operation(c,next));
    if(next!=prefs(c).getLong("next",0))prefs(c).edit().putLong("next",next).apply();
  }
  @Override public void onReceive(Context c,Intent i){
    if(i==null)return;
    if(!ACTION.equals(i.getAction())){schedule(c,true);return;}
    String today=LocalDate.now().toString();
    boolean deliver=enabled(c)&&allowed(c)&&today.equals(i.getStringExtra("day"))&&!today.equals(prefs(c).getString("lastDay",""));
    // Re-arm the next local occurrence before returning. No worker remains running.
    schedule(c,true);
    if(!deliver)return;
    Intent open=new Intent(c,MainActivity.class).putExtra("open_day",true).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
    PendingIntent content=PendingIntent.getActivity(c,REQUEST,open,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
    Notification n=new Notification.Builder(c,CHANNEL).setSmallIcon(R.drawable.leaf)
        .setContentTitle("Un pequeño paso para ti")
        .setContentText("Unas páginas, una idea o una pausa tranquila. ¿Qué eliges hoy?")
        .setContentIntent(content).setAutoCancel(true).setVisibility(Notification.VISIBILITY_PRIVATE).setOnlyAlertOnce(true).build();
    try{
      c.getSystemService(NotificationManager.class).notify(NOTIFICATION,n);
      prefs(c).edit().putString("lastDay",today).apply();
    }catch(SecurityException ignored){}
  }
}
