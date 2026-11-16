package com.anggixz.wchusblan.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.suke.widget.SwitchButton;

import java.util.ArrayList;
import java.util.List;

import com.anggixz.wchusblan.bean.AppInfoBean;
import com.anggixz.wchusblan.R;

/**
 * APP信息适配器
 */

public class APPInfoDevAdapter extends RecyclerView.Adapter<APPInfoDevAdapter.ViewHolder>  {
    private List<AppInfoBean> appInfoBeans;
    private APPCheckCallBack appCheckCallBack;
    public APPInfoDevAdapter(List<AppInfoBean> list ,APPCheckCallBack appCheckCallBack){
        appInfoBeans = list;
        this.appCheckCallBack = appCheckCallBack;
    }

    @NonNull
    @Override
    public APPInfoDevAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.app_info_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull APPInfoDevAdapter.ViewHolder holder, int position) {
        AppInfoBean appInfoBean = appInfoBeans.get(position);
        holder.appNameText.setText(appInfoBean.getLabel());
        holder.packageNameText.setText(appInfoBean.getPackage_name());
        holder.appIconImage.setImageDrawable(appInfoBean.getIcon());

        holder.appCheck.setOnCheckedChangeListener(null); // 清除之前的监听器
        holder.appCheck.setChecked(appInfoBean.isChecked());
        holder.appCheck.setOnCheckedChangeListener((buttonView, isChecked) -> {
            appInfoBean.setChecked(isChecked);
            if (appCheckCallBack != null){
                appCheckCallBack.onClick(isChecked);
            }
        });
    }

    @Override
    public int getItemCount() {
        return appInfoBeans.size();
    }


    public void updateData(){
        notifyDataSetChanged();
    }

    public void clearSelectionAndDisableClick() {
        for (int i = 0; i < appInfoBeans.size(); i++) {
            AppInfoBean app = appInfoBeans.get(i);
            if (app.isChecked()) {
                app.setChecked(false);
                notifyItemChanged(i); // 只刷新这一项
            }
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView appNameText,packageNameText;
        ImageView appIconImage;
        SwitchButton appCheck;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            appNameText = itemView.findViewById(R.id.app_name_text);
            packageNameText = itemView.findViewById(R.id.package_name_text);
            appIconImage = itemView.findViewById(R.id.app_icon_image);
            appCheck = itemView.findViewById(R.id.app_switch);
        }
    }

    // 获取选中的项
    public List<String> getSelectedItems() {
        List<String> selected = new ArrayList<>();
        for (AppInfoBean item : appInfoBeans) {
            if (item.isChecked()) {
                selected.add(item.getPackage_name());
            }
        }
        return selected;
    }

    public interface APPCheckCallBack{
        void onClick(boolean isChecked);
    }
}
