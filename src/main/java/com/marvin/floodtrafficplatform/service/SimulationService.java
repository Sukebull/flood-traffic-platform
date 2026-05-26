package com.marvin.floodtrafficplatform.service;

import com.marvin.floodtrafficplatform.dto.SimulationParamDTO;
import java.util.List;
import java.util.Map;

public interface SimulationService {
    // 返回值改成 Map，方便直接解析 JSON
    Map<String, Object> startPythonSimulation(SimulationParamDTO params);

    // 新增：根据 taskId 查询进度的方法
    Map<String, Object> getSimulationProgress(String taskId);

    // 获取管道节点溢流数据的方法
    Map<String, Object> getNodeOverflowData(String taskId, String nodeId);

    // 获取指定任务下所有存在溢流的管点 ID 列表
    List<String> getOverflowedNodeIds(String taskId);

    // 查询任务断点状态
    Map<String, Object> getCheckpoint(String taskId);

    // 发起断点续跑
    Map<String, Object> resumeSimulation(String taskId);

    // 删除任务本地产物目录
    boolean deleteTaskLocalFiles(String taskId);
}