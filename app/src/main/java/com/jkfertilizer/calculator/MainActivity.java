package com.jkfertilizer.calculator;

import android.app.Activity;
import android.os.Bundle;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import java.text.DecimalFormat;

public class MainActivity extends Activity {
    EditText rate, rupees, grams, kg;
    TextView result, salesLog;
    SharedPreferences prefs;
    DecimalFormat df = new DecimalFormat("0.##");
    AdView adView;

    int dp(float v) { return (int)(v * getResources().getDisplayMetrics().density + 0.5f); }
    TextView text(String s, int size, boolean bold) {
        TextView t = new TextView(this); t.setText(s); t.setTextSize(size);
        t.setTextColor(Color.rgb(35,45,40)); if(bold)t.setTypeface(null, Typeface.BOLD);
        return t;
    }
    LinearLayout column() {
        LinearLayout l = new LinearLayout(this); l.setOrientation(LinearLayout.VERTICAL);
        l.setPadding(dp(18),dp(10),dp(18),dp(10)); return l;
    }
    EditText field(String hint, String value) {
        EditText e = new EditText(this); e.setSingleLine(true); e.setText(value);
        e.setHint(hint); e.setInputType(8194); e.setTextSize(18);
        return e;
    }
    Button button(String label) {
        Button b = new Button(this); b.setText(label); return b;
    }
    double val(EditText e) {
        try { return Double.parseDouble(e.getText().toString().trim()); }
        catch(Exception ex) { return 0; }
    }
    void show(String s) { result.setText(s); }

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        prefs = getSharedPreferences("shop", MODE_PRIVATE);

        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setBackgroundColor(Color.rgb(246,249,246));

        ScrollView scroll = new ScrollView(this);
        LinearLayout root = column();
        root.setPadding(dp(18),dp(22),dp(18),dp(24));

        TextView title = text("🧮 JK Calculator",24,true);
        title.setTextColor(Color.rgb(20,75,170)); root.addView(title);
        root.addView(text("ସାର ଦୋକାନର ଓଜନ ଓ ଦାମ ହିସାବ",15,false));
        root.addView(space());

        root.addView(text("1 KG ସାରର ଦାମ (₹)",16,true));
        rate = field("ଯେପରି 49", prefs.getString("rate","49")); root.addView(rate);

        root.addView(text("ଟଙ୍କା ଅନୁସାରେ କେତେ ଗ୍ରାମ?",18,true));
        rupees = field("ଟଙ୍କା ଲେଖନ୍ତୁ", "10"); root.addView(rupees);
        Button calc1 = button("ଗ୍ରାମ ହିସାବ କରନ୍ତୁ"); root.addView(calc1);
        calc1.setOnClickListener(v -> {
            double r=val(rate), a=val(rupees);
            if(r<=0 || a<0){show("ଦୟାକରି ଠିକ୍ ଦାମ ଓ ଟଙ୍କା ଲେଖନ୍ତୁ");return;}
            prefs.edit().putString("rate",rate.getText().toString()).apply();
            double g=a*1000/r;
            show("ଦେବାକୁ ହେବ\n"+df.format(g)+" ଗ୍ରାମ  ("+df.format(g/1000)+" KG)");
        });

        root.addView(space());
        root.addView(text("ଓଜନ ଅନୁସାରେ ଟଙ୍କା",18,true));
        grams = field("ଗ୍ରାମ ଲେଖନ୍ତୁ","500"); root.addView(grams);
        kg = field("କିଲୋଗ୍ରାମ (ଇଚ୍ଛାଧୀନ)",""); root.addView(kg);
        Button calc2 = button("ଦାମ ବାହାର କରନ୍ତୁ"); root.addView(calc2);
        calc2.setOnClickListener(v -> {
            double r=val(rate), g=val(grams)+val(kg)*1000;
            if(r<=0 || g<0){show("ଦୟାକରି ଠିକ୍ ଓଜନ ଲେଖନ୍ତୁ");return;}
            show(df.format(g)+" ଗ୍ରାମର ଦାମ = ₹"+df.format(g*r/1000));
        });

        root.addView(space());
        root.addView(text("ଆଜିର ବିକ୍ରି ହିସାବ",18,true));
        Button addSale = button("ଏହି ବିକ୍ରିକୁ ହିସାବରେ ଯୋଡନ୍ତୁ"); root.addView(addSale);
        salesLog = text(prefs.getString("sales","କୌଣସି ବିକ୍ରି ଯୋଡା ହୋଇନାହିଁ"),14,false);
        root.addView(salesLog);
        addSale.setOnClickListener(v -> {
            double r=val(rate), a=val(rupees);
            if(r<=0 || a<=0){show("ପ୍ରଥମେ ଟଙ୍କା ଲେଖି ଗ୍ରାମ ହିସାବ କରନ୍ତୁ");return;}
            String old=prefs.getString("sales","");
            String line="₹"+df.format(a)+" — "+df.format(a*1000/r)+" ଗ୍ରାମ";
            String updated=(old.isEmpty() || old.equals("କୌଣସି ବିକ୍ରି ଯୋଡା ହୋଇନାହିଁ")?"":old+"\n")+line;
            prefs.edit().putString("sales",updated).apply(); salesLog.setText(updated);
            show("ବିକ୍ରି ହିସାବରେ ଯୋଡାଗଲା");
        });
        Button clear = button("ବିକ୍ରି ହିସାବ ସଫା କରନ୍ତୁ"); root.addView(clear);
        clear.setOnClickListener(v->{
            prefs.edit().putString("sales","କୌଣସି ବିକ୍ରି ଯୋଡା ହୋଇନାହିଁ").apply();
            salesLog.setText(prefs.getString("sales",""));
        });

        root.addView(space());
        result = text("ଉତ୍ତର ଏଠାରେ ଦେଖାଯିବ",20,true);
        result.setTextColor(Color.rgb(15,120,65)); result.setGravity(Gravity.CENTER);
        result.setPadding(dp(12),dp(18),dp(12),dp(18));
        root.addView(result);

        scroll.addView(root);
        page.addView(scroll, new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f));

        // Banner ad: Google's official test unit is used in debug builds.
        // The real AdMob unit is used only in a release build.
        LinearLayout adContainer = new LinearLayout(this);
        adContainer.setGravity(Gravity.CENTER);
        adContainer.setBackgroundColor(Color.WHITE);
        page.addView(adContainer, new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        setContentView(page);

        MobileAds.initialize(this, initializationStatus -> {});
        adView = new AdView(this);
        adView.setAdSize(AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(
            this, (int)(getResources().getDisplayMetrics().widthPixels /
            getResources().getDisplayMetrics().density)));
        if (BuildConfig.DEBUG) {
            adView.setAdUnitId("ca-app-pub-3940256099942544/9214589741");
        } else {
            adView.setAdUnitId("ca-app-pub-3047891402152601/7124992063");
        }
        adContainer.addView(adView);
        adView.loadAd(new AdRequest.Builder().build());
    }

    @Override protected void onPause() {
        if (adView != null) adView.pause();
        super.onPause();
    }
    @Override protected void onResume() {
        super.onResume();
        if (adView != null) adView.resume();
    }
    @Override protected void onDestroy() {
        if (adView != null) adView.destroy();
        super.onDestroy();
    }
    View space(){ View v=new View(this); v.setLayoutParams(new LinearLayout.LayoutParams(1,dp(14))); return v; }
}
