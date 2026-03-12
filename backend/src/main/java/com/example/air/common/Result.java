package com.example.air.common;

import lombok.Data;

/**
 * 统一返回结果封装类
 * 大数据项目标准：所有接口都必须返回这种格式，方便前端判断 code=200 还是 500
 */
@Data
public class Result<T> {
    private Integer code; // 状态码：200成功，500失败
    private String msg;   // 提示信息
    private T data;       // 真正的数据（比如 List<AirRecord>）

    // 成功时的静态构造方法
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMsg("success");
        result.setData(data);
        return result;
    }

    // 失败时的静态构造方法
    public static <T> Result<T> error(String msg) {
        Result<T> result = new Result<>();
        result.setCode(500);
        result.setMsg(msg);
        return result;
    }
}