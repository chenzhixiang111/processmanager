package com.makepower.processmanager.job;

import com.makepower.processmanager.util.CmdUtils;
import com.makepower.processmanager.util.SpringUtil;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * @Author czx
 * @Description SQL server监测任务
 * @Version 2019-10-26 14:50
 */
public class SqlserverMoniteJob implements Job {
    private static final Logger log = LoggerFactory.getLogger(SqlserverMoniteJob.class);
    public static final String TEST_SELECT = "SELECT 1";

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        JdbcTemplate jdbcTemplate = SpringUtil.getBean(JdbcTemplate.class);
        //向数据库发送查询，如果失败就重启数据库
        try {
            jdbcTemplate.execute(TEST_SELECT);
            log.info("本次测试数据库连接成功");
        }catch (Exception e) {
            log.error("数据库连接或查询异常，即将重启SQL server服务", e);
            CmdUtils.restartSqlserver();
        }
    }
}
