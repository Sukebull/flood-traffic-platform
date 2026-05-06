package com.marvin.floodtrafficplatform.dto;

import lombok.Data;

@Data
public class SimulationParamDTO {

    // 前端在地图上框选的研究区域范围 (GeoJSON 或边界坐标)
    private String regionBoundary;
    // 🌟 新增这个字段，用来接收前端的时间，并透传给 Python
    private String simStartTime;
    // 嵌套组合三个分类参数
    private StormParamDTO stormParams;
    private SwmmParamDTO swmmParams;
    private Wca2dParamDTO wca2dParams;
}