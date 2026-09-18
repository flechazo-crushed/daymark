package com.example.daymark;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public final class CountdownView extends View {
    private static final int WHITE = Color.WHITE;
    private static final int GLASS = 0x3A08111F;
    private static final int GLASS_EDGE = 0x72FFFFFF;
    private static final int BACKDROP = 0x52000000;
    private static final int MUTED_WHITE = 0xD6FFFFFF;

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();
    private final Path cardPath = new Path();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final long targetMillis = BuildConfig.COUNTDOWN_START_EPOCH_SECONDS * 1000L;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-M-d", Locale.CHINA);
    private final Drawable homeImage;
    private final Bitmap blurredBackground;
    private final Runnable ticker = new Runnable() {
        @Override
        public void run() {
            invalidate();
            handler.postDelayed(this, 1000L);
        }
    };

    private float density;

    public CountdownView(Context context) {
        super(context);
        density = getResources().getDisplayMetrics().density;
        homeImage = getResources().getDrawable(R.drawable.home_image, null);
        blurredBackground = createBlurredBackground(homeImage);
        dateFormat.setTimeZone(TimeZone.getDefault());
        setBackgroundColor(Color.BLACK);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        handler.post(ticker);
    }

    @Override
    protected void onDetachedFromWindow() {
        handler.removeCallbacks(ticker);
        super.onDetachedFromWindow();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float w = getWidth();
        float h = getHeight();

        drawImageCover(canvas, homeImage, 0, 0, w, h);
        paint.setColor(BACKDROP);
        canvas.drawRect(0, 0, w, h, paint);

        float cardLeft = dp(24);
        float cardRight = w - dp(24);
        float cardTop = h * 0.12f;
        float cardBottom = h * 0.86f;
        float radius = dp(28);
        rect.set(cardLeft, cardTop, cardRight, cardBottom);
        cardPath.reset();
        cardPath.addRoundRect(rect, radius, radius, Path.Direction.CW);

        // Draw a low-resolution blurred copy inside the card for the frosted-glass effect.
        canvas.save();
        canvas.clipPath(cardPath);
        drawBitmapCover(canvas, blurredBackground, 0, 0, w, h);
        paint.setColor(0x26000000);
        canvas.drawRect(rect, paint);
        canvas.restore();

        paint.setColor(GLASS);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawRoundRect(rect, radius, radius, paint);
        borderPaint.setColor(GLASS_EDGE);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(dp(1));
        canvas.drawRoundRect(rect, radius, radius, borderPaint);

        long diff = targetMillis - System.currentTimeMillis();
        boolean upcoming = diff >= 0;
        long totalDays = Math.abs(diff) / 86400000L;
        float centerX = w / 2f;

        text(canvas, upcoming ? "生日还有" : "生日已到", centerX,
                cardTop + dp(62), 28, WHITE, true, Paint.Align.CENTER);
        text(canvas, String.valueOf(totalDays), centerX,
                cardTop + (cardBottom - cardTop) * 0.56f, 92, WHITE, true, Paint.Align.CENTER);
        text(canvas, upcoming ? "天" : "天前", centerX,
                cardTop + (cardBottom - cardTop) * 0.56f + dp(42), 18, MUTED_WHITE,
                false, Paint.Align.CENTER);

        String targetDate = "目标日：" + dateFormat.format(new Date(targetMillis))
                + "  " + weekday(targetMillis);
        text(canvas, targetDate, centerX, cardBottom - dp(58), 15, MUTED_WHITE,
                false, Paint.Align.CENTER);

        text(canvas, "Daymark", centerX, cardBottom + dp(42), 20, WHITE, true, Paint.Align.CENTER);
        drawPill(canvas, centerX + dp(52), cardBottom + dp(25), "倒数日");
    }

    private void drawPill(Canvas canvas, float centerX, float centerY, String value) {
        paint.setColor(0x66000000);
        paint.setStyle(Paint.Style.FILL);
        rect.set(centerX - dp(27), centerY - dp(12), centerX + dp(27), centerY + dp(12));
        canvas.drawRoundRect(rect, dp(12), dp(12), paint);
        borderPaint.setColor(0xB8FFFFFF);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(dp(1));
        canvas.drawRoundRect(rect, dp(12), dp(12), borderPaint);
        text(canvas, value, centerX, centerY + dp(4), 11, WHITE, false, Paint.Align.CENTER);
    }

    private String weekday(long millis) {
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault(), Locale.CHINA);
        calendar.setTimeInMillis(millis);
        String[] names = {"星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六"};
        return names[calendar.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY];
    }

    private Bitmap createBlurredBackground(Drawable source) {
        int width = 180;
        int height = 320;
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawImageCover(canvas, source, 0, 0, width, height);
        return boxBlur(bitmap, 5);
    }

    private Bitmap boxBlur(Bitmap source, int radius) {
        int width = source.getWidth();
        int height = source.getHeight();
        int[] input = new int[width * height];
        int[] output = new int[input.length];
        source.getPixels(input, 0, width, 0, 0, width, height);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                long red = 0;
                long green = 0;
                long blue = 0;
                long alpha = 0;
                int count = 0;
                for (int yy = Math.max(0, y - radius); yy <= Math.min(height - 1, y + radius); yy++) {
                    for (int xx = Math.max(0, x - radius); xx <= Math.min(width - 1, x + radius); xx++) {
                        int color = input[yy * width + xx];
                        alpha += Color.alpha(color);
                        red += Color.red(color);
                        green += Color.green(color);
                        blue += Color.blue(color);
                        count++;
                    }
                }
                output[y * width + x] = Color.argb((int) (alpha / count), (int) (red / count),
                        (int) (green / count), (int) (blue / count));
            }
        }
        source.setPixels(output, 0, width, 0, 0, width, height);
        return source;
    }

    private void drawImageCover(Canvas canvas, Drawable image, float left, float top,
                                float right, float bottom) {
        if (image == null) {
            return;
        }
        int imageWidth = Math.max(1, image.getIntrinsicWidth());
        int imageHeight = Math.max(1, image.getIntrinsicHeight());
        float width = right - left;
        float height = bottom - top;
        float scale = Math.max(width / imageWidth, height / imageHeight);
        float drawWidth = imageWidth * scale;
        float drawHeight = imageHeight * scale;
        float drawLeft = left + (width - drawWidth) / 2f;
        float drawTop = top + (height - drawHeight) / 2f;
        image.setBounds(Math.round(drawLeft), Math.round(drawTop),
                Math.round(drawLeft + drawWidth), Math.round(drawTop + drawHeight));
        image.draw(canvas);
    }

    private void drawBitmapCover(Canvas canvas, Bitmap bitmap, float left, float top,
                                 float right, float bottom) {
        if (bitmap == null) {
            return;
        }
        float width = right - left;
        float height = bottom - top;
        float scale = Math.max(width / bitmap.getWidth(), height / bitmap.getHeight());
        float drawWidth = bitmap.getWidth() * scale;
        float drawHeight = bitmap.getHeight() * scale;
        rect.set(left + (width - drawWidth) / 2f, top + (height - drawHeight) / 2f,
                left + (width + drawWidth) / 2f, top + (height + drawHeight) / 2f);
        canvas.drawBitmap(bitmap, null, rect, paint);
    }

    private void text(Canvas canvas, String value, float x, float y, float sizeSp, int color,
                      boolean bold, Paint.Align align) {
        paint.setShader(null);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(color);
        paint.setTextSize(dp(sizeSp));
        paint.setTextAlign(align);
        paint.setTypeface(android.graphics.Typeface.create("sans", bold
                ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL));
        canvas.drawText(value, x, y, paint);
    }

    private float dp(float value) {
        return value * density;
    }
}
