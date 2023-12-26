/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.dtc.log.engine;

import com.carota.dtc.log.data.Instruction;
import com.carota.dtc.log.data.LogTask;
import com.carota.dtc.log.data.Piece;
import com.carota.dtc.util.CsvFileWriter;
import com.momock.util.FileHelper;
import com.momock.util.Logger;

import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

public class Processor {

    private String mWorkDir;

    public Processor(String workDir) {
        mWorkDir = workDir;
    }

    public synchronized boolean doProcess(Instruction ist) {
        List<LogTask> taskList = ist.getLogTask();
        for(LogTask task : taskList) {
            doLogTask(task);
        }
        return true;
    }

    private void doLogTask(LogTask task) {
        for (int i = 0; i < task.getCommandCount(); i++) {
            LogTask.Command cmd = task.getCommand(i);
            DataFilter dataFilter = new DataFilter();
            for (int j = 0; j < cmd.getFilterCount(); j++) {
                dataFilter.addRule(cmd.getFilter(j));
            }
            File file = new File(cmd.getPath());
            if (file.exists()) {
                paraseFilePath(file, mWorkDir, task, dataFilter);
            } else {
                Logger.debug("doLogTask %s is empty", cmd.getPath());
            }
        }
    }

    private void paraseFilePath(File file, String workdir, LogTask task, DataFilter dataFilter) {
        Logger.debug("paraseFilePath file = %s / workdir = %s ", file.getPath(), workdir);
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files == null || files.length <=0) {
                Logger.debug("doLogTask %s is empty", file.getPath());
            } else {
                for (File file1 : files) {
                    Logger.debug("paraseFilePath file1 = %b / workdir = %s ", file1.isDirectory(), file1.getPath());
                    if (file1.isDirectory()) {
                        paraseFilePath(file1, workdir + "/" + file1.getName(), task, dataFilter);
                    } else {
                        //异常中断后 检测是否已经生成过需要的csv文件
                        File destFile = new File(workdir, new File(file1.getPath()).getName() + ".csv");
                        Logger.debug("paraseFilePath else %s / exist = %b", destFile.getPath(), destFile.exists());
                        if (destFile.exists()) {
                            Logger.debug("doLogTask %s exist continue", destFile.getName());
                            continue;
                        }
                        //FileHelper.mkdirForFile(new File(workdir, file1.getName()));
                        FileHelper.mkdir(new File(workdir));
                        applyFilter(task.getFormat(), dataFilter, new CsvFileWriter(workdir, file1.getPath()));
                    }
                }
            }
        } else {
            File destFile = new File(workdir, new File(file.getPath()).getName() + ".csv");
            if (destFile.exists()) {
                Logger.debug("doLogTask %s exist continue", destFile.getName());
                return;
            }
            //FileHelper.mkdirForFile(new File(workdir, file.getName()));
            FileHelper.mkdir(new File(workdir));
            applyFilter(task.getFormat(), dataFilter, new CsvFileWriter(workdir, file.getPath()));
        }
    }

    private boolean applyFilter(Map<Integer,String> format, DataFilter df, CsvFileWriter csvFileWriter) {
        Logger.debug("Processor applyFilter start %s", csvFileWriter.getInputPath());
        try (InputStream is = csvFileWriter.getInputStream();
             InputStreamReader isr = new InputStreamReader(is);
             BufferedReader br = new BufferedReader(isr)) {
            String line;
            while ((line = br.readLine()) != null) {
                Piece p = Piece.parse(line, format);
                if (null == p) {
                    continue;
                }
                if (!p.isNeedFormat() || df.screen(p)) {
                    // TODO: record log line
                    csvFileWriter.append(p);
                }
            }
            csvFileWriter.finish();
            // TODO: finished file write
            Logger.debug("Processor applyFilter end %s", csvFileWriter.getInputPath());
            return true;
        } catch (Exception e) {
            Logger.error("Processor applyFilter error %s", e.getMessage());
        }
        Logger.debug("Processor applyFilter error %s", csvFileWriter.getInputPath());
        return false;
    }

}
