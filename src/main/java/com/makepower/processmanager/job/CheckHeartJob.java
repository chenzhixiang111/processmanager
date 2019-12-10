package com.makepower.processmanager.job;

import com.makepower.processmanager.util.CmdUtils;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author czx
 * @Description 检查心跳是否超时任务
 * @Version 2019-10-31 10:44
 */
public class CheckHeartJob implements Job {
    private static Logger logger = LoggerFactory.getLogger(CheckHeartJob.class);
    public static Map<String, Long> heartbeatMap = new ConcurrentHashMap<>();

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        Set<Map.Entry<String, Long>> entrySet = heartbeatMap.entrySet();
        for (Iterator<Map.Entry<String, Long>> iterator = entrySet.iterator(); iterator.hasNext(); ) {
            Map.Entry<String, Long> entry = iterator.next();
            if (System.currentTimeMillis() - entry.getValue() > 30){
                try {
                    CmdUtils.restartProcessByExePath(entry.getKey());
                } catch (IOException | InterruptedException e) {
                    logger.error("重启心跳超时的程序失败", e);
                }
                iterator.remove();
            }
        }
    }
}
