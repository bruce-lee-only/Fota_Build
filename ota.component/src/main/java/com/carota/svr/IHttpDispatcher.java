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

import fi.iki.elonen.NanoHTTPD;

public interface IHttpDispatcher {

    HttpResp process(NanoHTTPD.IHTTPSession session, byte[] body) throws Exception;
    void setHandler(String host, String path, IHttpHandler handler);
    void removeAllHandlers();
}
