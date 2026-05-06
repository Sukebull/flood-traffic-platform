package com.marvin.floodtrafficplatform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.marvin.floodtrafficplatform.entity.SimulationTask;
import com.marvin.floodtrafficplatform.mapper.SimulationTaskMapper;
import com.marvin.floodtrafficplatform.service.SimulationTaskService;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class SimulationTaskServiceImpl extends ServiceImpl<SimulationTaskMapper, SimulationTask> implements SimulationTaskService {

    // 这里统一维护“未完成任务”的状态集合，便于 completed 条件复用
    private static final List<String> UNFINISHED_STATUSES = Arrays.asList("PENDING", "RUNNING", "pending", "running");

    @Override
    public SimulationTask getTaskById(String taskId) {
        return getById(taskId);
    }

    @Override
    public List<SimulationTask> listTasks(String status, Boolean completed) {
        LambdaQueryWrapper<SimulationTask> queryWrapper = new LambdaQueryWrapper<>();

        // status 优先级更高：只要前端传了状态，就按状态精确筛选
        if (status != null && !status.trim().isEmpty()) {
            queryWrapper.eq(SimulationTask::getStatus, status.trim());
        } else if (completed != null) {
            // 没传 status 时，按 completed 判断查询“已完成 / 未完成”
            if (completed) {
                queryWrapper.and(wrapper -> wrapper.isNotNull(SimulationTask::getStatus)
                        .notIn(SimulationTask::getStatus, UNFINISHED_STATUSES));
            } else {
                queryWrapper.and(wrapper -> wrapper.isNull(SimulationTask::getStatus)
                        .or()
                        .in(SimulationTask::getStatus, UNFINISHED_STATUSES));
            }
        }

        // 默认按创建时间倒序，便于前端优先展示最新任务
        queryWrapper.orderByDesc(SimulationTask::getCreateTime);
        return list(queryWrapper);
    }
}
