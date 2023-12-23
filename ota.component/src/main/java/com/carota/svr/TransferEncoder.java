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

import com.momock.util.EncryptHelper;
import com.momock.util.Logger;

public class TransferEncoder {

    private final String mSalt;

    public TransferEncoder(String salt) {
        mSalt = salt;
    }

    public byte[] encodeToByte(byte[] raw, String seed) throws Exception{
        byte[] key = EncryptHelper.encryptHmacMD5(mSalt, seed);
        return EncryptHelper.encryptAES(raw, key, mSalt);
    }

    public byte[] decodeFromByte(byte[] raw, String seed) throws Exception{
        byte[] key = EncryptHelper.encryptHmacMD5(mSalt, seed);
        return EncryptHelper.decryptAES(raw, key, mSalt);
    }
}
