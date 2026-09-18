package com.VoidSoul.daymark;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.example.liquidglass.LiquidGlassTabBar;
import com.example.liquidglass.LiquidGlassView;

import java.util.Arrays;

import kotlin.Unit;

public final class CountdownView extends FrameLayout {
    private View backdropView;
    private LiquidGlassView glassView;
    private LiquidGlassTabBar modeSwitch;
    private TextView loveLabel;
    private CountdownCardView countdownCard;
    private float density;

    public CountdownView(Context context, AttributeSet attrs) {
        super(context, attrs);
        density = getResources().getDisplayMetrics().density;
        setClipChildren(false);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        backdropView = findViewById(R.id.glass_backdrop);
        glassView = findViewById(R.id.glass_card);
        modeSwitch = findViewById(R.id.switch_glass);
        loveLabel = findViewById(R.id.love_label);
        countdownCard = findViewById(R.id.countdown_card);

        // Both glass roots sample only the static image and dim layer, never
        // each other or the parent that contains their animated children.
        glassView.setBackdropSource(backdropView);
        modeSwitch.setBackdropSource(backdropView);
        modeSwitch.setTabTitles(Arrays.<CharSequence>asList("在一起", "小傻瓜", "木头"));
        modeSwitch.setOnTabSelected(index -> {
            countdownCard.setMode(index);
            return Unit.INSTANCE;
        });
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
        int switchWidth = Math.max(0, Math.min(dp(276), width - dp(40)));
        modeSwitch.measure(
                MeasureSpec.makeMeasureSpec(switchWidth, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(dp(54), MeasureSpec.EXACTLY));
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int width = right - left;
        int height = bottom - top;
        backdropView.layout(0, 0, width, height);

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

        int switchWidth = modeSwitch.getMeasuredWidth();
        int switchHeight = modeSwitch.getMeasuredHeight();
        int switchLeft = (width - switchWidth) / 2;
        int desiredTop = Math.max(loveLabel.getBottom() + dp(18),
                Math.round(height * 0.78f));
        int maxTop = Math.max(dp(24), height - switchHeight - dp(36));
        int switchTop = Math.min(desiredTop, maxTop);
        modeSwitch.layout(switchLeft, switchTop,
                switchLeft + switchWidth, switchTop + switchHeight);
    }

    private int dp(float value) {
        return Math.round(value * density);
    }
}
