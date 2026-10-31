package es.foco.next.core;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public final class FocusState {
  public Deadline window=new Deadline();
  public long usedMillis;
  public boolean warningSent;
  public Set<String> lockedApps=new LinkedHashSet<>();
  public String day="";
  public Map<String,Long> dailyPackageMillis=new LinkedHashMap<>();
  public Map<String,Map<String,Long>> weeklyOcio=new LinkedHashMap<>(), weeklyAlternative=new LinkedHashMap<>(), weeklyChoices=new LinkedHashMap<>();
  public Map<String,Long> weeklyBlocks=new LinkedHashMap<>();
  public transient boolean observedAlternative;
  // Certified live observation is intentionally not persisted across process death.
  public transient String observedPackage="";
  public transient long observedElapsed=-1;
  public transient int observedBoot=-1;
}
