package es.foco.next.core;

import java.text.Normalizer;
import java.util.*;

/** Explicit identities. Store categories and partial words cannot turn a social feed into a book. */
public final class AlternativeCatalog {
  public static final int READING=0,LEARNING=1,CREATING=2,PERSONAL=3;
  private static final Map<String,Integer> PACKAGES=new HashMap<>(),LABELS=new HashMap<>();
  static {
    add(READING,"com.google.android.apps.books","com.gtl.nextory","com.syc.librototal.El_Libro_Total",
        "com.amazon.kindle","org.readera","com.flyersoft.moonreader","com.flyersoft.moonreaderp",
        "com.foobnix.pdf.reader","com.foobnix.pro.pdf.reader","org.koreader.launcher",
        "org.geometerplus.zlibrary.ui.android","com.faultexception.reader","com.kobobooks.android","com.sirma.mobile.bible.android");
    labels(READING,"Play Libros","Google Play Libros","Nextory","El Libro Total","Libro Total","ReadEra","Kindle","Biblia","Santa Biblia","YouVersion Biblia");
    add(LEARNING,"com.duolingo","org.khanacademy.android","org.coursera.android","com.ichi2.anki",
        "org.wikipedia","org.edx.mobile","com.udemy.android","com.memrise.android.memrisecompanion",
        "com.busuu.android.enc","com.sololearn","com.microblink.photomath","org.brilliant.android");
    labels(LEARNING,"Duolingo","Khan Academy","AnkiDroid","Wikipedia","Coursera","Brilliant","Photomath");
    add(CREATING,"com.google.android.keep","com.samsung.android.app.notes","com.samsung.android.calendar",
        "com.samsung.android.app.reminder","com.google.android.calendar","md.obsidian","notion.id",
        "com.todoist","com.microsoft.office.onenote","com.microsoft.office.word","com.microsoft.office.excel",
        "com.google.android.apps.docs.editors.docs","com.google.android.apps.docs.editors.sheets",
        "com.openai.chatgpt","com.anthropic.claude","com.google.android.apps.bard");
    labels(CREATING,"Notas de Keep","Google Keep","Keep","Samsung Notes","Notes","Samsung Reminder","Reminder",
        "Calendario","Calendar","Obsidian","Notion","Todoist","AppSheet","Hojas de cálculo","ChatGPT","Claude","Gemini","Kimi");
  }
  private AlternativeCatalog(){}
  private static void add(int group,String... values){for(String v:values)PACKAGES.put(v.toLowerCase(Locale.ROOT),group);}
  private static void labels(int group,String... values){for(String v:values)LABELS.put(normalize(v),group);}
  private static String normalize(String s){return Normalizer.normalize(s==null?"":s,Normalizer.Form.NFD).replaceAll("\\p{M}","").trim().toLowerCase(Locale.ROOT).replaceAll("\\s+"," ");}
  public static boolean social(String pkg){
    String p=pkg==null?"":pkg.toLowerCase(Locale.ROOT);
    return p.equals("com.facebook")||p.startsWith("com.facebook.")||p.equals("com.instagram.android")
        ||p.equals("com.zhiliaoapp.musically")||p.equals("com.ss.android.ugc.trill")
        ||p.equals("com.reddit.frontpage")||p.equals("com.twitter.android")||p.equals("com.snapchat.android")
        ||p.equals("com.pinterest");
  }
  public static int group(String pkg,String label,boolean chosen){
    if(pkg==null||social(pkg))return -1;
    Integer known=PACKAGES.get(pkg.toLowerCase(Locale.ROOT));
    if(known==null)known=LABELS.get(normalize(label));
    return known!=null?known:chosen?PERSONAL:-1;
  }
  public static int priority(String pkg,String label){
    String p=pkg.toLowerCase(Locale.ROOT);
    if(p.equals("com.google.android.keep"))return 0;
    if(p.equals("com.samsung.android.app.notes"))return 1;
    if(p.equals("com.samsung.android.calendar"))return 2;
    if(p.equals("com.samsung.android.app.reminder"))return 3;
    if(normalize(label).equals("appsheet"))return 4;
    if(p.equals("com.openai.chatgpt"))return 5;
    return 10;
  }
}
