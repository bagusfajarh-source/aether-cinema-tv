package com.aether.cinematv;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

final class Ui {
    static final int BG = Color.rgb(7, 10, 19);
    static final int PANEL = Color.rgb(16, 23, 36);
    static final int WHITE = Color.rgb(245, 250, 255);
    static final int MUTED = Color.rgb(153, 171, 188);
    static final int AQUA = Color.rgb(94, 237, 228);
    static final int SUBTLE = Color.rgb(29, 40, 56);
    static final int BLACK = Color.BLACK;
    private Ui() {}

    static int dp(Context c, float d) { return (int) (d * c.getResources().getDisplayMetrics().density + .5f); }
    static GradientDrawable shape(Context c, int fill, float radius) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(fill); g.setCornerRadius(dp(c, radius)); return g;
    }
    static GradientDrawable outlined(Context c, int fill, int border, float radius, float borderWidth) {
        GradientDrawable g = shape(c, fill, radius);
        g.setStroke(dp(c, borderWidth), border); return g;
    }
    static GradientDrawable gradient(Context c, int[] colors, GradientDrawable.Orientation orientation, float radius) {
        GradientDrawable g = new GradientDrawable(orientation, colors);
        g.setCornerRadius(dp(c, radius)); return g;
    }
    static TextView text(Context c, String value, float size, int color, boolean bold) {
        TextView t = new TextView(c);
        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(color);
        t.setIncludeFontPadding(false);
        t.setGravity(Gravity.CENTER_VERTICAL);
        if (bold) t.setTypeface(Typeface.create("sans-serif", Typeface.BOLD));
        return t;
    }
    static TextView button(Context c, String text, boolean primary) {
        TextView b = text(c, text, 14.5f, primary ? BG : WHITE, true);
        b.setGravity(Gravity.CENTER);
        b.setPadding(dp(c, 20), dp(c, 12), dp(c, 20), dp(c, 12));
        b.setBackground(outlined(c, primary ? AQUA : 0xff1f2b3c, primary ? AQUA : 0xff38485e, 10, 1));
        b.setFocusable(true);
        b.setClickable(true);
        b.setOnFocusChangeListener((v, focused) -> {
            b.animate().scaleX(focused ? 1.045f : 1f).scaleY(focused ? 1.045f : 1f).setDuration(140).start();
            b.setBackground(outlined(c, primary ? AQUA : (focused ? 0xff284154 : 0xff1f2b3c),
                    focused ? WHITE : (primary ? AQUA : 0xff38485e), 10, focused ? 2 : 1));
        });
        return b;
    }
    static void focus(View v, Context c) {
        v.setFocusable(true); v.setClickable(true);
        v.setOnFocusChangeListener((view, hasFocus) -> {
            view.animate().scaleX(hasFocus ? 1.05f : 1f).scaleY(hasFocus ? 1.05f : 1f).setDuration(160).start();
            view.setElevation(dp(c, hasFocus ? 15 : 0));
            view.setBackground(outlined(c, PANEL, hasFocus ? AQUA : 0xff243044, 14, hasFocus ? 2 : 1));
        });
    }
}
