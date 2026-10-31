package es.foco.next.ui;
import static org.junit.Assert.*;
import android.app.Activity;import android.graphics.*;import android.view.*;import android.widget.*;
import org.junit.*;import org.junit.runner.RunWith;import org.robolectric.*;import org.robolectric.annotation.*;
@RunWith(RobolectricTestRunner.class) @Config(sdk=35) @GraphicsMode(GraphicsMode.Mode.NATIVE)
public class FloatingLeavesTest {
 @Test public void visibleLeavesMoveWhileControlsRemainClear(){var a=Robolectric.buildActivity(Activity.class).setup().visible();try{FrameLayout root=new FrameLayout(a.get());a.get().setContentView(root);Button button=new Button(a.get());button.setText("Texto protegido");FrameLayout.LayoutParams bp=new FrameLayout.LayoutParams(600,150);bp.topMargin=400;root.addView(button,bp);FloatingLeaves leaves=new FloatingLeaves(a.get());root.addView(leaves,new FrameLayout.LayoutParams(-1,-1));root.measure(View.MeasureSpec.makeMeasureSpec(600,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(1200,View.MeasureSpec.EXACTLY));root.layout(0,0,600,1200);leaves.protect(root);Bitmap first=Bitmap.createBitmap(600,1200,Bitmap.Config.ARGB_8888);leaves.frame(5,true);leaves.draw(new Canvas(first));Bitmap second=Bitmap.createBitmap(600,1200,Bitmap.Config.ARGB_8888);leaves.frame(10,true);leaves.draw(new Canvas(second));assertFalse(first.sameAs(second));assertEquals(0,first.getPixel(300,450));assertEquals(0,second.getPixel(300,450));assertFalse(leaves.isClickable());}finally{a.pause().stop().destroy();}}
}
