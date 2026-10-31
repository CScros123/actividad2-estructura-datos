package es.foco.next.ui;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextClock;
import android.widget.TextView;
import es.foco.next.R;
import es.foco.next.core.AppConfig;
import es.foco.next.core.FocusEngine;
import es.foco.next.core.FocusState;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Launcher minimalista: Inicio con reloj y favoritas, y un cajón de apps en texto.
 * Reutiliza el plan y el bloqueo existentes; no cambia cómo se mide ni cómo se bloquea.
 */
final class LauncherHome {
  private LauncherHome() {}

  static final int MAX_FAVORITES = 7;

  static final class App {
    final String pkg, label;
    final ComponentName component;
    App(String pkg, String label, ComponentName component) {
      this.pkg = pkg;
      this.label = label;
      this.component = component;
    }
  }

  static List<App> installed(Context c) {
    List<App> apps = new ArrayList<>();
    Intent query = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER);
    for (ResolveInfo r : c.getPackageManager().queryIntentActivities(query, 0)) {
      if (r.activityInfo == null || r.activityInfo.packageName.equals(c.getPackageName())) continue;
      String label = String.valueOf(r.loadLabel(c.getPackageManager()));
      apps.add(new App(r.activityInfo.packageName, label,
          new ComponentName(r.activityInfo.packageName, r.activityInfo.name)));
    }
    apps.sort((a, b) -> a.label.compareToIgnoreCase(b.label));
    return apps;
  }

  // ---- Favoritas: lista ordenada en preferencias propias del launcher. ----
  private static SharedPreferences prefs(Context c) {
    return c.getSharedPreferences("foco-launcher", Context.MODE_PRIVATE);
  }

  static List<String> favorites(Context c) {
    String raw = prefs(c).getString("favorites", "");
    List<String> out = new ArrayList<>();
    if (!raw.isEmpty()) out.addAll(Arrays.asList(raw.split(",")));
    return out;
  }

  private static void saveFavorites(Context c, List<String> pkgs) {
    prefs(c).edit().putString("favorites", String.join(",", pkgs)).apply();
  }

  // ---- Inicio ----
  /** Reloj, fecha, tiempo de ocio en una línea y favoritas. */
  static void renderHome(MainActivity a, LinearLayout body, TextView[] budgetOut) {
    body.setMinimumHeight(a.getResources().getDisplayMetrics().heightPixels * 2 / 3);

    TextClock clock = new TextClock(a);
    clock.setFormat24Hour("HH:mm");
    clock.setFormat12Hour("h:mm");
    clock.setTextSize(68);
    clock.setTextColor(a.getColor(R.color.text_primary));
    clock.setTypeface(android.graphics.Typeface.create("sans-serif-thin", android.graphics.Typeface.NORMAL));
    clock.setPadding(0, Design.dp(a, 40), 0, 0);
    clock.setOnClickListener(v -> openClockApp(a));
    body.addView(clock);

    TextClock date = new TextClock(a);
    date.setFormat24Hour("EEEE, d 'de' MMMM");
    date.setFormat12Hour("EEEE, d 'de' MMMM");
    date.setTextSize(16);
    date.setTextColor(a.getColor(R.color.text_secondary));
    body.addView(date);

    TextView budget = Design.text(a, "", 13, false);
    budget.setTextColor(a.getColor(R.color.copper));
    budget.setPadding(0, Design.dp(a, 10), 0, 0);
    body.addView(budget);
    budgetOut[0] = budget;
    budgetOut[1] = null;

    LinearLayout favs = Design.column(a);
    favs.setPadding(0, Design.dp(a, 56), 0, Design.dp(a, 24));
    favs.setTag(FloatingLeaves.SHIELD);
    body.addView(favs);
    List<App> apps = installed(a);
    AppConfig config = a.repository.config();
    boolean any = false;
    for (String pkg : favorites(a)) {
      App app = find(apps, pkg);
      if (app == null) continue;
      favs.addView(textRow(a, app, config, 26));
      any = true;
    }
    if (!any) {
      TextView hint = Design.text(a, "Mantén pulsada una app en el cajón\npara añadirla aquí.", 15, false);
      favs.addView(hint);
    }

    TextView drawer = Design.text(a, "︿  todas las apps", 13, false);
    drawer.setGravity(Gravity.CENTER);
    drawer.setPadding(0, Design.dp(a, 16), 0, Design.dp(a, 8));
    drawer.setOnClickListener(v -> a.showDrawer());
    body.addView(drawer, new LinearLayout.LayoutParams(-1, -2));

    // Mantener pulsado el reloj abre el menú de Foco (Hoy, Mi plan, Ajustes).
    clock.setOnLongClickListener(v -> {
      a.showMenu();
      return true;
    });
  }

  // ---- Cajón ----
  static void renderDrawer(MainActivity a, LinearLayout body) {
    EditText search = Design.field(a, "Buscar…", "", false);
    search.setSingleLine(true);
    search.setTag(FloatingLeaves.SHIELD);
    LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(-1, -2);
    sp.topMargin = Design.dp(a, 16);
    sp.bottomMargin = Design.dp(a, 12);
    body.addView(search, sp);

    LinearLayout list = Design.column(a);
    list.setTag(FloatingLeaves.SHIELD);
    body.addView(list);
    List<App> apps = installed(a);
    fill(a, list, apps, a.repository.config(), "");
    search.addTextChangedListener(new TextWatcher() {
      @Override public void beforeTextChanged(CharSequence s, int st, int c, int af) {}
      @Override public void onTextChanged(CharSequence s, int st, int b, int c) {}
      @Override public void afterTextChanged(Editable s) {
        String f = s.toString();
        fill(a, list, apps, a.repository.config(), f);
        // Si solo queda una app, Intro la abre directamente.
      }
    });
    search.setOnEditorActionListener((v, id, e) -> {
      if (list.getChildCount() > 0 && list.getChildAt(0).hasOnClickListeners()) list.getChildAt(0).performClick();
      return true;
    });
  }

  private static void fill(MainActivity a, LinearLayout list, List<App> apps, AppConfig config, String filter) {
    list.removeAllViews();
    String f = filter.trim().toLowerCase(Locale.ROOT);
    for (App app : apps) {
      if (!f.isEmpty() && !app.label.toLowerCase(Locale.ROOT).contains(f)) continue;
      list.addView(textRow(a, app, config, 20));
    }
    if (list.getChildCount() == 0) Design.note(list, "No hay apps con ese nombre.");
  }

  /** Una app como texto: sin iconos, como un launcher minimalista. */
  private static View textRow(MainActivity a, App app, AppConfig config, int size) {
    boolean ocio = config.watchedApps.contains(app.pkg);
    boolean blocked = ocio && isBlocked(a, app.pkg);
    TextView row = Design.text(a, app.label, size, false);
    row.setTextColor(a.getColor(R.color.text_primary));
    row.setTypeface(android.graphics.Typeface.create("sans-serif-light", android.graphics.Typeface.NORMAL));
    row.setPadding(0, Design.dp(a, size > 22 ? 10 : 9), 0, Design.dp(a, size > 22 ? 10 : 9));
    row.setMinHeight(Design.dp(a, 48));
    row.setGravity(Gravity.CENTER_VERTICAL);
    if (blocked) {
      row.setAlpha(.38f);
      row.setText(app.label + "   · descansando");
    }
    row.setContentDescription(app.label + (blocked ? ", bloqueada" : ocio ? ", app de ocio" : ""));
    row.setOnClickListener(v -> open(a, app, blocked));
    row.setOnLongClickListener(v -> {
      options(a, app, ocio);
      return true;
    });
    return row;
  }

  private static App find(List<App> apps, String pkg) {
    for (App app : apps) if (app.pkg.equals(pkg)) return app;
    return null;
  }

  private static boolean isBlocked(MainActivity a, String pkg) {
    if (!a.repository.locked()) return false;
    FocusState s = a.repository.state();
    return s.lockedApps.contains(pkg);
  }

  private static long minutesLeft(MainActivity a) {
    return (a.repository.remaining() + 59_999) / 60_000;
  }

  private static void open(MainActivity a, App app, boolean blocked) {
    if (blocked) {
      a.message("Tus apps de ocio descansan. Vuelven en " + minutesLeft(a) + " min.");
      return;
    }
    Intent i = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        .setComponent(app.component)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
    try {
      a.startActivity(i);
    } catch (RuntimeException e) {
      a.message("La app no está disponible.");
    }
  }

  private static void openClockApp(MainActivity a) {
    try {
      a.startActivity(new Intent(android.provider.AlarmClock.ACTION_SHOW_ALARMS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    } catch (RuntimeException ignored) {
    }
  }

  private static void options(MainActivity a, App app, boolean ocio) {
    List<String> labels = new ArrayList<>();
    List<Runnable> actions = new ArrayList<>();
    List<String> favs = favorites(a);
    boolean fav = favs.contains(app.pkg);
    if (fav || favs.size() < MAX_FAVORITES) {
      labels.add(fav ? "Quitar de Inicio" : "Añadir a Inicio");
      actions.add(() -> {
        List<String> next = favorites(a);
        if (fav) next.remove(app.pkg);
        else next.add(app.pkg);
        saveFavorites(a, next);
        a.render();
      });
    }
    if (ocio || !FocusEngine.essential(a.repository.config(), app.pkg)) {
      labels.add(ocio ? "Quitar de mis apps de ocio" : "Marcar como app de ocio");
      actions.add(() -> a.edit(() -> a.change(n -> {
        if (ocio) n.watchedApps.remove(app.pkg);
        else {
          n.watchedApps.add(app.pkg);
          if (app.pkg.equals(n.returnApp)) n.returnApp = "";
        }
      })));
    }
    labels.add("Información de la app");
    actions.add(() -> {
      try {
        a.startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.parse("package:" + app.pkg)));
      } catch (RuntimeException e) {
        a.message("Abre este ajuste desde Ajustes del teléfono.");
      }
    });
    new AlertDialog.Builder(a)
        .setTitle(app.label)
        .setItems(labels.toArray(new String[0]), (d, w) -> actions.get(w).run())
        .setNegativeButton(R.string.cancel, null)
        .show();
  }
}
