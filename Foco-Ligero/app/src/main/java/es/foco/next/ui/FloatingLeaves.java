package es.foco.next.ui;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.*;
import android.widget.*;

/** Foreground leaves across all pages, driven by the existing scene clock. Never receives touches. */
public final class FloatingLeaves extends View {
  private final Paint paint=new Paint(3);
  private final Path leaf=new Path(),mask=new Path(),wind=new Path();
  private final Rect rect=new Rect();private final int[] origin=new int[2];
  private View protectedRoot;
  private double time;
  private boolean enabled=true;
  private int count=16;
  private final float dp;
  public FloatingLeaves(Context c){this(c,null);}
  public FloatingLeaves(Context c,AttributeSet attrs){super(c,attrs);dp=c.getResources().getDisplayMetrics().density;leaf.moveTo(-12,0);leaf.cubicTo(-3,-16,12,-12,15,-5);leaf.cubicTo(10,10,-5,12,-12,0);leaf.close();setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);setClickable(false);setFocusable(false);configure();}
  void protect(View root){protectedRoot=root;rebuildMask();}
  void configure(){var p=getContext().getSharedPreferences("foco-ambient",0);enabled=p.getBoolean("enabled",true)&&p.getBoolean("leaves",true);count=p.getInt("wind",1)==0?10:p.getInt("wind",1)==2?22:16;invalidate();}
  void frame(double seconds,boolean moving){if(moving)time=seconds;if(enabled)invalidate();}
  void rebuildMask(){mask.reset();getLocationOnScreen(origin);if(protectedRoot!=null)protectTree(protectedRoot);}
  static final String SHIELD="leaf_shield";
  // A shielded container counts as one rectangle, so long lists stay cheap to mask and draw.
  private void protectTree(View v){if(v==this||v.getVisibility()!=VISIBLE)return;if(SHIELD.equals(v.getTag())||v instanceof TextView||v instanceof ImageButton||v instanceof OrbitApps){if(v.getGlobalVisibleRect(rect)){rect.offset(-origin[0],-origin[1]);rect.inset(-Math.round(3*dp),-Math.round(3*dp));mask.addRect(rect.left,rect.top,rect.right,rect.bottom,Path.Direction.CW);}return;}if(v instanceof ViewGroup){ViewGroup group=(ViewGroup)v;for(int i=0;i<group.getChildCount();i++)protectTree(group.getChildAt(i));}}
  @Override protected void onSizeChanged(int w,int h,int oldw,int oldh){rebuildMask();}
  @Override protected void onDraw(Canvas c){super.onDraw(c);if(!enabled)return;c.save();c.clipOutPath(mask);c.scale(dp,dp);float w=getWidth()/dp,h=getHeight()/dp;double t=time;
    for(int i=0;i<count;i++){double travel=t*(22+i%4*5)+i*(h+110)/count;float y=(float)(travel%(h+110))-55;float x=(float)((i*.6180339*w+t*(12+i%3*5))%(w+100))-50+(float)Math.sin(t*.8+i)*24;
      c.save();c.translate(x,y);c.rotate((float)(t*(18+i%3*8)+i*47+Math.sin(t*.6+i)*26));float size=.65f+(i%4)*.18f;c.scale(size,size);
      paint.setStyle(Paint.Style.FILL);paint.setColor(i%4==0?0xDD97BC60:i%3==0?0xDC6FAE70:0xE4478A54);c.drawPath(leaf,paint);paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(.8f);paint.setColor(0xAA173B28);c.drawPath(leaf,paint);paint.setColor(0xD5D7EAB5);c.drawLine(-10,0,12,-4,paint);c.drawLine(-1,-1,3,-7,paint);c.drawLine(4,-2,7,3,paint);c.restore();
    }
    paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(1);paint.setColor(0x4078B68D);for(int i=0;i<3;i++){float x=(float)((t*32+i*w*.4)%(w+140))-100,y=h*(.25f+i*.26f)+(float)Math.sin(t*.4+i)*16;wind.reset();wind.moveTo(x,y);wind.cubicTo(x+20,y-9,x+45,y+8,x+72,y-4);c.drawPath(wind,paint);}c.restore();
  }
}
