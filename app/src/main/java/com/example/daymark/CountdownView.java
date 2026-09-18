package com.example.daymark;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

public final class CountdownView extends FrameLayout {
    private View backgroundView;
    private View dimView;
    private View glassView;
    private TextView loveLabel;
    private float density;

    public CountdownView(Context context, AttributeSet attrs) {
        super(context, attrs);
        density = getResources().getDisplayMetrics().density;
        setClipChildren(false);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        backgroundView = findViewById(R.id.background_image);
        dimView = findViewById(R.id.background_dim);
        glassView = findViewById(R.id.glass_card);
        loveLabel = findViewById(R.id.love_label);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        int cardWidth = Math.round(Math.min(width - dp(120), height * 0.42f));
        int cardHeight = Math.round(cardWidth * 4f / 3f);
        glassView.measure(
                MeasureSpec.makeMeasureSpec(cardWidth, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(cardHeight, MeasureSpec.EXACTLY));
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int width = right - left;
        int height = bottom - top;
        backgroundView.layout(0, 0, width, height);
        dimView.layout(0, 0, width, height);

        int cardWidth = glassView.getMeasuredWidth();
        int cardHeight = glassView.getMeasuredHeight();
        int cardLeft = (width - cardWidth) / 2;
        int cardTop = Math.round(height * 0.5f - cardHeight * 0.5f);
        glassView.layout(cardLeft, cardTop, cardLeft + cardWidth, cardTop + cardHeight);

        int labelLeft = (width - loveLabel.getMeasuredWidth()) / 2;
        int labelTop = cardTop + cardHeight + dp(24);
        loveLabel.layout(labelLeft, labelTop,
                labelLeft + loveLabel.getMeasuredWidth(),
                labelTop + loveLabel.getMeasuredHeight());
    }

    private int dp(float value) {
        return Math.round(value * density);
    }
}
