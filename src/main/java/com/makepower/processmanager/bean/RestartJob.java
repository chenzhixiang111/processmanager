package com.makepower.processmanager.bean;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/**
 * @Author czx
 * @Description 重启任务实体类
 * @Version 2019-10-30 9:42
 */
public class RestartJob implements Serializable {
    /**任务名*/
    private String jobName;
    /**
     * 任务执行次数
     */
    private Integer jobCount;
    /**
     * 最近一次执行时间
     */
    private Date lastJobTime;
    /**
     * 间隔天数
     */
    private Long intervalDays;
    /**
     * 任务状态 -1不存在 0正常 1暂停  2完成  3错误  4阻塞
     */
    private String state;

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public Integer getJobCount() {
        return jobCount;
    }

    public void setJobCount(Integer jobCount) {
        this.jobCount = jobCount;
    }

    public Date getLastJobTime() {
        return lastJobTime;
    }

    public void setLastJobTime(Date lastJobTime) {
        this.lastJobTime = lastJobTime;
    }

    public Long getIntervalDays() {
        return intervalDays;
    }

    public void setIntervalDays(Long intervalDays) {
        this.intervalDays = intervalDays;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RestartJob)) return false;
        RestartJob job = (RestartJob) o;
        return Objects.equals(jobName, job.jobName) &&
                Objects.equals(jobCount, job.jobCount) &&
                Objects.equals(lastJobTime, job.lastJobTime) &&
                Objects.equals(intervalDays, job.intervalDays) &&
                Objects.equals(state, job.state);
    }

    @Override
    public int hashCode() {
        return Objects.hash(jobName, jobCount, lastJobTime, intervalDays, state);
    }

    @Override
    public String toString() {
        return "RestartJob{" +
                "jobName='" + jobName + '\'' +
                ", jobCount=" + jobCount +
                ", lastJobTime=" + lastJobTime +
                ", intervalDays=" + intervalDays +
                ", state=" + state +
                '}';
    }
}
