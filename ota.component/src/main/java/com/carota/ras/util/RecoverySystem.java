/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.ras.util;

import android.content.Context;
import android.os.PowerManager;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Locale;

/**
 * RecoverySystem contains methods for interacting with the Android
 * recovery system (the separate partition that can be used to install
 * system updates, wipe user data, etc.)
 */
public class RecoverySystem {
    private static final String TAG = "RecoverySystem";

    /**
     * Default location of zip file containing public keys (X509
     * certs) authorized to sign OTA updates.
     */
    private static final File DEFAULT_KEYSTORE =
            new File("/system/etc/security/otacerts.zip");

    /**
     * Send progress to listeners no more often than this (in ms).
     */
    private static final long PUBLISH_PROGRESS_INTERVAL_MS = 500;

    /**
     * Used to communicate with recovery.  See bootable/recovery/recovery.c.
     */
    private static File RECOVERY_DIR = new File("/cache/recovery");
    private static File COMMAND_FILE = new File(RECOVERY_DIR, "command");
    private static File UNCRYPT_FILE = new File(RECOVERY_DIR, "uncrypt_file");
    private static File LOG_FILE = new File(RECOVERY_DIR, "log");
    private static String LAST_PREFIX = "last_";

    // Length limits for reading files.
    private static int LOG_FILE_MAX_LENGTH = 64 * 1024;

    /**
     * Interface definition for a callback to be invoked regularly as
     * verification proceeds.
     */
    public interface ProgressListener {
        /**
         * Called periodically as the verification progresses.
         *
         * @param progress the approximate percentage of the
         *                 verification that has been completed, ranging from 0
         *                 to 100 (inclusive).
         */
        public void onProgress(int progress);
    }


    /**
     * Reboots the device in order to install the given update
     * package.
     * Requires the {@link android.Manifest.permission#REBOOT} permission.
     *
     * @param context     the Context to use
     * @param packageFile the update package to install.  Must be on
     *                    a partition mountable by recovery.  (The set of partitions
     *                    known to recovery may vary from device to device.  Generally,
     *                    /cache and /data are safe.)
     * @throws IOException if writing the recovery command file
     *                     fails, or if the reboot itself fails.
     */
    public static void installPackage(Context context, File packageFile)
            throws IOException {
        String filename = packageFile.getCanonicalPath();
        Log.w(TAG, "!!! REBOOTING TO INSTALL " + filename + " !!!");

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            FileWriter uncryptFile = new FileWriter(UNCRYPT_FILE);
            try {
                uncryptFile.write(filename + "\n");
            } finally {
                uncryptFile.close();
            }


            if (filename.startsWith("/data/")) {
                filename = "@/cache/recovery/block.map";
            }
        }

        final String filenameArg = "--update_rock_package=" + filename;
        final String localeArg = "--locale=" + Locale.getDefault().toString();
        final String filenameArg2 = "--update_package=" + filename;
        bootCommand(context, filenameArg, localeArg, filenameArg2);
    }

    /**
     * Reboot into the recovery system with the supplied argument.
     *
     * @param args to pass to the recovery utility.
     * @throws IOException if something goes wrong.
     */
    private static void bootCommand(Context context, String... args) throws IOException {
        RECOVERY_DIR.mkdirs();  // In case we need it
        COMMAND_FILE.delete();  // In case it's not writable
        LOG_FILE.delete();

        FileWriter command = new FileWriter(COMMAND_FILE);
        try {
            for (String arg : args) {
                if (!TextUtils.isEmpty(arg)) {
                    command.write(arg);
                    command.write("\n");
                }
            }
        } finally {
            command.close();
        }

        // Having written the command file, go ahead and reboot
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        pm.reboot("recovery");

        throw new IOException("Reboot failed (no permissions?)");
    }

    /**
     * Internally, recovery treats each line of the command file as a separate
     * argv, so we only need to protect against newlines and nulls.
     */
    private static String sanitizeArg(String arg) {
        arg = arg.replace('\0', '?');
        arg = arg.replace('\n', '?');
        return arg;
    }

    private void RecoverySystem() {
    }  // Do not instantiate
}
