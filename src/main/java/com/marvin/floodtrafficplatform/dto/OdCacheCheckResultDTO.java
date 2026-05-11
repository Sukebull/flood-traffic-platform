package com.marvin.floodtrafficplatform.dto;

import lombok.Data;

@Data
public class OdCacheCheckResultDTO {
    private Boolean cacheHit;
    private Boolean poiCached;
    private Boolean tazCached;
    private Boolean odCached;
    private String message;
}
