package com.VoidSoul.daymark;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public final class CountdownCardView extends View {
    public static final int MODE_TOGETHER = 0;
    public static final int MODE_LITTLE_FOOL = 1;
    public static final int MODE_WOOD = 2;

    private static final int WHITE = Color.WHITE;
    private static final int MUTED_WHITE = 0xD6FFFFFF;
    private static final long DAY_MILLIS = 86400000L;

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
    private final long relationshipTargetMillis = BuildConfig.COUNTDOWN_START_EPOCH_SECONDS * 1000L;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-M-d", Locale.CHINA);
    private final Typeface displayTypeface;
    private int mode = MODE_TOGETHER;
    private float density;

    public CountdownCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        density = getResources().getDisplayMetrics().density;
        dateFormat.setTimeZone(TimeZone.getDefault());
        displayTypeface = getResources().getFont(R.font.daymark_font_family);
    }

    public void setMode(int selectedMode) {
        int safeMode = Math.max(MODE_TOGETHER, Math.min(MODE_WOOD, selectedMode));
        if (mode != safeMode) {
            mode = safeMode;
            invalidate();
        }
    }

    public int getMode() {
        return mode;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float height = getHeight();
        float centerX = getWidth() / 2f;
        float topInset = dp(24);
        float bottomInset = dp(24);

        long now = System.currentTimeMillis();
        long targetMillis = targetMillisForMode(now);
        long diff = targetMillis - now;
        boolean upcoming = diff >= 0;
        long totalDays = Math.abs(diff) / DAY_MILLIS;

        String title = titleForMode(upcoming);
        text(canvas, title, centerX,
                topInset + dp(44), mode == MODE_TOGETHER && !upcoming ? 24 : 28, WHITE, true);
        text(canvas, String.valueOf(totalDays), centerX,
                height * 0.46f, 88, WHITE, true);
        text(canvas, "天", centerX,
                height * 0.46f + dp(52), 18, MUTED_WHITE, false);

        String dateLabel = mode == MODE_TOGETHER ? "起始日：" : "目标日：";
        String targetDate = dateLabel + dateFormat.format(new Date(targetMillis))
                + "  " + weekday(targetMillis);
        text(canvas, targetDate, centerX, height - bottomInset - dp(20), 15, MUTED_WHITE, false);
        postInvalidateDelayed(delayUntilNextDay(now));
    }

    private long delayUntilNextDay(long now) {
        Calendar nextDay = Calendar.getInstance(TimeZone.getDefault(), Locale.CHINA);
        nextDay.setTimeInMillis(now);
        nextDay.set(Calendar.HOUR_OF_DAY, 0);
        nextDay.set(Calendar.MINUTE, 0);
        nextDay.set(Calendar.SECOND, 0);
        nextDay.set(Calendar.MILLISECOND, 0);
        nextDay.add(Calendar.DAY_OF_YEAR, 1);
        return Math.max(1000L, nextDay.getTimeInMillis() - now + 100L);
    }

    private long targetMillisForMode(long now) {
        if (mode == MODE_LITTLE_FOOL) {
            return nextAnnualDateMillis(now, Calendar.JANUARY, 9);
        }
        if (mode == MODE_WOOD) {
            return nextAnnualDateMillis(now, Calendar.MARCH, 23);
        }
        return relationshipTargetMillis;
    }

    private String titleForMode(boolean upcoming) {
        if (mode == MODE_LITTLE_FOOL) {
            return "你的生日还有";
        }
        if (mode == MODE_WOOD) {
            return "距木头的生日还有";
        }
        return upcoming ? "距离在一起还有" : "和小傻子在一起已经";
    }

    private long nextAnnualDateMillis(long now, int month, int dayOfMonth) {
        Calendar target = Calendar.getInstance(TimeZone.getDefault(), Locale.CHINA);
        target.setTimeInMillis(now);
        target.set(Calendar.MONTH, month);
        target.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        target.set(Calendar.HOUR_OF_DAY, 0);
        target.set(Calendar.MINUTE, 0);
        target.set(Calendar.SECOND, 0);
        target.set(Calendar.MILLISECOND, 0);

        Calendar today = Calendar.getInstance(TimeZone.getDefault(), Locale.CHINA);
        today.setTimeInMillis(now);
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        if (target.before(today)) {
            target.add(Calendar.YEAR, 1);
        }
        return target.getTimeInMillis();
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
        paint.setTypeface(Typeface.create(displayTypeface,
                bold ? Typeface.BOLD : Typeface.NORMAL));
        canvas.drawText(value, x, y, paint);
    }

    private float dp(float value) {
        return value * density;
    }
}
