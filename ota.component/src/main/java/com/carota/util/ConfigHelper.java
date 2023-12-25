/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.util;

import android.content.Context;

import com.carota.build.Configuration;
import com.carota.build.Hacker;
import com.carota.build.IConfiguration;
import com.carota.build.ParamCMH;
import com.carota.build.ParamDM;
import com.carota.build.ParamAnalytics;
import com.carota.build.ParamDTC;
import com.carota.build.ParamExternalHttpProxy;
import com.carota.build.ParamHtml;
import com.carota.build.ParamHttpProxy;
import com.carota.build.ParamHub;
import com.carota.build.ParamLocal;
import com.carota.build.ParamMDA;
import com.carota.build.ParamRAS;
import com.carota.build.ParamRSM;
import com.carota.build.ParamRoute;
import com.carota.build.ParamSOTA;
import com.carota.build.ParamVSI;
import com.carota.build.ParamVSM;
import com.momock.util.Logger;
import com.momock.util.SystemHelper;

import org.xmlpull.v1.XmlPullParser;

public class ConfigHelper {

    private static IConfiguration sCfg = null;
    private static Hacker sHacker = null;

    public static IConfiguration get(Context context) {
        synchronized (ConfigHelper.class) {
            if(null == sHacker) {
                sHacker = new Hacker(context);
            }
            if(null == sCfg) {
                int resCfg = SystemHelper.getAppMeta(context, "carota.configure", 0);
                Logger.check(resCfg > 0, "CFG Missing meta-data : carota.configure");
                sCfg = ConfigHelper.newBasic(sHacker, context.getResources().getXml(resCfg));
            }
        }
        return sCfg;
    }

    public static IConfiguration newBasic(Hacker hacker, XmlPullParser cfg) {
        ParamLocal paramLocal = new ParamLocal();
        return new Configuration.Builder(hacker.load(paramLocal))
                .setDefaultParser(paramLocal)
                .addNodeParser(new ParamDM())
                .addNodeParser(new ParamMDA())
                .addNodeParser(new ParamHub())
                .addNodeParser(new ParamRAS())
                .addNodeParser(new ParamRoute())
                .addNodeParser(new ParamVSI())
                .addNodeParser(new ParamAnalytics())
                .addNodeParser(new ParamRSM())
                .addNodeParser(new ParamVSM())
                .addNodeParser(new ParamDTC())
                .addNodeParser(new ParamSOTA())
				.addNodeParser(new ParamCMH())
				.addNodeParser(new ParamHtml())
                .addNodeParser(new ParamHttpProxy())
                .addNodeParser(new ParamExternalHttpProxy())
                .setSrc(cfg).build();
    }

    public static void setTestModeEnabled(Context context, boolean enabled) {
        get(context);
        sHacker.setTestModeEnabled(enabled);
    }

    public static boolean isTestModeEnabled(Context context) {
        get(context);
        return sHacker.isTestModeEnabled();
    }

    public void showTestModeHint() {
        sHacker.showTestModeHint();
    }
}
