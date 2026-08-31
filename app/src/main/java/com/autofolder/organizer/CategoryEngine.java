package com.autofolder.organizer;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import java.util.*;

public final class CategoryEngine {
    private CategoryEngine() {}
    public static final String[] ORDER = {"Social Media","Messaging","Shopping","Finance","Entertainment","Work & Study","Travel","Food","Health & Fitness","Tools","Games","Other"};

    public static String category(String label, String pkg) {
        String s = (label + " " + pkg).toLowerCase(Locale.ROOT);
        if (contains(s,"instagram","facebook","snapchat","reddit","threads","twitter","x.com","linkedin","pinterest","quora","sharechat","moj","tiktok")) return "Social Media";
        if (contains(s,"whatsapp","telegram","signal","messenger","discord","skype","slack","teams","sms","messages","chat")) return "Messaging";
        if (contains(s,"amazon","flipkart","myntra","meesho","ajio","nykaa","blinkit","zepto","bigbasket","jiomart","shop")) return "Shopping";
        if (contains(s,"gpay","google pay","phonepe","paytm","cred","bhim","bank","banking","money","finance","groww","zerodha","upstox","icici","hdfc","sbi","axis")) return "Finance";
        if (contains(s,"youtube","netflix","prime video","hotstar","jiohotstar","spotify","music","mx player","sonyliv","zee5","twitch","vlc")) return "Entertainment";
        if (contains(s,"office","word","excel","powerpoint","docs","sheets","drive","notion","zoom","meet","github","outlook","gmail","calendar","classroom","coursera","udemy","linkedin")) return "Work & Study";
        if (contains(s,"uber","ola","maps","google maps","irctc","makemytrip","booking","airbnb","flight","rail","redbus","rapido")) return "Travel";
        if (contains(s,"swiggy","zomato","domino","pizza","food","restaurant","eat")) return "Food";
        if (contains(s,"fit","fitness","health","strava","step","workout","meditation","calm")) return "Health & Fitness";
        if (contains(s,"game","games","pubg","bgmi","free fire","minecraft","candy crush","roblox","clash")) return "Games";
        if (contains(s,"settings","calculator","clock","files","file manager","recorder","camera","contacts","phone","browser","chrome","photos","gallery","weather","security")) return "Tools";
        return "Other";
    }
    private static boolean contains(String s, String... words) { for (String w:words) if (s.contains(w)) return true; return false; }

    public static ArrayList<AppInfo> installedLaunchable(PackageManager pm) {
        ArrayList<AppInfo> out = new ArrayList<>();
        Intent i = new Intent(Intent.ACTION_MAIN); i.addCategory(Intent.CATEGORY_LAUNCHER);
        List<android.content.pm.ResolveInfo> ris = pm.queryIntentActivities(i, PackageManager.MATCH_ALL);
        HashSet<String> seen = new HashSet<>();
        for (android.content.pm.ResolveInfo ri : ris) {
            String pkg = ri.activityInfo.packageName; if (!seen.add(pkg)) continue;
            ApplicationInfo ai; try { ai = pm.getApplicationInfo(pkg,0); } catch(Exception e){continue;}
            CharSequence cs = pm.getApplicationLabel(ai); String label = cs == null ? pkg : cs.toString();
            out.add(new AppInfo(label,pkg,category(label,pkg)));
        }
        Collections.sort(out, Comparator.comparing((AppInfo a)->a.category).thenComparing(a->a.label,String.CASE_INSENSITIVE_ORDER));
        return out;
    }
}
