package com.marvin.floodtrafficplatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.marvin.floodtrafficplatform.entity.TrafficSimulationTask;

import java.util.List;

public interface TrafficSimulationTaskService extends IService<TrafficSimulationTask> {

    // 根据交通仿真任务 UUID 查询单个任务
    TrafficSimulationTask getTaskById(String taskId);

    // 条件查询交通仿真任务，规则与内涝任务保持一致
    List<TrafficSimulationTask> listTasks(String status, Boolean completed);
}
