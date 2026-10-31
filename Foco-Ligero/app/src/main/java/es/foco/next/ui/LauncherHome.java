package es.foco.next.ui;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextClock;
import android.widget.TextView;
import es.foco.next.R;
import es.foco.next.core.AppConfig;
import es.foco.next.core.FocusEngine;
import es.foco.next.core.FocusState;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Pantalla de inicio del launcher: reloj, tiempo de ocio restante y todas las apps instaladas.
 * Reutiliza el plan y el bloqueo existentes; no cambia cómo se mide ni cómo se bloquea.
 */
final class LauncherHome {
  private LauncherHome() {}

  static final class App {
    final String pkg, label;
    final ComponentName component;
    App(String pkg, String label, ComponentName component) {
      this.pkg = pkg;
      this.label = label;
      this.component = component;
    }
  }

  private static final Map<String, Drawable> icons = new HashMap<>();

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

  /** Construye la cabecera con reloj y la lista de apps dentro de {@code body}. */
  static void render(MainActivity a, LinearLayout body, TextView[] budgetOut) {
    AppConfig config = a.repository.config();

    LinearLayout clockBox = Design.column(a);
    clockBox.setGravity(Gravity.CENTER_HORIZONTAL);
    clockBox.setPadding(0, Design.dp(a, 12), 0, Design.dp(a, 20));
    TextClock clock = new TextClock(a);
    clock.setFormat24Hour("HH:mm");
    clock.setFormat12Hour("h:mm");
    clock.setTextSize(64);
    clock.setTextColor(a.getColor(R.color.text_primary));
    clock.setTypeface(android.graphics.Typeface.create("sans-serif-light", android.graphics.Typeface.NORMAL));
    clockBox.addView(clock);
    TextClock date = new TextClock(a);
    date.setFormat24Hour("EEEE, d 'de' MMMM");
    date.setFormat12Hour("EEEE, d 'de' MMMM");
    date.setTextSize(16);
    date.setTextColor(a.getColor(R.color.text_secondary));
    clockBox.addView(date);
    ImageView leaf = new ImageView(a);
    leaf.setImageResource(R.drawable.leaf);
    leaf.setColorFilter(a.getColor(R.color.copper));
    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(Design.dp(a, 28), Design.dp(a, 28));
    lp.topMargin = Design.dp(a, 10);
    clockBox.addView(leaf, lp);
    TextView calm = Design.text(a, "Respira. Elige con calma.", 14, false);
    calm.setGravity(Gravity.CENTER);
    clockBox.addView(calm);
    body.addView(clockBox);

    // Resumen del tiempo de ocio; MainActivity lo actualiza cada segundo.
    LinearLayout time = Design.card(body, "");
    TextView budget = Design.text(a, "", 20, true);
    TextView window = Design.text(a, "", 14, false);
    time.addView(budget);
    time.addView(window);
    budgetOut[0] = budget;
    budgetOut[1] = window;

    EditText search = Design.field(a, "Buscar app…", "", false);
    search.setSingleLine(true);
    LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(-1, -2);
    sp.bottomMargin = Design.dp(a, 12);
    body.addView(search, sp);

    LinearLayout list = Design.column(a);
    body.addView(list);
    List<App> apps = installed(a);
    fill(a, list, apps, config, "");
    search.addTextChangedListener(new TextWatcher() {
      @Override public void beforeTextChanged(CharSequence s, int st, int c, int af) {}
      @Override public void onTextChanged(CharSequence s, int st, int b, int c) {}
      @Override public void afterTextChanged(Editable s) {
        fill(a, list, apps, a.repository.config(), s.toString());
      }
    });
  }

  private static void fill(MainActivity a, LinearLayout list, List<App> apps, AppConfig config, String filter) {
    list.removeAllViews();
    String f = filter.trim().toLowerCase(Locale.ROOT);
    boolean locked = a.repository.locked();
    FocusState state = locked ? a.repository.state() : null;
    for (App app : apps) {
      if (!f.isEmpty() && !app.label.toLowerCase(Locale.ROOT).contains(f)) continue;
      boolean ocio = config.watchedApps.contains(app.pkg);
      boolean blocked = locked && ocio && state.lockedApps.contains(app.pkg);
      list.addView(row(a, app, ocio, blocked));
    }
    if (list.getChildCount() == 0) Design.note(list, "No hay apps con ese nombre.");
  }

  private static View row(MainActivity a, App app, boolean ocio, boolean blocked) {
    LinearLayout row = new LinearLayout(a);
    row.setOrientation(LinearLayout.HORIZONTAL);
    row.setGravity(Gravity.CENTER_VERTICAL);
    row.setMinimumHeight(Design.dp(a, 56));
    row.setPadding(Design.dp(a, 8), Design.dp(a, 6), Design.dp(a, 8), Design.dp(a, 6));
    GradientDrawable bg = new GradientDrawable();
    bg.setCornerRadius(Design.dp(a, 14));
    bg.setColor(a.getColor(R.color.surface_raised));
    bg.setAlpha(blocked ? 110 : 200);
    row.setBackground(bg);
    LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(-1, -2);
    rp.bottomMargin = Design.dp(a, 6);
    row.setLayoutParams(rp);

    ImageView icon = new ImageView(a);
    icon.setImageDrawable(icon(a, app.pkg));
    if (blocked) icon.setAlpha(.4f);
    row.addView(icon, new LinearLayout.LayoutParams(Design.dp(a, 40), Design.dp(a, 40)));

    LinearLayout texts = Design.column(a);
    texts.setPadding(Design.dp(a, 14), 0, 0, 0);
    TextView name = Design.text(a, app.label, 16, true);
    texts.addView(name);
    if (blocked) texts.addView(Design.text(a, "Descansando · vuelve en " + minutesLeft(a) + " min", 12, false));
    else if (ocio) texts.addView(Design.text(a, "Ocio · cuenta para tus 5 minutos", 12, false));
    row.addView(texts, new LinearLayout.LayoutParams(0, -2, 1));

    row.setContentDescription(app.label + (blocked ? ", bloqueada" : ocio ? ", app de ocio" : ""));
    row.setOnClickListener(v -> open(a, app, blocked));
    row.setOnLongClickListener(v -> {
      options(a, app, ocio);
      return true;
    });
    UiMotion.press(row);
    return row;
  }

  private static long minutesLeft(MainActivity a) {
    return (a.repository.remaining() + 59_999) / 60_000;
  }

  private static Drawable icon(Context c, String pkg) {
    Drawable d = icons.get(pkg);
    if (d != null) return d;
    try {
      d = c.getPackageManager().getApplicationIcon(pkg);
    } catch (Exception e) {
      d = c.getDrawable(R.drawable.leaf);
    }
    if (icons.size() > 400) icons.clear();
    icons.put(pkg, d);
    return d;
  }

  static void clearIcons() {
    icons.clear();
  }

  private static void open(MainActivity a, App app, boolean blocked) {
    if (blocked) {
      a.message("Tus apps de ocio descansan. Vuelven en " + minutesLeft(a)
          + " min. Mientras, ¿algo tranquilo?");
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

  private static void options(MainActivity a, App app, boolean ocio) {
    boolean canBeOcio = !FocusEngine.essential(a.repository.config(), app.pkg);
    List<String> labels = new ArrayList<>();
    List<Runnable> actions = new ArrayList<>();
    if (ocio || canBeOcio) {
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
