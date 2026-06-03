package com.marvin.floodtrafficplatform.dto;

import lombok.Data;
import java.util.Map;

@Data
public class TrafficSimParamDTO {
    private String floodTaskId;
    private String simStartTime;
    private Integer simEndTime;
    private Double stepLength;
    private String routingAlgorithm;
    private TrafficDemandDTO trafficDemand;
    private OdDemandDTO odDemand;
    private SignalControlDTO signalControl;
    private Map<String, Double> vehicleTypes;
    private Map<String, Map<String, String>> vehicleTypeParams;
}
