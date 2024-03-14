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

import java.util.List;
import java.util.Map;

public interface IHttpHandler {

    HttpResp get(String path, Map<String, List<String>> params, Object extra);

    HttpResp post(String path, Map<String, List<String>> params, byte[] body, Object extra);

    HttpResp other(String path, Map<String, List<String>> params, Object extra);
}
