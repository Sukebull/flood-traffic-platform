package com.marvin.floodtrafficplatform.controller;

import com.marvin.floodtrafficplatform.common.Result;
import com.marvin.floodtrafficplatform.dto.TrafficSimParamDTO;
import com.marvin.floodtrafficplatform.entity.TrafficSimulationTask;
import com.marvin.floodtrafficplatform.service.TrafficSimulationTaskService;
import com.marvin.floodtrafficplatform.service.impl.TrafficSimulationServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
}
