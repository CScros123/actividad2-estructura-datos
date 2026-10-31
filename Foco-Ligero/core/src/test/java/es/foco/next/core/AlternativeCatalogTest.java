package es.foco.next.core;
import static org.junit.Assert.*;
import org.junit.Test;
public class AlternativeCatalogTest {
 @Test public void facebookNeverBecomesAnEbookEvenWhenChosenOrMislabeled(){assertEquals(-1,AlternativeCatalog.group("com.facebook.katana","Facebook",false));assertEquals(-1,AlternativeCatalog.group("com.facebook.lite","Kindle",true));}
 @Test public void partialBookWordsAndUnknownAppsAreNotRecommendations(){for(String p:new String[]{"com.game.ebooks","com.app.productivity","com.company.mybooks"})assertEquals(-1,AlternativeCatalog.group(p,"An app",false));}
 @Test public void usersBookAppsAreExplicitlyRecognized(){for(String p:new String[]{"com.google.android.apps.books","com.gtl.nextory","com.syc.librototal.El_Libro_Total","com.sirma.mobile.bible.android"})assertEquals(AlternativeCatalog.READING,AlternativeCatalog.group(p,"Localized title",false));}
 @Test public void bibleLabelHasReadingGroupAndDuolingoLearning(){assertEquals(0,AlternativeCatalog.group("another.bible.reader","Santa Biblia",false));assertEquals(1,AlternativeCatalog.group("com.duolingo","Duolingo",false));}
 @Test public void selectedGameIsPersonalNotAutomaticallyEducational(){assertEquals(-1,AlternativeCatalog.group("com.example.iqdungeon","IQDungeon2",false));assertEquals(AlternativeCatalog.PERSONAL,AlternativeCatalog.group("com.example.iqdungeon","IQDungeon2",true));}
 @Test public void existingNotesAndOrganizationAppsHaveCreationGroup(){for(String p:new String[]{"com.google.android.keep","com.samsung.android.app.notes","com.samsung.android.calendar","com.samsung.android.app.reminder","com.openai.chatgpt"})assertEquals(2,AlternativeCatalog.group(p,"Title",false));assertEquals(2,AlternativeCatalog.group("an.appsheet.build","AppSheet",false));}
}
