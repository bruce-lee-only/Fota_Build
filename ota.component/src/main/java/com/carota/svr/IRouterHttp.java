/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.svr;

public interface IRouterHttp {
    boolean startServer();
    boolean stopServer();
    void setRequestHandler(String host, String path, IHttpHandler handler);
    int getPort();
    boolean isRunning();
}
