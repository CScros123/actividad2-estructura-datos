package es.foco.next.runtime;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityWindowInfo;
import java.util.List;

/** Resolve the focused app, excluding accessibility overlays and keyboards. */
final class ForegroundWindows {
  static String current(AccessibilityService service, boolean overlayVisible) {
    try {
      List<AccessibilityWindowInfo> windows=service.getWindows();
      for(AccessibilityWindowInfo w:windows) {
        if(w.getType()!=AccessibilityWindowInfo.TYPE_APPLICATION || !w.isFocused())continue;
        AccessibilityNodeInfo root=w.getRoot();
        try { if(root!=null && root.getPackageName()!=null)return root.getPackageName().toString(); }
        finally { if(root!=null)root.recycle(); }
      }
    } catch(RuntimeException ignored) {}
    AccessibilityNodeInfo root=null;
    try {
      root=service.getRootInActiveWindow();
      if(root==null || root.getPackageName()==null)return "";
      String pkg=root.getPackageName().toString();
      return overlayVisible && pkg.equals(service.getPackageName())?"":pkg;
    } catch(RuntimeException ignored) { return ""; }
    finally { if(root!=null)root.recycle(); }
  }
}
