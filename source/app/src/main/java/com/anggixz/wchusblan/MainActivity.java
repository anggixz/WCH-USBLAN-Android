package com.anggixz.wchusblan;

import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Window;
import android.widget.Toast;

import cn.wch.ch397driver.CH397Activity;
import cn.wch.ch397driver.CH397Activity;
import cn.wch.ch397driver.CH397NetInfo;
import cn.wch.ch397driver.manager.WCHUSBNICManager;
import com.anggixz.wchusblan.ui.NetInfoDialog;
import com.anggixz.wchusblan.util.LogUtil;
import cn.wch.ch397driver.exception.CH397RuntimeException;
import cn.wch.ch397driver.listener.DeviceStateEventListener;
import com.anggixz.wchusblan.databinding.ActivityMainBinding;

public class MainActivity extends CH397Activity {
    private ActivityMainBinding binding;

    private Context context;
    private boolean initResult = false;
    private boolean isDriverRunning = false;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());
        context = this;
        initToolbar();
        initCH397Driver();
        setViewClickListener();
        initCredit();
    }

    private void initCredit() {
        String html = "Dibuat oleh <a href=\"https://github.com/anggixz\"><font color=\"#2563EB\"><b>Anggixz</b></font></a>";
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            binding.tvCredit.setText(android.text.Html.fromHtml(html, android.text.Html.FROM_HTML_MODE_LEGACY));
        } else {
            binding.tvCredit.setText(android.text.Html.fromHtml(html));
        }
        binding.tvCredit.setMovementMethod(android.text.method.LinkMovementMethod.getInstance());
        binding.tvCredit.setOnClickListener(v -> {
            try {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://github.com/anggixz"));
                startActivity(browserIntent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
    @Override
    public void onResume() {
        super.onResume();
        try {
            if (!isDriverRunning && WCHUSBNICManager.getInstance().isSupportDriver()){
                WCHUSBNICManager.getInstance().startDriverService();
            }
        } catch (CH397RuntimeException e) {
            showToast("Gagal memulai driver: " + e.getErrCode());
        }
    }


    private void initToolbar() {
        binding.mainToolbar.setTitle(R.string.app_name);
    }

    // Inisialisasi CH397Driver
    private void initCH397Driver() {
        try {
            WCHUSBNICManager.getInstance().setDebugLogEnable(true);
            initResult = WCHUSBNICManager.getInstance().init(MyApplication.getContext(),deviceStateEventListener);
        } catch (CH397RuntimeException e) {
            showToast("Kesalahan inisialisasi: " + e.getErrCode());

        }
    }



    // Pengaturan listener klik
    private void setViewClickListener() {
        // Tombol Mulai/Berhenti
        binding.startTunServiceBtn.setOnClickListener(view -> {
            if (initResult){
                if(binding.startTunServiceBtn.getText().equals(getString(R.string.start))){
                    try {
                        WCHUSBNICManager.getInstance().startDriverService();
                    } catch (CH397RuntimeException e) {
                        showToast("Gagal memulai driver: " + e.getErrCode());
                    }
                }else {
                    try {
                        WCHUSBNICManager.getInstance().stopDriverService();
                    } catch (CH397RuntimeException e) {
                        showToast("Gagal menghentikan driver: " + e.getErrCode());
                    }
                }
            }
        });

        //获取网络信息
        binding.getNetInfoLin.setOnClickListener(v -> {
            NetInfoDialog netInfoDialog = NetInfoDialog.newInstance(MainActivity.this);
            netInfoDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            netInfoDialog.setCancelable(false);
            netInfoDialog.show();

        });
        //上网设置
        binding.setNetLin.setOnClickListener(v -> {
            Intent intent = new Intent((Context) MainActivity.this, APPBlockActivity.class);
            startActivity(intent);
        });
    }


    private final DeviceStateEventListener deviceStateEventListener = new DeviceStateEventListener() {
        @Override
        public void onDriverServiceState(boolean b) {
            isDriverRunning = b;
            // Driver state
            if (b){
                LogUtil.d("Driver aktif");
            }else {
                LogUtil.d("Driver nonaktif");
            }
            updateDriverStatus(b);
        }



        @Override
        public void onCH397Attached(UsbDevice device) {
            //此处代表驱动启动了一个CH397系列设备，不是回调所有设备
            updateDevInfo();
        }

        @Override
        public void onCH397Detached(UsbDevice device) {
            //此处代表启动的CH397系列设备拔出，不是回调所有设备
            updateDevInfo();

        }

        @Override
        public void onCH397PermissionResult(UsbDevice usbDevice, boolean result) {
            //此处回调驱动启动的CH397系列设备时，获取权限状态，不是回调所有设备

        }

        @Override
        public void onUpdateNetInfo() {
            //网卡网络数据有变化
            updateDevInfo();

        }

        @Override
        public void onCH397LinkState(boolean b) {
            //网线状态
            updateDevInfo();
        }

        @Override
        public void onError(int code, String message) {
            //其他错误
            if (code == 0){
                showToast(message);
            }else {
                showToast("Error:"+message);
            }
        }
    };


    // Update info status driver
    private void updateDriverStatus(boolean state){
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if (state){
                    binding.startTunServiceBtn.setText(R.string.stop);
                    binding.driverStatusText.setTextColor(ContextCompat.getColor(context, R.color.green));
                    binding.driverStatusText.setText("AKTIF");
                }else {
                    binding.startTunServiceBtn.setText(R.string.start);
                    binding.driverStatusText.setTextColor(ContextCompat.getColor(context, R.color.text_secondary));
                    binding.driverStatusText.setText("NONAKTIF");
                }
            }
        });
    }

    // Update info perangkat
    public void updateDevInfo() {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                CH397NetInfo ch397NetInfo = null;
                try {
                    ch397NetInfo = WCHUSBNICManager.getInstance().getNetInfo();
                } catch (CH397RuntimeException e) {
                    return;
                }
                if (ch397NetInfo != null) {
                    if(ch397NetInfo.isOpen()){
                        binding.devStatusText.setTextColor(ContextCompat.getColor(context,R.color.green));
                        binding.devStatusText.setText("Terhubung");
                        if (ch397NetInfo.isLinkState()){
                            binding.linkStatusText.setTextColor(ContextCompat.getColor(context,R.color.green));

                            binding.linkStatusText.setText("Terhubung");
                        }else {
                            binding.linkStatusText.setTextColor(ContextCompat.getColor(context,R.color.text_secondary));
                            binding.linkStatusText.setText("Tidak Terhubung");
                        }
                        if (ch397NetInfo.isLinkState() && (!ch397NetInfo.getIpAddr().equals("0.0.0.0")) ){
                            binding.netStatusText.setTextColor(ContextCompat.getColor(context,R.color.green));
                            binding.netStatusText.setText("Terhubung");
                        }else {
                            binding.netStatusText.setTextColor(ContextCompat.getColor(context,R.color.text_secondary));
                            binding.netStatusText.setText("Tidak Terhubung");
                        }
                    }else {
                        binding.devStatusText.setTextColor(ContextCompat.getColor(context,R.color.text_secondary));
                        binding.devStatusText.setText("Tidak Terhubung");
                        binding.linkStatusText.setTextColor(ContextCompat.getColor(context,R.color.text_secondary));
                        binding.linkStatusText.setText("Tidak Terhubung");
                        binding.netStatusText.setTextColor(ContextCompat.getColor(context,R.color.text_secondary));
                        binding.netStatusText.setText("Tidak Terhubung");
                    }
                }
            }
        });

    }


    //显示提示信息
    private void showToast(String message) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText((Context) MainActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            WCHUSBNICManager.getInstance().deInit();
        } catch (CH397RuntimeException e) {
           e.printStackTrace();
        }
    }
}