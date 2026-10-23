package com.aether.cinematv;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.util.LruCache;
import android.view.View;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

final class Images {
    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(3);
    private static final Handler MAIN = new Handler(Looper.getMainLooper());
    private static final LruCache<String, Bitmap> CACHE = new LruCache<String, Bitmap>(24 * 1024 * 1024) {
        @Override protected int sizeOf(String k, Bitmap value) { return value.getByteCount(); }
    };

    private Images() {}

    static void into(Context ctx, ImageView image, String source, String fallbackResource) {
        int fallback = ctx.getResources().getIdentifier(fallbackResource, "drawable", ctx.getPackageName());
        if (fallback != 0) image.setImageResource(fallback);
        image.setScaleType(ImageView.ScaleType.CENTER_CROP);
        image.setTag(source);
        if (source == null || source.trim().isEmpty()) return;
        if (!source.startsWith("http://") && !source.startsWith("https://")) {
            int id = ctx.getResources().getIdentifier(source, "drawable", ctx.getPackageName());
            if (id != 0) image.setImageResource(id);
            return;
        }
        Bitmap cached = CACHE.get(source);
        if (cached != null) { image.setImageBitmap(cached); return; }
        EXECUTOR.execute(() -> {
            HttpURLConnection c = null;
            try {
                c = (HttpURLConnection) new URL(source).openConnection();
                c.setConnectTimeout(6500);
                c.setReadTimeout(8500);
                c.setInstanceFollowRedirects(true);
                if (c.getResponseCode() != 200) return;
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                try (InputStream is = c.getInputStream()) {
                    byte[] buf = new byte[8192];
                    int count;
                    while ((count = is.read(buf)) != -1) {
                        bytes.write(buf, 0, count);
                        if (bytes.size() > 6_000_000) return;
                    }
                }
                byte[] data = bytes.toByteArray();
                BitmapFactory.Options size = new BitmapFactory.Options();
                size.inJustDecodeBounds = true;
                BitmapFactory.decodeByteArray(data, 0, data.length, size);
                BitmapFactory.Options opts = new BitmapFactory.Options();
                int sample = 1;
                while (size.outWidth / sample > 1280 || size.outHeight / sample > 1280) sample *= 2;
                opts.inSampleSize = sample;
                Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length, opts);
                if (bmp == null) return;
                CACHE.put(source, bmp);
                MAIN.post(() -> {
                    if (image.getWindowToken() != null && source.equals(image.getTag())) image.setImageBitmap(bmp);
                });
            } catch (Exception ignored) {
            } finally { if (c != null) c.disconnect(); }
        });
    }
}
