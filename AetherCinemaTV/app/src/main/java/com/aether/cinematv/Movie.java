package com.aether.cinematv;

import org.json.JSONObject;

public final class Movie {
    public final String id;
    public final String title;
    public final String year;
    public final String type;
    public final String genre;
    public final String rating;
    public final String runtime;
    public final String summary;
    public final String poster;
    public final String backdrop;
    public final String videoUrl;
    public final boolean featured;

    public Movie(JSONObject o) {
        id = o.optString("id", "").trim();
        title = o.optString("title", "Tanpa judul");
        year = o.optString("year", "");
        type = o.optString("type", "Film");
        genre = o.optString("genre", "Lainnya");
        rating = o.optString("rating", "Semua umur");
        runtime = o.optString("runtime", "");
        summary = o.optString("summary", "Belum ada deskripsi.");
        poster = o.optString("poster", "poster_default");
        backdrop = o.optString("backdrop", "hero_cinema");
        videoUrl = o.optString("videoUrl", "");
        featured = o.optBoolean("featured", false);
    }
}
