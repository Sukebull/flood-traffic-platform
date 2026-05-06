package com.marvin.floodtrafficplatform.dto;

import lombok.Data;

// 具体相位类
@Data
public class PhaseDTO {
    // 该相位持续时间（秒）
    private Integer duration;

    // 该相位的状态灯串（如 "GGggrrrrGGggrrrr"）
    private String state;
}
