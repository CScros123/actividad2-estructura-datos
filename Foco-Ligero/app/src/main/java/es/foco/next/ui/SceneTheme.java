package es.foco.next.ui;
import android.content.Context;
import android.content.res.Configuration;
import android.view.ContextThemeWrapper;
import es.foco.next.R;
import es.foco.next.core.DayScene;
import java.time.ZoneId;
public final class SceneTheme {
  private SceneTheme(){}
  private static java.time.Clock wallClock=java.time.Clock.systemUTC();
  public static boolean night(){return DayScene.night(wallClock.millis(),ZoneId.systemDefault());}
  static Configuration override(Context base){
    Configuration c=new Configuration();c.uiMode=(base.getResources().getConfiguration().uiMode&~Configuration.UI_MODE_NIGHT_MASK)|(night()?Configuration.UI_MODE_NIGHT_YES:Configuration.UI_MODE_NIGHT_NO);return c;
  }
  public static Context context(Context base){return new ContextThemeWrapper(base.createConfigurationContext(override(base)),R.style.AppTheme);}
  public static int landscape(){return night()?R.drawable.coast_night:R.drawable.coast_day;}
  public static long untilBoundary(){long now=wallClock.millis();return Math.max(1,DayScene.nextBoundary(now,ZoneId.systemDefault())-now);}
}
