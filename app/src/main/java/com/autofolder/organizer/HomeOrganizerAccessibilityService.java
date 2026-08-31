package com.autofolder.organizer;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.*;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.*;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;
import java.util.*;

/**
 * Deterministic, user-started automation for the stock iQOO/Vivo home screen.
 * It never becomes the default Home app and never calls uninstall APIs.
 *
 * The stock launcher owns its workspace, so this service operates the same
 * long-press/drag actions a user would perform. Exact UI behavior can vary
 * between Funtouch OS builds; the service is deliberately conservative.
 */
public class HomeOrganizerAccessibilityService extends AccessibilityService {
    private static final String PREFS="autofolder";
    private final Handler h = new Handler(Looper.getMainLooper());
    private ArrayList<AppInfo> apps = new ArrayList<>();
    private String homePackage="";
    private int categoryIndex=0, appIndex=0;
    private boolean busy=false;
    private float targetX, targetY;
    private final String[] categories = CategoryEngine.ORDER;

    @Override public void onServiceConnected(){
        super.onServiceConnected();
        android.view.WindowManager wm=(android.view.WindowManager)getSystemService(WINDOW_SERVICE);
        android.util.DisplayMetrics dm=new android.util.DisplayMetrics(); wm.getDefaultDisplay().getRealMetrics(dm);
        // Safe-ish grid position; user is told to keep this area free on the new/target page.
        targetX=dm.widthPixels*0.50f; targetY=dm.heightPixels*0.62f;
        homePackage=resolveHomePackage();
        loadApps();
        if(getSharedPreferences(PREFS,0).getBoolean("running",false)) scheduleStart();
    }

    private String resolveHomePackage(){
        Intent i=new Intent(Intent.ACTION_MAIN); i.addCategory(Intent.CATEGORY_HOME);
        android.content.pm.ResolveInfo ri=getPackageManager().resolveActivity(i,0);
        return ri==null?"":ri.activityInfo.packageName;
    }
    private void loadApps(){
        String raw=getSharedPreferences(PREFS,0).getString("apps",""); apps.clear();
        for(String line:raw.split("\\n")){ if(line.trim().isEmpty()) continue; String[] p=line.split("\\|",3); if(p.length>=3) apps.add(new AppInfo(p[2],p[1],p[0])); }
    }
    private void scheduleStart(){ h.postDelayed(()->{ if(isRunning()) runNextCategory(); },1500); }
    private boolean isRunning(){return getSharedPreferences(PREFS,0).getBoolean("running",false);}
    private void toast(String s){h.post(()->Toast.makeText(this,s,Toast.LENGTH_LONG).show());}

    @Override public void onAccessibilityEvent(AccessibilityEvent event){
        if(!isRunning() || busy) return;
        if(event.getPackageName()!=null && homePackage.isEmpty()) homePackage=event.getPackageName().toString();
    }
    @Override public void onInterrupt(){ busy=false; }

    private List<AppInfo> categoryApps(String category){
        ArrayList<AppInfo> out=new ArrayList<>(); for(AppInfo a:apps) if(a.category.equals(category)) out.add(a); return out;
    }
    private void runNextCategory(){
        if(!isRunning()){return;}
        while(categoryIndex<categories.length && categoryApps(categories[categoryIndex]).size()<2) categoryIndex++;
        if(categoryIndex>=categories.length){ finish(); return; }
        appIndex=0;
        // Use a free cell on the current home page. The user keeps the existing launcher.
        goHome(()->h.postDelayed(()->{ chooseFreeTarget(); dragAppFromDrawer(categoryApps(categories[categoryIndex]).get(0), true); },500));
    }

    private void dragAppFromDrawer(AppInfo app, boolean first){
        if(!isRunning()) return; busy=true;
        openDrawer();
        h.postDelayed(()->{
            AccessibilityNodeInfo root=getRootInActiveWindow();
            AccessibilityNodeInfo node=findLabel(root,app.label);
            if(node==null){ busy=false; toast("Couldn't find "+app.label+" in the app drawer. Stopping safely."); stopRun(); return; }
            Rect r=new Rect(); node.getBoundsInScreen(r); float sx=r.centerX(), sy=r.centerY();
            performLongDrag(sx,sy,targetX,targetY,()->{
                busy=false; appIndex++;
                h.postDelayed(()->afterDrop(app, first),900);
            });
        },850);
    }

    private void afterDrop(AppInfo app, boolean first){
        List<AppInfo> ca=categoryApps(categories[categoryIndex]);
        if(first){
            // Re-locate the dropped first icon, then put the second app exactly on it.
            AccessibilityNodeInfo root=getRootInActiveWindow(); AccessibilityNodeInfo n=findLabel(root,app.label);
            if(n!=null){ Rect rr=new Rect(); n.getBoundsInScreen(rr); targetX=rr.centerX(); targetY=rr.centerY(); }
            if(ca.size()>1){ dragAppFromDrawer(ca.get(1),false); }
            else { nextCategory(); }
        } else if(appIndex<ca.size()) {
            // Each later app is dropped onto the folder at the same location.
            dragAppFromDrawer(ca.get(appIndex),false);
        } else {
            // Naming is launcher-specific; try to open the folder and set its title when accessible.
            busy=true; goHome(()->h.postDelayed(()->tryNameFolder(categories[categoryIndex],()->{busy=false; nextCategory();}),700));
        }
    }


