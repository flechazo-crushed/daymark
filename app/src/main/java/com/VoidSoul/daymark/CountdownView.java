package com.VoidSoul.daymark;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.FrameLayout;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.example.liquidglass.LiquidGlassTabBar;
import com.example.liquidglass.LiquidGlassView;

import java.util.Arrays;

import kotlin.Unit;

public final class CountdownView extends FrameLayout {
    private static final float TAB_LABEL_OFFSET_DP = 14f;
    private static final float FIRST_TWO_TAB_LABEL_EXTRA_OFFSET_DP = 2f;
    private static final float WOOD_TAB_LABEL_EXTRA_OFFSET_DP = 4f;
    private static final float TAB_LABEL_UPWARD_OFFSET_DP = 4f;
    private static final float CONTENT_TRAVEL_DP = 18f;
    private static final long CONTENT_EXIT_DURATION_MS = 90L;
    private static final long CONTENT_ENTER_DURATION_MS = 200L;

    private View backdropView;
    private LiquidGlassView glassView;
    private LiquidGlassTabBar modeSwitch;
    private LiquidGlassView quoteGlass;
    private LiquidGlassView moreGlass;
    private TextView remoteText;
    private FrameLayout moreButton;
    private CountdownCardView countdownCard;
    private Typeface displayTypeface;
    private float density;
    private int contentTransitionToken;

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
        quoteGlass = findViewById(R.id.quote_glass);
        remoteText = findViewById(R.id.remote_text);
        moreButton = findViewById(R.id.more_button);
        moreGlass = findViewById(R.id.more_glass);
        moreGlass.setBackdropSource(backdropView);
        countdownCard = findViewById(R.id.countdown_card);
        displayTypeface = getResources().getFont(R.font.daymark_font_family);
        remoteText.setTypeface(displayTypeface);
        quoteGlass.setBackdropSource(backdropView);

