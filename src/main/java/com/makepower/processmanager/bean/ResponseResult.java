package com.makepower.processmanager.bean;

import lombok.Data;

/**
 * @Author czx
 * @Description
 * @Version 2019-10-14 10:30
 */
@Data
public class ResponseResult<T> {
    /**状态码 0表示失败，1表示成功*/
    private Integer code;
    private String msg;
    private T data;
}
