package cufe.wby.cytux.Views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * 判定线
 */
public class BPMLine extends View {
    private final Line mLine;
    Paint paint = new Paint();

    public BPMLine(Context context, AttributeSet attrs) {
        super(context, attrs);
        mLine = new Line(0);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mLine != null) {
            paint.setAntiAlias(true);
            paint.setColor(Color.WHITE);
            paint.setStrokeWidth((float) 3);
            canvas.drawLine(0, mLine.getY(), getWidth(), mLine.getY(), paint);
        }
        super.onDraw(canvas);
    }

    public int getLineY() {
        return mLine.getY();
    }

    public void setLineY(int Y) {
        mLine.setY(Y);
        invalidate();
    }
}


// 水平直线类
class Line {
    private int mY;

    public Line(int Y) {
        mY = Y;
    }

    public int getY() {
        return mY;
    }

    public void setY(int mY) {
        this.mY = mY;
    }
}