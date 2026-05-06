package com.marvin.floodtrafficplatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.marvin.floodtrafficplatform.entity.SimulationTask;

import java.util.List;

public interface SimulationTaskService extends IService<SimulationTask> {

    // 根据任务 UUID 查询单个内涝仿真任务
    SimulationTask getTaskById(String taskId);

    // 条件查询内涝仿真任务：
    // 1. status 不为空时按状态精确查询
    // 2. status 为空且 completed 不为空时按“是否完成”查询
    // 3. 两个条件都为空时返回全部任务
    List<SimulationTask> listTasks(String status, Boolean completed);
}
