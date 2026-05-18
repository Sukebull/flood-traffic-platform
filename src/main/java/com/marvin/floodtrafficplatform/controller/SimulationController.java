package com.marvin.floodtrafficplatform.controller;

import com.marvin.floodtrafficplatform.common.Result;
import com.marvin.floodtrafficplatform.dto.SimulationParamDTO;
import com.marvin.floodtrafficplatform.entity.SimulationTask;
import com.marvin.floodtrafficplatform.service.SimulationService;
import com.marvin.floodtrafficplatform.service.SimulationTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/simulation")
@CrossOrigin
public class SimulationController {

    @Autowired
    private SimulationService simulationService;

    @Autowired
    private SimulationTaskService taskService;

    // 1. 发起内涝仿真任务
    @PostMapping("/start")
    public Result<Map<String, Object>> startSimulation(@RequestBody SimulationParamDTO params) {
        Map<String, Object> pythonResponse = simulationService.startPythonSimulation(params);
        return Result.success(pythonResponse);
    }

    // 2. 查询内涝仿真进度
    @GetMapping("/progress/{taskId}")
    public Result<Map<String, Object>> getProgress(@PathVariable String taskId) {
        Map<String, Object> progressInfo = simulationService.getSimulationProgress(taskId);
        return Result.success(progressInfo);
    }

    // 3. 查询指定任务下某个节点的溢流结果
    @GetMapping("/result/node/{taskId}/{nodeId}")
    public Result<Map<String, Object>> getNodeData(@PathVariable String taskId, @PathVariable String nodeId) {
        Map<String, Object> data = simulationService.getNodeOverflowData(taskId, nodeId);
        return Result.success(data);
    }

    // 4. 条件查询内涝仿真任务列表
    // 支持：
    // - /api/simulation/tasks                  查询全部
    // - /api/simulation/tasks?status=running  按状态查询
    // - /api/simulation/tasks?completed=true  查询已完成任务
    @GetMapping("/tasks")
    public Result<List<SimulationTask>> getTaskList(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Boolean completed) {
        return Result.success(taskService.listTasks(status, completed));
    }

    // 5. 根据任务 UUID 查询单个内涝任务
    @GetMapping("/tasks/{taskId}")
    public Result<SimulationTask> getTaskById(@PathVariable String taskId) {
        SimulationTask task = taskService.getTaskById(taskId);
        if (task == null) {
            return Result.error(404, "task not found");
        }
        return Result.success(task);
    }

    // 6. 删除指定内涝仿真任务
    @DeleteMapping("/task/{taskId}")
    public Result<String> deleteTask(@PathVariable String taskId) {
        boolean success = taskService.removeById(taskId);
        if (success) {
            return Result.success("delete success");
        }
        return Result.error("delete failed");
    }

    // 7. 查询指定任务下所有存在溢流的管点 ID 列表
    @GetMapping("/result/nodes/{taskId}/overflowed")
    public Result<List<String>> getOverflowedNodeIds(@PathVariable String taskId) {
        List<String> nodeIds = simulationService.getOverflowedNodeIds(taskId);
        return Result.success(nodeIds);
    }
}
