package es.foco.next.data;
import static org.junit.Assert.*;
import android.content.Context;import org.junit.*;import org.junit.runner.RunWith;import org.robolectric.*;import org.robolectric.annotation.*;
@RunWith(RobolectricTestRunner.class) @Config(sdk=35)
public class NotebookStoreTest {
 private Context c;
 @Before public void init(){c=RuntimeEnvironment.getApplication();c.getSharedPreferences("foco-notebook",0).edit().clear().commit();}
 @Test public void datedNotesPersistAndExportInReadableForm(){NotebookStore n=new NotebookStore(c);n.save("2026-10-26","Leer un capítulo\nUna idea que aprendí.");n=new NotebookStore(c);assertTrue(n.get("2026-10-26").contains("capítulo"));assertTrue(n.exportText().contains("2026-10-26"));assertTrue(n.exportText().contains("Una idea que aprendí."));}
 @Test public void editsAndDeletionAffectOnlySelectedDate(){NotebookStore n=new NotebookStore(c);n.save("2026-10-25","A");n.save("2026-10-26","B");n.save("2026-10-26","C");assertEquals("C",new NotebookStore(c).get("2026-10-26"));n.save("2026-10-26","");assertEquals("A",n.get("2026-10-25"));assertEquals(1,n.entries().size());}
 @Test public void invalidNoteDoesNotOverwriteSavedText(){NotebookStore n=new NotebookStore(c);n.save("2026-10-26","Keep");String text=new String(new char[4001]).replace('\0','x');assertThrows(IllegalArgumentException.class,()->n.save("2026-10-26",text));assertEquals("Keep",new NotebookStore(c).get("2026-10-26"));}
 @Test public void corruptOriginalIsRetained(){c.getSharedPreferences("foco-notebook",0).edit().putString("entries","broken").commit();assertThrows(IllegalStateException.class,()->new NotebookStore(c));assertEquals("broken",c.getSharedPreferences("foco-notebook",0).getString("entries",""));}
 @Test public void focusCheckpointsNeverRewriteOrExportPrivateNotes(){NotebookStore n=new NotebookStore(c);n.save("2026-10-26","Private idea");String raw=c.getSharedPreferences("foco-notebook",0).getString("entries","");Repository r=Repository.get(c);assertFalse(r.exportWeek().contains("Private idea"));assertFalse(r.exportBackup().contains("Private idea"));assertEquals(raw,c.getSharedPreferences("foco-notebook",0).getString("entries",""));}
}
