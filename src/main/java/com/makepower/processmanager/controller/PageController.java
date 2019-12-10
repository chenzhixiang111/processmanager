package com.makepower.processmanager.controller;

import com.makepower.processmanager.bean.MemoryJobEntity;
import com.makepower.processmanager.bean.RestartJob;
import com.makepower.processmanager.job.MemoryMoniteJob;
import com.makepower.processmanager.util.CmdUtils;
import com.makepower.processmanager.util.QuartzUtil;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import java.io.*;
import java.util.*;

import static com.makepower.processmanager.controller.ProcessController.MEMORY_GROUP_NAME;
import static com.makepower.processmanager.controller.ProcessController.RESTART_GROUP_NAME;
import static org.quartz.Trigger.TriggerState;
/**
 * @Author czx
 * @Description 页面跳转控制器层
 * @Version 2019-10-29 9:36
 */
@Controller
@Slf4j
public class PageController {
    @Autowired
    private QuartzUtil quartzUtil;
    /**
     * 触发器状态与状态值的映射
     */
    private static EnumMap<TriggerState, String> triggerStateEnumMap;
    static {
        triggerStateEnumMap = new EnumMap<>(TriggerState.class);
        triggerStateEnumMap.put(TriggerState.NONE, "不存在");
        triggerStateEnumMap.put(TriggerState.NORMAL, "正常");
        triggerStateEnumMap.put(TriggerState.PAUSED, "暂停");
        triggerStateEnumMap.put(TriggerState.COMPLETE, "完成");
        triggerStateEnumMap.put(TriggerState.ERROR, "错误");
        triggerStateEnumMap.put(TriggerState.BLOCKED, "阻塞");
    }

    @RequestMapping(value = "/indexPage", method = RequestMethod.GET)
    public ModelAndView indexPage(){
        ModelAndView result = new ModelAndView("index");
        try {
            //获取重启组中的全部任务
            Set<JobKey> allRestartJobKeys = quartzUtil.getJobsByGroupName(RESTART_GROUP_NAME);
            List<RestartJob> restartJobEntities = new ArrayList<>();
            for (JobKey restartJobKey : allRestartJobKeys) {
                RestartJob job = new RestartJob();
                job.setJobName(restartJobKey.getName());
                SimpleTrigger trigger = (SimpleTrigger)quartzUtil.getTrigger(restartJobKey.getName(), RESTART_GROUP_NAME);
                job.setLastJobTime(trigger.getPreviousFireTime());
                job.setIntervalDays(trigger.getRepeatInterval() / 86400000);
                job.setJobCount(trigger.getTimesTriggered());
                TriggerState triggerState = quartzUtil.getTriggerState(restartJobKey.getName(), RESTART_GROUP_NAME);
                job.setState(triggerStateEnumMap.get(triggerState));
                restartJobEntities.add(job);
            }
            result.addObject("restartJobs", restartJobEntities);
            //对集合做一次深拷贝
            Collection<MemoryJobEntity> memoryJobEntities = deepCopy(MemoryMoniteJob.memoryProcessNames.values());
            for (MemoryJobEntity memoryJobEntity : memoryJobEntities) {
                Long memoryValue = CmdUtils.getProcessMemoryByPath(memoryJobEntity.getExePath());
                memoryJobEntity.setMemoryValue(memoryValue);
                memoryJobEntity.setThreshold(memoryJobEntity.getThreshold());
                TriggerState triggerState = quartzUtil.getTriggerState("内存监测触发器", MEMORY_GROUP_NAME);
                memoryJobEntity.setState(triggerStateEnumMap.get(triggerState));
            }
            result.addObject("memoryJobs", memoryJobEntities);
        }catch (Exception e){
            log.error("首页视图数据装载异常", e);
        }
        return result;
    }


    public static <T> Collection<T> deepCopy(Collection<T> src) throws IOException, ClassNotFoundException {
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(byteOut);
        out.writeObject(src);

        ByteArrayInputStream byteIn = new ByteArrayInputStream(byteOut.toByteArray());
        ObjectInputStream in = new ObjectInputStream(byteIn);
        @SuppressWarnings("unchecked")
        Collection<T> dest = (Collection<T>) in.readObject();
        return dest;
    }


}
