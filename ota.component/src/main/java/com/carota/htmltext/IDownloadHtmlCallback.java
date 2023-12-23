/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/
package com.carota.htmltext;

import java.io.File;

public interface IDownloadHtmlCallback {
    void onSuccess(File file);
    void onError(int errorCode, String msg);
    void onFinish();
}
