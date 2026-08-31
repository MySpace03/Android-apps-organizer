package com.autofolder.organizer;

import android.app.Activity;
import android.content.*;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.view.*;
import android.widget.*;
import java.util.*;

public class MainActivity extends Activity {
    static final String PREFS="autofolder";
    ArrayList<AppInfo> apps;
    LinearLayout list;
    TextView status;
    @Override public void onCreate(Bundle b){super.onCreate(b); build();}
    TextView tv(String text,int size){ TextView t=new TextView(this); t.setText(text); t.setTextColor(Color.WHITE); t.setTextSize(size); t.setPadding(0,8,0,8); return t; }
    Button button(String text){ Button b=new Button(this); b.setText(text); return b; }
    void build(){
        LinearLayout root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(28,28,28,20); root.setBackgroundColor(Color.rgb(11,13,18));
        TextView title=tv("AutoFolder",30); title.setTypeface(null,1); root.addView(title);
        TextView sub=tv("Organize your existing iQOO home screen — without becoming a launcher.",16); sub.setTextColor(Color.LTGRAY); root.addView(sub);
        Button access=button("1. Enable AutoFolder Accessibility"); access.setOnClickListener(v->startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))); root.addView(access);
        Button scan=button("2. Scan installed apps"); scan.setOnClickListener(v->scan()); root.addView(scan);
        status=tv("Not scanned yet.",15); status.setTextColor(Color.LTGRAY); root.addView(status);
        Button start=button("3. Start organization"); start.setOnClickListener(v->startOrganization()); root.addView(start);
        Button stop=button("Stop organization"); stop.setOnClickListener(v->getSharedPreferences(PREFS,0).edit().putBoolean("running",false).apply()); root.addView(stop);
        ScrollView sv=new ScrollView(this); list=new LinearLayout(this); list.setOrientation(LinearLayout.VERTICAL); sv.addView(list); root.addView(sv,new LinearLayout.LayoutParams(-1,0,1));
        setContentView(root);
    }
    void scan(){
        apps=CategoryEngine.installedLaunchable(getPackageManager());
        StringBuilder data=new StringBuilder(); list.removeAllViews(); HashMap<String,Integer> counts=new HashMap<>();
        for(AppInfo a:apps){counts.put(a.category,counts.getOrDefault(a.category,0)+1); data.append(a.category).append("|").append(a.packageName).append("|").append(a.label.replace("|"," ")).append("\n");}
        getSharedPreferences(PREFS,0).edit().putString("apps",data.toString()).apply();
        status.setText("Found "+apps.size()+" launchable apps. Review categories below before starting.");
        for(String c:CategoryEngine.ORDER){
            Integer n=counts.get(c);
            if(n!=null) {
                TextView header=tv("\n▾ "+c+" — "+n+" apps",18);
                header.setTypeface(null,1); header.setTextColor(Color.WHITE); list.addView(header);
                for(AppInfo a:apps) if(a.category.equals(c)) {
                    TextView item=tv("    • "+a.label,15);
                    item.setTextColor(Color.LTGRAY); list.addView(item);
                }
            }
        }
    }
    void startOrganization(){
        if(apps==null) scan();
        getSharedPreferences(PREFS,0).edit().putBoolean("running",true).apply();
        Toast.makeText(this,"Organization requested. Returning to your existing iQOO launcher now; AutoFolder will start when the launcher is visible.",Toast.LENGTH_LONG).show();
        Intent home=new Intent(Intent.ACTION_MAIN); home.addCategory(Intent.CATEGORY_HOME); home.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); startActivity(home);
    }
}
