package es.foco.next.ui;

import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.view.View;

/** Live foreground, sky and water over the terrace artwork, driven by AmbientView's sole clock. */
final class TerraceView extends View {
  private final Drawable terrace;
  private final Paint paint=new Paint(3);
  private final Path leaf=new Path();
  private final Rect visible=new Rect();
  private final float dp;
  private final boolean night;
  private double time;
  private float windScale=1;
  private boolean inViewport=true;
  TerraceView(Context c){super(c);dp=c.getResources().getDisplayMetrics().density;night=SceneTheme.night();terrace=c.getDrawable(SceneTheme.landscape());setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);leaf.moveTo(0,0);leaf.cubicTo(7,-10,20,-9,25,-3);leaf.cubicTo(17,6,7,8,0,0);leaf.close();}
  void wind(float scale){windScale=scale;}
  void viewport(){inViewport=getLocalVisibleRect(visible);}
  void tick(double t){time=t;if(inViewport&&isShown())invalidate();}
  @Override protected void onSizeChanged(int w,int h,int ow,int oh){float scale=Math.max(w/(float)terrace.getIntrinsicWidth(),h/(float)terrace.getIntrinsicHeight());int sw=Math.round(terrace.getIntrinsicWidth()*scale),sh=Math.round(terrace.getIntrinsicHeight()*scale);terrace.setBounds((w-sw)/2,(h-sh)/2,(w+sw)/2,(h+sh)/2);}
  @Override protected void onDraw(Canvas c){
    terrace.draw(c);c.save();c.scale(dp,dp);float w=getWidth()/dp,h=getHeight()/dp;float breeze=(float)AmbientView.wind(time)*windScale;
    // A transparent sky layer drifts independently, rather than moving the complete photograph.
    paint.setStyle(Paint.Style.FILL);paint.setColor(night?0x18436574:0x32FFFFFF);
    for(int i=0;i<3;i++){float x=(float)((time*1.7+i*w*.42)%(w+75))-45,y=14+i*11;c.drawOval(x-26,y-4,x+30,y+4,paint);c.drawOval(x-9,y-9,x+12,y+2,paint);}
    // Ripples stay on the sea, without moving the terrace architecture.
    paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(.6f);paint.setColor(night?0x406E9C9E:0x65E7FFFF);
    for(int i=0;i<7;i++){float x=w*.16f+i%3*w*.11f+(float)Math.sin(time*.6+i)*4,y=h*.49f+i*2.4f;c.drawLine(x,y,x+9+(float)Math.sin(time+i)*3,y,paint);}
    paint.setStyle(Paint.Style.FILL);
    for(int side=0;side<2;side++)for(int i=0;i<7;i++){
      c.save();c.translate(side==0?i%3*12:w-i%3*12,6+i*9);
      c.rotate((side==0?20:145)+(float)Math.sin(time*.8+i*1.7+side)*30*breeze);
      paint.setColor(night?0xC6345D43:0xCA3F7546);c.drawPath(leaf,paint);c.restore();
    }
    c.restore();
  }
}
