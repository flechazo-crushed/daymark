package com.VoidSoul.daymark;

import android.app.Activity;
import android.os.Bundle;
import android.content.SharedPreferences;
import android.widget.TextView;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;

public final class MainActivity extends Activity {
    private static final String TEXT_URL = "https://gh-proxy.org/https://github.com/flechazo-crushed/daymark/blob/main/remote-text.txt";
    private static final String PREFS = "remote_text_cache";
    private static final String CACHE_KEY = "text";
    private final ExecutorService textExecutor = Executors.newSingleThreadExecutor();
    private static final int IMMERSIVE_FLAGS = View.SYSTEM_UI_FLAG_FULLSCREEN
            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Configure edge-to-edge before the root view measures its first frame.
        applyFullscreen();
        setTitle(getString(R.string.app_name));
        setContentView(R.layout.activity_main);
        TextView remoteText = findViewById(R.id.remote_text);
        SharedPreferences cache = getSharedPreferences(PREFS, MODE_PRIVATE);
        showRemoteText(remoteText, cache.getString(CACHE_KEY, ""));
        fetchRemoteText(remoteText, cache);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            applyFullscreen();
        }
    }


    private void showRemoteText(TextView view, String text) {
        String value = text == null ? "" : text.trim();
        view.setText(value);
        view.setVisibility(value.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void fetchRemoteText(TextView view, SharedPreferences cache) {
        textExecutor.execute(() -> {
            HttpURLConnection connection = null;
            try {
                connection = (HttpURLConnection) new URL(TEXT_URL).openConnection();
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);
                connection.setRequestMethod("GET");
                connection.setUseCaches(false);
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) return;
                StringBuilder text = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                        connection.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null && text.length() < 500) {
                        if (text.length() > 0) text.append('\n');
                        text.append(line);
                    }
                }
                String result = text.toString().trim();
                if (result.isEmpty()) return;
                cache.edit().putString(CACHE_KEY, result).apply();
                runOnUiThread(() -> showRemoteText(view, result));
            } catch (Exception ignored) {
                // Keep the last cached value; with no cache the glass card stays hidden.
            } finally {
                if (connection != null) connection.disconnect();
            }
        });
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
        // Keep the legacy flags as a fallback for ColorOS builds that report a
        // controller but do not immediately apply its hidden-bar request.
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
}
