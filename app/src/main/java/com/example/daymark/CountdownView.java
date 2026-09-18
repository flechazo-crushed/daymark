package com.example.daymark;

import android.content.Context;
import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class CountdownView extends FrameLayout {
    private View backgroundView;
    private View dimView;
    private View glassView;
    private View cardOutline;
    private TextView loveLabel;
    private ImageButton screenshotButton;
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
        cardOutline = findViewById(R.id.card_outline);
        loveLabel = findViewById(R.id.love_label);
        screenshotButton = findViewById(R.id.screenshot_button);
        screenshotButton.setOnClickListener(view -> saveScreenshot());
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
        cardOutline.measure(
                MeasureSpec.makeMeasureSpec(cardWidth + dp(16), MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(cardHeight + dp(16), MeasureSpec.EXACTLY));
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
        int outlineLeft = cardLeft - dp(8);
        int outlineTop = cardTop - dp(8);
        cardOutline.layout(outlineLeft, outlineTop,
                outlineLeft + cardOutline.getMeasuredWidth(),
                outlineTop + cardOutline.getMeasuredHeight());

        int labelLeft = (width - loveLabel.getMeasuredWidth()) / 2;
        int labelTop = cardTop + cardHeight + dp(24);
        loveLabel.layout(labelLeft, labelTop,
                labelLeft + loveLabel.getMeasuredWidth(),
                labelTop + loveLabel.getMeasuredHeight());

        int buttonSize = screenshotButton.getMeasuredWidth();
        int buttonRight = width - dp(22);
        int buttonBottom = height - dp(22);
        screenshotButton.layout(buttonRight - buttonSize, buttonBottom - buttonSize,
                buttonRight, buttonBottom);
    }

    private void saveScreenshot() {
        if (getWidth() <= 0 || getHeight() <= 0) {
            return;
        }

        Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        String fileName = "Daymark_"
                + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA).format(new Date())
                + ".png";
        Uri uri = null;
        try {
            draw(new Canvas(bitmap));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
                values.put(MediaStore.Images.Media.RELATIVE_PATH,
                        Environment.DIRECTORY_PICTURES + "/Daymark");
                values.put(MediaStore.Images.Media.IS_PENDING, 1);
                uri = getContext().getContentResolver().insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                if (uri == null) {
                    throw new IllegalStateException("Unable to create media entry");
                }
                try (OutputStream output = getContext().getContentResolver().openOutputStream(uri)) {
                    if (output == null || !bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)) {
                        throw new IllegalStateException("Unable to write screenshot");
                    }
                }
                values.clear();
                values.put(MediaStore.Images.Media.IS_PENDING, 0);
                getContext().getContentResolver().update(uri, values, null, null);
            } else {
                File directory = new File(getContext().getExternalFilesDir(
                        Environment.DIRECTORY_PICTURES), "Daymark");
                if (!directory.exists() && !directory.mkdirs()) {
                    throw new IllegalStateException("Unable to create screenshot directory");
                }
                File file = new File(directory, fileName);
                try (FileOutputStream output = new FileOutputStream(file)) {
                    if (!bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)) {
                        throw new IllegalStateException("Unable to write screenshot");
                    }
                }
            }
            Toast.makeText(getContext(), "截图已保存到相册", Toast.LENGTH_SHORT).show();
        } catch (Exception exception) {
            if (uri != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                getContext().getContentResolver().delete(uri, null, null);
            }
            Toast.makeText(getContext(), "截图保存失败", Toast.LENGTH_SHORT).show();
        } finally {
            bitmap.recycle();
        }
    }

    private int dp(float value) {
        return Math.round(value * density);
    }
}
