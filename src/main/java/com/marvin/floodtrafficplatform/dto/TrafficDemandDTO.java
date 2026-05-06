package com.marvin.floodtrafficplatform.dto;

import lombok.Data;

@Data
public class TrafficDemandDTO {
    private String sourceType;
    private Double period;
    private Double fringeFactor;
    private String realDataFilePath;
}
