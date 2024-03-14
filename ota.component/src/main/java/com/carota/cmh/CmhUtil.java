/*******************************************************************************
 * Copyright (C) 2018-2021 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/
package com.carota.cmh;


import android.text.TextUtils;

import com.carota.core.ScheduleAttribute;
import com.carota.protobuf.ControlMessageHandler;
import com.momock.util.Logger;

import org.json.JSONException;
import org.json.JSONObject;

public class CmhUtil {
    private static final String SCHEDULE_TIME = "time";
    private static final String SCHEDULE_TYPE = "type";
    private static final String SCHEDULE_TID = "tid";
    private static final String TELEMETRY_TOKEN = "token";
    private static final String TELEMETRY_MODE = "mode";
    private static final String ENG_LV = "lv";
    private static final String ENG_FUNC = "func";
    private static final String EVENT_MSG = "msg";
    private static final String EVENT_TID = "tid";
    private static final String EVENT_TIMEOUT = "timeout";
    private static final String ERROR_WAKE_UP_CODE = "code";
    private static final String ERROR_WAKE_UP_DESC = "desc";

    public static final int GROUP_SCHEDULE = 1;
    public static final int GROUP_TELEMETRY = 2;
    public static final int GROUP_ENG = 3;
    public static final int GROUP_EVENT = 4;
    public static final int GROUP_ERROR_WAKE_UP = 101;

    public static final int WEU_CONDITION_NOT_MET = 1;
    public static final int WEU_WAKE_UP_MDA_FAIL = 2;
    public static final int WEU_WAKE_UP_VEHICLE_FAIL = 3;
    public static final int WEU_UPGRADE_CONDITION_NOT_MET = 4;


    public static JSONObject payloadsToJson(ControlMessageHandler.FieldPayload[] fieldPayloads) {
        if (fieldPayloads == null || fieldPayloads.length == 0) {
            return null;
        }

        JSONObject object = new JSONObject();
        try {
            for (int i = 0; i < fieldPayloads.length; i++) {
                String name = fieldPayloads[i].getName();
                if (TextUtils.isEmpty(name)) {
                    continue;
                }

                ControlMessageHandler.FieldPayload.Type type = fieldPayloads[i].getType();
                if (type == ControlMessageHandler.FieldPayload.Type.STRING) {
                    object.put(name, fieldPayloads[i].getValStr());
                } else if (type == ControlMessageHandler.FieldPayload.Type.LONG) {
                    object.put(name, fieldPayloads[i].getValLong());
                } else {//ERROR!
                    continue;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Logger.error(e);
        }

        return object;
    }

    public static ControlMessageHandler.FieldPayload[] jsonToPayloads(int group, JSONObject jsonObject) {
        ControlMessageHandler.FieldPayload[] fieldPayloads = null;
        if (jsonObject == null) {
            return null;
        }
        ControlMessageHandler.FieldPayload.Builder fieldPayload0 = ControlMessageHandler.FieldPayload.newBuilder();
        ControlMessageHandler.FieldPayload.Builder fieldPayload1 = ControlMessageHandler.FieldPayload.newBuilder();
        ControlMessageHandler.FieldPayload.Builder fieldPayload2 = ControlMessageHandler.FieldPayload.newBuilder();
        switch (group) {
            case 1://SCHEDULE
                fieldPayloads = new ControlMessageHandler.FieldPayload[3];
                fieldPayload0.setName(SCHEDULE_TIME);
                fieldPayload0.setTypeValue(1);
                fieldPayload0.setValLong(jsonObject.optLong(SCHEDULE_TIME, -1));
                fieldPayload1.setName(SCHEDULE_TYPE);
                fieldPayload1.setTypeValue(1);
                fieldPayload1.setValLong(jsonObject.optLong(SCHEDULE_TYPE, 0));
                fieldPayload2.setName(SCHEDULE_TID);
                fieldPayload2.setTypeValue(2);
                fieldPayload2.setValStr(jsonObject.optString(SCHEDULE_TID, ""));
                fieldPayloads[0] = fieldPayload0.build();
                fieldPayloads[1] = fieldPayload1.build();
                fieldPayloads[2] = fieldPayload2.build();
                break;
            case 2://TELEMETRY
                fieldPayloads = new ControlMessageHandler.FieldPayload[2];
                fieldPayload0.setName(TELEMETRY_TOKEN);
                fieldPayload0.setTypeValue(2);
                fieldPayload0.setValStr(jsonObject.optString(TELEMETRY_TOKEN, ""));
                fieldPayload1.setName(TELEMETRY_MODE);
                fieldPayload1.setTypeValue(1);
                fieldPayload1.setValLong(jsonObject.optLong(TELEMETRY_MODE, 0));
                fieldPayloads[0] = fieldPayload0.build();
                fieldPayloads[1] = fieldPayload1.build();
                break;
            case 3://ENG
                fieldPayloads = new ControlMessageHandler.FieldPayload[2];
                fieldPayload0.setName(ENG_LV);
                fieldPayload0.setTypeValue(1);
                fieldPayload0.setValLong(jsonObject.optLong(ENG_LV, 0));
                fieldPayload1.setName(ENG_FUNC);
                fieldPayload1.setTypeValue(1);
                fieldPayload1.setValLong(jsonObject.optLong(ENG_FUNC, 0));
                fieldPayloads[0] = fieldPayload0.build();
                fieldPayloads[1] = fieldPayload1.build();
                break;
            case 4://EVENT
                fieldPayloads = new ControlMessageHandler.FieldPayload[3];
                fieldPayload0.setName(EVENT_MSG);
                fieldPayload0.setTypeValue(1);
                fieldPayload0.setValLong(jsonObject.optLong(EVENT_MSG, 0));
                fieldPayload1.setName(EVENT_TID);
                fieldPayload1.setTypeValue(2);
                fieldPayload1.setValStr(jsonObject.optString(EVENT_TID, ""));
                fieldPayload2.setName(EVENT_TIMEOUT);
                fieldPayload2.setTypeValue(1);
                fieldPayload2.setValLong(jsonObject.optLong(EVENT_TIMEOUT, 0));
                fieldPayloads[0] = fieldPayload0.build();
                fieldPayloads[1] = fieldPayload1.build();
                fieldPayloads[2] = fieldPayload2.build();
                break;
            case 101://ERROR_WAKE_UP
                fieldPayloads = new ControlMessageHandler.FieldPayload[2];
                fieldPayload0.setName(ERROR_WAKE_UP_CODE);
                fieldPayload0.setTypeValue(1);
                fieldPayload0.setValLong(jsonObject.optLong(ERROR_WAKE_UP_CODE, 0));
                fieldPayload1.setName(ERROR_WAKE_UP_DESC);
                fieldPayload1.setTypeValue(2);
                fieldPayload1.setValStr(jsonObject.optString(ERROR_WAKE_UP_DESC, ""));
                fieldPayloads[0] = fieldPayload0.build();
                fieldPayloads[1] = fieldPayload1.build();
                break;
            default:
                break;
        }

        return fieldPayloads;
    }

    public static ScheduleAttribute rspToScheduleAttribute(ControlMessageHandler.QueryFieldRsp queryFieldRsp) {
        if (queryFieldRsp == null) {
            return null;
        }
        if (GROUP_SCHEDULE != queryFieldRsp.getGroup()) {
            return null;
        }

        ControlMessageHandler.FieldPayload[] fieldPayloads =
                queryFieldRsp.getValueList().toArray(new ControlMessageHandler.FieldPayload[0]);
        JSONObject jsonObject = payloadsToJson(fieldPayloads);
        if (jsonObject == null) {
            return null;
        }
        ScheduleAttribute scheduleAttribute = new ScheduleAttribute();
        scheduleAttribute.scheduleGroup = GROUP_SCHEDULE;
        scheduleAttribute.scheduleTrack = queryFieldRsp.getTrack();
        scheduleAttribute.scheduleTid = jsonObject.optString(SCHEDULE_TID, "");
        scheduleAttribute.scheduleType = jsonObject.optLong(SCHEDULE_TYPE, 0);
        scheduleAttribute.scheduleTime = jsonObject.optLong(SCHEDULE_TIME, -1);

        return scheduleAttribute;
    }

    public static ControlMessageHandler.SetFieldReq buildScheduleField(String tag, String track, long time, int type, String tid) {
        ControlMessageHandler.SetFieldReq.Builder builder = ControlMessageHandler.SetFieldReq.newBuilder();
        builder.setGroup(GROUP_SCHEDULE);
        builder.setTrack(track);
        builder.setTag(tag);
        ControlMessageHandler.FieldPayload.Builder fieldPayload0 = ControlMessageHandler.FieldPayload.newBuilder();
        fieldPayload0.setName(SCHEDULE_TIME);
        fieldPayload0.setTypeValue(1);
        fieldPayload0.setValLong(time);
        ControlMessageHandler.FieldPayload.Builder fieldPayload1 = ControlMessageHandler.FieldPayload.newBuilder();
        fieldPayload1.setName(SCHEDULE_TYPE);
        fieldPayload1.setTypeValue(1);
        fieldPayload1.setValLong(type);
        ControlMessageHandler.FieldPayload.Builder fieldPayload2 = ControlMessageHandler.FieldPayload.newBuilder();
        fieldPayload2.setName(SCHEDULE_TID);
        fieldPayload2.setTypeValue(2);
        fieldPayload2.setValStr(tid);

        builder.addValue(fieldPayload0.build());
        builder.addValue(fieldPayload1.build());
        builder.addValue(fieldPayload2.build());
        return builder.build();
    }

    public static ControlMessageHandler.SetFieldReq buildErrorWakeUpField(String tag, String track, long code, String desc) {
        ControlMessageHandler.SetFieldReq.Builder builder = ControlMessageHandler.SetFieldReq.newBuilder();
        builder.setGroup(GROUP_ERROR_WAKE_UP);
        builder.setTrack(track);
        builder.setTag(tag);
        ControlMessageHandler.FieldPayload.Builder fieldPayload0 = ControlMessageHandler.FieldPayload.newBuilder();
        fieldPayload0.setName(ERROR_WAKE_UP_CODE);
        fieldPayload0.setTypeValue(1);
        fieldPayload0.setValLong(code);
        ControlMessageHandler.FieldPayload.Builder fieldPayload1 = ControlMessageHandler.FieldPayload.newBuilder();
        fieldPayload1.setName(ERROR_WAKE_UP_DESC);
        fieldPayload1.setTypeValue(2);
        fieldPayload1.setValStr(desc);

        builder.addValue(fieldPayload0.build());
        builder.addValue(fieldPayload1.build());
        return builder.build();
    }

}
