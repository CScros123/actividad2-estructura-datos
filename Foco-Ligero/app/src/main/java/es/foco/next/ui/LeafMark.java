package es.foco.next.ui;
import android.content.Context;
import android.widget.ImageView;
import es.foco.next.R;
/** Static leaf; no animation callbacks or background work. */
final class LeafMark extends ImageView {
  LeafMark(Context c){super(c);setImageResource(R.drawable.leaf);int pad=Design.dp(c,20);setPadding(pad,pad,pad,pad);setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);}
}
