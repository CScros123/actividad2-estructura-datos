package es.foco.next.ui;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Rect;
import android.os.PowerManager;
import android.view.*;
import android.view.animation.DecelerateInterpolator;
import es.foco.next.R;

/** Short interaction animations only; no repeating animator on text or controls. */
final class UiMotion {
  private static final DecelerateInterpolator EASE=new DecelerateInterpolator(1.5f);
  static boolean allowed(Context c){
    var p=c.getSharedPreferences("foco-ambient",0);
    return ValueAnimator.areAnimatorsEnabled()&&!p.getBoolean("reduce",false)&&!p.getBoolean("saving",false)&&!c.getSystemService(PowerManager.class).isPowerSaveMode();
  }
  static void press(View view){
    view.setOnTouchListener((v,e)->{
      int action=e.getActionMasked();
      if(action==MotionEvent.ACTION_DOWN||action==MotionEvent.ACTION_UP||action==MotionEvent.ACTION_CANCEL){
        v.animate().cancel();
        if(allowed(v.getContext())){float s=action==MotionEvent.ACTION_DOWN?.965f:1;v.animate().scaleX(s).scaleY(s).setDuration(action==MotionEvent.ACTION_DOWN?110:220).setInterpolator(EASE).start();}
        else{v.setScaleX(1);v.setScaleY(1);}
      }
      return false;
    });
  }
  static void reveal(ViewGroup body,Rect viewport){
    if(!body.isAttachedToWindow())return;
    boolean enabled=allowed(body.getContext());int order=0;
    for(int i=0;i<body.getChildCount();i++){
      View v=body.getChildAt(i);
      if(v.getHeight()==0||v.getBottom()<viewport.top||v.getTop()>viewport.bottom||Boolean.TRUE.equals(v.getTag(R.id.motion_seen)))continue;
      v.setTag(R.id.motion_seen,true);
      if(enabled){v.setAlpha(.25f);v.setTranslationY(Design.dp(body.getContext(),18));v.animate().alpha(1).translationY(0).setStartDelay(Math.min(order++*35,140)).setDuration(420).setInterpolator(EASE).start();}
    }
  }
  static void stop(View v){
    v.animate().cancel();v.setAlpha(1);v.setScaleX(1);v.setScaleY(1);v.setTranslationY(0);v.setTranslationX(0);
    if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)stop(g.getChildAt(i));}
  }
}
