package br.usp.ime.compmus.dj.hoketus;

import org.puredata.core.PdBase;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;

public class MultitouchView extends View {

//  private static final int SIZE = 160;

  private SparseArray<PointF> mActivePointers;
  private Paint mPaint;
//  private int[] colors = { Color.BLUE, Color.GREEN, Color.MAGENTA,
//      Color.BLACK, Color.CYAN, Color.GRAY, Color.RED, Color.DKGRAY,
//      Color.LTGRAY, Color.YELLOW };

  private int[] colors = { Color.BLACK, Color.BLACK, Color.BLACK, Color.BLACK, 
		  Color.BLACK, Color.BLACK, Color.BLACK, Color.BLACK, Color.BLACK, 
		  Color.BLACK};

  
  private Paint textPaint;


  private int touchIds[] = new int[20];

  public MultitouchView(Context context, AttributeSet attrs) {
    super(context, attrs);
    initView();
  }

  private void initView() {
    mActivePointers = new SparseArray<PointF>();
    mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    // set painter color to a color you like
    mPaint.setColor(Color.BLUE);
    mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
    textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    textPaint.setTextSize(20);    
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {

    // get pointer index from the event object
    int pointerIndex = event.getActionIndex();

    // get pointer ID
    int pointerId = event.getPointerId(pointerIndex);

    // get masked (not specific to a pointer) action
    int maskedAction = event.getActionMasked();

    switch (maskedAction) {

    case MotionEvent.ACTION_DOWN:
    case MotionEvent.ACTION_POINTER_DOWN: {
      // We have a new pointer. Lets add it to the list of pointers

      PointF f = new PointF();
      f.x = event.getX(pointerIndex);
      f.y = event.getY(pointerIndex);
      mActivePointers.put(pointerId, f);
      
		for(int i = 0; i < event.getPointerCount(); i++) {
			int id = event.getPointerId(i);
			PdBase.sendFloat("sensorT"+id+"vx", event.getX());
			PdBase.sendFloat("sensorT"+id+"vy", event.getY());						
		}
      
      break;
    }
    case MotionEvent.ACTION_MOVE: { // a pointer was moved
      for (int size = event.getPointerCount(), i = 0; i < size; i++) {
        PointF point = mActivePointers.get(event.getPointerId(i));
        if (point != null) {
          point.x = event.getX(i);
          point.y = event.getY(i);
        }
      }
      
      for(int i = 0; i < event.getPointerCount(); i++) {
			int id = event.getPointerId(i);
			PdBase.sendFloat("sensorT"+id+"vx", event.getX());
			PdBase.sendFloat("sensorT"+id+"vy", event.getY());
		}
      
      
      break;
    }
    case MotionEvent.ACTION_UP:
    case MotionEvent.ACTION_POINTER_UP:
    case MotionEvent.ACTION_CANCEL: {
      mActivePointers.remove(pointerId);
      
		
		int id = event.getActionIndex();
		PdBase.sendFloat("sensorT"+id+"vx", -1);
		PdBase.sendFloat("sensorT"+id+"vy", -1);
      
      
      break;
    }
    }
    invalidate();

    return true;
  }
  
//Android Touch

  protected int getTouchIdAssignment() {
		for(int i = 0; i < touchIds.length; i++) {
			if(touchIds[i] == -1) {
				return i;
			}
		}
		return -1;
	}
	
	protected int getTouchId(int touchId) {
		for(int i = 0; i < touchIds.length; i++) {
			if(touchIds[i] == touchId) {
				return i;
			}
		}
		return -1;
	}

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);

    Resources res = getResources();
    Bitmap bitmap = BitmapFactory.decodeResource(res, R.drawable.icone);
    Bitmap img = Bitmap.createScaledBitmap( bitmap, bitmap.getWidth()+bitmap.getWidth()/2, bitmap.getHeight()+bitmap.getHeight()/2, true );
    
    // draw all pointers
    for (int size = mActivePointers.size(), i = 0; i < size; i++) {
      PointF point = mActivePointers.valueAt(i);
      if (point != null)
        mPaint.setColor(colors[i % 9]);
//      canvas.drawCircle(point.x, point.y, SIZE, mPaint);
//      canvas.drawBitmap(bitmap, point.x-50, point.y-50, mPaint);
      canvas.drawBitmap(img, point.x-img.getWidth()/2, point.y-img.getHeight()/2, mPaint);
    }
//    canvas.drawText("Total pointers: " + mActivePointers.size(), 10, 40 , textPaint);
    canvas.drawText("Hotspots: " + HoketusUserActivity.getHotspotList(), 10, 40 , textPaint);
  }

} 