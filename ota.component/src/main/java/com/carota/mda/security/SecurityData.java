/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.mda.security;

import android.util.Base64;

public class SecurityData {

    public final String id;
    public final String type;
    public final String md5;
    public final String url;
    public final long size;

    public SecurityData() {
        this(null, null, null, null, 0);
    }

    public SecurityData(String id, String type, String md5, String url, long size) {
        this.id = id;
        this.type = type;
        this.md5 = md5;
        this.url = url;
        this.size = size;
    }

    public static byte[] decodeCertificate(String cert) {
        return Base64.decode(cert, Base64.DEFAULT);
    }
}
