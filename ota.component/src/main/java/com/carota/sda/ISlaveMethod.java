/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.sda;

import android.content.Context;
import android.os.Bundle;

import com.carota.agent.IRemoteAgent;

import java.io.File;

public interface ISlaveMethod {
    Bundle readInfo(Context context, IRemoteAgent agent, String ecuName, Bundle bomInfo);
    Bundle queryStatus(Context context, IRemoteAgent agent, String ecuName);
    boolean startUpgrade(Context context, File path, IRemoteAgent agent, Bundle data);
    boolean finishUpgrade(Bundle data, IRemoteAgent agent);
}
