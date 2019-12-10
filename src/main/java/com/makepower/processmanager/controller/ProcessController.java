package com.makepower.processmanager.controller;

import com.makepower.processmanager.bean.MemoryJobEntity;
import com.makepower.processmanager.bean.ResponseResult;
import com.makepower.processmanager.job.CheckHeartJob;
import com.makepower.processmanager.job.MemoryMoniteJob;
import com.makepower.processmanager.job.RestartProcessJob;
import com.makepower.processmanager.util.CmdUtils;
import com.makepower.processmanager.util.DateTimeUtils;
import com.makepower.processmanager.util.QuartzUtil;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


/**
 * @Author czx
 * @Description 进程管理控制器层
 * @Version 2019-10-14 9:34
 */
@Controller
@Slf4j
public class ProcessController {
    @Autowired
    private QuartzUtil quartzUtil;

    public static final String RESTART_GROUP_NAME = "restartGroup";
    public static final String MEMORY_GROUP_NAME = "memoryGroup";
    public static final String HEART_GROUP_NAME = "heartGroup";
    /**
     * 根据进程pid杀进程
     * @param pid
     * @return
     */
    @GetMapping("/killProcess")
    @ResponseBody
    public ResponseResult<Void> killProcess(@RequestParam(required = true) Integer pid) {
        log.info("收到杀进程请求。进程pid：{}", pid);
        ResponseResult<Void> result = new ResponseResult<>();
        try {
            CmdUtils.killProcessByPid(pid);
            result.setCode(1);
            result.setMsg("成功");
        } catch (Exception e) {
            log.error("杀死进程失败", e);
            result.setCode(0);
            result.setMsg("杀死进程失败");
        }
        return result;
    }

    /**
     * 创建一个新的定时重启任务
     * @param exePath exe程序地址
     * @param intervalDays 间隔日期
     * @return
     */
    @RequestMapping(value = "/createNewRestartJob", method = RequestMethod.POST)
    @ResponseBody
    public ResponseResult<Void> createNewRestartJob(String exePath, Integer intervalDays){
        ResponseResult<Void> result = new ResponseResult<>();
        if (intervalDays <= 0){
            result.setCode(0);
            result.setMsg("间隔时间不能小于1天");
            return result;
        }
        try {
            //创建一个定时重启的任务
            JobDetail jobDetail = JobBuilder.newJob(RestartProcessJob.class)
                    .withIdentity(exePath, RESTART_GROUP_NAME) //设置任务的名字和分组
                    .build();
            jobDetail.getJobDataMap().put("exePath", exePath);
            //传入的时间单位是天，我把它转为秒
            Integer intervalSeconds = 86400 * intervalDays;
            //创建一个触发器，触发器名字就是文件全路径
            Trigger trigger = TriggerBuilder.newTrigger().withIdentity(exePath, RESTART_GROUP_NAME) //设置触发器的名字和分组
                    .startAt(DateBuilder.nextGivenSecondDate(new Date(), 2))
                    .withSchedule(SimpleScheduleBuilder.repeatSecondlyForever(intervalSeconds))
                    .build();
            quartzUtil.scheduleJob(jobDetail, trigger);
            result.setCode(1);
            result.setMsg("成功");
        }catch (SchedulerException e){
            log.error("添加定时任务失败", e);
            result.setCode(0);
            result.setMsg("添加定时任务失败");
        } catch (Exception e) {
            log.error("创建新的进程监控任务异常", e);
            result.setCode(0);
            result.setMsg("失败");
        }
        return result;
    }

    /**
     * 删除一个定时重启的任务
     * @return
     */
    @RequestMapping(value = "/removeRestartJob", method = RequestMethod.POST)
    @ResponseBody
    public ResponseResult<Void> removeRestartJob(String jobName){
        ResponseResult<Void> result = new ResponseResult<>();
        try {
            quartzUtil.removeJob(TriggerKey.triggerKey(jobName, RESTART_GROUP_NAME), JobKey.jobKey(jobName, RESTART_GROUP_NAME));
            result.setCode(1);
            result.setMsg("成功");
        } catch (SchedulerException e) {
            log.error("删除定时重启任务{}异常", jobName, e);
            result.setCode(0);
            result.setMsg("删除定时重启任务失败");
        }
        return result;
    }

