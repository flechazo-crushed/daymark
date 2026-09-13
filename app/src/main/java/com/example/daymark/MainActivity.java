package com.example.daymark;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;

public final class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(getString(R.string.app_name));
        setContentView(new CountdownView(this));

        Window window = getWindow();
        window.setStatusBarColor(0xFFF8FAFC);
        window.setNavigationBarColor(0xFFF8FAFC);
        // Force the decor view to be created before asking PhoneWindow for its
        // insets controller. Some Android 16/ColorOS builds otherwise call
        // DecorView.getWindowInsetsController() while the decor is still null.
        View decorView = window.getDecorView();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            WindowInsetsController controller = decorView.getWindowInsetsController();
            if (controller != null) {
                controller.setSystemBarsAppearance(
                        WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                                | WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS,
                        WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                                | WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS);
            }
        } else {
            decorView.setSystemUiVisibility(
                    android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                            | android.view.View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        }
    }
}
