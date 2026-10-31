package es.foco.next.ui;

import android.content.*;
import android.content.pm.*;
import android.widget.*;
import es.foco.next.core.*;
import java.util.*;

/** Installed alternatives, no store requests, feeds, tracking or productivity scoring. */
final class Alternatives {
  static final class Entry {
    final String pkg,label;final int group;
    Entry(String p,String l,int g){pkg=p;label=l;group=g;}
  }
  private static List<Entry> snapshot;
  private static long refreshedAt;
  static void invalidate(){snapshot=null;}
  private static List<Entry> scan(Context c){
    if(snapshot!=null && android.os.SystemClock.elapsedRealtime()-refreshedAt<10*60_000)return snapshot;
    List<Entry> found=new ArrayList<>();Set<String> seen=new HashSet<>();
    AppConfig base=new AppConfig();es.foco.next.runtime.BankAppGuard banks=new es.foco.next.runtime.BankAppGuard(c.getApplicationContext());
    for(ResolveInfo r:c.getPackageManager().queryIntentActivities(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER),0)){
      if(r.activityInfo==null)continue;String p=r.activityInfo.packageName;
      if(!seen.add(p)||FocusEngine.essential(base,p)||banks.hide(base,p)||AlternativeCatalog.social(p))continue;
      String label=AppPicker.label(c,p);found.add(new Entry(p,label,AlternativeCatalog.group(p,label,false)));
    }
    snapshot=found;refreshedAt=android.os.SystemClock.elapsedRealtime();return snapshot;
  }
  static List<Entry> installed(Context c,AppConfig config){
    List<Entry> out=new ArrayList<>();
    for(Entry app:scan(c)){
      if(config.watchedApps.contains(app.pkg)||config.sensitiveApps.contains(app.pkg))continue;
      int group=app.group>=0?app.group:app.pkg.equals(config.returnApp)?AlternativeCatalog.PERSONAL:-1;
      if(group>=0)out.add(new Entry(app.pkg,app.label,group));
    }
    out.sort(Comparator.comparingInt((Entry e)->e.group)
        .thenComparingInt(e->e.pkg.equals(config.returnApp)?-1:AlternativeCatalog.priority(e.pkg,e.label))
        .thenComparing(e->e.label,String.CASE_INSENSITIVE_ORDER));return out;
  }
  static void gallery(LinearLayout parent,AppConfig config,boolean compact,Runnable beforeLaunch){
    Context c=parent.getContext();List<Entry> entries=installed(c,config);
    String[] headings={"Unas páginas, otro mundo","Algo nuevo por descubrir","Crear y organizar tu día","Tu elección personal"};
    if(entries.isEmpty()){TextView empty=Design.text(c,"Tu siguiente paso puede ser leer, aprender o escribir una idea. Elige tu app en Mi plan.",15,false);if(compact)empty.setTextColor(0xFFABB3C4);parent.addView(empty);return;}
    for(int group=0;group<4;group++){
      int count=0;LinearLayout row=null;
      for(Entry e:entries){
        if(e.group!=group||count>=(compact?2:group==2?6:4))continue;
        if(count==0){TextView heading=Design.text(c,headings[group],compact?16:19,true);if(compact)heading.setTextColor(0xFFF6F7FB);parent.addView(heading);}
        if(count%2==0){row=new LinearLayout(c);parent.addView(row,new LinearLayout.LayoutParams(-1,-2));}
        LinearLayout tile=Design.column(c);tile.setPadding(Design.dp(c,12),Design.dp(c,16),Design.dp(c,12),Design.dp(c,16));
        android.graphics.drawable.GradientDrawable bg=new android.graphics.drawable.GradientDrawable();
        int[] colors=SceneTheme.night()?new int[]{0xFF243E30,0xFF343E2C,0xFF23453E,0xFF304239}:new int[]{0xFF46694F,0xFF596C47,0xFF3F6A60,0xFF526C64};bg.setColor(colors[group]);bg.setCornerRadius(Design.dp(c,20));tile.setBackground(bg);
        LinearLayout.LayoutParams tp=new LinearLayout.LayoutParams(0,-2,1);tp.setMargins(Design.dp(c,3),Design.dp(c,5),Design.dp(c,3),Design.dp(c,5));row.addView(tile,tp);
        ImageView icon=new ImageView(c);try{icon.setImageDrawable(c.getPackageManager().getApplicationIcon(e.pkg));}catch(PackageManager.NameNotFoundException ignored){}
        tile.addView(icon,new LinearLayout.LayoutParams(Design.dp(c,40),Design.dp(c,40)));
        TextView name=Design.text(c,e.label,16,true);name.setTextColor(0xFFF6F7FB);tile.addView(name);
        TextView caption=Design.text(c,e.pkg.equals(config.returnApp)?"Tu siguiente paso":"Abrir y empezar",12,false);caption.setTextColor(0xFFE1E9D8);tile.addView(caption);
        UiMotion.press(tile);tile.setFocusable(true);tile.setClickable(true);tile.setContentDescription("Abrir "+e.label);tile.setOnClickListener(v->{
          Intent i=c.getPackageManager().getLaunchIntentForPackage(e.pkg);if(i!=null){es.foco.next.data.Repository.get(c).recordChoice(e.pkg);beforeLaunch.run();try{c.startActivity(i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));}catch(RuntimeException ignored){}}
        });count++;
      }
      Design.gap(parent,8);
    }
  }
}
