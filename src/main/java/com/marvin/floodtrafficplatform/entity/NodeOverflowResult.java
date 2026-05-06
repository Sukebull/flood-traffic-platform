package com.marvin.floodtrafficplatform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;
import java.util.List;

@Data
@TableName(value = "node_overflow_result", autoResultMap = true)
public class NodeOverflowResult {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String taskId;

    private String nodeId;

    // 存放诸如 [0, 900, 1800, 2700] 的时间序列
    @TableField(typeHandler = com.marvin.floodtrafficplatform.handler.PgJsonbTypeHandler.class)
    private List<Integer> timeSeries;

    // 存放对应的溢流量数据序列
    @TableField(typeHandler = com.marvin.floodtrafficplatform.handler.PgJsonbTypeHandler.class)
    private List<Double> inflowSeries;
}