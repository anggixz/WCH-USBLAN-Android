package com.anggixz.wchusblan.ui;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import cn.wch.ch397driver.CH397NetInfo;
import cn.wch.ch397driver.exception.CH397RuntimeException;
import cn.wch.ch397driver.manager.WCHUSBNICManager;
import com.anggixz.wchusblan.R;
import com.anggixz.wchusblan.databinding.NetInfoDialogBinding;
import com.anggixz.wchusblan.util.FormatUtil;


public class NetInfoDialog extends Dialog {
    private Context context;
    private NetInfoDialogBinding binding;


    public static NetInfoDialog newInstance(Context context) {
        NetInfoDialog dialConfigConfig = new NetInfoDialog(context);
        return dialConfigConfig;
    }

    public NetInfoDialog(Context context ) {
        super(context);
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = NetInfoDialogBinding.inflate(getLayoutInflater());
        setContentView( binding.getRoot());
        if (getWindow() != null) {
            getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        }
        setBtnClickListener();
        initView();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (getWindow() != null) {
            android.util.DisplayMetrics dm = getContext().getResources().getDisplayMetrics();
            int width = (int) (dm.widthPixels * 0.92);
            int height = (int) (dm.heightPixels * 0.78);
            getWindow().setLayout(width, height);
            getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        }
    }



    //控件回调
    private void setBtnClickListener() {
        //退出
        binding.quitBtn.setOnClickListener(v -> {
            dismiss();
        });
    }

    //初始化界面
    private void initView() {
        updateSystemInfo();
        updateNetInfo();
    }


    // Update info perangkat & jaringan
    public void updateNetInfo() {
        CH397NetInfo ch397NetInfo = null;
        try {
            ch397NetInfo = WCHUSBNICManager.getInstance().getNetInfo();
        } catch (CH397RuntimeException e) {
            return;
        }
        if (ch397NetInfo != null) {
            if(ch397NetInfo.isOpen()){
                binding.devStateText.setTextColor(androidx.core.content.ContextCompat.getColor(context, com.anggixz.wchusblan.R.color.green));
                binding.devStateText.setText("Terhubung");
                if (ch397NetInfo.isLinkState()){
                    binding.linkStateText.setTextColor(androidx.core.content.ContextCompat.getColor(context, com.anggixz.wchusblan.R.color.green));
                    binding.linkStateText.setText("Terhubung");
                }else {
                    binding.linkStateText.setTextColor(androidx.core.content.ContextCompat.getColor(context, com.anggixz.wchusblan.R.color.text_secondary));
                    binding.linkStateText.setText("Tidak Terhubung");
                }
                binding.macText.setText(FormatUtil.bytesToMacString(ch397NetInfo.getMacAddr()));
            }else {
                binding.devStateText.setTextColor(androidx.core.content.ContextCompat.getColor(context, com.anggixz.wchusblan.R.color.text_secondary));
                binding.devStateText.setText("Tidak Terhubung");
                binding.linkStateText.setTextColor(androidx.core.content.ContextCompat.getColor(context, com.anggixz.wchusblan.R.color.text_secondary));
                binding.linkStateText.setText("Tidak Terhubung");
                binding.macText.setText("-");
            }

            binding.ipaddrText.setText(ch397NetInfo.getIpAddr());
            binding.netMaskText.setText(ch397NetInfo.getNetMask());
            binding.gatewayText.setText(ch397NetInfo.getGateWay());
            binding.dnsText.setText(ch397NetInfo.getDNS());
        }
    }

    // Update info sistem
    private void updateSystemInfo(){
        String systemVersion = android.os.Build.VERSION.RELEASE;
        if(systemVersion != null){
            binding.systemVersionText.setText("Android "+systemVersion);
        }
        try {
            if (WCHUSBNICManager.getInstance().usbFeatureSupported()){
                binding.supportUsbText.setTextColor(androidx.core.content.ContextCompat.getColor(context, com.anggixz.wchusblan.R.color.green));
                binding.supportUsbText.setText("Didukung");
            }else {
                binding.supportUsbText.setTextColor(androidx.core.content.ContextCompat.getColor(context, com.anggixz.wchusblan.R.color.red));
                binding.supportUsbText.setText("Tidak Didukung");
            }
            if(WCHUSBNICManager.getInstance().isSupportDriver()){
                binding.supportDriverText.setTextColor(androidx.core.content.ContextCompat.getColor(context, com.anggixz.wchusblan.R.color.green));
                binding.supportDriverText.setText("Didukung");
            }else {
                binding.supportDriverText.setTextColor(androidx.core.content.ContextCompat.getColor(context, com.anggixz.wchusblan.R.color.red));
                binding.supportDriverText.setText("Tidak Didukung");

            }
        } catch (CH397RuntimeException e) {
            e.printStackTrace();
        }

    }

}
