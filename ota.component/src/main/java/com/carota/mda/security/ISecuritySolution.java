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

import java.io.File;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;

public interface ISecuritySolution {
    File decryptPackage(AtomicBoolean certified, File file, File outDir, File signFile);
    Boolean decryptPackage(AtomicBoolean certified, String dmHost, String target, File signFile);
}
