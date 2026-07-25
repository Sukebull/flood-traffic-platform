package com.marvin.floodtrafficplatform.dto;

import lombok.Data;

@Data
public class Wca2dParamDTO {
    // 曼宁糙率系数
    private Double roadManning;     // 城市道路曼宁系数
    private Double buildingManning; // 建筑群曼宁系数
    private Double greenManning;    // 绿地/公园曼宁系数

    // WCA2D 时长控制参数
    private Integer durationSeconds; // WCA2D 总模拟时长 (s)
    private Integer outputPeriodSeconds; // WCA2D 输出步长 (s)

    // P3-003: 耦合模式开关：oneway 单向离线耦合(缺省, caflood) / twoway 双向在线耦合(Solver2D)
    // null 时 Python 端缺省 oneway；整体序列化转发与 DB JSONB 持久化自动覆盖，无需额外透传代码
    private String couplingMode;
}