        // Both glass roots sample only the static image and dim layer, never
        // each other or the parent that contains their animated children.
        glassView.setBackdropSource(backdropView);
        modeSwitch.setBackdropSource(backdropView);
        modeSwitch.setTabTitles(Arrays.<CharSequence>asList("小傻瓜", "木头"));
        offsetTabLabels();
        modeSwitch.setOnTabSelected(index -> {
            animateCountdownContent(index + 1);
            return Unit.INSTANCE;
        });
        moreButton.setOnClickListener(anchor -> {
            PopupMenu popup = new PopupMenu(getContext(), moreButton);
            popup.getMenu().add("其他").setOnMenuItemClickListener(item -> {
                animateCountdownContent(CountdownCardView.MODE_TOGETHER);
                return true;
            });
            popup.show();
        });
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        int quoteWidth = Math.max(0, Math.min(dp(300), width - dp(48)));
        quoteGlass.measure(MeasureSpec.makeMeasureSpec(quoteWidth, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(dp(96), MeasureSpec.EXACTLY));
        int cardWidth = Math.round(Math.min(width - dp(120), height * 0.42f));
        int cardHeight = Math.round(cardWidth * 4f / 3f);
        glassView.measure(
                MeasureSpec.makeMeasureSpec(cardWidth, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(cardHeight, MeasureSpec.EXACTLY));
        moreGlass.measure(MeasureSpec.makeMeasureSpec(dp(44), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(dp(44), MeasureSpec.EXACTLY));
        int switchWidth = Math.max(0, Math.min(dp(228), width - dp(64)));
        modeSwitch.measure(
                MeasureSpec.makeMeasureSpec(switchWidth, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(dp(46), MeasureSpec.EXACTLY));
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

        int quoteLeft = (width - quoteGlass.getMeasuredWidth()) / 2;
        int quoteTop = Math.max(dp(24), Math.round(height * 0.13f));
        quoteGlass.layout(quoteLeft, quoteTop,
                quoteLeft + quoteGlass.getMeasuredWidth(), quoteTop + quoteGlass.getMeasuredHeight());

        int moreSize = moreGlass.getMeasuredWidth();
        int moreLeft = width - dp(16) - moreSize;
        int moreTop = dp(18);
        moreGlass.layout(moreLeft, moreTop, moreLeft + moreSize, moreTop + moreGlass.getMeasuredHeight());

        int switchWidth = modeSwitch.getMeasuredWidth();
        int switchHeight = modeSwitch.getMeasuredHeight();
        int switchLeft = (width - switchWidth) / 2;
        int desiredTop = Math.max(cardTop + cardHeight + dp(24),
                Math.round(height * 0.78f));
        int maxTop = Math.max(dp(24), height - switchHeight - dp(36));
        int switchTop = Math.min(desiredTop, maxTop);
        modeSwitch.layout(switchLeft, switchTop,
                switchLeft + switchWidth, switchTop + switchHeight);
    }

    private int dp(float value) {
        return Math.round(value * density);
    }

    private void animateCountdownContent(int selectedMode) {
        final int nextMode = Math.max(CountdownCardView.MODE_TOGETHER,
                Math.min(CountdownCardView.MODE_WOOD, selectedMode));
        final int currentMode = countdownCard.getMode();

        // Invalidate a cancelled callback before restoring the visible content.
        contentTransitionToken++;
        countdownCard.animate().cancel();
        resetCountdownCardTransform();
        if (currentMode == nextMode) {
            return;
        }

        final int transitionToken = contentTransitionToken;
        final float direction = nextMode > currentMode ? 1f : -1f;
        final float travel = dp(CONTENT_TRAVEL_DP);
        countdownCard.animate()
                .alpha(0f)
                .translationX(-direction * travel)
                .scaleX(0.985f)
                .scaleY(0.985f)
                .setDuration(CONTENT_EXIT_DURATION_MS)
                .setInterpolator(new AccelerateInterpolator(1.2f))
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (transitionToken != contentTransitionToken) {
                            return;
                        }

                        countdownCard.setMode(nextMode);
                        countdownCard.setAlpha(0f);
                        countdownCard.setTranslationX(direction * travel);
                        countdownCard.setScaleX(0.985f);
                        countdownCard.setScaleY(0.985f);
                        countdownCard.animate()
                                .alpha(1f)
                                .translationX(0f)
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(CONTENT_ENTER_DURATION_MS)
                                .setInterpolator(new DecelerateInterpolator(1.5f))
                                .setListener(new AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        if (transitionToken == contentTransitionToken) {
                                            resetCountdownCardTransform();
                                        }
                                    }
                                })
                                .start();
                    }
                })
                .start();
    }

    private void resetCountdownCardTransform() {
        countdownCard.setAlpha(1f);
        countdownCard.setTranslationX(0f);
        countdownCard.setScaleX(1f);
        countdownCard.setScaleY(1f);
    }

    private void offsetTabLabels() {
        // QWEA0 positions its droplet from tab layout bounds, so shift only
        // the visible label roots to balance the CJK text inside each segment.
        View tabRow = modeSwitch.getChildAt(0);
        if (!(tabRow instanceof ViewGroup)) {
            return;
        }
        ViewGroup row = (ViewGroup) tabRow;
        for (int index = 0; index < row.getChildCount(); index++) {
            View tab = row.getChildAt(index);
            float offset = TAB_LABEL_OFFSET_DP;
            if (index == 0) {
                offset += FIRST_TWO_TAB_LABEL_EXTRA_OFFSET_DP;
            } else {
                offset += WOOD_TAB_LABEL_EXTRA_OFFSET_DP;
            }
            tab.setTranslationX(0f);
            tab.setTranslationY(-dp(TAB_LABEL_UPWARD_OFFSET_DP));
            applyDisplayTypeface(tab);
        }
    }

    private void applyDisplayTypeface(View view) {
        if (view instanceof TextView) {
            ((TextView) view).setTypeface(displayTypeface);
            return;
        }
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int index = 0; index < group.getChildCount(); index++) {
                applyDisplayTypeface(group.getChildAt(index));
            }
        }
    }
}
