package com.marvin.floodtrafficplatform.dto;

import lombok.Data;

@Data
public class OdDemandDTO {
    private Boolean enabled;
    private String baiduAk;
    private BoundsDTO bounds;
    private Double gridSizeKm;
    private Integer poiSearchRadiusMeters;
    private Integer tazSearchRadiusMeters;
    private String originKeyword;
    private String destinationKeyword;
    private Integer totalTrips;
    private Double beta;
    private Integer randomSeed;
    private Boolean sleepBetweenRequests;
    private Double requestIntervalSeconds;
}
