package com.marvin.floodtrafficplatform.service.impl;

import com.marvin.floodtrafficplatform.dto.SimulationParamDTO;
import com.marvin.floodtrafficplatform.entity.NodeOverflowResult;
import com.marvin.floodtrafficplatform.service.SimulationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
@Service
public class SimulationServiceImpl implements SimulationService {

    @Autowired
    private RestTemplate restTemplate;
    // ================= 注入 Mapper 和转换工具 =================
    @Autowired
    private com.marvin.floodtrafficplatform.mapper.SimulationTaskMapper simulationTaskMapper;

    @Autowired
    private com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    // 在类最上方注入写好的 Mapper
    @Autowired
    private com.marvin.floodtrafficplatform.mapper.NodeOverflowResultMapper nodeOverflowResultMapper;
    // ================================================================

    // P1-3: Python FastAPI 地址统一走 application.yml（原硬编码 localhost:8000）
    @org.springframework.beans.factory.annotation.Value("${python.base-url:http://localhost:8000}")
    private String pythonBaseUrl;

    private String pythonApiUrl() {
        return pythonBaseUrl + "/api/simulation";
    }
    // ================= 自动化仿真方法实现 =================
    @Override
    public Map<String, Object> startPythonSimulation(SimulationParamDTO params) {
        try {
            System.out.println("🚀 准备向 Python 发起仿真请求...");

            // ================= 处理选填的 regionBoundary =================
            if (params.getRegionBoundary() == null || params.getRegionBoundary().trim().isEmpty()) {
                System.out.println("⚠️ 未接收到自定义研究区域，默认应用天津市滨海区配置");
                params.setRegionBoundary("DEFAULT_BINHAI_AREA");
            } else {
                System.out.println("📍 检测到自定义边界数据，将一并透传给 Python");
            }

            // 这里拼接 "/start" 后，完整的 URL 刚好是正确的
            ResponseEntity<Map> response = restTemplate.postForEntity(pythonApiUrl() + "/start", params, Map.class);

            // 获取 Python 返回的字典
            Map<String, Object> responseBody = response.getBody();
            System.out.println("✅ Python 成功接收并返回: " + responseBody);

            // ================= 新增：在数据库里建档 =================
            if (responseBody != null && responseBody.containsKey("task_id")) {
                String taskId = (String) responseBody.get("task_id");

                com.marvin.floodtrafficplatform.entity.SimulationTask task = new com.marvin.floodtrafficplatform.entity.SimulationTask();
                task.setTaskId(taskId);
                task.setStatus("running");
                task.setCreateTime(java.time.LocalDateTime.now());

                // 把传给后端的 params 对象转成 Map 存进 JSONB 字段，保留犯罪现场
                Map<String, Object> paramMap = objectMapper.convertValue(params, Map.class);
                task.setParameters(paramMap);

                // 执行插入数据库
                simulationTaskMapper.insert(task);
                System.out.println("📁 数据库建档成功！任务ID: " + taskId);
            }
            // =========================================================

            return responseBody;

        } catch (Exception e) {
            System.err.println("❌ 调用 Python 仿真服务失败，请检查 Python 端是否已启动！");
            e.printStackTrace();

            // 构造一个 Map 来代替原来的 String 返回值
            Map<String, Object> errorMap = new java.util.HashMap<>();
            errorMap.put("status", "error");
            errorMap.put("message", "调用后端 Python 算法失败：" + e.getMessage());

            // 返回符合规范的 Map
            return errorMap;
        }
    }
    // ================= 去 Python 查进度方法实现 =================
    @Override
    public Map<String, Object> getSimulationProgress(String taskId) {
        try {
            // 拼装查询进度的 URL
            String url = pythonApiUrl() + "/progress/" + taskId;
            // 发起 GET 请求
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            return response.getBody();
        } catch (Exception e) {
            System.err.println("❌ 查询进度失败: " + e.getMessage());
            Map<String, Object> errorMap = new java.util.HashMap<>();
            errorMap.put("status", "error");
            errorMap.put("message", "查询进度失败: " + e.getMessage());
            errorMap.put("progress", 0);
            return errorMap;
        }
    }
    // ================= 获取管点节点溢流数据方法实现   =================
    @Override
    public Map<String, Object> getNodeOverflowData(String taskId, String nodeId) {
        // 构造查询条件
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.marvin.floodtrafficplatform.entity.NodeOverflowResult> queryWrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        queryWrapper.eq(com.marvin.floodtrafficplatform.entity.NodeOverflowResult::getTaskId, taskId)
                .eq(com.marvin.floodtrafficplatform.entity.NodeOverflowResult::getNodeId, nodeId)
                .last("LIMIT 1"); // 🌟 终极防弹衣：哪怕数据库里有一万条重复的脏数据，我只拿最新的一条！

        // 执行查询
        com.marvin.floodtrafficplatform.entity.NodeOverflowResult result = nodeOverflowResultMapper.selectOne(queryWrapper);

        // 包装成前端 Echarts 需要的格式
        Map<String, Object> map = new java.util.HashMap<>();
        if (result != null) {
            map.put("times", result.getTimeSeries());
            map.put("inflows", result.getInflowSeries());
        }
        return map;
    }

