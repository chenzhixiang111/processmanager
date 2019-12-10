package com.makepower.processmanager.util;

import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.Set;


/**
 * @Author czx
 * @Description 任务调度工具类
 * @Version 2019-10-26 15:25
 */
@Component
public class QuartzUtil {
    private static Logger log = LoggerFactory.getLogger(QuartzUtil.class);
    @Autowired
    private Scheduler scheduler;

    /**
     * 功能：添加一个定时任务,3秒钟后开始执行
     * @param jobName 任务名
     * @param triggerName 触发器名
     * @param jobClass 任务类
     * @param interval 时间间隔 单位是秒
     * @param object 需要传递给执行任务的信息
     * @throws SchedulerException
     */
    public void addJob(String jobName, String triggerName,
                              Class<? extends Job> jobClass, int interval,
                              Object object) {
        JobDetail jobDetail = JobBuilder.newJob(jobClass).withIdentity(jobName)
                .build();
        jobDetail.getJobDataMap().put(jobName, object);
        Date startTime = new Date();
        startTime.setTime(startTime.getTime() + 3000);
        //创建触发器对象
        SimpleTrigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(triggerName).startAt(startTime)
                .withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInSeconds(interval).repeatForever())
                .build();
        try{
            //任务调度器
            scheduler.scheduleJob(jobDetail, trigger);
            //启动
            if (!scheduler.isShutdown()){
                scheduler.start();
            }
        }catch (SchedulerException e){
            log.error("添加定时任务失败", e);
        }
    }

    /**
     * 添加一个定时任务
     * @param jobDetail
     * @param trigger
     * @return 返回最近一次执行任务的时间
     */
    public Date scheduleJob(JobDetail jobDetail, Trigger trigger) throws SchedulerException {
        Date result = scheduler.scheduleJob(jobDetail, trigger);
//        if (!scheduler.isShutdown()){
//            scheduler.start();
//        }
        return result;
    }

    /**
     * 修改一个任务触发时间
     * @param triggerName 触发器名
     * @param triggerGroupName 触发器组名
     * @param interval 间隔
     */
    public void modifyJobTime(String triggerName, String triggerGroupName,
                                     int interval) {
        try{
            TriggerKey triggerKey = TriggerKey.triggerKey(triggerName, triggerGroupName);
            SimpleTrigger trigger = (SimpleTrigger) scheduler.getTrigger(triggerKey);
            if (trigger == null) {
                return;
            }
            //获取正在用的触发器的cron表达式
            int oldInterval = (int) trigger.getRepeatInterval();
            //判断老的间隔和新的间隔是不是一样
            if (interval != oldInterval) {
                Date startTime = new Date();
                startTime.setTime(startTime.getTime() + 5000);
                SimpleTrigger newTrigger = TriggerBuilder.newTrigger().withIdentity(triggerName, triggerGroupName)
                        .startAt(startTime).withSchedule(SimpleScheduleBuilder.simpleSchedule()
                                .withIntervalInSeconds(interval).repeatForever())
                        .build();
                // 方式一 ：修改一个任务的触发时间
                scheduler.rescheduleJob(triggerKey, newTrigger);
            }
        }catch (SchedulerException e){
            log.error("修改定时任务失败", e);
        }
    }

    /**
     * 根据组名获取这个分组中的所有任务
     * @return 这个组中的所有任务
     * @throws SchedulerException
     */
    public Set<JobKey> getJobsByGroupName(String groupName) throws SchedulerException {
        return scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName));
    }

    /**
     * 根据组名获取这个分组中的所有触发器
     * @param groupName
     * @return
     */
    public Set<TriggerKey> getTriggersByGroupName(String groupName) throws SchedulerException {
        return scheduler.getTriggerKeys(GroupMatcher.triggerGroupEquals(groupName));
    }

    /**
     * 功能：删除一个任务，使用这个方法即使任务都删除完了，程序也不会停止，因为调度器还在一直等待着任务
     * @param triggerKey
     * @param jobKey
     * @throws SchedulerException
     */
    public void removeJob(TriggerKey triggerKey, JobKey jobKey) throws SchedulerException {
        // 暂停触发器
        scheduler.pauseTrigger(triggerKey);
        // 移除触发器
        scheduler.unscheduleJob(triggerKey);
        // 删除任务
        scheduler.deleteJob(jobKey);
    }

    /**
     * 删除一个组中所有的任务
     * @param groupName
     */
    public void removeGroup(String groupName) throws SchedulerException {
        // 暂停触发器
        GroupMatcher<TriggerKey> groupMatcher = GroupMatcher.triggerGroupEquals(groupName);
        scheduler.pauseTriggers(groupMatcher);
        // 移除触发器
        Set<TriggerKey> triggerKeys = scheduler.getTriggerKeys(groupMatcher);
        scheduler.unscheduleJobs(new ArrayList<>(triggerKeys));
        //暂停任务
        GroupMatcher<JobKey> jobKeyGroupMatcher = GroupMatcher.jobGroupEquals(groupName);
        scheduler.pauseJobs(jobKeyGroupMatcher);
        // 删除任务
        Set<JobKey> jobKeysSet= scheduler.getJobKeys(jobKeyGroupMatcher);
        scheduler.deleteJobs(new ArrayList<>(jobKeysSet));
    }

    /**
     * 暂停一个任务组中的所有任务
     * @param groupName 组名
     * @throws SchedulerException
     */
    public void suspendJobs(String groupName) throws SchedulerException {
        scheduler.pauseJobs(GroupMatcher.jobGroupEquals(groupName));
    }

    /**
     * 恢复一个组中所有暂停的任务
     * @param groupName
     * @throws SchedulerException
     */
    public void resumeJobs(String groupName) throws SchedulerException {
        scheduler.resumeJobs(GroupMatcher.jobGroupEquals(groupName));
    }

    /**
     * 启动所有任务
     */
    public void startJobs() {
        try {
            scheduler.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 关闭所有任务，其实是关闭调度器，但是任务和触发器还是在里面的。
     */
    public void shutdownJobs() {
        try {
            if (!scheduler.isShutdown()) {
                scheduler.shutdown();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 清除所有的任务和触发器，调用这个方法前记得先关闭调度器
     */
    public void clear() throws SchedulerException {
        scheduler.clear();
    }

    /**
     * 获取触发器上次触发时间
     * @param triggerName
     * @param groupName
     * @return
     * @throws SchedulerException
     */
    public Date getPreviousFireTime(String triggerName, String groupName) throws SchedulerException {
        Trigger trigger = getTrigger(triggerName, groupName);
        return trigger.getPreviousFireTime();
    }

    /**
     * 获取触发器下一次触发的时间
     * @param triggerName
     * @return
     * @throws SchedulerException
     */
    public Date getNextFireTime(String triggerName, String groupName) throws SchedulerException {
        Trigger trigger = getTrigger(triggerName, groupName);
        return trigger.getNextFireTime();
    }

    /**
     * 获取触发器
     * @param triggerName 触发器名字
     * @param groupName 组名
     * @return
     * @throws SchedulerException
     */
    public Trigger getTrigger(String triggerName, String groupName) throws SchedulerException {
        Trigger trigger = scheduler.getTrigger(TriggerKey.triggerKey(triggerName, groupName));
//        if (trigger == null){
//            throw new IllegalArgumentException("参数错误，未找到对应触发器...triggerName：" + triggerName + "，groupName：" + groupName);
//        }
        return trigger;
    }

    /**
     * 获取任务详情
     * @param jobName 任务名
     * @param jobGroup 任务组
     * @return
     * @throws SchedulerException
     */
    public JobDetail getJobDetail(String jobName, String jobGroup) throws SchedulerException {
        JobDetail jobDetail = scheduler.getJobDetail(JobKey.jobKey(jobName, jobGroup));
//        if (jobDetail == null){
//            throw new IllegalArgumentException("参数错误，为找到对应的JobDetail，任务名："+ jobName + ", 任务组："+ jobGroup);
//        }
        return jobDetail;
    }

    /**
     * 获取触发器状态
     * @param triggerName 触发器名字
     * @param groupName 触发器组
     * @return 触发器状态
     * @throws SchedulerException
     */
    public Trigger.TriggerState getTriggerState(String triggerName, String groupName) throws SchedulerException {
        return scheduler.getTriggerState(TriggerKey.triggerKey(triggerName, groupName));
    }
}
