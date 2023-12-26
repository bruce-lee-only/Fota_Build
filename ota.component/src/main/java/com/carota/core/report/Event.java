package com.carota.core.report;

public interface Event {
    /*
     *{
     * 		"at": 1676963828,
     * 		"upgrade_type ": 0,
     * 		"event_code": 0,
     * 		"msg": "",
     * 		"result": 0
     * }
     *
     * at : 事件触发时间
     */

    /*
     * upgrade_type:
     * 0:立即升级
     * 1:预约升级
     * 2:自动升级
     * 3:工厂模式
     * 4:静默升级
     */
    interface UpgradeType {
        int DEFAULT = 0;
        int SCHEDULE = 1;
        int AUTO_UPGRADE = 2;
        int FACTORY = 3;
        int SILENT = 4;
    }

    /*
      result:
      0:成功/前置条件满足
      1:失败/前置条不满足
     */
    interface Result {
        int RESULT_SUCCESS = 0;
        int RESULT_FAIL = 1;
    }


    interface EventCode {
        /*
        升级流程事件(0-4999)
        0升级结果
        1"升级任务过期"
        2"进入升级模式失败";
        3"退出升级模式失败";
        4"下三电失败";
        5"恢复三电失败";
         */

        int EVENT_CODE_UPGRADE_RESULT = 0;
        int EVENT_CODE_TASK_TIMEOUT = 1;
        int EVENT_CODE_ENTEROTA_FAIL = 2;
        int EVENT_CODE_EXITOTA_FAIL = 3;
        int EVENT_CODE_CLOSE_EIC_SYSTEM_FAIL = 4;
        int EVENT_CODE_RESUME_EIC_SYSTEM_FAIL = 5;

        /*
        用户事件(6001-)
        6001：设置预约时间
        6002”用户确认”
        6003“用户取消”
        */

        int EVENT_CODE_USER_SETTIME = 6001;
        int EVENT_CODE_USER_AGREE = 6002;
        int EVENT_CODE_USER_CANCLE = 6003;



        /*
        前置条件相关(5000-6000)
        5000 "电源为Ready";
        5001"车速不为0";
        5002"驻车系统未激活";
        5003"档位不为P/N";
        5004"动力电池电量过低";
        5005“电源状态不满足”(ACC)
        5006"车辆未下电";
        5007"车辆正在充电";
        5008"蓄电池电压不足";
        5009"蓄电池电量不足";
        5010"引擎处于工作状态";
        5011“自动启停未关闭”
        5012“本地诊断停止”
        5013”远程诊断停止”
        6000"其他前置条件不满足";
         */

        int EVENT_CODE_PRE_ERROR_READY = 5000;
        int EVENT_CODE_PRE_ERROR_SPEED = 5001;
        int EVENT_CODE_PRE_ERROR_HANDBREAK = 5002;
        int EVENT_CODE_PRE_ERROR_GREA = 5003;
        int EVENT_CODE_PRE_ERROR_SOC = 5004;
        int EVENT_CODE_PRE_POWER_ERROR = 5005;
        int EVENT_CODE_PRE_ERROR_STANDBY = 5006;
        int EVENT_CODE_PRE_ERROR_CHARGING = 5007;
        int EVENT_CODE_PRE_ERROR_BATTERY = 5008;
        int EVENT_CODE_PRE_ERROR_BATTERY_POWER = 5009;
        int EVENT_CODE_PRE_ERROR_ENGINE = 5010;
        int EVENT_CODE_PRE_ERROR_STT = 5011;
        int EVENT_CODE_PRE_ERROR_OBD = 5012;
        int EVENT_CODE_PRE_ERROR_OBD_REMOTE = 5013;

        int EVENT_CODE_PRE_ERROR_OTHER = 6000;
    }


}
