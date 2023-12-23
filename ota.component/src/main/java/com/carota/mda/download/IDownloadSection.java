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

public interface IDownloadSection {
    String getName();
    String getDM();
    int getProgress();
    int getIndex();
	int getSpeed();
    int getErrorCode();
}