    /**
     * 添加一个被监测内存的程序
     * @param exePath exe文件全路径
     * @param threshold 阈值
     * @return
     */
    @RequestMapping(value = "/addProcessMemory", method = RequestMethod.POST)
    @ResponseBody
    public ResponseResult<Void> addProcessMemory(String exePath, Long threshold){
        ResponseResult<Void> result = new ResponseResult<>();
        try {
            MemoryJobEntity memoryJobEntity = new MemoryJobEntity();
            memoryJobEntity.setExePath(exePath);
            memoryJobEntity.setThreshold(threshold);
            MemoryMoniteJob.memoryProcessNames.put(exePath, memoryJobEntity);
            /*
            注意内存监测和定时重启有一个区别，定时重启是有多个定时任务来定时重启程序，一个定时任务监测一个程序。
            内存监测是只用一个定时器去监测多个程序的内存。
             */
            //先判断内存监测任务组中是否有任务，有的话就不添加，没有的话就添加一个任务进去
            Set<TriggerKey> triggerKeys = quartzUtil.getTriggersByGroupName(MEMORY_GROUP_NAME);
            if (triggerKeys.size() <= 0){
                JobDetail jobDetail = JobBuilder.newJob(MemoryMoniteJob.class)
                        .withIdentity("内存监测任务", MEMORY_GROUP_NAME).build();
                Trigger trigger = TriggerBuilder.newTrigger().withIdentity("内存监测触发器", MEMORY_GROUP_NAME)
                        .startAt(new Date())
                        .withSchedule(SimpleScheduleBuilder.repeatSecondlyForever(10))
                        .build();
                quartzUtil.scheduleJob(jobDetail, trigger);
            }
            result.setCode(1);
            result.setMsg("成功");
        } catch (Exception e) {
            log.error("添加监测内存任务出错", e);
            result.setCode(0);
            result.setMsg("添加监测内存任务出错");
        }
        return result;
    }

    /**
     * 移除一个进程内存监控
     * @param exePath
     * @return
     */
    @RequestMapping(value = "/removeMemoryJob", method = RequestMethod.POST)
    @ResponseBody
    public ResponseResult<Void> removeMemoryJob(String exePath){
        ResponseResult<Void> result = new ResponseResult<>();
        MemoryMoniteJob.memoryProcessNames.remove(exePath);
        result.setCode(1);
        result.setMsg("成功");
        return result;
    }


    @GetMapping("/heartbeat")
    @ResponseBody
    public ResponseResult<Void> heartbeat(String exePath) {
        ResponseResult<Void> result = new ResponseResult<>();
        Long aLong = CheckHeartJob.heartbeatMap.get(exePath);
        try {
            //第一次收到某个程序的心跳或心跳间隔小于30秒
            if (aLong == null || System.currentTimeMillis() - aLong < 30){
                result.setCode(1);
                result.setMsg("成功");
                CheckHeartJob.heartbeatMap.put(exePath, System.currentTimeMillis());
                //启动定时任务
                JobDetail jobDetail = JobBuilder.newJob(CheckHeartJob.class)
                        .withIdentity("心跳监测任务任务", HEART_GROUP_NAME).build();
                Trigger trigger = TriggerBuilder.newTrigger().withIdentity("心跳监测触发器", HEART_GROUP_NAME)
                        .startAt(new Date())
                        .withSchedule(SimpleScheduleBuilder.repeatSecondlyForever(10))
                        .build();
                quartzUtil.scheduleJob(jobDetail, trigger);
            }
            else {
                result.setCode(0);
                result.setMsg("心跳超时");
                CheckHeartJob.heartbeatMap.remove(exePath);
            }
        }catch (Exception e){
            log.error("心跳接口异常", e);
            result.setCode(0);
            result.setMsg("心跳接口异常");
        }
        return result;
    }
}
