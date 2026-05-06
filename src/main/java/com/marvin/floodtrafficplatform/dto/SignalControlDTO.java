package com.marvin.floodtrafficplatform.dto;

import lombok.Data;
import java.util.List;

// 信号控制主类,两个子类分别为phaseDTO和customTimingDTO
@Data
public class SignalControlDTO {
    // 信号灯模式："auto" (感应式) 或 "manual" (固定配时)
    private String mode;

    // 当 mode 为 "manual" 时，包含用户自定义的路口配时列表
    private List<CustomTimingDTO> customTimings;
}

