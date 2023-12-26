/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.mda.download;

public interface IDownloadObserver {
    void onStart(String usid);
    void onDownload(String usid, IDownloadSection target);
    void onDownloading(String usid, IDownloadSection target);
    void onDownloaded(String usid, IDownloadSection target);
    void onError(String usid, IDownloadSection target);
    void onStop(String usid, boolean finished, IDownloadSection target);
}
