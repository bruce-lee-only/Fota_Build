/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.core;

import com.momock.util.Convert;

public class SystemAttribute {

    public static final int MODE_UNKNOWN = 0;
    public static final int MODE_ON = 1;
    public static final int MODE_OFF = 2;
    public static final int MODE_PHASE = 3;

    public long systemClock;
    public long systemTimer;
    public int modeOTA;
    public int modePWR;
    public int otaState;
    public String otaTask;
    public int otaProgress;

    public static class Configure {
        public enum ConfigType {
            NOTIFY,
            TASK,
            STATE,
            PROGRESS,
            EXTRA,
            VIN,
            USID
        }

        public final ConfigType type;
        private Object data;

        public Configure(ConfigType type, Long data) {
            this.type = type;
            this.data = data;
        }

        public Configure(ConfigType type, String data) {
            this.type = type;
            this.data = data;
        }

        public Configure(ConfigType type, Integer data) {
            this.type = type;
            this.data = data;
        }

        public long get(long def) {
            return Convert.toLong(data, def);
        }

        public String get() {
            return Convert.toString(data);
        }
    }
}
