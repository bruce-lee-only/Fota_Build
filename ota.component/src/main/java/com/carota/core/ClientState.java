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

public class ClientState {
    public static final int UPGRADE_STATE_IDLE = 0;
    public static final int UPGRADE_STATE_UPGRADE = 1;
    public static final int UPGRADE_STATE_SUCCESS = 2;
    public static final int UPGRADE_STATE_ROLLBACK = 3;
    public static final int UPGRADE_STATE_ERROR = 4;
    public static final int UPGRADE_STATE_FAILURE = 5;

    public static final int DOWNLOAD_STATE_IDLE = 0;
    public static final int DOWNLOAD_STATE_RUNNING = 1;
    public static final int DOWNLOAD_STATE_COMPLETE = 2;
    public static final int DOWNLOAD_STATE_ERROR = 3;
}
