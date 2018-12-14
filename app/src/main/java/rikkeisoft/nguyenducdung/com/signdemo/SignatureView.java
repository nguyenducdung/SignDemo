package rikkeisoft.nguyenducdung.com.signdemo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;


public class SignatureView extends View {
    private Context mContext;
    private int targetWidth = 100, targetHeight = 100;
    private ISignatureCallBack signatureCallBack;

    public void setSignatureCallBack(ISignatureCallBack signatureCallBack) {
        this.signatureCallBack = signatureCallBack;
    }

    private float mX;
    private float mY;
    private final Paint mGesturePaint = new Paint();
    private final Path mPath = new Path();
    private Canvas cacheCanvas;
    private Bitmap cachebBitmap;
    private int mPaintWidth = 10;
    private int mPenColor = Color.BLACK;
    private int mBackColor = Color.WHITE;

    private CountDownTimer mCountDownTimer;

    public SignatureView(Context context) {
        super(context);
        init(context);
    }

    public SignatureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SignatureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void init(Context context) {
        this.mContext = context;
        mGesturePaint.setAntiAlias(true);
        mGesturePaint.setStyle(Paint.Style.STROKE);
        mGesturePaint.setStrokeWidth(mPaintWidth);
        mGesturePaint.setColor(mPenColor);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchDown(event);
                break;
            case MotionEvent.ACTION_MOVE:
                touchMove(event);
                break;
            case MotionEvent.ACTION_UP:
                if (mCountDownTimer != null) {
                    mCountDownTimer.cancel();
                    mCountDownTimer=null;
                }
                    mCountDownTimer = new CountDownTimer(1000, 1000) {
                        @Override
                        public void onTick(long millisUntilFinished) {

                        }
                        @Override
                        public void onFinish() {
                            cachebBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
                            cacheCanvas = new Canvas(cachebBitmap);
                            cacheCanvas.drawColor(mBackColor);
                            cacheCanvas.drawPath(mPath, mGesturePaint);
                            if (signatureCallBack != null) {
                                Bitmap b = clearBlank(cachebBitmap, 0);
                                if (b == null) {
                                    clear();
                                    return;
                                }
                                signatureCallBack.onSignCompeleted(SignatureView.this, clearBlank(cachebBitmap, 0));
                            }
                            mPath.reset();
                            invalidate();
                        }
                    };
                    mCountDownTimer.start();
                break;
        }
        // 更新绘制
        invalidate();
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawPath(mPath, mGesturePaint);
    }
    private long startTime;
    private void touchDown(MotionEvent event) {
        startTime= SystemClock.elapsedRealtime();
        float x = event.getX();
        float y = event.getY();
        mX = x;
        mY = y;
        mPath.moveTo(x, y);
        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
            mCountDownTimer=null;
        }
    }
    private void touchMove(MotionEvent event) {
        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
            mCountDownTimer=null;
        }
        final float x = event.getX();
        final float y = event.getY();
        final float previousX = mX;
        final float previousY = mY;
        final float dx = Math.abs(x - previousX);
        final float dy = Math.abs(y - previousY);
        if (dx >= 3 || dy >= 3) {
            float cX = (x + previousX) / 2;
            float cY = (y + previousY) / 2;
            mPath.quadTo(previousX, previousY, cX, cY);
            mX = x;
            mY = y;
        }
    }

    public void clear() {
        if (cacheCanvas != null) {
            mGesturePaint.setColor(mPenColor);
            cacheCanvas.drawColor(mBackColor, PorterDuff.Mode.CLEAR);
            mGesturePaint.setColor(mPenColor);
            invalidate();
        }
    }

    public void save(String path) throws IOException {
        save(path, false, 0);
    }

    public void save(String path, boolean clearBlank, int blank) throws IOException {

        Bitmap bitmap = cachebBitmap;
        if (clearBlank) {
            bitmap = clearBlank(bitmap, blank);
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
        byte[] buffer = bos.toByteArray();
        if (buffer != null) {
            File file = new File(path);
            if (file.exists()) {
                file.delete();
            }
            OutputStream outputStream = new FileOutputStream(file);
            outputStream.write(buffer);
            outputStream.close();
        }
    }

    public Bitmap getBitMap() {
        setDrawingCacheEnabled(true);
        buildDrawingCache();
        Bitmap bitmap = getDrawingCache();
        setDrawingCacheEnabled(false);
        return bitmap;
    }

    private Bitmap clearBlank(Bitmap bp, int blank) {
        try {
            int HEIGHT = bp.getHeight();
            int WIDTH = bp.getWidth();
            int top = 0, left = 0, right = 0, bottom = 0;
            int[] pixs = new int[WIDTH];
            boolean isStop;
            for (int y = 0; y < HEIGHT; y++) {
                bp.getPixels(pixs, 0, WIDTH, 0, y, WIDTH, 1);
                isStop = false;
                for (int pix : pixs) {
                    if (pix != mBackColor) {
                        top = y;
                        isStop = true;
                        break;
                    }
                }
                if (isStop) {
                    break;
                }
            }
            for (int y = HEIGHT - 1; y >= 0; y--) {
                bp.getPixels(pixs, 0, WIDTH, 0, y, WIDTH, 1);
                isStop = false;
                for (int pix : pixs) {
                    if (pix != mBackColor) {
                        bottom = y;
                        isStop = true;
                        break;
                    }
                }
                if (isStop) {
                    break;
                }
            }
            pixs = new int[HEIGHT];
            for (int x = 0; x < WIDTH; x++) {
                bp.getPixels(pixs, 0, 1, x, 0, 1, HEIGHT);
                isStop = false;
                for (int pix : pixs) {
                    if (pix != mBackColor) {
                        left = x;
                        isStop = true;
                        break;
                    }
                }
                if (isStop) {
                    break;
                }
            }
            for (int x = WIDTH - 1; x > 0; x--) {
                bp.getPixels(pixs, 0, 1, x, 0, 1, HEIGHT);
                isStop = false;
                for (int pix : pixs) {
                    if (pix != mBackColor) {
                        right = x;
                        isStop = true;
                        break;
                    }
                }
                if (isStop) {
                    break;
                }
            }
            if (blank < 0) {
                blank = 0;
            }
            left = left - blank > 0 ? left - blank : 0;
            top = top - blank > 0 ? top - blank : 0;
            right = right + blank > WIDTH - 1 ? WIDTH - 1 : right + blank;
            bottom = bottom + blank > HEIGHT - 1 ? HEIGHT - 1 : bottom + blank;
            Bitmap b=Bitmap.createBitmap(bp, left, top, (right - left), (bottom - top));
            int resultW= b.getWidth()*targetHeight/b.getHeight();
            return Bitmap.createScaledBitmap(b, resultW, targetHeight, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public interface ISignatureCallBack {
        void onSignCompeleted(View view, Bitmap bitmap);
    }
}
