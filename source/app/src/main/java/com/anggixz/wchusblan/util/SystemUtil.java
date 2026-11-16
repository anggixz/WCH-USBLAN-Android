package com.anggixz.wchusblan.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import java.util.ArrayList;
import java.util.List;

import com.anggixz.wchusblan.bean.AppInfoBean;

public class SystemUtil {


    /**
     * 获取手机已安装应用列表
     * @param ctx
     * @param isFilterSystem 是否过滤系统应用
     * @return
     */
    public static ArrayList<AppInfoBean> getAllAppInfo(Context ctx, boolean isFilterSystem) {
        ArrayList<AppInfoBean> appBeanList = new ArrayList<>();

        PackageManager packageManager = ctx.getPackageManager();
        List<ApplicationInfo> installedApplications = packageManager.getInstalledApplications(0);
        for (ApplicationInfo p : installedApplications) {
            int flags = p.flags;
            // Filter aplikasi sistem murni jika isFilterSystem bernilai true
            if (isFilterSystem && (flags & ApplicationInfo.FLAG_SYSTEM) != 0 && (flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 0) {
                continue;
            }

            // Abaikan paket aplikasi kita sendiri dari daftar blokir
            if (p.packageName.equals(ctx.getPackageName())) {
                continue;
            }

            AppInfoBean bean = new AppInfoBean();
            bean.setPackage_name(p.packageName);
            try {
                CharSequence label = packageManager.getApplicationLabel(p);
                bean.setLabel(label != null ? label.toString() : p.packageName);
            } catch (Exception e) {
                bean.setLabel(p.packageName);
            }
            try {
                bean.setIcon(packageManager.getApplicationIcon(p));
            } catch (Exception e) {
                bean.setIcon(packageManager.getDefaultActivityIcon());
            }

            appBeanList.add(bean);
        }

        // Urutkan daftar aplikasi sesuai abjad nama aplikasi (A-Z)
        java.util.Collections.sort(appBeanList, (o1, o2) -> {
            if (o1.getLabel() == null) return -1;
            if (o2.getLabel() == null) return 1;
            return o1.getLabel().compareToIgnoreCase(o2.getLabel());
        });

        return appBeanList;
    }
}
