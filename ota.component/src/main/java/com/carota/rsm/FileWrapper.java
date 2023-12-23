/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.rsm;


import com.carota.util.MimeType;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicLong;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class FileWrapper extends MimeType {

    public final String CHECKSUM;
    public final String TYPE;
    public final File TARGET;
    public final String DESCRIPTION;

    public FileWrapper(File target, String type, String checksum, String desc) {
        TYPE = type;
        TARGET = target;
        CHECKSUM = checksum;
        DESCRIPTION = desc;
    }

    public InputStream createStream(AtomicLong size) throws IOException {
        size.set(TARGET.length());
        switch (TYPE) {
            case ZIP:
                ZipFile zf = new ZipFile(TARGET);
                ZipEntry ze = zf.getEntry(DESCRIPTION);
                if(null == ze){
                    throw new FileNotFoundException("not found file in the zip");
                }
                size.set(ze.getSize());
                return zf.getInputStream(ze);
            case XOR:
                return new XorInputStream(TARGET, DESCRIPTION);
        }
        return new FileInputStream(TARGET);
    }
}
