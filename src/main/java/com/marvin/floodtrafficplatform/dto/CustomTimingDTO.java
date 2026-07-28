package com.marvin.floodtrafficplatform.dto;

import lombok.Data;

import java.util.List;

// 自定义路口配时类
@Data
public class CustomTimingDTO {
    // 对应 SUMO 中的交叉口 ID (如 "Junction_A")
    private String junctionId;

    // 配时类型，通常为 "static"
    private String type;

    // 该路口包含的具体红绿灯相位列表
    private List<PhaseDTO> phases;

    // P0-4: 信号相位偏移量（秒），用于多路口绿波协调
    // 之前该字段前端有发、DTO 无定义被静默丢弃，Python 端永远拿到 0
    private Integer offset;
}
