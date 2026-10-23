package com.aether.cinematv;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class CatalogRepository {
    public interface Callback { void done(boolean ok, String message); }
    private final Context app;
    private final Handler main = new Handler(Looper.getMainLooper());
    private final ExecutorService worker = Executors.newSingleThreadExecutor();
    private List<Movie> items = Collections.emptyList();

    CatalogRepository(Context context) {
        app = context.getApplicationContext();
        try (InputStream in = app.getAssets().open("catalog.json")) {
            items = parse(readText(in));
        } catch (Exception ignored) {
            items = Collections.emptyList();
        }
    }

    public synchronized List<Movie> snapshot() { return new ArrayList<>(items); }

    public void loadRemote(String url, Callback cb) {
        if (!url.startsWith("https://") && !url.startsWith("http://")) {
            main.post(() -> cb.done(false, "URL harus diawali http:// atau https://"));
            return;
        }
        worker.execute(() -> {
            HttpURLConnection conn = null;
            try {
                conn = (HttpURLConnection) new URL(url).openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(7000);
                conn.setReadTimeout(9000);
                conn.setRequestProperty("Accept", "application/json");
                if (conn.getResponseCode() != 200) throw new Exception("HTTP " + conn.getResponseCode());
                final List<Movie> remote;
                try (InputStream in = conn.getInputStream()) { remote = parse(readText(in)); }
                if (remote.isEmpty()) throw new Exception("Katalog kosong atau tidak valid");
                synchronized (this) { items = remote; }
                main.post(() -> cb.done(true, "Berhasil memuat " + remote.size() + " judul"));
            } catch (Exception e) {
                final String message = e.getMessage() == null ? "Gagal mengambil katalog" : e.getMessage();
                main.post(() -> cb.done(false, message));
            } finally {
                if (conn != null) conn.disconnect();
            }
        });
    }

    public void resetDemo(Callback cb) {
        try (InputStream in = app.getAssets().open("catalog.json")) {
            List<Movie> demo = parse(readText(in));
            synchronized (this) { items = demo; }
            cb.done(true, "Katalog contoh dipulihkan");
        } catch (Exception e) {
            cb.done(false, "Tidak bisa memuat katalog contoh");
        }
    }

    private static List<Movie> parse(String json) throws Exception {
        JSONObject root = new JSONObject(json);
        JSONArray movies = root.getJSONArray("movies");
        List<Movie> out = new ArrayList<>();
        for (int i = 0; i < movies.length() && i < 250; i++) {
            Movie m = new Movie(movies.getJSONObject(i));
            if (!m.id.isEmpty()) out.add(m);
        }
        return out;
    }

    private static String readText(InputStream in) throws Exception {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        byte[] chunk = new byte[8192];
        int n;
        while ((n = in.read(chunk)) != -1) {
            b.write(chunk, 0, n);
            if (b.size() > 2_000_000) throw new Exception("File JSON maksimal 2 MB");
        }
        return b.toString(StandardCharsets.UTF_8.name());
    }
}
