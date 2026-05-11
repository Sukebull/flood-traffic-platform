package com.marvin.floodtrafficplatform.dto;

import lombok.Data;

@Data
public class OdCacheCheckDTO {
    private BoundsDTO bounds;
    private Double gridSizeKm;
    private String originKeyword;
    private String destinationKeyword;
    private Integer poiSearchRadiusMeters;
    private Double beta;
}
