package com.makepower.processmanager.scheduler;

import com.makepower.processmanager.job.SqlserverMoniteJob;
import com.makepower.processmanager.util.QuartzUtil;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * @Author czx
 * @Description sqlserver服务监控组件,在程序启动后开启一个定时任务去监测数据库
 * 可以修改配置文件来设定这个功能的开关
 * @Version 2019-10-14 9:37
 */
@Component
@Slf4j
public class MsSqlServerMonitor implements CommandLineRunner {
    @Value("${start-sqlserver-monitor}")
    private Boolean startSqlserverMonitor;
    @Value("${scheduled-query-interval}")
    private Integer queryInterval;

    @Autowired
    private QuartzUtil quartzUtil;

    @Override
    public void run(String... args) throws Exception {
        if (startSqlserverMonitor){
            JobDetail sqlMonitorJobDetail = JobBuilder.newJob(SqlserverMoniteJob.class)
                    .withIdentity("定时监测SQL server任务")//设置任务名
                    .build();
            Date startTime = DateBuilder.nextGivenSecondDate(new Date(), 3);
            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity("每隔90秒触发") //设置触发器名
                    .startAt(startTime)
                    .withSchedule(SimpleScheduleBuilder.repeatSecondlyForever(queryInterval))
                    .build();
            quartzUtil.scheduleJob(sqlMonitorJobDetail, trigger);
        }
    }

}
