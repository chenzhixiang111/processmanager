package com.makepower.processmanager.job;

import com.makepower.processmanager.util.CmdUtils;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @Author czx
 * @Description 重启程序的任务
 * @Version 2019-10-28 11:19
 */
public class RestartProcessJob implements Job {
    private static Logger logger = LoggerFactory.getLogger(RestartProcessJob.class);
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        JobDataMap jobDataMap = jobExecutionContext.getJobDetail().getJobDataMap();
        //获取exe文件的全路径
        String exePath = jobDataMap.getString("exePath");
        try {
            CmdUtils.restartProcessByExePath(exePath);
        } catch (IOException | InterruptedException e) {
            logger.error("重启程序{}异常", exePath, e);
        }
    }
}
