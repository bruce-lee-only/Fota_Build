package com.carota.config.remote;

import com.carota.config.data.ConfigInfo;

import java.io.File;

/*******************************************************************************
 * Copyright (C) 2022-2025 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/
public interface IActionConfig {
    ConfigInfo getConfigInfo(String url, String vin, String md5);
    File downloadConfig(String url, String md5, File fileDir, int maxRetry);
}
