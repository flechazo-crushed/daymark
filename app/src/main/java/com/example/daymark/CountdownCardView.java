package com.example.daymark;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public final class CountdownCardView extends View {
    private static final int WHITE = Color.WHITE;
    private static final int MUTED_WHITE = 0xD6FFFFFF;
    private static final long DAY_MILLIS = 86400000L;

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
    private final long targetMillis = BuildConfig.COUNTDOWN_START_EPOCH_SECONDS * 1000L;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-M-d", Locale.CHINA);
    private float density;

    public CountdownCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        density = getResources().getDisplayMetrics().density;
        dateFormat.setTimeZone(TimeZone.getDefault());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float height = getHeight();
        float centerX = getWidth() / 2f;

        long diff = targetMillis - System.currentTimeMillis();
        boolean upcoming = diff >= 0;
        long totalDays = Math.abs(diff) / DAY_MILLIS;

        text(canvas, upcoming ? "生日还有" : "和小傻子在一起已经", centerX,
                dp(62), 28, WHITE, true);
        text(canvas, String.valueOf(totalDays), centerX,
                height * 0.56f, 92, WHITE, true);
        text(canvas, "天", centerX,
                height * 0.56f + dp(42), 18, MUTED_WHITE, false);

        String targetDate = "目标日：" + dateFormat.format(new Date(targetMillis))
                + "  " + weekday(targetMillis);
        text(canvas, targetDate, centerX, height - dp(58), 15, MUTED_WHITE, false);
        postInvalidateDelayed(1000L);
    }

    private String weekday(long millis) {
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault(), Locale.CHINA);
        calendar.setTimeInMillis(millis);
        String[] names = {"星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六"};
        return names[calendar.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY];
    }

    private void text(Canvas canvas, String value, float x, float y, float sizeSp,
                      int color, boolean bold) {
        paint.setShader(null);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(color);
        paint.setTextSize(dp(sizeSp));
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTypeface(android.graphics.Typeface.create("sans",
                bold ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL));
        canvas.drawText(value, x, y, paint);
    }

    private float dp(float value) {
        return value * density;
    }
}
