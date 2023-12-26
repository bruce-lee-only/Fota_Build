/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/
package com.carota.sota.remote;

import com.carota.mda.remote.info.EventInfo;
import com.carota.sota.store.UpdateCampaign;

import java.util.Map;

public interface IActionSOTA {
    public static final int RESPONSE_OK = 200;
    public static final int RESPONSE_ERROR = 400;
    public static final int RESPONSE_SERVER_ERROR = 500;

    UpdateCampaign queryAppList(String url, String vin, String brand, String model, Map<String,String> extra);
    boolean sendUpgradeReport(String url, String vin, EventInfo ei);
}
