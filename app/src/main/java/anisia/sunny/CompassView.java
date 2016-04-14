package anisia.sunny;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by Utente on 12/04/2016.
 */
public class CompassView extends View {

    private Bitmap bm;
    private Matrix mtx = new Matrix();
    private float mDirection = 65;
    private float density = 0;

    public CompassView(Context context) {
        super(context);
        init();
    }

    public CompassView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CompassView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        bm = BitmapFactory.decodeResource(getResources(), R.drawable.wind_direction);
        density = getContext().getResources().getDisplayMetrics().density;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.translate(18 * density, 18 * density);
        canvas.drawBitmap(bm, rotate(bm), null);
    }

    public Matrix rotate(Bitmap bm){
        mtx.setRotate(mDirection, bm.getWidth() / 2, bm.getHeight() / 2);
        return mtx;
    }

    public void update(float direction){
        mDirection = direction;
        invalidate();
    }
}
