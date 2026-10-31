package es.foco.next.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import es.foco.next.R;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

final class AppPicker {
  static String label(Context c, String pkg) {
    try {
      return c.getPackageManager()
          .getApplicationLabel(c.getPackageManager().getApplicationInfo(pkg, 0))
          .toString();
    } catch (Exception ignored) {
      return pkg;
    }
  }

  private static List<String> packages(MainActivity a) {
    Map<String, String> unique = new LinkedHashMap<>();
    es.foco.next.core.AppConfig config=a.repository.config();
    es.foco.next.runtime.BankAppGuard banks=new es.foco.next.runtime.BankAppGuard(a);
    for (ResolveInfo r :
        a.getPackageManager()
            .queryIntentActivities(
                new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER), 0)) {
      String pkg = r.activityInfo.packageName;
      if (es.foco.next.core.FocusEngine.essential(config,pkg)
          || banks.hide(config,pkg)
          || pkg.equals(a.getPackageName())
          || pkg.equals("com.android.settings")
          || pkg.equals("com.android.systemui")
          || pkg.contains("dialer")
          || pkg.equals("com.android.phone")) continue;
      unique.put(pkg, label(a, pkg));
    }
    List<String> list = new ArrayList<>(unique.keySet());
    list.sort(Comparator.comparing(unique::get, String.CASE_INSENSITIVE_ORDER));
    return list;
  }

  static void many(MainActivity a, Set<String> original, Consumer<Set<String>> done) {
    List<String> pkgs = packages(a);
    for (String p : original) if (!pkgs.contains(p)) pkgs.add(p);
    String[] labels = new String[pkgs.size()];
    boolean[] checked = new boolean[pkgs.size()];
    Set<String> chosen = new HashSet<>(original);
    for (int i = 0; i < pkgs.size(); i++) {
      labels[i] = label(a, pkgs.get(i));
      checked[i] = chosen.contains(pkgs.get(i));
    }
    new AlertDialog.Builder(a)
        .setTitle(R.string.choose_apps)
        .setMultiChoiceItems(
            labels,
            checked,
            (d, i, on) -> {
              if (on) chosen.add(pkgs.get(i));
              else chosen.remove(pkgs.get(i));
            })
        .setPositiveButton(R.string.save, (d, n) -> done.accept(chosen))
        .setNegativeButton(R.string.cancel, null)
        .show();
  }

  static void one(MainActivity a, Consumer<String> done) {
    List<String> pkgs = packages(a);
    pkgs.removeIf(p -> a.repository.config().watchedApps.contains(p) || es.foco.next.core.AlternativeCatalog.social(p));
    String[] labels = pkgs.stream().map(p -> label(a, p)).toArray(String[]::new);
    new AlertDialog.Builder(a)
        .setTitle(R.string.choose_one_app)
        .setItems(labels, (d, i) -> done.accept(pkgs.get(i)))
        .setNegativeButton(R.string.cancel, null)
        .show();
  }
}
