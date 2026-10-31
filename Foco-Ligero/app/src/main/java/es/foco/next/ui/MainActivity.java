package es.foco.next.ui;

import android.app.*;
import android.content.*;
import android.net.*;
import android.os.*;
import android.provider.Settings;
import android.widget.*;
import es.foco.next.R;
import es.foco.next.core.*;
import es.foco.next.data.Repository;
import es.foco.next.data.NotebookStore;
import es.foco.next.runtime.DailyReminder;
import es.foco.next.runtime.FocusAccessibilityService;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Consumer;

/** Three destinations: your time, your intention, essential settings. No perpetual UI timer. */
public final class MainActivity extends Activity {
  Repository repository;
  LinearLayout body,navigation;
  private int page;
  private boolean resumed;
  private AmbientView ambient;
  private FloatingLeaves floatingLeaves;
  private Button energyButton;
  private final android.graphics.Rect visibleCards=new android.graphics.Rect();
  private final Runnable revealCards=()->{if(resumed&&body!=null){ScrollView scroll=findViewById(R.id.page_scroll);visibleCards.set(0,scroll.getScrollY(),body.getWidth(),scroll.getScrollY()+scroll.getHeight());UiMotion.reveal(body,visibleCards);}};
  private TextView budgetText,windowText;
  private ProgressBar progress;
  private QuietMusic music;
  private Button musicButton;
  private TextView musicCaption;
  private boolean appliedNight,sceneReceiverRegistered;
  private final Runnable sceneBoundary=()->{if(resumed)recreate();};
  private final BroadcastReceiver sceneChanges=new BroadcastReceiver(){@Override public void onReceive(Context c,Intent i){if(resumed){if(appliedNight!=SceneTheme.night())recreate();else{render();scheduleScene();}}}};
  private NotebookStore notebook;
  private android.view.View dayCard;
  int pendingReminderMinute=-1;
  private final Handler main=new Handler(Looper.getMainLooper());
  private final Runnable update=new Runnable(){public void run(){if(resumed&&page==0){updateTime();main.postDelayed(this,1000);}}};
  @Override protected void attachBaseContext(Context base){appliedNight=SceneTheme.night();super.attachBaseContext(base.createConfigurationContext(SceneTheme.override(base)));}
  @Override public void onCreate(Bundle b){
    setTheme(R.style.AppTheme);
    super.onCreate(b);setContentView(R.layout.activity_main);body=findViewById(R.id.page_body);navigation=findViewById(R.id.navigation);
    ambient=findViewById(R.id.ambient);floatingLeaves=findViewById(R.id.floating_leaves);floatingLeaves.protect(findViewById(R.id.root));ambient.foreground(floatingLeaves);energyButton=findViewById(R.id.energy_toggle);
    energyButton.setOnClickListener(v->ambient.toggleSaving());UiMotion.press(energyButton);
    ambient.onPolicyChanged(()->{energyButton.setText(ambient.saving()?"Ahorro de energía · activado":"Ahorro de energía · desactivado");energyButton.setSelected(ambient.saving());if(!UiMotion.allowed(this)){UiMotion.stop(body);UiMotion.stop(navigation);UiMotion.stop(energyButton);}});
    ambient.refresh();
    body.addOnLayoutChangeListener((v,l,t,r,bottom,ol,ot,or,ob)->{ambient.setScroll(((ScrollView)findViewById(R.id.page_scroll)).getScrollY());floatingLeaves.rebuildMask();revealCards.run();});
    ((ScrollView)findViewById(R.id.page_scroll)).setOnScrollChangeListener((v,x,y,ox,oy)->{ambient.setScroll(y);floatingLeaves.rebuildMask();revealCards.run();});
    if(b!=null){page=Math.max(0,Math.min(2,b.getInt("page")));pendingReminderMinute=b.getInt("pendingReminder",-1);}
    try{
      // Old periodic DNS jobs/notifications belong to this package and have no role in Lite.
      if(getPreferences(MODE_PRIVATE).getInt("cleanup",0)<52){
        getSystemService(android.app.job.JobScheduler.class).cancelAll();getSystemService(NotificationManager.class).cancelAll();getPreferences(MODE_PRIVATE).edit().putInt("cleanup",52).apply();
      }
      repository=Repository.get(this);music=new QuietMusic(this,this::refreshMusicControl);
    }catch(RuntimeException e){body.addView(Design.text(this,"Tus datos se conservan",26,true));Design.note(body,"No se pudieron abrir los datos. Conserva tu copia anterior; no se han reiniciado tus ajustes.");}
  }
  @Override protected void onResume(){
    super.onResume();if(appliedNight!=SceneTheme.night()){recreate();return;}resumed=true;ambient.setActive(true);
    if(!sceneReceiverRegistered){IntentFilter filter=new IntentFilter();filter.addAction(Intent.ACTION_TIME_CHANGED);filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);if(Build.VERSION.SDK_INT>=33)registerReceiver(sceneChanges,filter,Context.RECEIVER_NOT_EXPORTED);else registerReceiver(sceneChanges,filter);sceneReceiverRegistered=true;}
    FocusAccessibilityService.appOpened();Alternatives.invalidate();
    if(repository!=null){DailyReminder.ensure(this);if(getIntent().getBooleanExtra("open_day",false))page=0;render();if(music!=null)music.enter();scheduleScene();}
  }
  @Override protected void onPause(){resumed=false;if(ambient!=null)ambient.setActive(false);body.removeCallbacks(revealCards);UiMotion.stop(body);UiMotion.stop(navigation);UiMotion.stop(energyButton);if(music!=null)music.stop();main.removeCallbacks(update);main.removeCallbacks(sceneBoundary);if(sceneReceiverRegistered){unregisterReceiver(sceneChanges);sceneReceiverRegistered=false;}super.onPause();}
  @Override protected void onSaveInstanceState(Bundle b){super.onSaveInstanceState(b);b.putInt("page",page);b.putInt("pendingReminder",pendingReminderMinute);}
  void render(){
    UiMotion.stop(body);UiMotion.stop(navigation);body.removeCallbacks(revealCards);ambient.detail(null);body.removeAllViews();navigation.removeAllViews();budgetText=null;windowText=null;progress=null;musicButton=null;musicCaption=null;
    String[] names={"Hoy","Mi plan","Ajustes"};int[] icons={R.drawable.tab_home,R.drawable.tab_rules,R.drawable.tab_settings};
    ((TextView)findViewById(R.id.page_title)).setText(names[page]);
    for(int i=0;i<3;i++){
      final int p=i;Button b=Design.button(this,names[i],false,()->{if(page==p)return;page=p;((ScrollView)findViewById(R.id.page_scroll)).scrollTo(0,0);render();});b.setTextSize(14);b.setPadding(0,Design.dp(this,8),0,Design.dp(this,8));
      var icon=getDrawable(icons[i]).mutate();icon.setTint(getColor(i==page?R.color.copper:R.color.text_secondary));icon.setBounds(0,0,Design.dp(this,22),Design.dp(this,22));b.setCompoundDrawables(null,icon,null,null);b.setSelected(i==page);navigation.addView(b,new LinearLayout.LayoutParams(0,-2,1));
    }
    if(android.os.Build.VERSION.SDK_INT>=28)((TextView)findViewById(R.id.page_title)).setAccessibilityHeading(true);
    if(page==0)today();else if(page==1)plan();else settings();
    if(page==0&&dayCard!=null&&getIntent().getBooleanExtra("open_day",false)){
      dayCard.post(()->{((ScrollView)findViewById(R.id.page_scroll)).scrollTo(0,dayCard.getTop());getIntent().removeExtra("open_day");});
    }
    body.post(revealCards);
    if(UiMotion.allowed(this)){android.view.View selected=navigation.getChildAt(page);selected.setScaleX(.94f);selected.setScaleY(.94f);selected.animate().scaleX(1).scaleY(1).setDuration(280).start();}
    main.removeCallbacks(update);if(resumed&&page==0)main.postDelayed(update,1000);
  }
  private void scheduleScene(){main.removeCallbacks(sceneBoundary);if(resumed)main.postDelayed(sceneBoundary,SceneTheme.untilBoundary());}
  private void refreshMusicControl(){if(resumed&&music!=null){if(musicButton!=null)musicButton.setText(music.button());if(musicCaption!=null)musicCaption.setText(music.caption());}}
  private void today(){
    AppConfig c=repository.config();
    Design.note(body,SceneTheme.night()?"Una noche para bajar el ritmo":"Un día para descubrir algo bueno");
    LinearLayout hero=Design.card(body,"Haz espacio para lo que te importa");hero.setBackgroundResource(R.drawable.focus_card);
    TerraceView landscape=new TerraceView(this);ambient.detail(landscape);
    android.graphics.drawable.GradientDrawable rounded=new android.graphics.drawable.GradientDrawable();rounded.setColor(0xFFE4EEDC);rounded.setCornerRadius(Design.dp(this,20));landscape.setBackground(rounded);landscape.setClipToOutline(true);
    hero.addView(landscape,new LinearLayout.LayoutParams(-1,Design.dp(this,170)));Design.gap(hero,16);
    hero.addView(Design.text(this,c.intention,27,true));
    if(!c.returnApp.isEmpty())hero.addView(Design.button(this,"Empezar · "+AppPicker.label(this,c.returnApp),true,()->launch(c.returnApp)));
    else hero.addView(Design.button(this,"Elegir mi siguiente paso",true,()->{page=1;render();}));
    musicButton=Design.button(this,"Música tranquila",false,()->{if(music!=null){music.toggle();refreshMusicControl();}});hero.addView(musicButton);
    musicCaption=Design.text(this,"",13,false);hero.addView(musicCaption);refreshMusicControl();
    Design.note(hero,"Sin conexión. La música acompaña Foco y la pausa; puedes silenciarla.");
    LinearLayout next=Design.card(body,"Un pequeño paso cuenta");Alternatives.gallery(next,c,false,()->{});
    dayCard=DailySpace.render(this);
    LinearLayout time=Design.card(body,"Tu tiempo de ocio");budgetText=Design.text(this,"",36,true);time.addView(budgetText);
    progress=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);progress.setMax(300);time.addView(progress,new LinearLayout.LayoutParams(-1,Design.dp(this,10)));
    windowText=Design.text(this,"",15,false);time.addView(windowText);
    Design.note(time,"5 minutos compartidos entre tus apps de ocio. La ventana de 60 minutos empieza al abrir una de ellas.");
    if(!FocusAccessibilityService.connected())time.addView(Design.button(this,"Activar medición y bloqueo",true,()->open(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))));
    if(c.watchedApps.isEmpty())time.addView(Design.button(this,"Elegir mis apps de ocio",true,()->{page=1;render();}));
    updateTime();
    LinearLayout history=Design.card(body,"Lo observado hoy");FocusState s=repository.state();
    if(!s.day.equals(LocalDate.now().toString())||s.dailyPackageMillis.isEmpty())Design.note(history,"Aquí verás el tiempo de tus apps elegidas. Foco no lee lo que miras ni clasifica cada vídeo.");
    else s.dailyPackageMillis.entrySet().stream().sorted(Map.Entry.<String,Long>comparingByValue().reversed()).limit(8).forEach(e->Design.note(history,AppPicker.label(this,e.getKey())+" · "+Design.duration(this,e.getValue())));
    LinearLayout week=Design.card(body,"Tu semana, sin juicios");
    Design.note(week,"Siete días de tiempo observado en ocio y en tu alternativa elegida, bloqueos y alternativas abiertas desde Foco. Un registro para decidir, no para puntuarte.");
    week.addView(Design.button(this,"Exportar mis 7 días · CSV",false,()->openForResult(new Intent(Intent.ACTION_CREATE_DOCUMENT).setType("text/csv").addCategory(Intent.CATEGORY_OPENABLE).putExtra(Intent.EXTRA_TITLE,"Foco-semana-"+LocalDate.now()+".csv"),12)));
    Design.note(history,"El tiempo observado no mide cuánto has aprendido ni tu productividad.");
  }
  private void updateTime(){
    if(budgetText==null)return;
    long used=repository.liveUsed(),left=repository.remaining();boolean locked=repository.locked();
    budgetText.setText(Design.duration(this,Math.max(0,FocusEngine.BUDGET-used))+" disponibles");progress.setProgress((int)(used/1000));
    String state=!repository.config().enabled?"Plan desactivado":!FocusAccessibilityService.connected()?"Activa Accesibilidad para medir":locked?"Ocio bloqueado": "Ocio disponible";
    windowText.setText(state+(left>0?" · la ventana termina en "+((left+59999)/60000)+" min":" · sin ventana en curso"));
  }
  private void plan(){
    AppConfig c=repository.config();boolean locked=repository.locked();
    LinearLayout choose=Design.card(body,"Cinco minutos. Después, tú eliges algo más.");
    Design.note(choose,"Al agotar 5 minutos acumulados, tus apps de ocio se bloquean hasta el final de la ventana de una hora. No hay ampliaciones durante el bloqueo.");
    if(locked)Design.note(choose,"Tu plan está protegido hasta que termine esta ventana. Podrás editarlo después.");
    choose.addView(Design.button(this,"Apps de ocio · "+c.watchedApps.size(),true,()->edit(()->AppPicker.many(this,c.watchedApps,chosen->change(n->{chosen.removeIf(p->FocusEngine.essential(n,p));chosen.remove(n.returnApp);n.watchedApps=chosen;})))));
    LinearLayout intention=Design.card(body,"¿A qué quieres volver?");Design.note(intention,c.intention);
    intention.addView(Design.button(this,"Escribir mi siguiente paso",false,()->edit(()->{
      EditText field=Design.field(this,"Algo concreto y pequeño",c.intention,false);field.setFilters(new android.text.InputFilter[]{new android.text.InputFilter.LengthFilter(120)});
      new AlertDialog.Builder(this).setTitle("Mi siguiente paso").setView(field).setPositiveButton("Guardar",(d,w)->change(n->n.intention=field.getText().toString().trim())).setNegativeButton("Cancelar",null).show();
    })));
    intention.addView(Design.button(this,c.returnApp.isEmpty()?"Elegir app para empezar":"Mi app · "+AppPicker.label(this,c.returnApp),true,()->edit(()->AppPicker.one(this,p->change(n->n.returnApp=p)))));
    Design.note(intention,"Leer unas páginas, practicar un idioma o escribir una idea. Sin puntos ni rachas que te pidan volver a Foco.");
    LinearLayout examples=Design.card(body,"Tus alternativas instaladas");Alternatives.gallery(examples,c,false,()->{});
  }
  private void settings(){
    AppConfig c=repository.config();LinearLayout essential=Design.card(body,"Solo lo necesario");
    Design.note(essential,FocusAccessibilityService.connected()?"Medición conectada":"Activa Accesibilidad para medir y bloquear");
    essential.addView(Design.button(this,"Accesibilidad",true,()->open(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))));
    essential.addView(Design.button(this,c.enabled?"Desactivar mi plan":"Activar mi plan",false,()->change(n->n.enabled=!n.enabled)));
    Design.note(essential,"Foco detecta cambios de app. No lee mensajes, vídeos ni direcciones web. Sin animación en segundo plano, consultas de uso periódicas ni servicio VPN.");
    Design.note(essential,"Llamadas, WhatsApp, Inicio, Ajustes y bancos reconocidos siguen disponibles. Android permite desactivar Accesibilidad o desinstalar Foco; el bloqueo no puede impedirlo.");
    essential.addView(Design.button(this,"Otras apps esenciales · "+c.sensitiveApps.size(),false,()->edit(()->AppPicker.many(this,c.sensitiveApps,p->change(n->n.sensitiveApps=p)))));
    AmbientView.settings(body,ambient);
    LinearLayout filter=Design.card(body,"Adultos, anuncios y rastreadores");
    Design.note(filter,privateDnsStatus());
    Design.note(filter,"Android puede encargarse del filtro con DNS privado. Selecciona Nombre de host del proveedor y pega:");
    filter.addView(Design.text(this,"family.adguard-dns.com",18,true));
    filter.addView(Design.button(this,"Copiar y abrir DNS privado",true,()->{
      getSystemService(ClipboardManager.class).setPrimaryClip(ClipData.newPlainText("DNS familiar","family.adguard-dns.com"));
      try{startActivity(new Intent("android.settings.PRIVATE_DNS_SETTINGS"));}catch(RuntimeException e){open(new Intent(Settings.ACTION_WIRELESS_SETTINGS));}
    }));
    Design.note(filter,"Este filtro externo de AdGuard se aplica a todo el teléfono. No elimina todos los anuncios ni el contenido adulto dentro de redes sociales; las apps con DNS propio pueden evitarlo. Foco no guarda consultas ni activa el filtro por su cuenta.");
    if(repository.legacyVpn){Design.note(filter,"La VPN anterior ya no forma parte de Foco. Si estaba como VPN permanente, quita esa asignación y Bloquear conexiones sin VPN antes de usar DNS privado.");filter.addView(Design.button(this,"Revisar VPN anterior",false,()->open(new Intent(Settings.ACTION_VPN_SETTINGS))));}
    LinearLayout protection=Design.card(body,"Protección del plan");
    Design.note(protection,"Durante el bloqueo, Foco impide cambiar el plan o restaurar una copia para ampliar el tiempo. Android mantiene el control de Accesibilidad, Ajustes, DNS y desinstalación.");
    Design.note(protection,"Bloquear esos controles de forma efectiva requiere administración especial del dispositivo; esta instalación no tiene ese permiso. Las llamadas y apps esenciales siguen disponibles.");
    Design.note(protection,"Música: Morning Routine · Ghostrifter Official; Colorful Flowers · Tokyo Music Walker. Archivos que proporcionaste, reproducidos localmente.");
    LinearLayout data=Design.card(body,"Tus datos");
    if(repository.migrated)Design.note(data,"Se conservaron tus apps, alternativa y tiempos diarios. Las reglas y ciclos visuales antiguos se archivaron en el archivo original; esta versión empieza un presupuesto nuevo de 5 min por hora.");
    data.addView(Design.button(this,"Guardar copia del plan y tiempos",false,()->openForResult(new Intent(Intent.ACTION_CREATE_DOCUMENT).setType("application/json").addCategory(Intent.CATEGORY_OPENABLE).putExtra(Intent.EXTRA_TITLE,"Foco-Ligero.json"),10)));
    data.addView(Design.button(this,"Restaurar copia de Foco Ligero",false,()->edit(()->openForResult(new Intent(Intent.ACTION_OPEN_DOCUMENT).setType("application/json").addCategory(Intent.CATEGORY_OPENABLE),11))));
    Design.note(data,"Foco 0.58 · Brisa viva. Datos locales; sin cuenta ni telemetría.");
  }
  private String privateDnsStatus(){
    try{ConnectivityManager cm=getSystemService(ConnectivityManager.class);LinkProperties p=cm.getLinkProperties(cm.getActiveNetwork());
      if(p==null)return "Sin red activa; no se puede comprobar el DNS privado.";
      if(Build.VERSION.SDK_INT>=28&&p.isPrivateDnsActive())return "DNS privado activo · "+(p.getPrivateDnsServerName()==null?"automático":p.getPrivateDnsServerName());
    }catch(RuntimeException ignored){}
    return "El DNS privado no figura activo en la red actual.";
  }
  private void edit(Runnable action){FocusAccessibilityService.checkpoint();if(repository.locked())message("Tu plan se puede cambiar al terminar esta ventana.");else action.run();}
  void change(Consumer<AppConfig> action){
    FocusAccessibilityService.checkpoint();try{AppConfig c=repository.config();action.accept(c);repository.saveConfig(c);render();}catch(RuntimeException e){message(e.getMessage());}
  }
  private void launch(String p){Intent i=getPackageManager().getLaunchIntentForPackage(p);if(i!=null){repository.recordChoice(p);open(i);}else message("La app no está disponible.");}
  private void open(Intent i){try{startActivity(i);}catch(RuntimeException e){message("Abre este ajuste desde Ajustes del teléfono.");}}
  private void openForResult(Intent i,int code){try{startActivityForResult(i,code);}catch(RuntimeException e){message("No se pudo abrir el selector de archivos.");}}
  void message(String text){Toast.makeText(this,text==null?"No se pudo guardar el cambio":text,Toast.LENGTH_LONG).show();}
  NotebookStore notebook(){if(notebook==null)notebook=new NotebookStore(this);return notebook;}
  void exportNotes(){openForResult(new Intent(Intent.ACTION_CREATE_DOCUMENT).setType("text/plain").addCategory(Intent.CATEGORY_OPENABLE).putExtra(Intent.EXTRA_TITLE,"Foco-apuntes-"+LocalDate.now()+".txt"),13);}
  @Override protected void onNewIntent(Intent intent){super.onNewIntent(intent);setIntent(intent);if(repository!=null&&intent.getBooleanExtra("open_day",false)){page=0;render();}}
  @Override public void onRequestPermissionsResult(int request,String[] permissions,int[] results){
    super.onRequestPermissionsResult(request,permissions,results);
    if(request==52){
      if(results.length>0&&results[0]==android.content.pm.PackageManager.PERMISSION_GRANTED)DailySpace.enableReminder(this);
      else{pendingReminderMinute=-1;message("El recordatorio sigue apagado. Puedes activarlo cuando quieras.");render();}
    }
  }
  @Override protected void onActivityResult(int request,int result,Intent data){
    super.onActivityResult(request,result,data);if(result!=RESULT_OK||data==null||data.getData()==null)return;
    try{
      if(request==13){try(OutputStream out=getContentResolver().openOutputStream(data.getData(),"wt")){if(out==null)throw new IOException();out.write(notebook().exportText().getBytes(StandardCharsets.UTF_8));}message("Apuntes exportados. Esta copia es independiente del plan y los tiempos.");}
      if(request==12){FocusAccessibilityService.checkpoint();try(OutputStream out=getContentResolver().openOutputStream(data.getData(),"wt")){if(out==null)throw new IOException();out.write(repository.exportWeek().getBytes(StandardCharsets.UTF_8));}message("Registro exportado. Puedes abrirlo en una hoja de cálculo.");}
      if(request==10){FocusAccessibilityService.checkpoint();try(OutputStream out=getContentResolver().openOutputStream(data.getData(),"wt")){if(out==null)throw new IOException();out.write(repository.exportBackup().getBytes(StandardCharsets.UTF_8));}message("Copia guardada");}
      if(request==11){try(InputStream in=getContentResolver().openInputStream(data.getData());ByteArrayOutputStream out=new ByteArrayOutputStream()){
        if(in==null)throw new IOException();byte[] bytes=new byte[8192];int n;while((n=in.read(bytes))!=-1){if(out.size()+n>1_048_576)throw new IOException();out.write(bytes,0,n);}String json=out.toString(StandardCharsets.UTF_8.name());
        new AlertDialog.Builder(this).setMessage("¿Sustituir el plan y los tiempos por esta copia?").setPositiveButton("Restaurar",(d,w)->{FocusAccessibilityService.checkpoint();try{repository.importBackup(json);render();}catch(RuntimeException e){message(e.getMessage());}}).setNegativeButton("Cancelar",null).show();
      }}
    }catch(Exception e){message("No se pudo procesar la copia. Tus datos se conservan.");}
  }
}
