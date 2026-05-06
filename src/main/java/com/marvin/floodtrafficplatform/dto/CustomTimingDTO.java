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
}