    private void chooseFreeTarget(){
        android.view.WindowManager wm=(android.view.WindowManager)getSystemService(WINDOW_SERVICE);
        android.util.DisplayMetrics dm=new android.util.DisplayMetrics(); wm.getDefaultDisplay().getRealMetrics(dm);
        AccessibilityNodeInfo root=getRootInActiveWindow();
        float[] xs={dm.widthPixels*.18f,dm.widthPixels*.38f,dm.widthPixels*.58f,dm.widthPixels*.78f};
        float[] ys={dm.heightPixels*.28f,dm.heightPixels*.42f,dm.heightPixels*.56f,dm.heightPixels*.70f};
        for(float y:ys) for(float x:xs){
            if(!occupied(root,x,y)){ targetX=x; targetY=y; return; }
        }
        // If the current page is full, use a new-page-friendly edge position.
        targetX=dm.widthPixels*.50f; targetY=dm.heightPixels*.60f;
    }
    private boolean occupied(AccessibilityNodeInfo root,float x,float y){
        if(root==null)return true; return occupiedDfs(root,x,y);
    }
    private boolean occupiedDfs(AccessibilityNodeInfo n,float x,float y){
        if(n==null)return false; Rect r=new Rect(); n.getBoundsInScreen(r);
        if(n.isVisibleToUser() && r.width()>40 && r.height()>40 && r.contains((int)x,(int)y)) return true;
        for(int i=0;i<n.getChildCount();i++) if(occupiedDfs(n.getChild(i),x,y)) return true;
        return false;
    }

    private void nextCategory(){ categoryIndex++; appIndex=0; h.postDelayed(this::runNextCategory,700); }
    private void finish(){ getSharedPreferences(PREFS,0).edit().putBoolean("running",false).apply(); toast("AutoFolder finished. Apps were only moved as home-screen shortcuts; nothing was uninstalled."); }
    private void stopRun(){ getSharedPreferences(PREFS,0).edit().putBoolean("running",false).apply(); }

    private void goHome(Runnable after){
        performGlobalAction(GLOBAL_ACTION_HOME); h.postDelayed(after,650);
    }
    private void openDrawer(){
        android.view.WindowManager wm=(android.view.WindowManager)getSystemService(WINDOW_SERVICE); android.util.DisplayMetrics dm=new android.util.DisplayMetrics(); wm.getDefaultDisplay().getRealMetrics(dm);
        Path p=new Path(); p.moveTo(dm.widthPixels/2f,dm.heightPixels-80); p.lineTo(dm.widthPixels/2f,dm.heightPixels*0.28f);
        dispatchGesture(new GestureDescription.Builder().addStroke(new GestureDescription.StrokeDescription(p,0,500)).build(),null,null);
    }
    private void performLongDrag(float sx,float sy,float ex,float ey,Runnable done){
        Path p=new Path(); p.moveTo(sx,sy); p.lineTo(ex,ey);
        GestureDescription.StrokeDescription stroke=new GestureDescription.StrokeDescription(p,650,1100);
        dispatchGesture(new GestureDescription.Builder().addStroke(stroke).build(),new GestureResultCallback(){@Override public void onCompleted(GestureDescription g){done.run();}@Override public void onCancelled(GestureDescription g){toast("Launcher rejected a drag; stopping safely.");stopRun();done.run();}},null);
    }
    private AccessibilityNodeInfo findLabel(AccessibilityNodeInfo root,String label){
        if(root==null) return null;
        List<AccessibilityNodeInfo> exact=root.findAccessibilityNodeInfosByText(label);
        if(exact!=null && !exact.isEmpty()) for(AccessibilityNodeInfo n:exact) if(n.isVisibleToUser()) return n;
        // Fallback: case-insensitive containment through a shallow DFS.
        return dfs(root,label.toLowerCase(Locale.ROOT));
    }
    private AccessibilityNodeInfo dfs(AccessibilityNodeInfo n,String wanted){
        if(n==null) return null; CharSequence t=n.getText(); CharSequence d=n.getContentDescription();
        if(n.isVisibleToUser() && ((t!=null&&t.toString().toLowerCase(Locale.ROOT).contains(wanted))||(d!=null&&d.toString().toLowerCase(Locale.ROOT).contains(wanted)))) return n;
        for(int i=0;i<n.getChildCount();i++){AccessibilityNodeInfo r=dfs(n.getChild(i),wanted);if(r!=null)return r;}
        return null;
    }
    private void tryNameFolder(String name,Runnable done){
        // Tap the expected folder location, then look for an editable title field.
        tap(targetX,targetY,()->h.postDelayed(()->{
            AccessibilityNodeInfo root=getRootInActiveWindow(); AccessibilityNodeInfo edit=findEditable(root);
            if(edit!=null){ edit.performAction(AccessibilityNodeInfo.ACTION_FOCUS); android.os.Bundle b=new android.os.Bundle(); b.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,name); edit.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT,b); }
            goHome(done);
        },450));
    }
    private AccessibilityNodeInfo findEditable(AccessibilityNodeInfo n){
        if(n==null)return null; if(n.isEditable()&&n.isVisibleToUser())return n; for(int i=0;i<n.getChildCount();i++){AccessibilityNodeInfo r=findEditable(n.getChild(i));if(r!=null)return r;} return null;
    }
    private void tap(float x,float y,Runnable done){Path p=new Path();p.moveTo(x,y);dispatchGesture(new GestureDescription.Builder().addStroke(new GestureDescription.StrokeDescription(p,0,80)).build(),new GestureResultCallback(){@Override public void onCompleted(GestureDescription g){done.run();}},null);}
}
