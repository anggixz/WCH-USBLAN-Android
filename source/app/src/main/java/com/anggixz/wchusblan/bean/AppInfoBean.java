package com.anggixz.wchusblan.bean;

import android.graphics.drawable.Drawable;

public class AppInfoBean {

    public int uid;
    public String label;//应用名称
    public String package_name;//应用包名
    public Drawable icon;//应用icon
    private boolean isChecked;//是否选择

    public  AppInfoBean() {
        uid = 0;
        label = "";
        package_name = "";
        icon = null;
        isChecked = false;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getPackage_name() {
        return package_name;
    }

    public void setPackage_name(String package_name) {
        this.package_name = package_name;
    }

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }
}
