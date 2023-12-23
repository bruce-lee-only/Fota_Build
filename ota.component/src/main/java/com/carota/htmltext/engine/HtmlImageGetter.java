package com.carota.htmltext.engine;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Looper;
import android.text.Html;
import android.widget.TextView;

import com.carota.htmltext.IHtmlImageLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class HtmlImageGetter implements Html.ImageGetter, Drawable.Callback {
    private static final String IMAGE_TAG_REGULAR = "<(img|IMG)\\s+([^>]*)>";
    private static final Pattern IMAGE_TAG_PATTERN = Pattern.compile(IMAGE_TAG_REGULAR);
    private static final Pattern IMAGE_WIDTH_PATTERN = Pattern.compile("(width|WIDTH)\\s*=\\s*\"?(\\w+)\"?");
    private static final Pattern IMAGE_HEIGHT_PATTERN = Pattern.compile("(height|HEIGHT)\\s*=\\s*\"?(\\w+)\"?");

    private TextView textView;
    private IHtmlImageLoader imageLoader;
    private List<ImageSize> imageSizeList;
    private int index;

    public HtmlImageGetter() {
        imageSizeList = new ArrayList<>();
    }

    public void setTextView(TextView textView) {
        this.textView = textView;
        textView.setTag(this);
    }

    public void setImageLoader(IHtmlImageLoader imageLoader) {
        this.imageLoader = imageLoader;
    }

    public void getImageSize(String source) {
        Matcher imageMatcher = IMAGE_TAG_PATTERN.matcher(source);
        while (imageMatcher.find()) {
            String attrs = imageMatcher.group(2).trim();
            int width = -1;
            int height = -1;
            Matcher widthMatcher = IMAGE_WIDTH_PATTERN.matcher(attrs);
            if (widthMatcher.find()) {
                width = parseSize(widthMatcher.group(2).trim());
            }
            Matcher heightMatcher = IMAGE_HEIGHT_PATTERN.matcher(attrs);
            if (heightMatcher.find()) {
                height = parseSize(heightMatcher.group(2).trim());
            }
            ImageSize imageSize = new ImageSize(width, height);
            imageSizeList.add(imageSize);
        }
    }

    @Override
    public Drawable getDrawable(String source) {
        final ImageDrawable imageDrawable = new ImageDrawable(index++);

        if (imageLoader != null) {
            imageDrawable.setDrawable(imageLoader.getDefaultDrawable(), false);
            imageLoader.loadImage(source, new IHtmlImageLoader.Callback() {
                @Override
                public void onLoadComplete(final Drawable drawable) {
                    runOnUi(new Runnable() {
                        @Override
                        public void run() {
                            imageDrawable.setDrawable(drawable, true);
                            textView.setText(textView.getText());
                        }
                    });
                }

                @Override
                public void onLoadFailed() {
                    runOnUi(new Runnable() {
                        @Override
                        public void run() {
                            imageDrawable.setDrawable(imageLoader.getErrorDrawable(), false);
                            textView.setText(textView.getText());
                        }
                    });
                }
            });
        }

        return imageDrawable;
    }

    private void runOnUi(Runnable r) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            r.run();
        } else {
            textView.post(r);
        }
    }

    private static int parseSize(String size) {
        try {
            return Integer.valueOf(size);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    @Override
    public void invalidateDrawable(Drawable who) {
        //textView.invalidateDrawable(who);
        textView.invalidate();
    }

    @Override
    public void scheduleDrawable(Drawable who, Runnable what, long when) {
        textView.scheduleDrawable(who, what, when);
    }

    @Override
    public void unscheduleDrawable(Drawable who, Runnable what) {
        textView.unscheduleDrawable(who, what);
    }

    private static class ImageSize {
        private final int width;
        private final int height;

        public ImageSize(int width, int height) {
            this.width = width;
            this.height = height;
        }

        public boolean valid() {
            return width >= 0 && height >= 0;
        }
    }

    private class ImageDrawable extends BitmapDrawable {
        // img 标签出现的位置
        private final int position;
        private Drawable mDrawable;

        public ImageDrawable(int position) {
            super();
            this.position = position;
        }

        public void setDrawable(Drawable drawable, boolean fitSize) {
            mDrawable = drawable;

            if (mDrawable == null) {
                setBounds(0, 0, 0, 0);
                return;
            }

            int maxWidth = (imageLoader == null) ? 0 : imageLoader.getMaxWidth();
            boolean fitWidth = imageLoader != null && imageLoader.fitWidth();
            int width, height;
            if (fitSize) { // real image
                ImageSize imageSize = (imageSizeList.size() > position) ? imageSizeList.get(position) : null;
                if (imageSize != null && imageSize.valid()) {
                    width = dp2px(imageSize.width);
                    height = dp2px(imageSize.height);
                } else {
                    width = mDrawable.getIntrinsicWidth();
                    height = mDrawable.getIntrinsicHeight();
                }
            } else { // placeholder image
                width = mDrawable.getIntrinsicWidth();
                height = mDrawable.getIntrinsicHeight();
            }

            if (width > 0 && height > 0) {
                // too large or should fit width
                if (maxWidth > 0 && (width > maxWidth || fitWidth)) {
                    height = (int) ((float) height / width * maxWidth);
                    width = maxWidth;
                }
            }

            mDrawable.setBounds(0, 0, width, height);
            setBounds(0, 0, width, height);
        }

        @Override
        public void draw(Canvas canvas) {
            // override the draw to facilitate refresh function later
            if (mDrawable != null) {
                if (mDrawable instanceof BitmapDrawable) {
                    BitmapDrawable bitmapDrawable = (BitmapDrawable) mDrawable;
                    Bitmap bitmap = bitmapDrawable.getBitmap();
                    if (bitmap == null || bitmap.isRecycled()) {
                        return;
                    }
                }
                mDrawable.draw(canvas);
            }
        }

        private int dp2px(float dpValue) {
            float scale = textView.getResources().getDisplayMetrics().density;
            return (int) (dpValue * scale + 0.5f);
        }
    }
}
