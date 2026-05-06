package com.marvin.floodtrafficplatform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@TableName(value = "simulation_task", autoResultMap = true)
public class SimulationTask {

    @TableId(type = IdType.INPUT)
    private String taskId;

    private String status;

    // 使用 JacksonTypeHandler 实现 JSONB 与 Java Map 的自动转换（已不适用）
    // 把原来的 JacksonTypeHandler.class 换成下面这个
    @TableField(typeHandler = com.marvin.floodtrafficplatform.handler.PgJsonbTypeHandler.class)
    private Map<String, Object> parameters;

    private String resultTifPath;

    private LocalDateTime createTime;

    private LocalDateTime endTime;
}