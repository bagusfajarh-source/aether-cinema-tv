package com.aether.cinematv;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;
import android.widget.Toast;
import java.util.Locale;

public final class PlayerActivity extends Activity {
    private VideoView video;
    private TextView playButton;
    private TextView timestamp;
    private SeekBar timeline;
    private ProgressBar loading;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean seeking;
    private boolean completed;
    private String movieId;
    private SharedPreferences prefs;

    private final Runnable progressUpdater = new Runnable() {
        @Override public void run() {
            if (video != null && video.getDuration() > 0) {
                int duration = video.getDuration();
                int at = Math.max(0, video.getCurrentPosition());
                if (!seeking) { timeline.setMax(duration); timeline.setProgress(at); }
                timestamp.setText(time(at) + "  /  " + time(duration));
                playButton.setText(video.isPlaying() ? "Ⅱ  JEDA" : "▶  PUTAR");
            }
            handler.postDelayed(this, 1000);
        }
    };

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        prefs = getSharedPreferences("aether_tv", MODE_PRIVATE);
        movieId = getIntent().getStringExtra("id");
        if (movieId == null) movieId = "untitled";
        String title = getIntent().getStringExtra("title");
        String url = getIntent().getStringExtra("url");
        if (url == null || (!url.startsWith("http://") && !url.startsWith("https://"))) {
            Toast.makeText(this, "URL video tidak valid", Toast.LENGTH_LONG).show();
            finish(); return;
        }

        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(Color.BLACK);
        video = new VideoView(this);
        FrameLayout.LayoutParams vp = new FrameLayout.LayoutParams(-1, -1, Gravity.CENTER);
        root.addView(video, vp);
        loading = new ProgressBar(this);
        loading.setIndeterminate(true);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(d(48), d(48), Gravity.CENTER);
        root.addView(loading, lp);

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.VERTICAL);
        header.setPadding(d(38), d(20), d(38), d(16));
        header.setBackground(Ui.gradient(this, new int[]{0xee080d18, 0x00080d18},
                GradientDrawable.Orientation.TOP_BOTTOM, 0));
        header.addView(Ui.text(this, "✦  AETHER CINEMA", 12, Ui.AQUA, true),
                new LinearLayout.LayoutParams(-1, d(24)));
        header.addView(Ui.text(this, title == null ? "Memutar video" : title, 23, Ui.WHITE, true),
                new LinearLayout.LayoutParams(-1, d(39)));
        root.addView(header, new FrameLayout.LayoutParams(-1, d(110), Gravity.TOP));

        LinearLayout controls = new LinearLayout(this);
        controls.setOrientation(LinearLayout.VERTICAL);
        controls.setPadding(d(38), d(24), d(38), d(22));
        controls.setBackground(Ui.gradient(this, new int[]{0x00080d18, 0xee080d18, 0xff080d18},
                GradientDrawable.Orientation.TOP_BOTTOM, 0));
        LinearLayout actions = new LinearLayout(this);
        actions.setGravity(Gravity.CENTER_VERTICAL);
        playButton = Ui.button(this, "Ⅱ  JEDA", true);
        playButton.setOnClickListener(v -> togglePlayback());
        actions.addView(playButton, new LinearLayout.LayoutParams(d(128), d(48)));
        TextView back = Ui.button(this, "↺  -10 DTK", false);
        LinearLayout.LayoutParams space = new LinearLayout.LayoutParams(d(137), d(48));
        space.leftMargin = d(12);
        actions.addView(back, space);
        back.setOnClickListener(v -> skip(-10000));
        TextView forward = Ui.button(this, "+10 DTK  ↻", false);
        LinearLayout.LayoutParams space2 = new LinearLayout.LayoutParams(d(137), d(48));
        space2.leftMargin = d(12);
        actions.addView(forward, space2);
        forward.setOnClickListener(v -> skip(10000));
        View filler = new View(this);
        actions.addView(filler, new LinearLayout.LayoutParams(0, d(1), 1));
        TextView exit = Ui.button(this, "✕  KELUAR", false);
        exit.setOnClickListener(v -> finish());
        actions.addView(exit, new LinearLayout.LayoutParams(d(132), d(48)));
        controls.addView(actions, new LinearLayout.LayoutParams(-1, d(55)));
        LinearLayout seekLine = new LinearLayout(this);
        seekLine.setOrientation(LinearLayout.HORIZONTAL);
        seekLine.setGravity(Gravity.CENTER_VERTICAL);
        timeline = new SeekBar(this);
        timeline.setMax(100);
        timeline.setProgressTintList(android.content.res.ColorStateList.valueOf(Ui.AQUA));
        timeline.setThumbTintList(android.content.res.ColorStateList.valueOf(Ui.WHITE));
        timeline.setKeyProgressIncrement(10000);
        seekLine.addView(timeline, new LinearLayout.LayoutParams(0, d(44), 1));
        timestamp = Ui.text(this, "00:00 / 00:00", 12, Ui.WHITE, false);
        timestamp.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        seekLine.addView(timestamp, new LinearLayout.LayoutParams(d(132), d(42)));
        controls.addView(seekLine, new LinearLayout.LayoutParams(-1, d(45)));
        controls.addView(Ui.text(this, "Gunakan tombol remote untuk memilih kontrol  •  BACK untuk kembali", 11, Ui.MUTED, false),
                new LinearLayout.LayoutParams(-1, d(21)));
        root.addView(controls, new FrameLayout.LayoutParams(-1, d(178), Gravity.BOTTOM));
        setContentView(root);

        timeline.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onStartTrackingTouch(SeekBar b) { seeking = true; }
            public void onStopTrackingTouch(SeekBar b) { video.seekTo(b.getProgress()); seeking = false; }
            public void onProgressChanged(SeekBar b, int p, boolean fromUser) {
                if (fromUser) { video.seekTo(p); timestamp.setText(time(p) + " / " + time(video.getDuration())); }
            }
        });
        video.setOnPreparedListener(mp -> {
            loading.setVisibility(View.GONE);
            int resume = prefs.getInt("progress:" + movieId, 0);
            if (resume > 0 && (video.getDuration() <= 0 || resume < video.getDuration() - 12000)) video.seekTo(resume);
            mp.setOnInfoListener((player, what, extra) -> {
                if (what == MediaPlayer.MEDIA_INFO_BUFFERING_START) loading.setVisibility(View.VISIBLE);
                if (what == MediaPlayer.MEDIA_INFO_BUFFERING_END) loading.setVisibility(View.GONE);
                return false;
            });
            video.start();
            playButton.requestFocus();
        });
        video.setOnCompletionListener(mp -> {
            completed = true;
            prefs.edit().remove("progress:" + movieId).apply();
            playButton.setText("▶  PUTAR ULANG");
            playButton.requestFocus();
        });
        video.setOnErrorListener((mp, what, extra) -> {
            loading.setVisibility(View.GONE);
            Toast.makeText(this, "Video tidak dapat dimainkan. Periksa URL/codec/format video.", Toast.LENGTH_LONG).show();
            return true;
        });
        video.setVideoURI(Uri.parse(url));
        video.requestFocus();
        handler.post(progressUpdater);
    }

    private void togglePlayback() {
        if (video.isPlaying()) video.pause();
        else {
            if (completed) { completed = false; video.seekTo(0); }
            video.start();
        }
        playButton.setText(video.isPlaying() ? "Ⅱ  JEDA" : "▶  PUTAR");
    }
    private void skip(int ms) {
        if (video == null) return;
        video.seekTo(Math.max(0, Math.min(video.getDuration(), video.getCurrentPosition() + ms)));
    }
    private static String time(int ms) {
        if (ms < 0) return "00:00";
        int sec = ms / 1000;
        int h = sec / 3600;
        return h > 0 ? String.format(Locale.US, "%d:%02d:%02d", h, (sec % 3600) / 60, sec % 60)
                : String.format(Locale.US, "%02d:%02d", sec / 60, sec % 60);
    }
    private int d(float n) { return Ui.dp(this, n); }

    @Override public boolean onKeyDown(int keyCode, KeyEvent e) {
        if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY ||
                keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE) {
            togglePlayback(); return true;
        }
        return super.onKeyDown(keyCode, e);
    }
    @Override protected void onPause() {
        super.onPause();
        if (video != null && !completed) {
            int p = video.getCurrentPosition();
            if (p > 1000) prefs.edit().putInt("progress:" + movieId, p).apply();
            video.pause();
        }
    }
    @Override protected void onDestroy() {
        handler.removeCallbacks(progressUpdater);
        if (video != null) video.stopPlayback();
        super.onDestroy();
    }
}
