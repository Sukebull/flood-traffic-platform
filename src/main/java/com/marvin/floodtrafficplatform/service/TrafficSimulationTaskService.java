package com.marvin.floodtrafficplatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.marvin.floodtrafficplatform.entity.TrafficSimulationTask;

import java.util.List;
import java.util.Map;

public interface TrafficSimulationTaskService extends IService<TrafficSimulationTask> {

    // 根据交通仿真任务 UUID 查询单个任务
    TrafficSimulationTask getTaskById(String taskId);

    // 条件查询交通仿真任务，规则与内涝任务保持一致
    List<TrafficSimulationTask> listTasks(String status, Boolean completed);

    // 查询交通仿真任务断点状态
    Map<String, Object> getCheckpoint(String taskId);

    // 发起交通仿真断点续跑
    Map<String, Object> resumeSimulation(String taskId);
}
