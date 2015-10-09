package jp.or.ixqsware.rc_zoids.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.SeekBar;

import jp.or.ixqsware.rc_zoids.R;

/**
 * カスタムシークバー
 *   - 縦方向
 *   - つまみ変更
 *   - 進捗率表示開始位置を変更
 *
 * Created by hisanaka on 15/10/02.
 */
public class CustomSeekBar extends SeekBar {
    private Paint paintBar = new Paint();
    private Paint paintMeter = new Paint();
    private OnSeekBarChangeListener mOnSeekBarChangeListener;

    public CustomSeekBar(Context context) {
        super(context);
    }

    public CustomSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);

        setThumbDrawable(MotionEvent.ACTION_CANCEL);
        setProgressDrawable(getResources().getDrawable(R.drawable.progress, context.getTheme()));

        int thumbHeight = getThumb().getIntrinsicHeight() / 2;
        setThumbOffset(thumbHeight);
        setPadding(0, 0, thumbHeight, 0);
    }

    public void setOnSeekBarChangeListener(OnSeekBarChangeListener l) {
        mOnSeekBarChangeListener = l;
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(h, w, oldh, oldw);
    }

    @Override
    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(heightMeasureSpec, widthMeasureSpec);
        setMeasuredDimension(getMeasuredHeight(), getMeasuredWidth());
    }

    protected void onDraw(Canvas canvas) {
        canvas.rotate(270);
        canvas.translate(-1 * getHeight() + getThumbOffset(), 0);
        float ratioWidth = (float) (getHeight() - getThumbOffset()) / getHeight();
        canvas.scale(ratioWidth, 1.0f);

        int color = getColor();
        paintBar.setColor(getColor());
        Shader shader = new LinearGradient(
                getProgress() < 255 ? 0 : getProgressHeight(),
                getRectY(),
                getProgressHeight() / 2,
                getRectY(),
                color,
                Color.TRANSPARENT,
                Shader.TileMode.CLAMP
        );
        paintBar.setShader(shader);
        paintBar.setStrokeWidth((float) (getThumbOffset() * 0.8));

        canvas.drawLine(
                getRectX() + (getProgress() - (getMax() / 2)) * getProgressHeight() / getMax(),
                getRectY(),
                getRectX(),
                getRectY(),
                paintBar
        );

        drawScale(canvas);

        super.onDraw(canvas);
    }

    private void drawScale(Canvas canvas) {
        int meterColor;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            meterColor = getResources().getColor(R.color.scale_color, getContext().getTheme());
        } else {
            meterColor = getResources().getColor(R.color.scale_color);
        }
        paintMeter.setColor(meterColor);
        paintMeter.setStrokeWidth(2);

        int centerY = getThumb().getIntrinsicHeight() - getThumbOffset();
        int unit = getProgressDrawable().getBounds().width() / 10;

        float sy = centerY - getThumbOffset() / 2;
        canvas.drawLine(0, sy, unit * 10, sy, paintMeter);

        float ey = (float) centerY + getThumbOffset() / 2;
        canvas.drawLine(0, ey, unit * 10, ey, paintMeter);

        for (int i = 0; i < 11; i++) {
            if (i == 0 || i == 5 || i == 10) {
                paintMeter.setStrokeWidth(4);
                canvas.drawLine(unit * i, 0, unit * i, sy, paintMeter);
                canvas.drawLine(unit * i, ey, unit * i, ey + sy, paintMeter);
            } else {
                paintMeter.setStrokeWidth(2);
                canvas.drawLine(unit * i, sy / 2, unit * i, sy, paintMeter);
                canvas.drawLine(unit * i, ey, unit * i, ey + (sy / 2), paintMeter);
            }
        }
    }

    private int getRectX(){
        Rect rectProgress = getProgressDrawable().getBounds();
        return rectProgress.centerX();
    }

    private int getRectY(){
        Rect rectProgress = getProgressDrawable().getBounds();
        return rectProgress.centerY();
    }

    private int getProgressHeight(){
        Rect rectProgress = getProgressDrawable().getBounds();
        return rectProgress.width();
    }

    private int getColor() {
        int colorId = R.color.progress_forward;
        if (getProgress() < 255) {
            colorId = R.color.progress_backward;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return getResources().getColor(colorId, getContext().getTheme());
        } else {
            return getResources().getColor(colorId);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled()) {
            return false;
        }

        setThumbDrawable(event.getAction());
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                setProgress(getMax() - (int) (getMax() * event.getY() / getHeight()));
                mOnSeekBarChangeListener.onStartTrackingTouch(this);
                onSizeChanged(getWidth(), getHeight(), 0, 0);
                break;

            case MotionEvent.ACTION_MOVE:
                setProgress(getMax() - (int) (getMax() * event.getY() / getHeight()));
                mOnSeekBarChangeListener.onProgressChanged(this, getProgress(), true);
                onSizeChanged(getWidth(), getHeight(), 0, 0);
                break;

            case MotionEvent.ACTION_UP:
                setProgress(255);
                mOnSeekBarChangeListener.onProgressChanged(this, getProgress(), true);
                mOnSeekBarChangeListener.onStopTrackingTouch(this);
                onSizeChanged(getWidth(), getHeight(), 0, 0);
                break;

            case MotionEvent.ACTION_CANCEL:
                break;
        }
        return true;
    }

    private void setThumbDrawable(int action) {
        Bitmap bmpOrigin;
        switch (action) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                bmpOrigin = BitmapFactory.decodeResource(getResources(), R.drawable.thumb_on);
                break;
            default:
                bmpOrigin = BitmapFactory.decodeResource(getResources(), R.drawable.thumb_off);
                break;
        }

        int mWidth = bmpOrigin.getWidth();
        int mHeight = bmpOrigin.getHeight();
        Matrix matrix = new Matrix();
        matrix.setRotate(90.0f);
        Bitmap bmp = Bitmap.createBitmap(bmpOrigin, 0, 0, mWidth, mHeight, matrix, true);
        Bitmap bmpThumb = Bitmap.createScaledBitmap(bmp, mWidth * 2, mHeight * 2, true);
        Drawable drawThumb = new BitmapDrawable(getResources(), bmpThumb);
        setThumb(drawThumb);
    }
}
