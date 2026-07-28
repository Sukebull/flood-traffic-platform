package com.marvin.floodtrafficplatform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marvin.floodtrafficplatform.dto.BoundsDTO;
import com.marvin.floodtrafficplatform.dto.OdDemandDTO;
import com.marvin.floodtrafficplatform.dto.TrafficDemandDTO;
import com.marvin.floodtrafficplatform.dto.TrafficSimParamDTO;
import com.marvin.floodtrafficplatform.entity.TrafficSimulationTask;
import com.marvin.floodtrafficplatform.mapper.TrafficSimulationTaskMapper;
import com.marvin.floodtrafficplatform.service.TrafficSimulationTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class TrafficSimulationServiceImpl extends ServiceImpl<TrafficSimulationTaskMapper, TrafficSimulationTask>
        implements TrafficSimulationTaskService {

    private static final List<String> UNFINISHED_STATUSES = Arrays.asList("PENDING", "RUNNING", "pending", "running");

    @Autowired
    private TrafficSimulationTaskMapper trafficMapper;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @org.springframework.beans.factory.annotation.Value("${python.base-url:http://localhost:8000}")
    private String pythonBaseUrl;

    public String submitTrafficTask(TrafficSimParamDTO paramDTO) {
        validateParamDTO(paramDTO);

        String taskId = UUID.randomUUID().toString();
        TrafficSimulationTask task = new TrafficSimulationTask();
        task.setId(taskId);
        task.setFloodTaskId(paramDTO.getFloodTaskId());
        task.setStatus("PENDING");
        task.setTaskName(buildTaskName(paramDTO));
        task.setCreateTime(new Date());

        try {
            task.setParameters(buildSanitizedParametersJson(paramDTO));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON serialization failed", e);
        }

        trafficMapper.insert(task);

        Map<String, Object> payload = new HashMap<>();
        payload.put("taskId", taskId);
        payload.put("sumoParams", paramDTO);

        String pythonUrl = pythonBaseUrl + "/api/traffic/build-and-run";
        try {
            restTemplate.postForEntity(pythonUrl, payload, String.class);
            task.setStatus("RUNNING");
            trafficMapper.updateById(task);
        } catch (Exception e) {
            task.setStatus("FAILED");
            task.setErrorMessage("Python service call failed: " + e.getMessage());
            trafficMapper.updateById(task);
            // P1-1: 如实上抛 —— 原先吞掉异常仍返回 taskId，Controller 回 status:"running" 误导前端
            throw new RuntimeException("Python 仿真服务不可达，任务已标记 FAILED: " + e.getMessage(), e);
        }

        return taskId;
    }

    private void validateParamDTO(TrafficSimParamDTO paramDTO) {
        if (paramDTO == null) {
            throw new IllegalArgumentException("交通仿真请求体不能为空");
        }
        if (isBlank(paramDTO.getFloodTaskId())) {
            throw new IllegalArgumentException("floodTaskId 不能为空");
        }
        if (isBlank(paramDTO.getSimStartTime())) {
            throw new IllegalArgumentException("simStartTime 不能为空");
        }
        if (paramDTO.getSimEndTime() == null || paramDTO.getSimEndTime() <= 0) {
            throw new IllegalArgumentException("simEndTime 必须大于 0");
        }
        if (paramDTO.getStepLength() == null || paramDTO.getStepLength() <= 0) {
            throw new IllegalArgumentException("stepLength 必须大于 0");
        }
        if (isBlank(paramDTO.getRoutingAlgorithm())) {
            throw new IllegalArgumentException("routingAlgorithm 不能为空");
        }

        TrafficDemandDTO demandDTO = paramDTO.getTrafficDemand();
        if (demandDTO == null || isBlank(demandDTO.getSourceType())) {
            throw new IllegalArgumentException("trafficDemand.sourceType 不能为空");
        }

        if ("REAL_OD".equalsIgnoreCase(demandDTO.getSourceType())) {
            validateRealOdDemand(paramDTO.getOdDemand());
        } else {
            validateRandomDemand(demandDTO);
        }

        validateVehicleTypes(paramDTO.getVehicleTypes());
    }

    private void validateVehicleTypes(java.util.Map<String, Double> vehicleTypes) {
        if (vehicleTypes == null || vehicleTypes.isEmpty()) {
            return; // 未传时后端使用默认单车型
        }
        double total = vehicleTypes.values().stream().mapToDouble(Double::doubleValue).sum();
        if (Math.abs(total - 1.0) > 0.001) {
            throw new IllegalArgumentException("车型比例总和必须为 1.0（100%），当前为 " + String.format("%.2f", total));
        }
    }

    private void validateRandomDemand(TrafficDemandDTO demandDTO) {
        if (demandDTO.getPeriod() == null || demandDTO.getPeriod() <= 0) {
            throw new IllegalArgumentException("随机交通模式下，trafficDemand.period 必须大于 0");
        }
        if (demandDTO.getFringeFactor() == null || demandDTO.getFringeFactor() <= 0) {
            throw new IllegalArgumentException("随机交通模式下，trafficDemand.fringeFactor 必须大于 0");
        }
    }

    private void validateRealOdDemand(OdDemandDTO odDemand) {
        if (odDemand == null) {
            throw new IllegalArgumentException("真实 OD 模式下，odDemand 不能为空");
        }
        if (isBlank(odDemand.getBaiduAk())) {
            throw new IllegalArgumentException("真实 OD 模式下，必须提供 baiduAk");
        }

        BoundsDTO bounds = odDemand.getBounds();
        if (bounds == null || bounds.getTop() == null || bounds.getBottom() == null || bounds.getLeft() == null || bounds.getRight() == null) {
            throw new IllegalArgumentException("真实 OD 模式下，研究区域 bounds 不能为空");
        }
        if (bounds.getTop() <= bounds.getBottom()) {
            throw new IllegalArgumentException("北边界纬度必须大于南边界纬度");
        }
        if (bounds.getRight() <= bounds.getLeft()) {
            throw new IllegalArgumentException("东边界经度必须大于西边界经度");
        }
        if (odDemand.getGridSizeKm() == null || odDemand.getGridSizeKm() <= 0) {
            throw new IllegalArgumentException("gridSizeKm 必须大于 0");
        }
        if (odDemand.getPoiSearchRadiusMeters() == null || odDemand.getPoiSearchRadiusMeters() <= 0) {
            throw new IllegalArgumentException("poiSearchRadiusMeters 必须大于 0");
        }
        if (odDemand.getTazSearchRadiusMeters() == null || odDemand.getTazSearchRadiusMeters() <= 0) {
            throw new IllegalArgumentException("tazSearchRadiusMeters 必须大于 0");
        }
        if (odDemand.getTotalTrips() == null || odDemand.getTotalTrips() <= 0) {
            throw new IllegalArgumentException("totalTrips 必须大于 0");
        }
        if (odDemand.getBeta() == null || odDemand.getBeta() <= 0) {
            throw new IllegalArgumentException("beta 必须大于 0");
        }
    }

    private String buildTaskName(TrafficSimParamDTO paramDTO) {
        String sourceType = paramDTO.getTrafficDemand() == null ? "RANDOM" : paramDTO.getTrafficDemand().getSourceType();
        if ("REAL_OD".equalsIgnoreCase(sourceType)) {
            return "真实OD交通仿真任务";
        }
        return "随机交通仿真任务";
    }

    @SuppressWarnings("unchecked")
    private String buildSanitizedParametersJson(TrafficSimParamDTO paramDTO) throws JsonProcessingException {
        Map<String, Object> parameterMap = objectMapper.convertValue(paramDTO, new TypeReference<Map<String, Object>>() {});
        Object odDemand = parameterMap.get("odDemand");
        if (odDemand instanceof Map) {
            Map<String, Object> odDemandMap = (Map<String, Object>) odDemand;
            if (odDemandMap.containsKey("baiduAk")) {
                Object ak = odDemandMap.get("baiduAk");
                odDemandMap.put("baiduAk", maskSecret(ak == null ? "" : String.valueOf(ak)));
            }
        }
        return objectMapper.writeValueAsString(parameterMap);
    }

    private String maskSecret(String secret) {
        if (isBlank(secret)) {
            return "";
        }
        if (secret.length() <= 8) {
            return "****";
        }
        return secret.substring(0, 4) + "****" + secret.substring(secret.length() - 4);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    @Override
    public TrafficSimulationTask getTaskById(String taskId) {
        return getById(taskId);
    }

    @Override
    public List<TrafficSimulationTask> listTasks(String status, Boolean completed) {
        LambdaQueryWrapper<TrafficSimulationTask> queryWrapper = new LambdaQueryWrapper<>();

        if (status != null && !status.trim().isEmpty()) {
            queryWrapper.eq(TrafficSimulationTask::getStatus, status.trim());
        } else if (completed != null) {
            if (completed) {
                queryWrapper.and(wrapper -> wrapper.isNotNull(TrafficSimulationTask::getStatus)
                        .notIn(TrafficSimulationTask::getStatus, UNFINISHED_STATUSES));
            } else {
                queryWrapper.and(wrapper -> wrapper.isNull(TrafficSimulationTask::getStatus)
                        .or()
                        .in(TrafficSimulationTask::getStatus, UNFINISHED_STATUSES));
            }
        }

        queryWrapper.orderByDesc(TrafficSimulationTask::getCreateTime);
        return list(queryWrapper);
    }

    // ================= 查询交通仿真断点状态 =================
    @Override
    public Map<String, Object> getCheckpoint(String taskId) {
        try {
            String url = "http://localhost:8000/api/traffic/checkpoint/" + taskId;
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            return response.getBody();
        } catch (Exception e) {
            System.err.println("❌ 查询交通仿真断点状态失败: " + e.getMessage());
            Map<String, Object> errorMap = new HashMap<>();
            errorMap.put("status", "error");
            errorMap.put("message", "查询断点状态失败: " + e.getMessage());
            return errorMap;
        }
    }

    // ================= 发起交通仿真断点续跑 =================
    @Override
    public Map<String, Object> resumeSimulation(String taskId) {
        try {
            String url = "http://localhost:8000/api/traffic/resume/" + taskId;
            ResponseEntity<Map> response = restTemplate.postForEntity(url, null, Map.class);
            return response.getBody();
        } catch (Exception e) {
            System.err.println("❌ 交通仿真断点续跑请求失败: " + e.getMessage());
            Map<String, Object> errorMap = new HashMap<>();
            errorMap.put("status", "error");
            errorMap.put("message", "断点续跑请求失败: " + e.getMessage());
            return errorMap;
        }
    }
}
