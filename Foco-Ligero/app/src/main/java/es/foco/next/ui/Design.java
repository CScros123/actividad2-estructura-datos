package es.foco.next.ui;

import android.content.Context;
import android.graphics.Typeface;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import es.foco.next.R;

final class Design {
  private Design() {}

  static int dp(Context c, float n) {
    return Math.round(c.getResources().getDisplayMetrics().density * n);
  }

  static LinearLayout column(Context c) {
    LinearLayout v = new LinearLayout(c);
    v.setOrientation(LinearLayout.VERTICAL);
    return v;
  }

  static TextView text(Context c, String value, int sp, boolean strong) {
    TextView v = new TextView(c);
    v.setText(value);
    v.setTextSize(sp);
    v.setTextColor(c.getColor(strong ? R.color.text_primary : R.color.text_secondary));
    v.setPadding(0, dp(c, 4), 0, dp(c, 4));
    v.setTypeface(Typeface.create(strong && sp>=23 ? "serif" : strong ? "sans-serif-medium" : "sans-serif", Typeface.NORMAL));
    v.setIncludeFontPadding(false);
    v.setLetterSpacing(strong && sp >= 20 ? -.02f : 0f);
    v.setLineSpacing(dp(c, 3), 1f);
    return v;
  }

  static void note(LinearLayout parent, String value) {
    parent.addView(text(parent.getContext(), value, 15, false));
  }

  static LinearLayout card(LinearLayout parent, String title) {
    Context c = parent.getContext();
    LinearLayout box = column(c);
    box.setBackgroundResource(R.drawable.card);
    box.getBackground().mutate().setAlpha(237);
    LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2);
    p.bottomMargin = dp(c, 16);
    
    parent.addView(box, p);

    if (!title.isEmpty()) {
      TextView heading = text(c, title, 20, true);
      if (android.os.Build.VERSION.SDK_INT >= 28) heading.setAccessibilityHeading(true);
      box.addView(heading);
      gap(box, 8);
    }
    return box;
  }

  static Button button(Context c, String label, boolean primary, Runnable run) {
    Button b = new Button(c);
    b.setText(label);
    b.setAllCaps(false);
    b.setTextSize(16);
    b.setMinHeight(dp(c, 52));
    b.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
    b.setTextColor(c.getColor(primary ? R.color.on_accent : R.color.copper));
    b.setBackgroundResource(primary ? R.drawable.button_primary : R.drawable.button_secondary);
    b.setPadding(dp(c, 16), dp(c, 10), dp(c, 16), dp(c, 10));
    LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2);
    p.topMargin = dp(c, 8);
    b.setLayoutParams(p);
    b.setOnClickListener(v -> run.run());
    UiMotion.press(b);


    return b;
  }

  static void gap(LinearLayout parent, int dp) {
    parent.addView(
        new View(parent.getContext()),
        new LinearLayout.LayoutParams(1, dp(parent.getContext(), dp)));
  }

  static EditText field(Context c, String hint, String value, boolean number) {
    EditText e = new EditText(c);
    e.setHint(hint);
    e.setText(value);
    e.setTextColor(c.getColor(R.color.text_primary));
    e.setHintTextColor(c.getColor(R.color.text_secondary));
    e.setMinHeight(dp(c, 52));
    e.setTypeface(Typeface.create("sans-serif", Typeface.NORMAL));
    e.setTextSize(16);
    android.graphics.drawable.GradientDrawable fieldBackground=new android.graphics.drawable.GradientDrawable();
    fieldBackground.setColor(c.getColor(R.color.surface));
    fieldBackground.setCornerRadius(dp(c,14));
    e.setBackground(fieldBackground);
    e.setPadding(dp(c,14),dp(c,12),dp(c,14),dp(c,12));
    if (number) e.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
    return e;
  }

  static String duration(Context c, long ms) {
    long sec = Math.max(0, ms / 1000);
    return c.getString(R.string.duration_minutes, sec / 60, sec % 60);
  }
}
