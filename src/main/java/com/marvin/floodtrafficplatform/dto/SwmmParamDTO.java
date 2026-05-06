package com.marvin.floodtrafficplatform.dto;

import lombok.Data;

@Data
public class SwmmParamDTO {
    // 洼蓄参数
    private Double dimp;  // 不透水区洼蓄量 (mm)
    private Double dperv; // 透水区洼蓄量 (mm)

    // Horton 下渗模型参数
    private Double f0; // 初始下渗率 (mm/h)
    private Double fc; // 稳定下渗率 (mm/h)
    private Double k;  // 衰减系数 (h^-1)

    // SWMM 时长控制参数
    private Integer durationMinutes; // SWMM 总模拟时长 (min)
    private Integer reportStepSeconds; // SWMM 输出步长 (s)
}