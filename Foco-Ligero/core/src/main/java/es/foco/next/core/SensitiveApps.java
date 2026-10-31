package es.foco.next.core;

import java.util.Locale;

/** Conservative package hints plus the user's explicit no-overlay list. */
public final class SensitiveApps {
  private static final String[] TOKENS={"bgeneral","bancogeneral","banco.general","bbva","bancolombia","bancomer","santander","caixabank","bankinter","banorte","scotiabank","bancobogota","interbank","davivienda","bancoppel","banreservas","bancofalabella","bancoestado","bancochile","itau","bradesco","bcp.bank"};
  private SensitiveApps() {}
  public static boolean isSensitive(AppConfig c,String pkg){
    if(pkg==null||pkg.isEmpty())return false;
    if(c.sensitiveApps.contains(pkg))return true;
    String name=pkg.toLowerCase(Locale.ROOT);
    for(String token:TOKENS)
      if(name.contains(token))return true;
    return name.contains(".bank.")||name.endsWith(".bank")||name.equals("com.nu.production")||name.equals("com.bcp.innovacxion.yapeapp");
  }
}
