package com.makepower.processmanager.job;

import com.makepower.processmanager.bean.MemoryJobEntity;
import com.makepower.processmanager.util.CmdUtils;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author czx
 * @Description 内存监测任务
 * @Version 2019-10-29 14:40
 */
public class MemoryMoniteJob implements Job {
    private static Logger logger = LoggerFactory.getLogger(MemoryMoniteJob.class);
    /**
     * k是要监测内存的程序路径全名，v是MemoryJobEntity实体对象
     */
    public static Map<String, MemoryJobEntity> memoryProcessNames = new ConcurrentHashMap<>();

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        Collection<MemoryJobEntity> memoryJobEntities = memoryProcessNames.values();
        for (Iterator<MemoryJobEntity> iterator = memoryJobEntities.iterator(); iterator.hasNext(); ){
            MemoryJobEntity entry = iterator.next();
            //用cmd指令获取程序所占内存，并且和阈值比较，如果超过阈值就重启程序
            try {
                Long memoryValue = CmdUtils.getProcessMemoryByPath(entry.getExePath());
                if (memoryValue > entry.getThreshold() || memoryValue == -1){
                    //重启程序，重启成功了就给MemoryJobEntity对象的jobCount+1
                    CmdUtils.restartProcessByExePath(entry.getExePath());
                    entry.increaseJobCount();
                    entry.setLastJobTime(new Date());
                }
            } catch (IOException | InterruptedException | ParseException e) {
                logger.error("重启程序：{} 失败", entry.getExePath(), e);
            }
        }
    }
}
