package com.aether.cinematv;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class MainActivity extends Activity {
    private CatalogRepository repository;
    private SharedPreferences preferences;
    private LinearLayout side;
    private FrameLayout area;
    private String screen = "home";
    private String previousScreen = "home";
    private Movie selected;
    private boolean resumedOnce;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        preferences = getSharedPreferences("aether_tv", MODE_PRIVATE);
        repository = new CatalogRepository(this);
        buildShell();
        showHome();
        String url = preferences.getString("catalog_url", "");
        if (!url.isEmpty()) {
            repository.loadRemote(url, (ok, message) -> { if (ok) refreshScreen(); });
        }
    }

    @Override protected void onResume() {
        super.onResume();
        if (resumedOnce) refreshScreen();
        resumedOnce = true;
    }

    private void buildShell() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.HORIZONTAL);
        root.setBackgroundColor(Ui.BG);
        side = new LinearLayout(this);
        side.setOrientation(LinearLayout.VERTICAL);
        side.setPadding(d(21), d(25), d(13), d(15));
        side.setBackground(Ui.gradient(this, new int[]{0xff111c30, 0xff09121f, 0xff090e19},
                GradientDrawable.Orientation.TL_BR, 0));
        root.addView(side, new LinearLayout.LayoutParams(d(203), ViewGroup.LayoutParams.MATCH_PARENT));
        View line = new View(this);
        line.setBackgroundColor(0xff25364b);
        root.addView(line, new LinearLayout.LayoutParams(d(1), ViewGroup.LayoutParams.MATCH_PARENT));
        area = new FrameLayout(this);
        area.setBackgroundColor(Ui.BG);
        root.addView(area, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1));
        setContentView(root);
        buildMenu();
    }

    private void buildMenu() {
        side.removeAllViews();
        TextView logo = Ui.text(this, "✦  AETHER", 22, Ui.AQUA, true);
        side.addView(logo);
        TextView sub = Ui.text(this, "C I N E M A   T V", 10.2f, 0xffb8cbe0, true);
        LinearLayout.LayoutParams sp = lp(ViewGroup.LayoutParams.MATCH_PARENT, d(28));
        sp.bottomMargin = d(45);
        side.addView(sub, sp);
        addNav("⌂", "Beranda", "home");
        addNav("⌕", "Pencarian", "search");
        addNav("▣", "Film", "movies");
        addNav("▤", "Serial", "series");
        addNav("♡", "Daftar Saya", "saved");
        addNav("⚙", "Pengaturan", "settings");
        View fill = new View(this);
        side.addView(fill, new LinearLayout.LayoutParams(1, 0, 1));
        TextView tag = Ui.text(this, "●  MEDIA HUB", 10, Ui.AQUA, true);
        side.addView(tag, lp(-1, d(28)));
        TextView line = Ui.text(this, "PERSONAL STREAMING\nDESIGNED FOR TV", 10, Ui.MUTED, false);
        line.setLineSpacing(d(4), 1f);
        side.addView(line);
    }

    private void addNav(String glyph, String label, String target) {
        LinearLayout item = new LinearLayout(this);
        item.setOrientation(LinearLayout.HORIZONTAL);
        item.setGravity(Gravity.CENTER_VERTICAL);
        item.setPadding(d(14), 0, d(6), 0);
        TextView icon = Ui.text(this, glyph, 21, Ui.AQUA, true);
        item.addView(icon, lp(d(32), -1));
        TextView text = Ui.text(this, label, 14, Ui.WHITE, true);
        item.addView(text, lp(-1, -1));
        boolean active = screen.equals(target) || (screen.equals("detail") && previousScreen.equals(target));
        item.setBackground(Ui.outlined(this, active ? 0xff20374b : 0x00151d2b,
                active ? 0xff365368 : 0x00000000, 11, 1));
        item.setFocusable(true);
        item.setClickable(true);
        item.setOnClickListener(v -> navigate(target));
        item.setOnFocusChangeListener((v, hasFocus) -> {
            item.setBackground(Ui.outlined(this, hasFocus ? 0xff2f5265 :
                    (active ? 0xff20374b : 0x00151d2b), hasFocus ? Ui.AQUA :
                    (active ? 0xff365368 : 0x00000000), 11, hasFocus ? 1.7f : 1));
            text.setTextColor(hasFocus ? Ui.AQUA : Ui.WHITE);
        });
        LinearLayout.LayoutParams p = lp(-1, d(49));
        p.bottomMargin = d(8);
        side.addView(item, p);
    }

    private void navigate(String target) {
        selected = null;
        screen = target;
        buildMenu();
        switch (target) {
            case "search": showSearch(); break;
            case "movies": showCatalog("Film", "Jelajahi koleksi film", "Film"); break;
            case "series": showCatalog("Serial", "Temukan serial favoritmu", "Serial"); break;
            case "saved": showSaved(); break;
            case "settings": showSettings(); break;
            default: showHome();
        }
    }

    private void refreshScreen() {
        if ("detail".equals(screen) && selected != null) showDetail(selected);
        else navigate(screen);
    }

    private ScrollView scroll() {
        ScrollView s = new ScrollView(this);
        s.setClipToPadding(false);
        s.setFillViewport(true);
        s.setVerticalScrollBarEnabled(false);
        s.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
        return s;
    }
    private LinearLayout vertical() {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        l.setClipChildren(false);
        l.setClipToPadding(false);
        return l;
    }
    private void setPage(View content) { area.removeAllViews(); area.addView(content,
            new FrameLayout.LayoutParams(-1, -1)); }

    private void showHome() {
        screen = "home";
        buildMenu();
        List<Movie> all = repository.snapshot();
        ScrollView s = scroll();
        LinearLayout body = vertical();
        body.setPadding(d(25), d(19), d(15), d(32));
        s.addView(body);
        setPage(s);
        if (all.isEmpty()) { empty(body, "Katalog belum tersedia", "Isi catalog.json atau URL katalog di Pengaturan."); return; }
        Movie feature = all.get(0);
        for (Movie m : all) if (m.featured) { feature = m; break; }
        createHero(body, feature);
        List<Movie> unfinished = filtered(all, "continue");
        if (!unfinished.isEmpty()) addRow(body, "LANJUTKAN MENONTON", "Lanjut dari posisi terakhir", unfinished);
        addRow(body, "PILIHAN UNTUKMU", "Koleksi yang siap ditonton", all);
        List<Movie> films = filtered(all, "Film");
        if (!films.isEmpty()) addRow(body, "FILM PILIHAN", "Film & dokumenter", films);
        List<Movie> series = filtered(all, "Serial");
        if (!series.isEmpty()) addRow(body, "SERIAL & EPISODE", "Temukan cerita berikutnya", series);
        Set<String> savedIds = bookmarks();
        List<Movie> saved = new ArrayList<>();
        for (Movie m : all) if (savedIds.contains(m.id)) saved.add(m);
        if (!saved.isEmpty()) addRow(body, "DAFTAR SAYA", "Tersimpan untuk nanti", saved);
    }

    private void createHero(LinearLayout parent, Movie movie) {
        FrameLayout hero = new FrameLayout(this);
        hero.setBackground(Ui.shape(this, Ui.PANEL, 18));
        hero.setClipToOutline(true);
        ImageView art = new ImageView(this);
        Images.into(this, art, movie.backdrop, "hero_cinema");
        hero.addView(art, new FrameLayout.LayoutParams(-1, -1));
        View dark = new View(this);
        dark.setBackground(Ui.gradient(this,
                new int[]{0xee07121d, 0xdb07121d, 0x8e091321, 0x1105090e},
                GradientDrawable.Orientation.LEFT_RIGHT, 0));
        hero.addView(dark, new FrameLayout.LayoutParams(-1, -1));
        LinearLayout text = vertical();
        text.setGravity(Gravity.CENTER_VERTICAL);
        text.setPadding(d(32), d(14), d(10), d(14));
        TextView badge = Ui.text(this, "✦  UNGGULAN AETHER CINEMA", 11, Ui.AQUA, true);
        text.addView(badge, lp(-1, d(28)));
        TextView title = Ui.text(this, movie.title, 40, Ui.WHITE, true);
        title.setMaxLines(2);
        text.addView(title, lp(-1, -2));
        TextView tags = Ui.text(this,
                movie.year + "    •    " + movie.genre + "    •    " + movie.rating, 13, 0xffd0dfef, true);
        LinearLayout.LayoutParams tp = lp(-1, d(39));
        text.addView(tags, tp);
        TextView description = Ui.text(this, movie.summary, 14, 0xffdce6ed, false);
        description.setMaxLines(3);
        text.addView(description, lp(-1, -2));
        LinearLayout buttons = new LinearLayout(this);
        buttons.setOrientation(LinearLayout.HORIZONTAL);
        buttons.setPadding(0, d(22), 0, 0);
        TextView play = Ui.button(this, "▶  PUTAR", true);
        play.setOnClickListener(v -> play(movie));
        buttons.addView(play, lp(-2, d(46)));
        TextView details = Ui.button(this, "DETAIL FILM   →", false);
        LinearLayout.LayoutParams bp = lp(-2, d(46)); bp.leftMargin = d(12);
        buttons.addView(details, bp);
        details.setOnClickListener(v -> showDetail(movie));
        text.addView(buttons);
        FrameLayout.LayoutParams textParams = new FrameLayout.LayoutParams(d(520), -1, Gravity.START);
        hero.addView(text, textParams);
        LinearLayout.LayoutParams heroParams = lp(-1, d(324));
        heroParams.bottomMargin = d(30);
        parent.addView(hero, heroParams);
    }

    private List<Movie> filtered(List<Movie> all, String filter) {
        List<Movie> out = new ArrayList<>();
        for (Movie m : all) {
            if ("continue".equals(filter)) {
                if (preferences.getInt("progress:" + m.id, 0) >= 10000) out.add(m);
            } else if (filter.equalsIgnoreCase(m.type)) out.add(m);
        }
        return out;
    }

    private void addRow(LinearLayout body, String heading, String sub, List<Movie> items) {
        TextView h = Ui.text(this, heading, 19, Ui.WHITE, true);
        body.addView(h, lp(-1, d(27)));
        TextView hint = Ui.text(this, sub, 12, Ui.MUTED, false);
        LinearLayout.LayoutParams hp = lp(-1, d(24)); hp.bottomMargin = d(13);
        body.addView(hint, hp);
        HorizontalScrollView horizontal = new HorizontalScrollView(this);
        horizontal.setClipChildren(false); horizontal.setClipToPadding(false);
        horizontal.setHorizontalScrollBarEnabled(false);
        LinearLayout track = new LinearLayout(this);
        track.setClipChildren(false); track.setClipToPadding(false);
        track.setOrientation(LinearLayout.HORIZONTAL);
        track.setPadding(d(5), d(7), d(16), d(10));
        for (Movie m : items) {
            View card = posterCard(m, d(159), d(201));
            LinearLayout.LayoutParams cp = lp(d(159), -2);
            cp.rightMargin = d(15);
            track.addView(card, cp);
        }
        horizontal.addView(track);
        LinearLayout.LayoutParams pp = lp(-1, d(281)); pp.bottomMargin = d(13);
        body.addView(horizontal, pp);
    }

    private View posterCard(Movie m, int width, int posterHeight) {
        LinearLayout card = vertical();
        card.setPadding(d(4), d(4), d(4), d(3));
        card.setBackground(Ui.outlined(this, Ui.PANEL, 0xff243044, 14, 1));
        ImageView art = new ImageView(this);
        art.setClipToOutline(true);
        art.setBackground(Ui.shape(this, 0xff1c304b, 11));
        Images.into(this, art, m.poster, "poster_default");
        card.addView(art, lp(-1, posterHeight));
        TextView title = Ui.text(this, m.title, 12.5f, Ui.WHITE, true);
        title.setSingleLine(true); title.setEllipsize(android.text.TextUtils.TruncateAt.END);
        title.setPadding(d(5), d(8), 0, 0);
        card.addView(title, lp(-1, d(27)));
        TextView info = Ui.text(this, m.year + "  •  " + m.genre, 10.4f, Ui.MUTED, false);
        info.setSingleLine(true); info.setEllipsize(android.text.TextUtils.TruncateAt.END);
        info.setPadding(d(5), 0, 0, 0);
        card.addView(info, lp(-1, d(18)));
        Ui.focus(card, this);
        card.setOnClickListener(v -> showDetail(m));
        return card;
    }

    private void showCatalog(String title, String subtitle, String filter) {
        screen = "Film".equals(filter) ? "movies" : "series";
        ScrollView s = scroll();
        LinearLayout body = vertical();
        body.setPadding(d(27), d(30), d(19), d(40));
        s.addView(body);
        header(body, title.toUpperCase(Locale.ROOT), subtitle);
        List<Movie> movies = filtered(repository.snapshot(), filter);
        makeGrid(body, movies);
        setPage(s);
    }

    private void showSaved() {
        screen = "saved";
        ScrollView s = scroll();
        LinearLayout body = vertical();
        body.setPadding(d(27), d(30), d(19), d(30));
        s.addView(body);
        header(body, "DAFTAR SAYA", "Koleksi favorit di perangkat ini");
        List<Movie> saved = new ArrayList<>();
        Set<String> ids = bookmarks();
        for (Movie m : repository.snapshot()) if (ids.contains(m.id)) saved.add(m);
        if (saved.isEmpty()) empty(body, "Daftar kamu masih kosong", "Buka detail film lalu pilih + DAFTAR SAYA.");
        else makeGrid(body, saved);
        setPage(s);
    }

    private void showSearch() {
        screen = "search";
        LinearLayout body = vertical();
        body.setPadding(d(27), d(30), d(19), d(22));
        header(body, "CARI FILM & SERIAL", "Cari berdasarkan judul, genre atau tahun");
        EditText query = new EditText(this);
        query.setSingleLine(true);
        query.setTextSize(19);
        query.setTextColor(Ui.WHITE);
        query.setHintTextColor(Ui.MUTED);
        query.setHint("⌕  Ketik judul film atau genre...");
        query.setPadding(d(20), d(8), d(20), d(8));
        query.setInputType(android.text.InputType.TYPE_CLASS_TEXT);
        query.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        query.setBackground(Ui.outlined(this, Ui.PANEL, 0xff465b73, 10, 1));
        LinearLayout.LayoutParams q = lp(-1, d(57)); q.bottomMargin = d(19);
        body.addView(query, q);
        ScrollView scroll = scroll();
        LinearLayout results = vertical();
        scroll.addView(results);
        body.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        setPage(body);
        Runnable render = () -> {
            String search = query.getText().toString().trim().toLowerCase(Locale.ROOT);
            List<Movie> found = new ArrayList<>();
            for (Movie movie : repository.snapshot()) {
                if (search.isEmpty() || (movie.title + " " + movie.genre + " " + movie.year + " " + movie.summary)
                        .toLowerCase(Locale.ROOT).contains(search)) found.add(movie);
            }
            results.removeAllViews();
            TextView count = Ui.text(this, found.size() + " hasil ditemukan", 12, Ui.MUTED, false);
            results.addView(count, lp(-1, d(30)));
            if (found.isEmpty()) empty(results, "Tidak ada hasil", "Coba kata kunci lain.");
            else makeGrid(results, found);
        };
        query.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int st, int count, int after) {}
            public void onTextChanged(CharSequence s, int st, int before, int count) { render.run(); }
            public void afterTextChanged(Editable e) {}
        });
        render.run();
    }

    private void makeGrid(LinearLayout body, List<Movie> movies) {
        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(5);
        grid.setClipChildren(false);
        grid.setClipToPadding(false);
        grid.setPadding(0, d(8), 0, d(15));
        for (Movie m : movies) {
            View card = posterCard(m, d(155), d(201));
            GridLayout.LayoutParams p = new GridLayout.LayoutParams();
            p.width = d(157); p.height = d(267);
            p.setMargins(d(3), d(5), d(11), d(12));
            grid.addView(card, p);
        }
        body.addView(grid, lp(-1, -2));
    }

    private void header(LinearLayout body, String title, String sub) {
        TextView tiny = Ui.text(this, "✦  AETHER CINEMA / EXPLORE", 11, Ui.AQUA, true);
        body.addView(tiny, lp(-1, d(27)));
        TextView heading = Ui.text(this, title, 30, Ui.WHITE, true);
        body.addView(heading, lp(-1, d(49)));
        TextView subtitle = Ui.text(this, sub, 14, Ui.MUTED, false);
        LinearLayout.LayoutParams p = lp(-1, d(28)); p.bottomMargin = d(15);
        body.addView(subtitle, p);
    }
    private void empty(LinearLayout body, String title, String description) {
        TextView t = Ui.text(this, title, 24, Ui.WHITE, true);
        LinearLayout.LayoutParams p = lp(-1, d(55)); p.topMargin = d(60);
        body.addView(t, p);
        body.addView(Ui.text(this, description, 14, Ui.MUTED, false), lp(-1, d(40)));
    }

    private void showDetail(Movie m) {
        if (!"detail".equals(screen)) previousScreen = screen;
        screen = "detail";
        selected = m;
        buildMenu();
        ScrollView s = scroll();
        LinearLayout page = vertical();
        page.setPadding(d(25), d(22), d(20), d(30));
        s.addView(page);
        FrameLayout banner = new FrameLayout(this);
        banner.setClipToOutline(true);
        banner.setBackground(Ui.shape(this, Ui.PANEL, 16));
        ImageView art = new ImageView(this);
        Images.into(this, art, m.backdrop, "hero_cinema");
        banner.addView(art, new FrameLayout.LayoutParams(-1, -1));
        View tint = new View(this);
        tint.setBackground(Ui.gradient(this, new int[]{0xf309121d, 0xdc081421, 0x89070e18, 0x22070e18},
                GradientDrawable.Orientation.LEFT_RIGHT, 0));
        banner.addView(tint, new FrameLayout.LayoutParams(-1, -1));
        LinearLayout block = vertical();
        block.setGravity(Gravity.CENTER_VERTICAL);
        block.setPadding(d(28), d(18), 0, d(18));
        block.addView(Ui.text(this, "✦   NOW SHOWING ON AETHER", 11, Ui.AQUA, true), lp(-1, d(31)));
        TextView title = Ui.text(this, m.title, 39, Ui.WHITE, true);
        title.setMaxLines(2);
        block.addView(title, lp(-1, -2));
        block.addView(Ui.text(this, m.year + "   •   " + m.type + "   •   " + m.genre
                + "   •   " + m.rating, 13, Ui.MUTED, true), lp(-1, d(37)));
        TextView desc = Ui.text(this, m.summary, 14.5f, Ui.WHITE, false);
        desc.setMaxLines(4);
        block.addView(desc, lp(-1, -2));
        LinearLayout buttons = new LinearLayout(this);
        buttons.setOrientation(LinearLayout.HORIZONTAL);
        buttons.setPadding(0, d(22), 0, 0);
        TextView watch = Ui.button(this, preferences.getInt("progress:" + m.id, 0) > 10000
                ? "▶  LANJUTKAN" : "▶  PUTAR FILM", true);
        watch.setOnClickListener(v -> play(m));
        buttons.addView(watch, lp(-2, d(48)));
        boolean isSaved = bookmarks().contains(m.id);
        TextView save = Ui.button(this, isSaved ? "✓  TERSIMPAN" : "+  DAFTAR SAYA", false);
        LinearLayout.LayoutParams pp = lp(-2, d(48)); pp.leftMargin = d(12);
        buttons.addView(save, pp);
        save.setOnClickListener(v -> {
            updateSaved(m.id);
            save.setText(bookmarks().contains(m.id) ? "✓  TERSIMPAN" : "+  DAFTAR SAYA");
        });
        block.addView(buttons);
        banner.addView(block, new FrameLayout.LayoutParams(d(550), -1, Gravity.START));
        page.addView(banner, lp(-1, d(385)));
        TextView synopsis = Ui.text(this, "TENTANG FILM INI", 18, Ui.WHITE, true);
        LinearLayout.LayoutParams ap = lp(-1, d(43)); ap.topMargin = d(16);
        page.addView(synopsis, ap);
        TextView full = Ui.text(this, m.summary + "\n\n" + m.type + "  •  " + m.genre
                + (m.runtime.isEmpty() ? "" : "  •  " + m.runtime), 14, Ui.MUTED, false);
        page.addView(full, lp(-1, -2));
        List<Movie> other = repository.snapshot();
        for (int i = other.size() - 1; i >= 0; i--) {
            if (other.get(i).id.equals(m.id)) other.remove(i);
        }
        if (!other.isEmpty()) {
            View gap = new View(this); page.addView(gap, lp(-1, d(30)));
            addRow(page, "JELAJAHI JUGA", "Judul lain dari katalog", other);
        }
        setPage(s);
    }

    private void play(Movie m) {
        String u = m.videoUrl;
        if (!u.startsWith("https://") && !u.startsWith("http://")) {
            Toast.makeText(this, "URL video belum diisi (http/https). Edit catalog.json.", Toast.LENGTH_LONG).show();
            return;
        }
        Intent i = new Intent(this, PlayerActivity.class);
        i.putExtra("id", m.id);
        i.putExtra("title", m.title);
        i.putExtra("url", m.videoUrl);
        startActivity(i);
    }

    private Set<String> bookmarks() {
        return new HashSet<>(preferences.getStringSet("saved", new HashSet<>()));
    }
    private void updateSaved(String id) {
        Set<String> ids = bookmarks();
        if (ids.contains(id)) ids.remove(id); else ids.add(id);
        preferences.edit().putStringSet("saved", ids).apply();
    }

    private void showSettings() {
        screen = "settings";
        ScrollView s = scroll();
        LinearLayout body = vertical();
        body.setPadding(d(30), d(34), d(27), d(30));
        s.addView(body);
        header(body, "PENGATURAN", "Kelola katalog video pribadi di Google TV");
        TextView h = Ui.text(this, "SUMBER KATALOG (JSON)", 18, Ui.WHITE, true);
        LinearLayout.LayoutParams hp = lp(-1, d(40)); hp.topMargin = d(6);
        body.addView(h, hp);
        TextView help = Ui.text(this,
                "Isi URL catalog.json dari server HTTPS atau LAN. Kosongkan untuk memakai katalog demo.",
                14, Ui.MUTED, false);
        body.addView(help, lp(-1, d(55)));
        EditText url = new EditText(this);
        url.setSingleLine(true);
        url.setTextSize(16);
        url.setTextColor(Ui.WHITE);
        url.setHintTextColor(Ui.MUTED);
        url.setPadding(d(16), d(8), d(16), d(8));
        url.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_URI);
        url.setText(preferences.getString("catalog_url", ""));
        url.setHint("https://server-anda.com/catalog.json");
        url.setBackground(Ui.outlined(this, Ui.PANEL, 0xff41586f, 10, 1));
        body.addView(url, lp(-1, d(58)));
        LinearLayout actions = new LinearLayout(this);
        LinearLayout.LayoutParams ac = lp(-1, d(52)); ac.topMargin = d(17);
        body.addView(actions, ac);
        TextView save = Ui.button(this, "↻  SIMPAN & MUAT", true);
        actions.addView(save, lp(-2, d(49)));
        TextView reset = Ui.button(this, "PULIHKAN DEMO", false);
        LinearLayout.LayoutParams rp = lp(-2, d(49)); rp.leftMargin = d(12);
        actions.addView(reset, rp);
        TextView status = Ui.text(this, "Status: katalog lokal siap digunakan", 13, Ui.MUTED, false);
        LinearLayout.LayoutParams st = lp(-1, d(55)); st.topMargin = d(10);
        body.addView(status, st);
        save.setOnClickListener(v -> {
            String value = url.getText().toString().trim();
            if (value.isEmpty()) {
                preferences.edit().remove("catalog_url").apply();
                repository.resetDemo((ok, message) -> {
                    status.setText(message);
                    if (ok) Toast.makeText(this, "Katalog demo aktif", Toast.LENGTH_SHORT).show();
                });
                return;
            }
            status.setText("Memeriksa URL katalog...");
            repository.loadRemote(value, (ok, message) -> {
                status.setText((ok ? "✓ " : "✕ ") + message);
                if (ok) preferences.edit().putString("catalog_url", value).apply();
            });
        });
        reset.setOnClickListener(v -> {
            preferences.edit().remove("catalog_url").apply();
            url.setText("");
            repository.resetDemo((ok, message) -> status.setText(message));
        });
        View spacer = new View(this); body.addView(spacer, lp(1, d(26)));
        body.addView(Ui.text(this, "PETUNJUK", 18, Ui.WHITE, true), lp(-1, d(44)));
        TextView guidance = Ui.text(this,
                "• Berkas contoh berada di app/src/main/assets/catalog.json.\n" +
                "• Edit judul, kategori, poster, backdrop dan videoUrl sesuka kamu.\n" +
                "• Gambar lokal: tulis nama drawable tanpa ekstensi; gambar online: URL http(s).\n" +
                "• Film dari NAS: sediakan URL HTTP/HTTPS ke file MP4/HLS yang didukung Google TV.\n" +
                "• Favorit dan posisi tontonan tersimpan di perangkat ini.\n" +
                "• Mendukung DPAD remote; gunakan tombol BACK untuk kembali.",
                14, Ui.MUTED, false);
        guidance.setLineSpacing(d(8), 1f);
        body.addView(guidance, lp(-1, -2));
        setPage(s);
    }

    private int d(float dp) { return Ui.dp(this, dp); }
    private LinearLayout.LayoutParams lp(int w, int h) { return new LinearLayout.LayoutParams(w, h); }

    @Override public void onBackPressed() {
        if ("detail".equals(screen)) navigate(previousScreen);
        else if (!"home".equals(screen)) navigate("home");
        else super.onBackPressed();
    }
}
