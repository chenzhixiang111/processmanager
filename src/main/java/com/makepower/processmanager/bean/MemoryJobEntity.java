package com.makepower.processmanager.bean;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/**
 * @Author czx
 * @Description 内存监测任务实体类
 * @Version 2019-10-30 22:27
 */
public class MemoryJobEntity implements Serializable {
    /**
     * 监测文件的路径
     */
    private String exePath;
    /**
     * 重启次数
     */
    private Integer jobCount = 0;
    /**
     * 上次重启时间
     */
    private Date lastJobTime;
    /**
     * 当前内存
     */
    private Long memoryValue;
    /**
     * 内存阈值
     */
    private Long threshold;
    /**
     * 状态
     */
    private String state;

    public String getExePath() {
        return exePath;
    }

    public void setExePath(String exePath) {
        this.exePath = exePath;
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

    public Long getMemoryValue() {
        return memoryValue;
    }

    public void setMemoryValue(Long memoryValue) {
        this.memoryValue = memoryValue;
    }

    public Long getThreshold() {
        return threshold;
    }

    public void setThreshold(Long threshold) {
        this.threshold = threshold;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    /**
     * jobCount递增
     */
    public Integer increaseJobCount(){
        return ++jobCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MemoryJobEntity)) return false;
        MemoryJobEntity that = (MemoryJobEntity) o;
        return Objects.equals(exePath, that.exePath) &&
                Objects.equals(jobCount, that.jobCount) &&
                Objects.equals(lastJobTime, that.lastJobTime) &&
                Objects.equals(memoryValue, that.memoryValue) &&
                Objects.equals(threshold, that.threshold) &&
                Objects.equals(state, that.state);
    }

    @Override
    public int hashCode() {
        return Objects.hash(exePath, jobCount, lastJobTime, memoryValue, threshold, state);
    }

    @Override
    public String toString() {
        return "MemoryJobEntity{" +
                "exePath='" + exePath + '\'' +
                ", jobCount=" + jobCount +
                ", lastJobTime=" + lastJobTime +
                ", memoryValue=" + memoryValue +
                ", threshold=" + threshold +
                ", state=" + state +
                '}';
    }
}
