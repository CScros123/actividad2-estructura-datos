package es.foco.next.runtime;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.media.AudioManager;
import android.net.Uri;
import android.telecom.TelecomManager;
import java.util.HashSet;
import java.util.Set;

final class CallMonitor {
  final AudioManager audio;
  final Set<String> packages = new HashSet<>();

  CallMonitor(Context context) {
    audio = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    String[] known = {
      "com.android.phone",
      "com.android.server.telecom",
      "com.android.emergency",
      "com.samsung.android.incallui",
      "com.google.android.dialer",
      "com.android.dialer",
      "com.samsung.android.dialer"
    };
    java.util.Collections.addAll(packages, known);
    add(context, new Intent(Intent.ACTION_DIAL, Uri.parse("tel:")));
    add(context, new Intent("com.android.phone.EmergencyDialer.DIAL"));
    try {
      TelecomManager telecom = (TelecomManager) context.getSystemService(Context.TELECOM_SERVICE);
      if (telecom != null && telecom.getDefaultDialerPackage() != null)
        packages.add(telecom.getDefaultDialerPackage());
    } catch (RuntimeException ignored) {
    }
  }

  private void add(Context context, Intent intent) {
    try {
      for (ResolveInfo entry : context.getPackageManager().queryIntentActivities(intent, 0))
        if (entry.activityInfo != null) packages.add(entry.activityInfo.packageName);
    } catch (RuntimeException ignored) {
    }
  }

  boolean active(String foreground) {
    if (packages.contains(foreground)) return true;
    try {
      int mode = audio == null ? AudioManager.MODE_NORMAL : audio.getMode();
      return mode == AudioManager.MODE_IN_CALL
          || mode == AudioManager.MODE_IN_COMMUNICATION
          || mode == AudioManager.MODE_RINGTONE;
    } catch (RuntimeException ignored) {
      return false;
    }
  }
}
