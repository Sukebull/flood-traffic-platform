package com.marvin.floodtrafficplatform.service;

import com.marvin.floodtrafficplatform.dto.SimulationParamDTO;
import java.util.Map;

public interface SimulationService {
    // 返回值改成 Map，方便直接解析 JSON
    Map<String, Object> startPythonSimulation(SimulationParamDTO params);

    // 新增：根据 taskId 查询进度的方法
    Map<String, Object> getSimulationProgress(String taskId);

    // 获取管道节点溢流数据的方法
    Map<String, Object> getNodeOverflowData(String taskId, String nodeId);
}