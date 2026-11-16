package com.anggixz.wchusblan.util;

public class FormatUtil {


    public static String bytesToMacString(byte[] bArr) {
        if (bArr == null || bArr.length==0)
            return "";
        StringBuffer sb = new StringBuffer(bArr.length);
        String sTmp;
        for (int i = 0; i < bArr.length; i++) {
            sTmp = Integer.toHexString(0xFF & bArr[i]);
            if (sTmp.length() < 2) {
                sb.append(0);
            }
            sb.append(sTmp.toUpperCase());
            if (i != (bArr.length - 1)){
                sb.append(":");
            }
        }
        return sb.toString();
    }
}
