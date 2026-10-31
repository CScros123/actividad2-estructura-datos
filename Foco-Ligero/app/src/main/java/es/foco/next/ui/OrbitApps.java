package es.foco.next.ui;

import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.widget.*;
import java.util.*;
import java.util.function.Consumer;

/** Real installed apps above the cinematic layer. The video supplies the animation clock. */
final class OrbitApps extends LinearLayout {
  static final int PAGE_SIZE=7;
  private final List<Alternatives.Entry> apps;
  private final Consumer<Alternatives.Entry> launch;
  private final Ring ring;
  private final TextView caption,pageLabel;
  private final Button open,previous,next;
  private int selected,page;
  private double phase,offset,frozen;
  private boolean held,animated;
  private float downX,lastX;
  private final int slop;
  OrbitApps(Context c,List<Alternatives.Entry> entries,String preferred,Consumer<Alternatives.Entry> launch){
    super(c);setOrientation(VERTICAL);this.launch=launch;apps=new ArrayList<>(entries);for(int i=0;i<apps.size();i++)if(apps.get(i).pkg.equals(preferred)){Collections.swap(apps,0,i);break;}
    slop=ViewConfiguration.get(c).getScaledTouchSlop();ring=new Ring(c);addView(ring,new LayoutParams(-1,Design.dp(c,238)));
    caption=Design.text(c,"",18,true);caption.setTextColor(0xFFFFE7A3);caption.setGravity(Gravity.CENTER);addView(caption);
    open=goldButton(c,"",()->launchSelected());addView(open);
    LinearLayout pager=new LinearLayout(c);previous=goldButton(c,"‹",()->page(-1));next=goldButton(c,"›",()->page(1));previous.setContentDescription("Apps anteriores");next.setContentDescription("Más apps");
    pageLabel=Design.text(c,"",13,false);pageLabel.setTextColor(0xFFE4D0A1);pageLabel.setGravity(Gravity.CENTER);
    pager.addView(previous,new LayoutParams(Design.dp(c,56),Design.dp(c,48)));pager.addView(pageLabel,new LayoutParams(0,Design.dp(c,48),1));pager.addView(next,new LayoutParams(Design.dp(c,56),Design.dp(c,48)));addView(pager);
    TextView tip=Design.text(c,"Desliza para girar · toca un icono para elegir",12,false);tip.setTextColor(0xFFE4D0A1);tip.setGravity(Gravity.CENTER);addView(tip);
    if(apps.isEmpty()){ring.setVisibility(GONE);open.setVisibility(GONE);pager.setVisibility(GONE);tip.setVisibility(GONE);caption.setText("Elige tu siguiente app en Mi plan.");}else showPage();
  }
  int selectedIndex(){return selected;}
  int currentPage(){return page;}
  int count(){return apps.size();}
  void select(int index){if(index<page*PAGE_SIZE||index>=Math.min(apps.size(),(page+1)*PAGE_SIZE))return;selected=index;caption.setText(apps.get(index).label);open.setText("Abrir · "+apps.get(index).label);ring.highlight();ring.arrange();announceForAccessibility(apps.get(index).label+", seleccionada");}
  void launchSelected(){if(!apps.isEmpty())launch.accept(apps.get(selected));}
  void page(int delta){if(apps.isEmpty())return;int pages=(apps.size()+PAGE_SIZE-1)/PAGE_SIZE;page=Math.floorMod(page+delta,pages);selected=page*PAGE_SIZE;showPage();}
  private void showPage(){ring.bind();select(selected);int pages=(apps.size()+PAGE_SIZE-1)/PAGE_SIZE;pageLabel.setText((page+1)+" / "+pages+" · "+apps.size()+" apps");previous.setEnabled(pages>1);next.setEnabled(pages>1);}
  void videoFrame(int position,int duration,boolean moving){animated=moving;if(moving&&duration>0)phase=2*Math.PI*position/duration;ring.arrange();ring.invalidate();}
  void motion(boolean enabled){animated=enabled;if(!enabled){held=false;UiMotion.stop(this);}ring.arrange();ring.invalidate();}
  private static Button goldButton(Context c,String text,Runnable run){Button b=Design.button(c,text,false,run);GradientDrawable bg=new GradientDrawable();bg.setColor(0xE51D1810);bg.setCornerRadius(Design.dp(c,16));bg.setStroke(Design.dp(c,1),0xFFCDA85D);b.setBackground(bg);b.setTextColor(0xFFFFE7A3);return b;}
  private final class Ring extends FrameLayout {
    final ImageButton[] icons=new ImageButton[PAGE_SIZE];
    final Paint paint=new Paint(3);
    Shader glow;
    boolean dragging;
    Ring(Context c){super(c);setWillNotDraw(false);setClipChildren(false);setClipToPadding(false);for(int slot=0;slot<PAGE_SIZE;slot++){
      final int s=slot;ImageButton icon=new ImageButton(c);icon.setScaleType(ImageView.ScaleType.FIT_CENTER);icon.setPadding(Design.dp(c,12),Design.dp(c,12),Design.dp(c,12),Design.dp(c,12));
      icon.setOnClickListener(v->{int index=page*PAGE_SIZE+s;if(index==selected)launchSelected();else select(index);});
      icons[slot]=icon;addView(icon,new FrameLayout.LayoutParams(Design.dp(c,60),Design.dp(c,60)));
    }}
    void bind(){for(int slot=0;slot<PAGE_SIZE;slot++){int index=page*PAGE_SIZE+slot;ImageButton icon=icons[slot];icon.setVisibility(index<apps.size()?VISIBLE:GONE);if(index<apps.size()){Alternatives.Entry e=apps.get(index);try{android.graphics.drawable.Drawable art=getContext().getPackageManager().getApplicationIcon(e.pkg);if(art==null)icon.setImageResource(es.foco.next.R.drawable.leaf);else icon.setImageDrawable(art);}catch(android.content.pm.PackageManager.NameNotFoundException ex){icon.setImageResource(es.foco.next.R.drawable.leaf);}icon.setContentDescription(e.label);icon.setTooltipText(e.label);}}}
    void highlight(){for(int slot=0;slot<PAGE_SIZE;slot++){boolean chosen=page*PAGE_SIZE+slot==selected;ImageButton icon=icons[slot];GradientDrawable bg=new GradientDrawable();bg.setColor(chosen?0xF24F3818:0xD5141C20);bg.setShape(GradientDrawable.OVAL);bg.setStroke(Design.dp(getContext(),chosen?2:1),chosen?0xFFFFDF8A:0x9987B9A3);icon.setBackground(bg);icon.setSelected(chosen);}}
    @Override protected void onSizeChanged(int w,int h,int oldw,int oldh){glow=new RadialGradient(w/2f,h/2f,Design.dp(getContext(),59),new int[]{0xB8FFD570,0x50E5B246,0x00E5B246},new float[]{0,.55f,1},Shader.TileMode.CLAMP);arrange();}
    void arrange(){if(getWidth()==0||apps.isEmpty())return;int total=Math.min(PAGE_SIZE,apps.size()-page*PAGE_SIZE),other=0;float density=getResources().getDisplayMetrics().density,cx=getWidth()/2f,cy=getHeight()/2f,size=Design.dp(getContext(),60),rx=Math.max(0,cx-Design.dp(getContext(),35)),ry=Design.dp(getContext(),73);double angle=held?frozen:phase+offset;
      for(int slot=0;slot<total;slot++){ImageButton icon=icons[slot];boolean chosen=page*PAGE_SIZE+slot==selected;double a=angle+(total>1?other*2*Math.PI/(total-1):0);float depth=chosen?1:(float)(.86+.10*Math.sin(a));if(!chosen)other++;
        icon.setTranslationX(cx-size/2+(chosen?0:(float)Math.cos(a)*rx));icon.setTranslationY(cy-size/2+(chosen?0:(float)Math.sin(a)*ry));icon.setScaleX(chosen?1.2f:depth);icon.setScaleY(chosen?1.2f:depth);icon.setAlpha(chosen?1:.68f+.25f*(float)((Math.sin(a)+1)/2));icon.setTranslationZ(chosen?8*density:(float)(1+Math.sin(a))*density);
      }
    }
    @Override protected void onDraw(Canvas canvas){super.onDraw(canvas);if(apps.isEmpty())return;float cx=getWidth()/2f,cy=getHeight()/2f,rx=Math.max(0,cx-Design.dp(getContext(),35)),ry=Design.dp(getContext(),73);paint.setStyle(Paint.Style.FILL);paint.setShader(glow);paint.setAlpha(animated?(int)(180+45*Math.sin(phase*3)):210);canvas.drawCircle(cx,cy,Design.dp(getContext(),59),paint);paint.setShader(null);paint.setAlpha(255);paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(Design.dp(getContext(),1));paint.setColor(0xAA80DEC0);canvas.drawOval(cx-rx,cy-ry,cx+rx,cy+ry,paint);paint.setStyle(Paint.Style.FILL);
      double angle=held?frozen:phase+offset;for(int i=0;i<10;i++){double a=angle+i*Math.PI/5;paint.setColor(i%2==0?0xFFFFD278:0xB89BFFE2);canvas.drawCircle(cx+(float)Math.cos(a)*rx,cy+(float)Math.sin(a)*ry,Design.dp(getContext(),i%3==0?2:1),paint);}
    }
    @Override public boolean dispatchTouchEvent(MotionEvent e){if(e.getActionMasked()==MotionEvent.ACTION_DOWN){held=true;frozen=phase+offset;downX=lastX=e.getX();getParent().requestDisallowInterceptTouchEvent(true);}boolean result=super.dispatchTouchEvent(e);if(e.getActionMasked()==MotionEvent.ACTION_UP||e.getActionMasked()==MotionEvent.ACTION_CANCEL){offset=frozen-phase;held=false;dragging=false;getParent().requestDisallowInterceptTouchEvent(false);arrange();}return result;}
    @Override public boolean onInterceptTouchEvent(MotionEvent e){if(e.getActionMasked()==MotionEvent.ACTION_MOVE&&Math.abs(e.getX()-downX)>slop){dragging=true;return true;}return false;}
    @Override public boolean onTouchEvent(MotionEvent e){if(e.getActionMasked()==MotionEvent.ACTION_MOVE&&held){frozen+=(e.getX()-lastX)/Math.max(1,getWidth())*Math.PI*2;lastX=e.getX();arrange();invalidate();return true;}if(e.getActionMasked()==MotionEvent.ACTION_UP){if(!dragging)performClick();return true;}return true;}
    @Override public boolean performClick(){super.performClick();return true;}
  }
}
