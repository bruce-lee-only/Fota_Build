/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.dtc.util;


import com.carota.dtc.log.data.Piece;
import com.momock.util.FileHelper;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class CsvFileWriter {
    private static final String COLUMN_TIME_TITLE = "Time";
    private static final String COLUMN_TAG_TITLE = "TAG";
    private static final String COLUMN_PID_TITLE = "PID";
    private static final String COLUMN_LEVEL_TITLE = "Level";
    private static final String COLUMN_MESSAGE_TITLE = "Message";
    private static final int BUFFER_SIZE = 8192;

    private FileInputStream fis = null;
    private FileOutputStream fos = null;
    private BufferedOutputStream bos = null;
    private StringBuilder builder = null;
    private File inputFile;
    private File recordFile;
    private String mWorkDir;
    private String mInputPath;

    public CsvFileWriter(String workDir, String inputPath) {
        builder = new StringBuilder(BUFFER_SIZE);
        mWorkDir = workDir;
        mInputPath = inputPath;
        inputFile = new File(inputPath);
        initCsvFileHeader();
    }

    private void initCsvFileHeader() {
        try {
            recordFile = new File(mWorkDir,inputFile.getName() + "_temp");
            if (recordFile.exists()) {
                FileHelper.delete(recordFile);
            }
            fos = new FileOutputStream(recordFile);
            bos = new BufferedOutputStream(fos);
            bos.write(getBOM());
            String header = COLUMN_TIME_TITLE + "," + COLUMN_TAG_TITLE + "," + COLUMN_PID_TITLE + "," + COLUMN_LEVEL_TITLE + "," + COLUMN_MESSAGE_TITLE + "\n";
            bos.write(header.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public InputStream getInputStream() {
        if (fis == null) {
            try {
                fis =  new FileInputStream(inputFile);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        return fis;
    }

    public boolean append(Piece log) {
        if (bos == null) {
            initCsvFileHeader();
        }
        if (bos == null) {
            return false;
        }
        try {
            String piece = log.toString();
            if (builder.toString().getBytes().length + piece.getBytes().length > BUFFER_SIZE) {
                bos.write(builder.toString().getBytes());
                builder.delete(0, builder.length());
            }
            builder.append(piece).append("\n");
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void finish() {
        try {
            if (bos != null) {
                if (builder != null && builder.length() > 0) {
                    bos.write(builder.toString().getBytes());
                }
                bos.close();
            }
            if (fos != null) {
                fos.close();
            }
            if (fis != null) {
                fis.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        if (recordFile != null && recordFile.exists()) {
            String fileName = recordFile.getName();
            fileName = fileName.substring(0, fileName.length() - 5);
            recordFile.renameTo(new File(mWorkDir, fileName + ".csv"));
            recordFile = null;
        }
        inputFile = null;
    }

    public String getInputPath() {
        return mInputPath;
    }

    /**
     * 功能说明：获取UTF-8编码文本文件开头的BOM签名。
     * BOM(Byte Order Mark)，是UTF编码方案里用于标识编码的标准标记。例：接收者收到以EF BB BF开头的字节流，就知道是UTF-8编码。
     * @return UTF-8编码文本文件开头的BOM签名
     */
    public static byte[] getBOM() {
        return new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
    }
}
