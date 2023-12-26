package com.carota.mda.telemetry;

public class FotaState{

    public static class OTA {
        public static final int STATE_CONNECT = 100100;
        public static final int STATE_RECEIVED = 100200;
        public static final int STATE_DOWNLOADING = 100300;
        public static final int STATE_DOWNLOAD_CANCEL = 100400;
        public static final int STATE_DOWNLOAD_FAILURE = 100500;
        public static final int STATE_DOWNLOADED = 100600;
        public static final int STATE_UPGRADE = 100700;
        public static final int STATE_UPDATE_INTERRUPT = 100800;
        public static final int STATE_UPDATE_FAILURE = 100900;
        public static final int STATE_ROLLBACK = 101000;
        public static final int STATE_ROLLBACK_INTERRUPT = 101100;
        public static final int STATE_ROLLBACK_FAILURE = 101200;
        public static final int STATE_ROLLBACK_SUCCESS = 101300;
        public static final int STATE_UPGRADE_SUCCESS = 101400;


        public static class DOWNLOAD {
            public static final int CODE_DOWNLOAD_MAX_RETRY = 100501;
            public static final int CODE_DOWNLOAD_STORAGE = 100502;
            public static final int CODE_DOWNLOAD_VERIFY_MD5 = 100503;
            public static final int CODE_DOWNLOAD_VERIFY_PKI = 100504;
        }

        public static class UPGRADE {
            public static final int CODE_INSTALL_CONDITION = 100701;
            public static final int CODE_INSTALL_VERIFY_MD5 = 100702; //MD5校验成功
            public static final int CODE_INSTALL_VERIFY_PKI = 100703; //PKI校验通过
            public static final int CODE_INSTALL_TRANSFER = 100704;  // 传输包成功
            public static final int CODE_INSTALL_TRIGGER = 100705;  //触发升级成功
        }

        public static class INTERRUPT {
            public static final int CODE_INTERRUPT_CONDITION = 100801; // 升级车辆条件不满足
            public static final int CODE_INTERRUPT_USER_CANCEL = 100802; // 远程立即升级车端用户取消
            public static final int CODE_INTERRUPT_INVALID = 100803; // OTA升
        }

        public static class FAILURE {
            public static final int CODE_INSTALL_CONDITION = 100901;    // 车况检查
            public static final int CODE_INSTALL_VERIFY_MD5 = 100902;   // MD5校验
            public static final int CODE_INSTALL_VERIFY_PKI = 100903;   // PKI验证
            public static final int CODE_INSTALL_TRANSFER = 100904;     // 包传输
            public static final int CODE_INSTALL_TRIGGER = 100905;      // 触发执行
            public static final int CODE_INSTALL_CONFIRM = 100906;      // 确认结果

        }

        public static class ROLLBACK {
            public static final int CODE_INSTALL_CONDITION = 101001;
            public static final int CODE_INSTALL_VERIFY_MD5 = 101002;
            public static final int CODE_INSTALL_VERIFY_PKI = 101003;
            public static final int CODE_INSTALL_TRANSFER = 101004;
            public static final int CODE_INSTALL_TRIGGER = 101005;
        }

        public static class RBFailure {
            public static final int CODE_INSTALL_CONDITION = 101201;
            public static final int CODE_INSTALL_VERIFY_MD5 = 101202;
            public static final int CODE_INSTALL_VERIFY_PKI = 101203;
            public static final int CODE_INSTALL_TRANSFER = 101204;
            public static final int CODE_INSTALL_TRIGGER = 101205;
            public static final int CODE_INSTALL_CONFIRM = 101206;
        }

    }

}
