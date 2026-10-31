package es.foco.next.runtime;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import es.foco.next.core.AppConfig;
import es.foco.next.core.SensitiveApps;
import java.text.Normalizer;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/** Only reads installed-app metadata, never banking screen contents. */
public final class BankAppGuard {
  private final Context context;
  private final Map<String,Boolean> labels=new HashMap<>();
  public BankAppGuard(Context c){context=c;}
  public boolean hide(AppConfig c,String pkg){
    if(pkg==null||pkg.isEmpty())return false;
    if(SensitiveApps.isSensitive(c,pkg))return true;
    Boolean known=labels.get(pkg);if(known!=null)return known;
    boolean bank=false;
    try{
      ApplicationInfo info=context.getPackageManager().getApplicationInfo(pkg,0);
      String name=Normalizer.normalize(context.getPackageManager().getApplicationLabel(info).toString(),Normalizer.Form.NFD)
          .replaceAll("\\p{M}","").toLowerCase(Locale.ROOT);
      bank=name.contains("banco general")||name.contains("banco ")||name.contains(" bank")||name.startsWith("bank ");
    }catch(android.content.pm.PackageManager.NameNotFoundException|RuntimeException ignored){}
    if(labels.size()>256)labels.clear();labels.put(pkg,bank);return bank;
  }
}
