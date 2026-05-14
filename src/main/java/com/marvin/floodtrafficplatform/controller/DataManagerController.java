package com.marvin.floodtrafficplatform.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marvin.floodtrafficplatform.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import java.util.Collections;
import java.util.Map;

/**
 * 后台数据资产管理中心 Java 中转层 Controller
 * 统一转发到 Python 服务 (8000 端口)
 */
@RestController
@RequestMapping("/data-manager")
public class DataManagerController {

    @Value("${python.service.url:http://localhost:8000}")
    private String pythonServiceUrl;

    @Autowired
    private RestTemplate restTemplate;

    private static final String PYTHON_DM_PREFIX = "/api/data-manager";

    /**
     * 资产发现 GET /data-manager/assets
     */
    @GetMapping("/assets")
    public Result<?> listAssets() {
        String url = pythonServiceUrl + PYTHON_DM_PREFIX + "/assets";
        String resp = restTemplate.getForObject(url, String.class);
        return parsePythonResponse(resp);
    }
    /**
     * 源码预览 GET /data-manager/preview?path=xxx
     * 采用占位符绑定防范 SSRF 与 Open Redirect 注入
     */
    @GetMapping("/preview")
    public Result<?> previewSource(@RequestParam("path") String path) {
        // 1. 基础路径绝不直接拼接外部参数，仅使用静态受控常量
        String baseUrl = pythonServiceUrl + PYTHON_DM_PREFIX + "/preview?path={path}";

        // 2. 将不受信任的 path 放入变量 Map 中，交由底层进行绝对安全的转义与绑定
        Map<String, String> uriVariables = Collections.singletonMap("path", path);

        // 3. 执行请求
        String resp = restTemplate.getForObject(baseUrl, String.class, uriVariables);
        return parsePythonResponse(resp);
    }

    /**
     * 物理删除 DELETE /data-manager/delete
     */
    @DeleteMapping("/delete")
    public Result<?> deleteAsset(@RequestBody Map<String, Object> body) {
        String url = pythonServiceUrl + PYTHON_DM_PREFIX + "/delete";
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(body);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.DELETE, requestEntity, String.class);
        return parsePythonResponse(response.getBody());
    }

    /**
     * 解析 Python 返回的 JSON 字符串，统一包装为标准 Result<?>
     */
    private Result<?> parsePythonResponse(String resp) {
        if (resp == null || resp.isEmpty()) {
            return Result.error("Python 服务无响应");
        }

        try {
            // 使用 Spring Boot 原生内置的 ObjectMapper
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(resp);

            // 安全提取字段
            int code = rootNode.has("code") ? rootNode.get("code").asInt() : 500;
            String message = rootNode.has("message") ? rootNode.get("message").asText() : null;

            // 提取真正的 payload 载荷对象
            Object data = null;
            if (rootNode.has("data") && !rootNode.get("data").isNull()) {
                // 将 data 节点转为通用的 Object 结构供 Result 包装
                data = mapper.convertValue(rootNode.get("data"), Object.class);
            }

            if (code == 200) {
                Result<Object> result = Result.success(data);
                if (message != null) {
                    result.setMsg(message);
                }
                return result;
            }

            return Result.error(code, message != null ? message : "Python 服务异常");
        } catch (Exception e) {
            return Result.error("解析 Python 响应失败: " + e.getMessage());
        }
    }
}
