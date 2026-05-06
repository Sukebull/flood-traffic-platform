package com.marvin.floodtrafficplatform.common;

import lombok.Data;
//全局统一返回类，将后端的回复包装成统一的格式，不管成功还是失败，前端收到的永远是一个包含 code（状态码）、msg（提示信息）和 data（核心数据）的标准化 JSON
@Data
public class Result<T> {

    private Integer code; // 状态码：例如 200 代表成功，500 代表后端报错
    private String msg;   // 给前端的提示信息
    private T data;       // 真正要传给前端的数据（可以是 String、List、或者另一个复杂的 DTO）

    // 快捷方法：成功时调用
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMsg("操作成功");
        result.setData(data);
        return result;
    }

    // 快捷方法：成功时调用（不带数据，只返回成功信息）
    public static <T> Result<T> success() {
        return success(null);
    }

    // 快捷方法：失败时调用
    public static <T> Result<T> error(Integer code, String msg) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMsg(msg);
        return result;
    }

    // 🌟 新增的快捷方法：只传错误信息，默认状态码给 500
    public static <T> Result<T> error(String msg) {
        return error(500, msg);
    }
}