    // ================= 批量获取存在溢流的管点 ID 列表 =================
    @Override
    public List<String> getOverflowedNodeIds(String taskId) {
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.marvin.floodtrafficplatform.entity.NodeOverflowResult> queryWrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        queryWrapper.eq(com.marvin.floodtrafficplatform.entity.NodeOverflowResult::getTaskId, taskId);

        List<NodeOverflowResult> results = nodeOverflowResultMapper.selectList(queryWrapper);

        List<String> overflowedIds = new java.util.ArrayList<>();
        for (com.marvin.floodtrafficplatform.entity.NodeOverflowResult result : results) {
            List<Double> inflows = result.getInflowSeries();
            if (inflows != null && !inflows.isEmpty()) {
                boolean hasOverflow = false;
                for (Double val : inflows) {
                    if (val != null && val > 0) {
                        hasOverflow = true;
                        break;
                    }
                }
                if (hasOverflow) {
                    overflowedIds.add(result.getNodeId());
                }
            }
        }
        return overflowedIds;
    }

    // ================= 查询断点状态 =================
    @Override
    public Map<String, Object> getCheckpoint(String taskId) {
        try {
            String url = pythonApiUrl() + "/checkpoint/" + taskId;
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            return response.getBody();
        } catch (Exception e) {
            System.err.println("❌ 查询断点状态失败: " + e.getMessage());
            Map<String, Object> errorMap = new java.util.HashMap<>();
            errorMap.put("status", "error");
            errorMap.put("message", "查询断点状态失败: " + e.getMessage());
            return errorMap;
        }
    }

    // ================= 发起断点续跑 =================
    @Override
    public Map<String, Object> resumeSimulation(String taskId) {
        try {
            String url = pythonApiUrl() + "/resume/" + taskId;
            ResponseEntity<Map> response = restTemplate.postForEntity(url, null, Map.class);
            return response.getBody();
        } catch (Exception e) {
            System.err.println("❌ 断点续跑请求失败: " + e.getMessage());
            Map<String, Object> errorMap = new java.util.HashMap<>();
            errorMap.put("status", "error");
            errorMap.put("message", "断点续跑请求失败: " + e.getMessage());
            return errorMap;
        }
    }

    // ================= 删除任务本地产物目录 =================
    @Override
    public boolean deleteTaskLocalFiles(String taskId) {
        try {
            // Python 项目根目录（与 Java 项目同级）
            java.io.File pythonProjectDir = new java.io.File("D:/BaiduSyncdisk/GraduateSource/Research/Major/main/swmm-wca2d");
            String[] subDirs = {"data/swmm_tasks", "data/csvtest", "data/output", "data/outtif"};
            boolean anyDeleted = false;
            for (String sub : subDirs) {
                java.io.File dir = new java.io.File(pythonProjectDir, sub + "/" + taskId);
                if (dir.exists() && dir.isDirectory()) {
                    deleteDirectory(dir);
                    System.out.println("🗑️ 已删除目录: " + dir.getAbsolutePath());
                    anyDeleted = true;
                }
            }
            return anyDeleted;
        } catch (Exception e) {
            System.err.println("❌ 删除本地文件失败: " + e.getMessage());
            return false;
        }
    }

    private void deleteDirectory(java.io.File dir) {
        java.io.File[] files = dir.listFiles();
        if (files != null) {
            for (java.io.File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
        dir.delete();
    }
}