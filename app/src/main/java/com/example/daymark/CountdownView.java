package com.example.daymark;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public final class CountdownView extends View {
    private static final int BG = Color.rgb(248, 250, 252);
    private static final int INK = Color.rgb(15, 23, 42);
    private static final int MUTED = Color.rgb(100, 116, 139);
    private static final int CARD = Color.WHITE;
    private static final int TEAL = Color.rgb(15, 118, 110);
    private static final int TEAL_DARK = Color.rgb(13, 83, 79);
    private static final int GOLD = Color.rgb(245, 158, 11);

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final long startMillis = BuildConfig.COUNTDOWN_START_EPOCH_SECONDS * 1000L;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy年M月d日 HH:mm", Locale.CHINA);
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
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        paint.setTypeface(android.graphics.Typeface.create("sans", android.graphics.Typeface.NORMAL));
        dateFormat.setTimeZone(TimeZone.getDefault());
        setBackgroundColor(BG);
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
        float p = dp(24);

        paint.setShader(new LinearGradient(0, 0, 0, dp(360),
                Color.rgb(232, 247, 245), BG, Shader.TileMode.CLAMP));
        canvas.drawRect(0, 0, w, Math.min(h, dp(390)), paint);
        paint.setShader(null);

        drawTopBar(canvas, p);
        drawHero(canvas, p, w);
        drawInfoCard(canvas, p, w);
        drawFooter(canvas, p, w, h);
    }

    private void drawTopBar(Canvas canvas, float p) {
        text(canvas, "DAYMARK", p, dp(42), 12, TEAL, true, Paint.Align.LEFT);
        text(canvas, "固定纪念日", getWidth() - p, dp(42), 12, MUTED, false, Paint.Align.RIGHT);
    }

    private void drawHero(Canvas canvas, float p, float w) {
        long now = System.currentTimeMillis();
        long diff = startMillis - now;
        boolean upcoming = diff >= 0;
        long seconds = Math.abs(diff) / 1000L;
        long days = seconds / 86400L;
        long hours = (seconds % 86400L) / 3600L;
        long minutes = (seconds % 3600L) / 60L;
        long remainingSeconds = seconds % 60L;

        text(canvas, upcoming ? "距离那一天" : "那一天已经到来", p, dp(98), 28, INK, true, Paint.Align.LEFT);
        text(canvas, upcoming ? "把期待留给时间" : "时间会把每一刻留下来", p, dp(128), 14, MUTED, false, Paint.Align.LEFT);

        float top = dp(160);
        float cardW = (w - p * 2 - dp(24)) / 2f;
        drawRoundCard(canvas, p, top, cardW, dp(150), TEAL_DARK);
        drawRoundCard(canvas, p + cardW + dp(24), top, cardW, dp(150), CARD);

        text(canvas, String.valueOf(days), p + dp(22), top + dp(82), 52, Color.WHITE, true, Paint.Align.LEFT);
        text(canvas, upcoming ? "天" : "天前", p + dp(24), top + dp(119), 14, Color.rgb(190, 238, 232), false, Paint.Align.LEFT);
        text(canvas, "起始时间", p + dp(22), top + dp(139), 11, Color.rgb(174, 221, 216), false, Paint.Align.LEFT);

        String clock = String.format(Locale.CHINA, "%02d:%02d:%02d", hours, minutes, remainingSeconds);
        text(canvas, clock, p + cardW + dp(46), top + dp(74), 24, INK, true, Paint.Align.LEFT);
        text(canvas, upcoming ? "时 · 分 · 秒" : "已持续", p + cardW + dp(46), top + dp(108), 12, MUTED, false, Paint.Align.LEFT);
        drawDot(canvas, p + cardW + dp(46), top + dp(133), upcoming ? GOLD : TEAL);
        text(canvas, upcoming ? "每秒都更接近" : "每秒都在发生", p + cardW + dp(58), top + dp(137), 11, MUTED, false, Paint.Align.LEFT);
    }

    private void drawInfoCard(Canvas canvas, float p, float w) {
        float top = dp(338);
        drawRoundCard(canvas, p, top, w - p * 2, dp(116), CARD);
        text(canvas, "时间锚点", p + dp(20), top + dp(30), 12, TEAL, true, Paint.Align.LEFT);
        text(canvas, dateFormat.format(new Date(startMillis)), p + dp(20), top + dp(62), 19, INK, true, Paint.Align.LEFT);
        text(canvas, "这个时间写入当前安装包，应用内无法修改", p + dp(20), top + dp(91), 12, MUTED, false, Paint.Align.LEFT);
    }

    private void drawFooter(Canvas canvas, float p, float w, float h) {
        text(canvas, "专注此刻，等待值得等待的事", p, Math.max(dp(500), h - dp(36)), 12, MUTED, false, Paint.Align.LEFT);
        text(canvas, "v1.0.0", w - p, Math.max(dp(500), h - dp(36)), 12, MUTED, false, Paint.Align.RIGHT);
    }

    private void drawRoundCard(Canvas canvas, float left, float top, float width, float height, int color) {
        paint.setColor(color);
        paint.setStyle(Paint.Style.FILL);
        paint.setShadowLayer(dp(12), 0, dp(4), 0x160F172A);
        rect.set(left, top, left + width, top + height);
        canvas.drawRoundRect(rect, dp(20), dp(20), paint);
        paint.clearShadowLayer();
    }

    private void drawDot(Canvas canvas, float x, float y, int color) {
        paint.setColor(color);
        canvas.drawCircle(x, y - dp(4), dp(4), paint);
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
