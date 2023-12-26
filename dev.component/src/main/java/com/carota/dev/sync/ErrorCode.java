package com.carota.dev.sync;


import com.carota.sync.analytics.AppAnalytics;

public class ErrorCode{

    public ErrorCode() {
    }

    private static class ErrorCodeHolder{
        public static ErrorCode instance = new ErrorCode();
    }

    public static ErrorCode getInstance() {
        return ErrorCodeHolder.instance;
    }

    public void addAppErrCodeBuried(AppAnalytics analy, String usid, int erCode, String msg) {
        if(usid == null) return;
        if(analy == null) return;
        AppErrorCodeEm em = AppErrorCodeEm.getAppErrCodeEmByCode(erCode);
        analy.logAction(usid,em.id, msg != null ? String.format(em.erMsg, msg) : em.erMsg);
    }

    enum AppErrorCodeEm{
        APP_BURIED_CODE_DEFAULT(-1, 99999, "(code -1) 未定义异常"),
        APP_BURIED_CODE_0(0, 99900, "(code 0) 检测到任务"),
        APP_BURIED_CODE_1(1, 99901, "(code 1) 检测更新任务异常"),
        APP_BURIED_CODE_2(2, 99903, "(code 2) 获取pki信息成功"),
        APP_BURIED_CODE_3(3, 99902, "(code 3) 获取pki签名信息达到最大次数3次"),
        APP_BURIED_CODE_4(4, 99904, "(code 4) 下载成功"),
        APP_BURIED_CODE_5(5,99905,"(code 5) 下载失败"),
        APP_BURIED_CODE_6(6, 10100, "(code 6) 弹出升级通知推送框"),
        APP_BURIED_CODE_7(7, 10101,"(code 7) 点击推送框的查看按钮"),
        APP_BURIED_CODE_8(8, 10102, "(code 8) 点击推送框的忽略按钮"),
        APP_BURIED_CODE_9(9,10200,"(code 9) 显示fota app 主页面")
        ;



        private final int id;
        private final int erCode;
        private final String erMsg;

        AppErrorCodeEm(int erCode, int id, String erMsg) {
            this.id = id;
            this.erCode = erCode;
            this.erMsg = erMsg;
        }

        public static AppErrorCodeEm getAppErrCodeEmByCode(int erCode) {
            AppErrorCodeEm[] ems = AppErrorCodeEm.values();
            for(int i = 0; i < ems.length; i++){
                if(ems[i].erCode == erCode)
                    return ems[i];
            }
            return getAppErrCodeEmByCode(-1);
        }
    }


}
