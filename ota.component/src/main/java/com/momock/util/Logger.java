/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/
package com.momock.util;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.Process;
import android.text.TextUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Logger {
    public static final int LEVEL_ALL = 0;

    public static final int LEVEL_DEBUG = 3;

    public static final int LEVEL_INFO = 4;

    public static final int LEVEL_WARN = 5;

    public static final int LEVEL_ERROR = 6;

    public static final int LEVEL_NONE = 7;

    private static PrintStream logStream = null;
    private static String logName = "Record";
    private static int logLevel = LEVEL_DEBUG;
    private static boolean enabled = true;
    private static File logDir = null;
    private static String logFileName = null;

    private static void writeLogFile(String msg) {
        if (null != logStream) {
            logStream.println(msg);
            logStream.flush();
//        } else {
//            System.out.println(msg);
//            System.out.flush();
        }
    }

    private static class LogFile {

        private final int mIndex;
        private final File mTarget;

        private LogFile(File target, int index) {
            mTarget = target;
            mIndex = index;
        }

        private LogFile(File dir, String name, int index) {
            this(new File(dir, name), index);
        }

        public int getIndex() {
            return mIndex;
        }

        public String getName() {
            return mTarget.getName();
        }

        public boolean isExist() {
            return mTarget.exists();
        }

        public boolean delete() {
            return mTarget.delete();
        }

        public static List<LogFile> createSortedList(File dir, String[] files) {
            List<LogFile> allLogs = new ArrayList<LogFile>();
            Pattern p = Pattern.compile("(?<=\\[)\\d+(?=T)");

            for (int i = 0; files != null && i < files.length; i++) {
                allLogs.add(new LogFile(dir, files[i], parseIndexFromName(files[i], p)));
            }

            Collections.sort(allLogs, new Comparator<LogFile>() {
                @Override
                public int compare(LogFile o1, LogFile o2) {
                    if (o1.getIndex() < o2.getIndex()) {
                        return -1;
                    } else if (o1.getIndex() > o2.getIndex()) {
                        return 1;
                    }
                    return 0;
                }
            });
            return allLogs;
        }

        public static int parseIndexFromName(String fileName, Pattern p) {
            int index = -1;
            if (p != null) {
                Matcher matcher = p.matcher(fileName);
                if (matcher.find()) {
                    index = Integer.parseInt(matcher.group(0));
                }
            }
            return index;
        }

    }

    private static int deleteTooMuchLogFile(List<LogFile> sortedFileList, int maxLogFiles) {
        int index = 1;
        if (sortedFileList.size() > 0) {
            index = sortedFileList.get(sortedFileList.size() - 1).getIndex();
            if (index > 0 && index < (Integer.MAX_VALUE >> 1)) {
                for (int i = 0; i < sortedFileList.size() - maxLogFiles + 1; i++) {
                    sortedFileList.get(i).delete();
                }
                index++;
            } else {
                for(LogFile lf : sortedFileList) {
                    lf.delete();
                }
                index = 1;
            }
        }
        return index;
    }

    private static List<LogFile> findAllLogFiles(File logDir,
                                                 final String logStartWith,
                                                 final String logEndsWith) {
        String[] fs = logDir.list(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String filename) {
                return filename.startsWith(logStartWith)
                        && filename.endsWith(logEndsWith);
            }

        });

        return LogFile.createSortedList(logDir, fs);
    }

    private static String getCurrentProcessName(Context context) {
        int pid = android.os.Process.myPid();
        String processName = "";
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if(null != manager) {
            for (ActivityManager.RunningAppProcessInfo process : manager.getRunningAppProcesses()) {
                if (process.pid == pid) {
                    processName = process.processName.replace(":", "$");
                    break;
                }
            }
        }
        if(TextUtils.isEmpty(processName)) {
            processName = context.getPackageName() + "@E";
        }
        return processName;
    }

    private static PrintStream initLogStream(File dir, String file, int lastIndex) throws FileNotFoundException {
        String logFile = file + "[" + lastIndex + "T"
                + new SimpleDateFormat("yyyy-MMdd-HHmm-ss", Locale.US).format(new Date())
                + "].log";

        return new PrintStream(new File(dir, logFile));
    }

    public static void open(Context context, String name, int maxLogFiles, int level) {
        if (maxLogFiles < 1)
            throw new IllegalArgumentException("maxLogFiles must be greater than 0");
        if (level < 0 || level > 7) throw new IllegalArgumentException("level error");
        if (!enabled) return;
        logName = name;
        logFileName = getCurrentProcessName(context);
        if (null == logStream) {
            try {
                File rootDir = context.getExternalCacheDir();
                if (context.checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Process.myPid(), Process.myUid()) == PackageManager.PERMISSION_GRANTED) {
                    rootDir = Environment.getExternalStorageDirectory();
                }
                logDir = new File(rootDir, logName);
                logDir.mkdirs();

                android.util.Log.d("Logger", logDir.getAbsolutePath());

                List<LogFile> sortedFileList = findAllLogFiles(logDir, logFileName, ".log");
                int nextIndex = deleteTooMuchLogFile(sortedFileList, maxLogFiles);

                logStream = initLogStream(logDir, logFileName, nextIndex);
            } catch (IOException e) {
                android.util.Log.e("Logger", "Fails to create log file!", e);
            }
        }//android.util.Log.isLoggable()
        logLevel = level;
        writeLogFile("========== Logger Begin ==========");
        writeLogFile("@" + logDir + "/" + logFileName);
    }

    public static String getLogDirPath() {
        if (logDir == null) {
            return null;
        } else {
            return logDir.getAbsolutePath();
        }
    }

    public static void close() {
        if (!enabled) return;
        if (logStream == null) return;
        logStream.println("========== Logger End   ==========");
        logStream.close();
        logStream = null;
        logDir = null;
    }

    static String getLog(String level, String msg, Throwable t) {
        StackTraceElement trace = t.getStackTrace()[2];
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.ENGLISH);
        return "[" + level + "] " + sdf.format(new Date()) + " in " + trace.getFileName() + "(" + trace.getLineNumber() + ") >" + msg;
    }

    static String getSourceInfo(Throwable t) {
        StackTraceElement trace = t.getStackTrace()[2];
        return trace.getFileName() + "(" + trace.getLineNumber() + ")";
    }

    static String logout(String tag, int level, String msg, Object... args) {
        if (!enabled || logLevel > level || null == msg) {
            return null;
        }
        String realMsg = args.length > 0 ? String.format(msg, args) : msg;
        Throwable t = new Throwable();
        writeLogFile(getLog(tag, realMsg, t));
        return realMsg + " @ " + getSourceInfo(t);
    }

    public static void debug(String msg, Object... args) {
        String logMsg = logout("DEBUG", LEVEL_DEBUG, msg, args);
        if (null != logMsg) {
            android.util.Log.d(logName, logMsg);
        }
    }

    public static void info(String msg, Object... args) {
        String logMsg = logout("INFO", LEVEL_INFO, msg, args);
        if (null != logMsg) {
            android.util.Log.i(logName, logMsg);
        }
    }

    public static void warn(String msg, Object... args) {
        String logMsg = logout("WARN", LEVEL_WARN, msg, args);
        if (null != logMsg) {
            android.util.Log.w(logName, logMsg);
        }
    }

    public static void error(String msg, Object... args) {
        String logMsg = logout("ERROR", LEVEL_ERROR, msg, args);
        if (null != logMsg) {
            android.util.Log.e(logName, logMsg);
        }
    }

    public static String getStackTrace(Throwable e) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        e.printStackTrace(ps);
        return new String(baos.toByteArray());
    }

    public static void error(Throwable e) {
        if (!enabled || logLevel > LEVEL_ERROR) {
            return;
        }
        error(getStackTrace(e));
    }

    public static void check(boolean condition, String msg, Object... args) {
        if (!condition) {
            String logMsg = logout("ASSERT", LEVEL_ERROR, null == msg ? "" : msg, args);
            if (null != logMsg) {
                android.util.Log.e(logName, logMsg);
                throw new RuntimeException(logMsg);
            }
        }
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean enabled) {
        Logger.enabled = enabled;
    }
}
