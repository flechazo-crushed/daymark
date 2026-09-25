package com.VoidSoul.daymark;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.widget.TextView;

import java.util.Arrays;

/** A self-contained six-digit lock screen shown before the countdown. */
public final class LockActivity extends Activity {
    private static final int PIN_LENGTH = 6;
    private static final int IMMERSIVE_FLAGS = View.SYSTEM_UI_FLAG_FULLSCREEN
            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;

    private final char[] enteredPin = new char[PIN_LENGTH];
    private LockMode mode;
    private char[] pendingPin;
    private int enteredLength;
    private boolean handlingInput;
    private TextView titleView;
    private TextView messageView;
    private TextView errorView;
    private View dotsRow;
    private View[] dots;

    private enum LockMode {
        CREATE,
        CONFIRM,
        UNLOCK
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        applyFullscreen();
        setContentView(R.layout.activity_lock);

        titleView = findViewById(R.id.lock_title);
        messageView = findViewById(R.id.lock_message);
        errorView = findViewById(R.id.lock_error);
        dotsRow = findViewById(R.id.lock_dots);
        dots = new View[]{
                findViewById(R.id.lock_dot_1),
                findViewById(R.id.lock_dot_2),
                findViewById(R.id.lock_dot_3),
                findViewById(R.id.lock_dot_4),
                findViewById(R.id.lock_dot_5),
                findViewById(R.id.lock_dot_6)
        };

        int[] keyIds = {
                R.id.lock_key_0,
                R.id.lock_key_1,
                R.id.lock_key_2,
                R.id.lock_key_3,
                R.id.lock_key_4,
                R.id.lock_key_5,
                R.id.lock_key_6,
                R.id.lock_key_7,
                R.id.lock_key_8,
                R.id.lock_key_9
        };
        for (int keyId : keyIds) {
            View key = findViewById(keyId);
            key.setOnClickListener(view -> appendDigit((String) view.getTag()));
        }
        findViewById(R.id.lock_key_delete).setOnClickListener(view -> deleteDigit());

        mode = PinStore.hasPin(this) ? LockMode.UNLOCK : LockMode.CREATE;
        renderMode();
        renderDots();
        findViewById(R.id.lock_content).setAlpha(0f);
        findViewById(R.id.lock_content).setTranslationY(dp(18));
        findViewById(R.id.lock_content).animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(280L)
                .start();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            applyFullscreen();
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    private void appendDigit(String digit) {
        if (handlingInput || enteredLength >= PIN_LENGTH || digit == null || digit.length() != 1) {
            return;
        }

        enteredPin[enteredLength++] = digit.charAt(0);
        renderDots();
        if (enteredLength == PIN_LENGTH) {
            handleCompletePin();
        }
    }

    private void deleteDigit() {
        if (handlingInput || enteredLength == 0) {
            return;
        }

        enteredPin[--enteredLength] = '\0';
        renderDots();
    }

    private void handleCompletePin() {
        handlingInput = true;
        if (mode == LockMode.CREATE) {
            pendingPin = Arrays.copyOf(enteredPin, PIN_LENGTH);
            clearInput();
            mode = LockMode.CONFIRM;
            renderMode();
            handlingInput = false;
            return;
        }

        if (mode == LockMode.CONFIRM) {
            if (pendingPin != null && MessageComparator.equals(pendingPin, enteredPin)) {
                PinStore.save(this, enteredPin);
                clearPendingPin();
                unlockAndOpenHome();
            } else {
                clearPendingPin();
                mode = LockMode.CREATE;
                showError(getString(R.string.lock_error_mismatch));
            }
            return;
        }

        if (PinStore.matches(this, enteredPin)) {
            unlockAndOpenHome();
        } else {
            showError(getString(R.string.lock_error_wrong));
        }
    }

    private void unlockAndOpenHome() {
        AppLockSession.unlock();
        clearInput();
        Intent homeIntent = new Intent(this, MainActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(homeIntent);
        finish();
    }

    private void showError(String message) {
        errorView.setText(message);
        errorView.setVisibility(View.VISIBLE);
        ObjectAnimator.ofFloat(dotsRow, View.TRANSLATION_X,
                        0f, dp(12), -dp(12), dp(8), -dp(8), 0f)
                .setDuration(360L)
                .start();
        dotsRow.postDelayed(() -> {
            clearInput();
            renderMode();
            handlingInput = false;
        }, 460L);
    }

    private void clearInput() {
        Arrays.fill(enteredPin, '\0');
        enteredLength = 0;
        renderDots();
    }

    private void clearPendingPin() {
        if (pendingPin != null) {
            Arrays.fill(pendingPin, '\0');
            pendingPin = null;
        }
    }

    private void renderMode() {
        errorView.setText(null);
        errorView.setVisibility(View.INVISIBLE);
        if (mode == LockMode.CREATE) {
            titleView.setText(R.string.lock_create_title);
            messageView.setText(R.string.lock_create_message);
        } else if (mode == LockMode.CONFIRM) {
            titleView.setText(R.string.lock_confirm_title);
            messageView.setText(R.string.lock_confirm_message);
        } else {
            titleView.setText(R.string.lock_unlock_title);
            messageView.setText(R.string.lock_unlock_message);
        }
    }

    private void renderDots() {
        for (int index = 0; index < dots.length; index++) {
            dots[index].setSelected(index < enteredLength);
        }
    }

    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }

    private void applyFullscreen() {
        Window window = getWindow();
        window.setStatusBarColor(0x00000000);
        window.setNavigationBarColor(0x00000000);
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        window.clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            window.setNavigationBarContrastEnforced(false);
        }

        View decorView = window.getDecorView();
        decorView.setSystemUiVisibility(IMMERSIVE_FLAGS);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false);
            WindowInsetsController controller = decorView.getWindowInsetsController();
            if (controller != null) {
                controller.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
                controller.setSystemBarsBehavior(
                        WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
                controller.setSystemBarsAppearance(
                        0,
                        WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                                | WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS);
            }
        }
        decorView.post(() -> decorView.setSystemUiVisibility(IMMERSIVE_FLAGS));
    }

    /** Avoids keeping a completed PIN in a String while comparing setup input. */
    private static final class MessageComparator {
        private MessageComparator() {
        }

        static boolean equals(char[] first, char[] second) {
            if (first.length != second.length) {
                return false;
            }
            int difference = 0;
            for (int index = 0; index < first.length; index++) {
                difference |= first[index] ^ second[index];
            }
            return difference == 0;
        }
    }
}
