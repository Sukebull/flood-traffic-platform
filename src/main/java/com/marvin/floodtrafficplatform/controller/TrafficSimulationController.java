package com.marvin.floodtrafficplatform.controller;

import com.marvin.floodtrafficplatform.common.Result;
import com.marvin.floodtrafficplatform.dto.OdCacheCheckDTO;
import com.marvin.floodtrafficplatform.dto.OdCacheCheckResultDTO;
import com.marvin.floodtrafficplatform.dto.TrafficSimParamDTO;
import com.marvin.floodtrafficplatform.entity.TrafficSimulationTask;
import com.marvin.floodtrafficplatform.service.TrafficSimulationTaskService;
import com.marvin.floodtrafficplatform.service.impl.TrafficSimulationServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/traffic")
@CrossOrigin
public class TrafficSimulationController {

    @Autowired
    private TrafficSimulationServiceImpl trafficService;

    @Autowired
    private TrafficSimulationTaskService trafficTaskService;

    @Autowired
    private RestTemplate restTemplate;

    @PostMapping("/start")
    public Result<Map<String, String>> startSimulation(@RequestBody TrafficSimParamDTO paramDTO) {
        try {
            String taskId = trafficService.submitTrafficTask(paramDTO);
            Map<String, String> data = new HashMap<>();
            data.put("taskId", taskId);
            data.put("status", "running");
            return Result.success(data);
        } catch (IllegalArgumentException e) {
            return Result.error(400, e.getMessage());
        } catch (Exception e) {
            return Result.error(500, "交通仿真任务提交失败: " + e.getMessage());
        }
    }

    @GetMapping("/status/{taskId}")
    public Result<TrafficSimulationTask> getTaskStatus(@PathVariable String taskId) {
        TrafficSimulationTask task = trafficTaskService.getTaskById(taskId);
        if (task == null) {
            return Result.error(404, "task not found");
        }
        return Result.success(task);
    }

    @GetMapping("/tasks")
    public Result<List<TrafficSimulationTask>> getTaskList(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Boolean completed) {
        return Result.success(trafficTaskService.listTasks(status, completed));
    }

    @GetMapping("/tasks/{taskId}")
    public Result<TrafficSimulationTask> getTaskById(@PathVariable String taskId) {
        TrafficSimulationTask task = trafficTaskService.getTaskById(taskId);
        if (task == null) {
            return Result.error(404, "task not found");
        }
        return Result.success(task);
    }

    @PostMapping("/callback")
    public Result<String> pythonCallback(@RequestBody Map<String, String> callbackData) {
        String taskId = callbackData.get("taskId");
        String status = callbackData.get("status");
        String resultPath = callbackData.get("resultPath");
        String errorMessage = callbackData.get("errorMessage");

        TrafficSimulationTask task = trafficTaskService.getTaskById(taskId);
        if (task != null) {
            task.setStatus(status);
            if (resultPath != null) {
                task.setResultPath(resultPath);
            }
            if (errorMessage != null) {
                task.setErrorMessage(errorMessage);
            }
            trafficTaskService.updateById(task);
        }
        return Result.success("status updated");
    }

    @PostMapping("/check-od-cache")
    public Result<OdCacheCheckResultDTO> checkOdCache(@RequestBody OdCacheCheckDTO checkDTO) {
        try {
            String pythonUrl = "http://localhost:8000/api/traffic/check-od-cache";
            Map<String, Object> payload = new HashMap<>();
            payload.put("bounds", checkDTO.getBounds());
            payload.put("gridSizeKm", checkDTO.getGridSizeKm());
            payload.put("originKeyword", checkDTO.getOriginKeyword());
            payload.put("destinationKeyword", checkDTO.getDestinationKeyword());
            payload.put("poiSearchRadiusMeters", checkDTO.getPoiSearchRadiusMeters());
            payload.put("beta", checkDTO.getBeta());

            ResponseEntity<Map> response = restTemplate.postForEntity(pythonUrl, payload, Map.class);
            Map<String, Object> responseBody = response.getBody();
            Map<String, Object> data = (Map<String, Object>) responseBody.get("data");

            OdCacheCheckResultDTO result = new OdCacheCheckResultDTO();
            result.setCacheHit((Boolean) data.get("cacheHit"));
            result.setPoiCached((Boolean) data.get("poiCached"));
            result.setTazCached((Boolean) data.get("tazCached"));
            result.setOdCached((Boolean) data.get("odCached"));
            result.setMessage((String) data.get("message"));

            return Result.success(result);
        } catch (Exception e) {
            return Result.error(500, "缓存状态查询失败: " + e.getMessage());
        }
    }

    /**
     * 查询任务原始参数（供 Python 断点续跑回查使用）
     */
    @GetMapping("/task/{taskId}/params")
    public Result<Map<String, Object>> getTaskParams(@PathVariable String taskId) {
        TrafficSimulationTask task = trafficTaskService.getTaskById(taskId);
        if (task == null) {
            return Result.error(404, "task not found");
        }
        String parameters = task.getParameters();
        if (parameters == null || parameters.isEmpty()) {
            return Result.error(404, "task parameters not found");
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> paramsMap = mapper.readValue(parameters, Map.class);
            return Result.success(paramsMap);
        } catch (Exception e) {
            return Result.error(500, "参数解析失败: " + e.getMessage());
        }
    }

    // ================= 断点续跑接口 =================
    @GetMapping("/checkpoint/{taskId}")
    public Result<Map<String, Object>> getCheckpoint(@PathVariable String taskId) {
        Map<String, Object> data = trafficService.getCheckpoint(taskId);
        return Result.success(data);
    }

    @PostMapping("/resume/{taskId}")
    public Result<Map<String, Object>> resumeSimulation(@PathVariable String taskId) {
        Map<String, Object> data = trafficService.resumeSimulation(taskId);
        return Result.success(data);
    }
}
