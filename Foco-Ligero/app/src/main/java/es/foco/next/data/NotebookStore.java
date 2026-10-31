package es.foco.next.data;

import android.content.Context;
import android.content.SharedPreferences;
import java.time.LocalDate;
import java.util.*;
import org.json.*;

/** Separate storage: the focus engine and reminder never load or rewrite private notes. */
public final class NotebookStore {
  public static final int MAX_DAYS=366,MAX_TEXT=4000;
  private final SharedPreferences preferences;
  private final TreeMap<String,String> notes=new TreeMap<>();
  public NotebookStore(Context context){
    preferences=context.getApplicationContext().getSharedPreferences("foco-notebook",Context.MODE_PRIVATE);
    try{
      JSONObject j=new JSONObject(preferences.getString("entries","{}"));
      if(j.length()>MAX_DAYS)throw new IllegalArgumentException();
      Iterator<String> it=j.keys();while(it.hasNext()){
        String date=it.next();LocalDate.parse(date);String text=j.getString(date);
        if(text.length()>MAX_TEXT)throw new IllegalArgumentException();notes.put(date,text);
      }
    }catch(Exception e){throw new IllegalStateException("No se pudieron abrir tus apuntes. El archivo original se conserva.",e);}
  }
  public String today(){return get(LocalDate.now().toString());}
  public String get(String date){return notes.getOrDefault(date,"");}
  public Map<String,String> entries(){return Collections.unmodifiableMap(new TreeMap<>(notes));}
  public void save(String date,String text){
    LocalDate.parse(date);
    if(text==null||text.length()>MAX_TEXT)throw new IllegalArgumentException("El apunte puede tener hasta 4000 caracteres.");
    if(!notes.containsKey(date)&&!text.trim().isEmpty()&&notes.size()>=MAX_DAYS)
      throw new IllegalStateException("Tu cuaderno está lleno. Exporta tus apuntes y elimina alguno para seguir.");
    TreeMap<String,String> next=new TreeMap<>(notes);
    if(text.trim().isEmpty())next.remove(date);else next.put(date,text);
    if(!preferences.edit().putString("entries",new JSONObject(next).toString()).commit())
      throw new IllegalStateException("No se pudo guardar el apunte. El anterior se conserva.");
    notes.clear();notes.putAll(next);
  }
  public String exportText(){
    StringBuilder out=new StringBuilder("FOCO · MIS APUNTES\nDatos escritos por ti; no son métricas de productividad.\n");
    for(var e:notes.descendingMap().entrySet())out.append("\n──────── ").append(e.getKey()).append(" ────────\n").append(e.getValue()).append('\n');
    return out.toString();
  }
}
