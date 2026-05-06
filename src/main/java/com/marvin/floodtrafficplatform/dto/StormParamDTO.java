package com.marvin.floodtrafficplatform.dto;

import lombok.Data;
//  暴雨与雨型参数
@Data
public class StormParamDTO {
    // 必填核心参数
    private Integer p; // 设计重现期 (年)
    private Integer t; // 降雨历时 (min)
    private Double r;  // 雨峰位置系数

    // 地方经验参数 (可由前端传入覆盖默认值) [cite: 214]
    private Double a1; // 雨力参数
    private Double c;  // 雨力变动参数
    private Double b;  // 降雨历时修正参数
    private Double n;  // 暴雨衰减指数
}