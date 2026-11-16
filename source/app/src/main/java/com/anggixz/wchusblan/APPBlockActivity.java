package com.anggixz.wchusblan;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.suke.widget.SwitchButton;
import com.touchmcu.ui.DialogUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.wch.ch397driver.exception.CH397RuntimeException;
import cn.wch.ch397driver.manager.WCHUSBNICManager;
import com.anggixz.wchusblan.adapter.APPInfoDevAdapter;
import com.anggixz.wchusblan.bean.AppInfoBean;
import com.anggixz.wchusblan.util.LogUtil;
import com.anggixz.wchusblan.util.SystemUtil;
import com.anggixz.wchusblan.room.FilteredApp;
import com.anggixz.wchusblan.databinding.ActivityAppblockBinding;

public class APPBlockActivity extends AppCompatActivity {

    private ActivityAppblockBinding binding;

    private APPInfoDevAdapter appInfoDevAdapter;
    private List<AppInfoBean> appInfoBeans;

    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable timeoutRunnable;
    private static final int LOADING_TIMEOUT = 30000; // Batas timeout: 30 detik

    private boolean isSwitch = false; //防止点击的太快，频繁切换SwitchButton出错
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAppblockBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().setStatusBarColor(getColor(R.color.color_main));
        }
        initToolbar();
        initView();
        setViewClickListener();
        loading(true,"Memuat daftar aplikasi...");
    }
    private void initToolbar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Pengaturan Jaringan");
            actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back_black); // Icon kembali
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // 按下返回箭头，关闭当前 Activity
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //初始化界面
    private void initView() {
        appInfoBeans = new ArrayList<>();
        appInfoDevAdapter = new APPInfoDevAdapter(appInfoBeans,appCheckCallBack);
        binding.appInfoRecycler.setLayoutManager(new LinearLayoutManager(MyApplication.getContext(),LinearLayoutManager.VERTICAL,false));
        binding.appInfoRecycler.setAdapter(appInfoDevAdapter);
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<AppInfoBean> appInfoBeans1 = SystemUtil.getAllAppInfo(MyApplication.getContext(), true);
                List<FilteredApp> filteredApps = MyApplication.getDb().filteredAppDao().getAll();

                // 使用 Map 提高查找效率
                Map<String, AppInfoBean> appMap = new HashMap<>();
                for (AppInfoBean bean : appInfoBeans1) {
                    appMap.put(bean.package_name, bean);
                }

                // 遍历过滤列表，直接设置选中状态
                for (FilteredApp filteredApp : filteredApps) {
                    AppInfoBean bean = appMap.get(filteredApp.packageName);
                    if (bean != null) {
                        bean.setChecked(true);
                    }
                }

                // 合并数据到主列表
                appInfoBeans.addAll(appInfoBeans1);

                // 回到主线程更新 UI
                runOnUiThread(() -> {
                    updateGlobalRoutingSwitch(filteredApps.isEmpty());
                    appInfoDevAdapter.updateData();
                    loading(false, null);
                });
            }
        }).start();

    }
    private void setViewClickListener(){
        binding.globalRoutingSwitch.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SwitchButton view, boolean isChecked) {
                isSwitch  =false;
                LogUtil.d("globalRoutingSwitch : "+isChecked);
                if (isChecked){
                    //选择了全局路由，需要关闭其他APP选择
                    appInfoDevAdapter.clearSelectionAndDisableClick();
                }
            }
        });

        //保存配置
        binding.saveConfigBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<String> appFiltrateList = appInfoDevAdapter.getSelectedItems();
                if(appFiltrateList.isEmpty() &&
                 !binding.globalRoutingSwitch.isChecked()){
                    showToast("Silakan pilih salah satu konfigurasi");
                }
                // Simpan konfigurasi
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                       MyApplication.getDb().filteredAppDao().deleteAll();
                        try {
                            WCHUSBNICManager.getInstance().stopDriverService();
                            if (appFiltrateList.isEmpty()){
                                if(WCHUSBNICManager.getInstance().setNetworkWhitelist(null)){
                                    showToast("Pengaturan berhasil disimpan, silakan mulai ulang driver");
                                }else {
                                    showToast("Gagal menyimpan pengaturan, silakan mulai ulang driver");
                                }
                                return;
                            }

                            if(!WCHUSBNICManager.getInstance().setNetworkWhitelist(appFiltrateList)){
                                showToast("Gagal menyimpan pengaturan");
                                return;
                            }
                        } catch (CH397RuntimeException e) {
                            showToast("Gagal menyimpan pengaturan");
                            return;
                        }
                       for (String str :appFiltrateList){
                           FilteredApp filteredApp = new FilteredApp();
                           filteredApp.packageName = str;
                           MyApplication.getDb().filteredAppDao().insert(filteredApp);
                       }
                        showToast("Pengaturan berhasil disimpan, silakan mulai ulang driver");
                    }
                }).start();
            }
        });
    }
    //更新全局变量选择
    private void updateGlobalRoutingSwitch(boolean enable){
        isSwitch = true;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (binding.globalRoutingSwitch.isChecked() != enable ){
                    LogUtil.d("update globalRoutingSwitch : "+enable);
                    binding.globalRoutingSwitch.setChecked(enable);
                }
            }
        });
    }

    private final APPInfoDevAdapter.APPCheckCallBack appCheckCallBack = new APPInfoDevAdapter.APPCheckCallBack() {
        @Override
        public void onClick(boolean isChecked) {
            //列表
            if(isChecked && !isSwitch){
                //选择了其他APP ，需要关闭全局路由选择
                updateGlobalRoutingSwitch(false);
            }
        }
    };


    //Loading
    private void loading(boolean enable, String message) {
        if (enable) {
            DialogUtil.getInstance().showLoadingDialog((Activity) APPBlockActivity.this, message);
            // 清除旧任务，防止重复触发
            if (timeoutRunnable != null) {
                handler.removeCallbacks(timeoutRunnable);
            }

            // 设置超时自动关闭逻辑
            timeoutRunnable = new Runnable() {
                @Override
                public void run() {
                    if (!isFinishing() && !isDestroyed()) {
                        DialogUtil.getInstance().hideLoadingDialog();
                        showToast("Waktu proses habis");
                    }
                }
            };

            // 启动超时计时器
            handler.postDelayed(timeoutRunnable, LOADING_TIMEOUT);

        } else {
            DialogUtil.getInstance().hideLoadingDialog();

            // 移除超时回调
            if (timeoutRunnable != null) {
                handler.removeCallbacks(timeoutRunnable);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timeoutRunnable != null) {
            handler.removeCallbacks(timeoutRunnable);
        }
        DialogUtil.getInstance().hideLoadingDialog();
    }


    //显示提示信息
    private void showToast(String message) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText((Context) APPBlockActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

}