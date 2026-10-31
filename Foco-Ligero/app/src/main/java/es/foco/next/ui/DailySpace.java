package es.foco.next.ui;

import android.Manifest;
import android.app.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.InputFilter;
import android.widget.*;
import es.foco.next.data.NotebookStore;
import es.foco.next.runtime.DailyReminder;
import java.time.LocalDate;
import java.util.*;

/** A dated note and one gentle reminder, not a second task manager. */
final class DailySpace {
  static LinearLayout render(MainActivity a){
    LinearLayout box=Design.card(a.body,"Una idea para hoy");
    try{
      String text=a.notebook().today();
      Design.note(box,text.isEmpty()?"¿Qué te gustaría leer, aprender o crear? Puedes dejar aquí una idea, o algo que quieras recordar.":text.length()>240?text.substring(0,240)+"…":text);
      box.addView(Design.button(a,text.isEmpty()?"Escribir un apunte":"Editar mi apunte de hoy",true,()->edit(a,LocalDate.now().toString())));
      box.addView(Design.button(a,"Mis apuntes · ver y exportar",false,()->archive(a)));
    }catch(RuntimeException e){Design.note(box,e.getMessage());}
    boolean on=DailyReminder.enabled(a);
    String status=!on?"apagado":!DailyReminder.allowed(a)?"sin permiso de notificaciones":time(DailyReminder.minute(a));
    box.addView(Design.button(a,"Recordatorio diario · "+status,false,()->chooseReminder(a)));
    Design.note(box,"Un aviso opcional y silencioso. Android puede posponerlo para ahorrar batería.");
    return box;
  }
  private static String time(int minute){return String.format(Locale.getDefault(),"%02d:%02d",minute/60,minute%60);}
  static void edit(MainActivity a,String date){
    try{
      EditText field=Design.field(a,"Una idea pequeña también cuenta",a.notebook().get(date),false);
      field.setInputType(android.text.InputType.TYPE_CLASS_TEXT|android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE|android.text.InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
      field.setMinLines(5);field.setMaxLines(10);field.setGravity(android.view.Gravity.TOP);
      field.setFilters(new InputFilter[]{new InputFilter.LengthFilter(NotebookStore.MAX_TEXT)});
      AlertDialog.Builder builder=new AlertDialog.Builder(a).setTitle(date.equals(LocalDate.now().toString())?"Mi apunte de hoy":"Apunte · "+date).setView(field)
          .setPositiveButton("Guardar",null).setNegativeButton("Cancelar",null);
      if(!a.notebook().get(date).isEmpty())builder.setNeutralButton("Eliminar",(d,w)->{
        try{a.notebook().save(date,"");a.render();}catch(RuntimeException e){a.message(e.getMessage());}
      });
      AlertDialog dialog=builder.create();
      dialog.setOnShowListener(d->dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{
        try{a.notebook().save(date,field.getText().toString());dialog.dismiss();a.render();}catch(RuntimeException e){a.message(e.getMessage());}
      }));dialog.show();
    }catch(RuntimeException e){a.message(e.getMessage());}
  }
  private static void archive(MainActivity a){
    try{
      Map<String,String> entries=a.notebook().entries();List<String> dates=new ArrayList<>(entries.keySet());Collections.reverse(dates);
      String[] labels=new String[dates.size()];
      for(int i=0;i<dates.size();i++){String text=entries.get(dates.get(i)).replace('\n',' ');labels[i]=dates.get(i)+" · "+text.substring(0,Math.min(65,text.length()));}
      AlertDialog.Builder b=new AlertDialog.Builder(a).setTitle("Mis apuntes").setNegativeButton("Cerrar",null).setNeutralButton("Exportar .txt",(d,w)->a.exportNotes());
      if(dates.isEmpty())b.setMessage("Tu cuaderno está vacío. Los apuntes se guardan solo en este teléfono.").setPositiveButton("Escribir hoy",(d,w)->edit(a,LocalDate.now().toString()));
      else b.setItems(labels,(d,w)->edit(a,dates.get(w)));
      b.show();
    }catch(RuntimeException e){a.message(e.getMessage());}
  }
  private static void chooseReminder(MainActivity a){
    int minute=DailyReminder.minute(a);
    TimePickerDialog dialog=new TimePickerDialog(a,(view,h,m)->{
      a.pendingReminderMinute=h*60+m;
      if(Build.VERSION.SDK_INT>=33&&a.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)
        a.requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},52);
      else enableReminder(a);
    },minute/60,minute%60,android.text.format.DateFormat.is24HourFormat(a));
    dialog.setTitle("Un pequeño paso diario");
    if(DailyReminder.enabled(a))dialog.setButton(AlertDialog.BUTTON_NEUTRAL,"Desactivar",(d,w)->{
      try{DailyReminder.configure(a,false,minute);a.render();}catch(RuntimeException e){a.message(e.getMessage());}
    });dialog.show();
  }
  static void enableReminder(MainActivity a){
    if(a.pendingReminderMinute<0)return;
    try{
      DailyReminder.configure(a,true,a.pendingReminderMinute);
      if(!DailyReminder.allowed(a)){
        a.message("Permite las notificaciones de Foco para recibir el aviso.");
        a.startActivity(new Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(android.provider.Settings.EXTRA_APP_PACKAGE,a.getPackageName()));
      }
      a.pendingReminderMinute=-1;a.render();
    }catch(RuntimeException e){a.message(e.getMessage());}
  }
}
