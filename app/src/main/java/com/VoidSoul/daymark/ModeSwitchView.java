package com.VoidSoul.daymark;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.PathInterpolator;
import android.widget.FrameLayout;
import android.widget.TextView;

public final class ModeSwitchView extends FrameLayout {
    public interface OnModeChangedListener {
        void onModeChanged(int mode);
    }

    private static final int MODE_COUNT = 3;
    private static final int ACTIVE_TEXT_COLOR = Color.WHITE;
    private static final int INACTIVE_TEXT_COLOR = 0xBFFFFFFF;

    private View thumb;
    private TextView[] labels;
    private OnModeChangedListener listener;
    private int selectedMode;
    private float downX;
    private float startTranslationX;
    private boolean dragging;
    private final int touchSlop;

    public ModeSwitchView(Context context) {
        this(context, null);
    }

    public ModeSwitchView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ModeSwitchView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        setClickable(true);
        setFocusable(true);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        thumb = findViewById(R.id.switch_thumb);
        labels = new TextView[]{
                findViewById(R.id.switch_together),
                findViewById(R.id.switch_little_fool),
                findViewById(R.id.switch_wood)
        };
        updateLabels();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (thumb == null) {
            return;
        }

        int inset = dp(3);
        int availableWidth = Math.max(0, getMeasuredWidth() - inset * 2);
        int thumbWidth = Math.round(availableWidth / (float) MODE_COUNT);
        int thumbHeight = Math.max(0, getMeasuredHeight() - inset * 2);
        thumb.measure(
                MeasureSpec.makeMeasureSpec(thumbWidth, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(thumbHeight, MeasureSpec.EXACTLY));
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (thumb == null) {
            return;
        }

        int inset = dp(3);
        thumb.layout(inset, inset, inset + thumb.getMeasuredWidth(),
                getHeight() - inset);
        moveThumb(false);
    }

    public void setOnModeChangedListener(OnModeChangedListener modeListener) {
        listener = modeListener;
        if (listener != null) {
            listener.onModeChanged(selectedMode);
        }
    }

    private void selectMode(int mode, boolean animate) {
        int safeMode = Math.max(0, Math.min(MODE_COUNT - 1, mode));
        boolean changed = selectedMode != safeMode;
        selectedMode = safeMode;
        updateLabels();
        moveThumb(animate);
        if (changed && listener != null) {
            listener.onModeChanged(selectedMode);
        }
    }

    private void moveThumb(boolean animate) {
        if (thumb == null) {
            return;
        }

        float target = maxThumbTranslation() * selectedMode / (MODE_COUNT - 1f);
        thumb.animate().cancel();
        if (animate && isLaidOut()) {
            thumb.animate()
                    .translationX(target)
                    .setDuration(220L)
                    .setInterpolator(new PathInterpolator(0.2f, 0f, 0f, 1f))
                    .start();
        } else {
            thumb.setTranslationX(target);
        }
    }

    private void updateLabels() {
        if (labels == null) {
            return;
        }
        for (int index = 0; index < labels.length; index++) {
            labels[index].setTextColor(index == selectedMode
                    ? ACTIVE_TEXT_COLOR : INACTIVE_TEXT_COLOR);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (thumb == null) {
            return super.onTouchEvent(event);
        }

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                downX = event.getX();
                startTranslationX = thumb.getTranslationX();
                dragging = false;
                thumb.animate().cancel();
                setPressed(true);
                return true;
            case MotionEvent.ACTION_MOVE:
                float delta = event.getX() - downX;
                if (Math.abs(delta) > touchSlop) {
                    dragging = true;
                }
                if (dragging) {
                    thumb.setTranslationX(clamp(startTranslationX + delta,
                            0f, maxThumbTranslation()));
                }
                return true;
            case MotionEvent.ACTION_UP:
                setPressed(false);
                selectMode(dragging ? modeForTranslation() : modeForX(event.getX()), true);
                performClick();
                return true;
            case MotionEvent.ACTION_CANCEL:
                setPressed(false);
                moveThumb(true);
                return true;
            default:
                return super.onTouchEvent(event);
        }
    }

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }

    private int modeForX(float x) {
        int inset = dp(3);
        float availableWidth = Math.max(1f, getWidth() - inset * 2f);
        return Math.min(MODE_COUNT - 1, Math.max(0,
                (int) ((x - inset) / (availableWidth / MODE_COUNT))));
    }

    private int modeForTranslation() {
        float maxTranslation = maxThumbTranslation();
        if (maxTranslation == 0f) {
            return selectedMode;
        }
        return Math.round(thumb.getTranslationX() / maxTranslation * (MODE_COUNT - 1));
    }

    private float maxThumbTranslation() {
        if (thumb == null) {
            return 0f;
        }
        int inset = dp(3);
        int availableWidth = Math.max(0, getWidth() - inset * 2);
        return Math.max(0f, availableWidth - thumb.getWidth());
    }

    private float clamp(float value, float minimum, float maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }

    private int dp(float value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
