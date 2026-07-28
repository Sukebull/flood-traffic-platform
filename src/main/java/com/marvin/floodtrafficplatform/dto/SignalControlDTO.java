package com.marvin.floodtrafficplatform.dto;

import lombok.Data;
import java.util.List;

// 信号控制主类,两个子类分别为phaseDTO和customTimingDTO
@Data
public class SignalControlDTO {
    // 信号灯模式："auto" (沿用路网原始信号逻辑) 或 "manual" (用户自定义配时)
    private String mode;

    // 当 mode 为 "manual" 时，包含用户自定义的路口配时列表
    private List<CustomTimingDTO> customTimings;

    // G5: 控制类型（manual 模式下生效）：static 固定配时（默认）/ actuated 感应控制
    private String controlType;

    // G5: 感应相位最短绿灯（秒，SUMO minDur），默认 5
    private Integer actuatedMinDur;

    // G5: 进口道检测器长度（米，车道末端起算），默认 25
    private Integer actuatedDetLength;
}

