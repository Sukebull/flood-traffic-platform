package com.marvin.floodtrafficplatform.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.marvin.floodtrafficplatform.handler.PgJsonbTypeHandler;
import lombok.Data;
import java.util.Date;

@Data
@TableName(value = "traffic_simulation_task", autoResultMap = true)
public class TrafficSimulationTask {
    @TableId
    private String id; // 任务UUID

    private String taskName;

    private String floodTaskId; // 级联的内涝任务ID

    private String status; // PENDING, RUNNING, SUCCESS, FAILED

    // 复用你已有的 PgJsonbTypeHandler 来存储复杂的 DTO 参数
    @TableField(typeHandler = PgJsonbTypeHandler.class)
    private String parameters; // 存入 JSON 字符串

    private String resultPath; // 生成的 config.sumocfg 路径

    private String errorMessage;

    private Date createTime;
}