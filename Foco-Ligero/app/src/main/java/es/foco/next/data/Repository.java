package es.foco.next.data;

import android.content.Context;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.AtomicFile;
import es.foco.next.core.*;
import org.json.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** One live state, atomic checkpoints, and an explicit one-time migration. */
public final class Repository {
  public interface Listener {void changed();}
  private static Repository instance;
  public static synchronized Repository get(Context c){if(instance==null)instance=new Repository(c.getApplicationContext());return instance;}
  static synchronized void resetForTests(){instance=null;}
  private final Context context;
  private final AtomicFile file;
  private final FocusEngine engine=new FocusEngine();
  private AppConfig config=new AppConfig();
  private FocusState state=new FocusState();
  private final List<Listener> listeners=new ArrayList<>();
  private long lastSave;
  public boolean migrated,legacyVpn;
  private Repository(Context c){
    context=c;file=new AtomicFile(new File(c.getFilesDir(),"focus-lite-v1.json"));
    try{
      if(file.getBaseFile().exists()||new File(file.getBaseFile()+".bak").exists())read(new JSONObject(new String(file.readFully(),StandardCharsets.UTF_8)));
      else{
        AtomicFile old=new AtomicFile(new File(c.getFilesDir(),"focus-state-v1.json"));
        if(old.getBaseFile().exists()||new File(old.getBaseFile()+".bak").exists())migrate(new JSONObject(new String(old.readFully(),StandardCharsets.UTF_8)));
        write();
      }
      FocusEngine.forget(state);
    }catch(Exception e){throw new IllegalStateException("No se pudieron abrir los datos. Los archivos originales se conservan.",e);}
  }
  public synchronized AppConfig config(){return decodeConfig(encodeConfig(config));}
  public synchronized FocusState state(){return decodeState(encodeState(state));}
  public synchronized boolean locked(){return liveUsed()>=FocusEngine.BUDGET&&remaining()>0;}
  public synchronized long remaining(){return FocusEngine.remaining(state,System.currentTimeMillis(),SystemClock.elapsedRealtime(),boot(context));}
  public synchronized long liveUsed(){
    if(remaining()==0)return 0;
    long delta=state.observedBoot==boot(context)&&state.observedElapsed>=0?SystemClock.elapsedRealtime()-state.observedElapsed:0;
    if(delta<0||delta>FocusEngine.CHECKPOINT+5000)delta=0;
    return Math.min(FocusEngine.BUDGET,state.usedMillis+(state.observedAlternative?0:delta));
  }
  public synchronized FocusEngine.Decision sample(FocusEngine.Input in,boolean checkpoint){
    long used=state.usedMillis;String day=state.day,pkg=state.observedPackage;
    boolean present=state.window.present();
    FocusEngine.Decision d=engine.step(config,state,in);
    boolean threshold=used<FocusEngine.BUDGET&&state.usedMillis>=FocusEngine.BUDGET;
    if(checkpoint||threshold||d.warning||present!=state.window.present()||!day.equals(state.day)
        ||!pkg.equals(state.observedPackage)||((!pkg.isEmpty()||!state.observedPackage.isEmpty())&&in.elapsed-lastSave>=FocusEngine.CHECKPOINT)){write();lastSave=in.elapsed;}
    return d;
  }
  public synchronized void forget(){FocusEngine.forget(state);write();}
  public void saveConfig(AppConfig next){
    synchronized(this){
      if(locked())throw new IllegalStateException("Tu hora sigue en curso. El plan se puede cambiar al terminar.");
      AppConfig clean=decodeConfig(encodeConfig(next));validate(clean,state);AppConfig old=config;config=clean;try{write();}catch(RuntimeException e){config=old;throw e;}
    }
    for(Listener l:new ArrayList<>(listeners))l.changed();
  }
  public void addListener(Listener l){listeners.add(l);}
  public void removeListener(Listener l){listeners.remove(l);}
  public synchronized void recordChoice(String pkg){
    checkPackage(pkg);String day=java.time.LocalDate.now().toString();
    FocusEngine.record(state.weeklyChoices,day,pkg,1);FocusEngine.prune(state,day);write();
  }
  public synchronized String exportWeek(){
    FocusEngine.prune(state,java.time.LocalDate.now().toString());
    StringBuilder csv=new StringBuilder("fecha,tipo,paquete,app,segundos_observados,veces\r\n");
    Map<String,Map<String,Map<String,Long>>> kinds=new LinkedHashMap<>();kinds.put("ocio",state.weeklyOcio);kinds.put("alternativa_elegida",state.weeklyAlternative);kinds.put("alternativa_abierta_desde_foco",state.weeklyChoices);
    for(var kind:kinds.entrySet())
      for(String day:new TreeSet<>(kind.getValue().keySet()))for(var e:kind.getValue().get(day).entrySet()){
        String label=e.getKey();try{label=context.getPackageManager().getApplicationLabel(context.getPackageManager().getApplicationInfo(label,0)).toString();}catch(Exception ignored){}
        boolean choice=kind.getKey().equals("alternativa_abierta_desde_foco");
        csv.append(cell(day)).append(',').append(cell(kind.getKey())).append(',').append(cell(e.getKey())).append(',').append(cell(label)).append(',').append(choice?0:e.getValue()/1000).append(',').append(choice?e.getValue():0).append("\r\n");
      }
    for(String day:new TreeSet<>(state.weeklyBlocks.keySet()))csv.append(cell(day)).append(",bloqueo_por_limite,,,0,").append(state.weeklyBlocks.get(day)).append("\r\n");
    return csv.toString();
  }
  private static String cell(String value){
    if(!value.isEmpty()&&"=+-@\t\r\n".indexOf(value.charAt(0))>=0)value="'"+value;
    return "\""+value.replace("\"","\"\"")+"\"";
  }
  public synchronized String exportBackup(){return document().toString();}
  public void importBackup(String raw){
    synchronized(this){
      if(locked())throw new IllegalStateException("No puedes sustituir el plan durante el bloqueo.");
      if(raw.length()>1_048_576)throw new IllegalArgumentException("Copia demasiado grande");
      try{
        JSONObject j=new JSONObject(raw);
        if(j.getInt("format")!=51)throw new IllegalArgumentException("Usa una copia de Foco Ligero");
        AppConfig c=decodeConfig(j.getJSONObject("config"));FocusState s=decodeState(j.getJSONObject("state"));validate(c,s);
        AppConfig oldConfig=config;FocusState oldState=state;
        config=c;state=s;
        try{write();}catch(RuntimeException e){config=oldConfig;state=oldState;throw e;}
      }catch(JSONException e){throw new IllegalArgumentException("Copia no válida",e);}
    }
    for(Listener l:new ArrayList<>(listeners))l.changed();
  }
  public static int boot(Context c){try{return Settings.Global.getInt(c.getContentResolver(),Settings.Global.BOOT_COUNT,-1);}catch(RuntimeException e){return -1;}}
  private void migrate(JSONObject j)throws JSONException{
    JSONObject c=j.getJSONObject("config"),s=j.getJSONObject("state");
    config.enabled=c.optBoolean("enabled",true);config.returnApp=c.optString("returnApp","");
    if(AlternativeCatalog.social(config.returnApp))config.returnApp="";
    config.watchedApps=set(c.optJSONArray("watchedApps"));config.sensitiveApps=set(c.optJSONArray("sensitiveApps"));
    if(c.optBoolean("autoSocial",true))Collections.addAll(config.watchedApps,
        "com.zhiliaoapp.musically","com.ss.android.ugc.trill","com.instagram.android","com.facebook.katana",
        "com.facebook.lite","com.google.android.youtube","com.twitter.android","com.reddit.frontpage","com.snapchat.android");
    config.watchedApps.removeAll(set(c.optJSONArray("excludedApps")));
    config.watchedApps.removeIf(p->FocusEngine.essential(config,p)||p.equals(config.returnApp));
    state.day=s.optString("day","");state.dailyPackageMillis=map(s.optJSONObject("dailyPackageMillis"));
    // Old 10-minute/one-hour visual cycles have different semantics. Archive, don't reinterpret.
    if(!state.day.isEmpty())state.weeklyOcio.put(state.day,new LinkedHashMap<>(state.dailyPackageMillis));
    migrated=true;legacyVpn=c.optBoolean("dnsEnabled",false);validate(config,state);
  }
  private void read(JSONObject j)throws JSONException{
    if(j.getInt("format")!=51)throw new IllegalArgumentException("Formato desconocido");
    config=decodeConfig(j.getJSONObject("config"));state=decodeState(j.getJSONObject("state"));
    migrated=j.optBoolean("migrated");legacyVpn=j.optBoolean("legacyVpn");validate(config,state);
  }
  private JSONObject document(){
    JSONObject j=new JSONObject();put(j,"format",51);put(j,"config",encodeConfig(config));put(j,"state",encodeState(state));put(j,"migrated",migrated);put(j,"legacyVpn",legacyVpn);return j;
  }
  private void write(){
    byte[] bytes=document().toString().getBytes(StandardCharsets.UTF_8);FileOutputStream out=null;
    try{out=file.startWrite();out.write(bytes);file.finishWrite(out);}catch(IOException e){if(out!=null)file.failWrite(out);throw new IllegalStateException("No se pudieron guardar los datos",e);}
  }
  private static JSONObject encodeConfig(AppConfig c){
    JSONObject j=new JSONObject();put(j,"enabled",c.enabled);put(j,"watchedApps",new JSONArray(c.watchedApps));put(j,"sensitiveApps",new JSONArray(c.sensitiveApps));put(j,"intention",c.intention);put(j,"returnApp",c.returnApp);return j;
  }
  private static AppConfig decodeConfig(JSONObject j){
    AppConfig c=new AppConfig();c.enabled=j.optBoolean("enabled",true);c.watchedApps=set(j.optJSONArray("watchedApps"));c.sensitiveApps=set(j.optJSONArray("sensitiveApps"));c.intention=j.optString("intention",c.intention);c.returnApp=j.optString("returnApp","");if(AlternativeCatalog.social(c.returnApp))c.returnApp="";return c;
  }
  private static JSONObject encodeState(FocusState s){
    JSONObject j=new JSONObject(),d=new JSONObject();put(d,"wall",s.window.wall);put(d,"elapsed",s.window.elapsed);put(d,"boot",s.window.boot);
    put(j,"window",d);put(j,"usedMillis",s.usedMillis);put(j,"warningSent",s.warningSent);put(j,"lockedApps",new JSONArray(s.lockedApps));put(j,"day",s.day);put(j,"dailyPackageMillis",new JSONObject(s.dailyPackageMillis));
    put(j,"weeklyOcio",nested(s.weeklyOcio));put(j,"weeklyAlternative",nested(s.weeklyAlternative));put(j,"weeklyChoices",nested(s.weeklyChoices));put(j,"weeklyBlocks",new JSONObject(s.weeklyBlocks));return j;
  }
  private static FocusState decodeState(JSONObject j){
    FocusState s=new FocusState();JSONObject d=j.optJSONObject("window");
    if(d!=null){s.window.wall=d.optLong("wall");s.window.elapsed=d.optLong("elapsed");s.window.boot=d.optInt("boot",-1);}
    s.usedMillis=j.optLong("usedMillis");s.warningSent=j.optBoolean("warningSent");s.lockedApps=set(j.optJSONArray("lockedApps"));s.day=j.optString("day","");s.dailyPackageMillis=map(j.optJSONObject("dailyPackageMillis"));
    s.weeklyOcio=nestedMap(j.optJSONObject("weeklyOcio"));s.weeklyAlternative=nestedMap(j.optJSONObject("weeklyAlternative"));s.weeklyChoices=nestedMap(j.optJSONObject("weeklyChoices"));s.weeklyBlocks=map(j.optJSONObject("weeklyBlocks"));return s;
  }
  private static JSONObject nested(Map<String,Map<String,Long>> values){JSONObject j=new JSONObject();for(var e:values.entrySet())put(j,e.getKey(),new JSONObject(e.getValue()));return j;}
  private static Map<String,Map<String,Long>> nestedMap(JSONObject j){
    Map<String,Map<String,Long>> out=new LinkedHashMap<>();if(j==null)return out;
    if(j.length()>7)throw new IllegalArgumentException("El registro contiene más de siete días");
    Iterator<String> keys=j.keys();while(keys.hasNext()){String k=keys.next();out.put(k,map(j.optJSONObject(k)));}return out;
  }
  private static Set<String> set(JSONArray a){
    Set<String> s=new LinkedHashSet<>();if(a!=null){if(a.length()>2000)throw new IllegalArgumentException("Lista demasiado grande");for(int i=0;i<a.length();i++)s.add(a.optString(i));}return s;
  }
  private static Map<String,Long> map(JSONObject j){
    Map<String,Long> m=new LinkedHashMap<>();if(j!=null){if(j.length()>2000)throw new IllegalArgumentException("Lista demasiado grande");Iterator<String> keys=j.keys();while(keys.hasNext()){String k=keys.next();m.put(k,j.optLong(k,-1));}}return m;
  }
  private static void validate(AppConfig c,FocusState s){
    if(c.intention.length()>120||s.usedMillis<0||s.usedMillis>FocusEngine.BUDGET||s.window.wall<0||s.window.elapsed<0||s.window.boot< -1)throw new IllegalArgumentException("Valores no válidos");
    for(Set<String> pkgs:java.util.Arrays.asList(c.watchedApps,c.sensitiveApps,s.lockedApps))for(String p:pkgs)checkPackage(p);
    if(!c.returnApp.isEmpty()){checkPackage(c.returnApp);if(c.watchedApps.contains(c.returnApp))throw new IllegalArgumentException("La alternativa no puede estar en ocio");}
    for(var e:s.dailyPackageMillis.entrySet()){checkPackage(e.getKey());if(e.getValue()<0||e.getValue()>24*FocusEngine.HOUR)throw new IllegalArgumentException("Tiempo no válido");}
    for(Map<String,Map<String,Long>> history:java.util.Arrays.asList(s.weeklyOcio,s.weeklyAlternative,s.weeklyChoices)){
      if(history.size()>7)throw new IllegalArgumentException("Registro demasiado grande");
      for(var row:history.entrySet()){java.time.LocalDate.parse(row.getKey());for(var value:row.getValue().entrySet()){checkPackage(value.getKey());if(value.getValue()<0||value.getValue()>24*FocusEngine.HOUR)throw new IllegalArgumentException("Registro no válido");}}
    }
    if(s.weeklyBlocks.size()>7)throw new IllegalArgumentException("Registro demasiado grande");
    for(var e:s.weeklyBlocks.entrySet()){java.time.LocalDate.parse(e.getKey());if(e.getValue()<0||e.getValue()>100000)throw new IllegalArgumentException("Registro no válido");}
    if(s.usedMillis>=FocusEngine.BUDGET&&s.lockedApps.isEmpty())s.lockedApps.addAll(c.watchedApps);
  }
  private static void checkPackage(String p){if(p.length()>256||!p.matches("[A-Za-z0-9_]+(\\.[A-Za-z0-9_]+)+"))throw new IllegalArgumentException("Paquete no válido");}
  private static void put(JSONObject j,String k,Object v){try{j.put(k,v);}catch(JSONException e){throw new IllegalArgumentException(e);}}
}
