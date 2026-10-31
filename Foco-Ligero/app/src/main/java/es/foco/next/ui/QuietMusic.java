package es.foco.next.ui;
import android.content.Context;
import es.foco.next.runtime.CalmAudio;
/** A visible owner of the single shared audio session. */
final class QuietMusic {
  private final Context context;
  private final Runnable changed;
  private boolean entered;
  private final boolean intervention;
  QuietMusic(Context c,Runnable changed){this(c,changed,false);}
  QuietMusic(Context c,Runnable changed,boolean intervention){context=c;this.changed=changed;this.intervention=intervention;}
  void enter(){if(!entered){entered=true;CalmAudio.enter(context,this,changed,intervention);}}
  boolean playing(){return entered&&CalmAudio.active();}
  String button(){return playing()?"Pausar música":CalmAudio.pending()?"Preparando música…":"Reproducir música";}
  String caption(){return CalmAudio.title();}
  void toggle(){if(!entered){enter();if(CalmAudio.pending())return;}CalmAudio.toggle(context);}
  void stop(){if(entered){entered=false;CalmAudio.leave(this);}}
}
