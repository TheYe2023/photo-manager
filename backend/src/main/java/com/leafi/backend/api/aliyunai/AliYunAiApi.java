package com.leafi.backend.api.aliyunai;

import com.leafi.backend.api.aliyunai.model.CreateAiAnalysisRequest;
import com.leafi.backend.api.aliyunai.AliYunAiApi;
import com.leafi.backend.api.aliyunai.model.CreateAiAnalysisResponse;
import com.leafi.backend.api.aliyunai.model.GetAiAnalysisResponse;
import com.leafi.backend.api.aliyunai.model.SyncAiResponse;
import com.leafi.backend.exception.BusinessException;
import com.leafi.backend.exception.ErrorCode;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class AliYunAiApi {

    @Value("${aliYunAi.apiKey}")
    private String apiKey;

    private static final String CREATE_TASK_URL = "https://dashscope.aliyuncs.com/api/v1/services/aigc/multimodal-generation/generation"; // 修正为百炼标准异步地址
    private static final String GET_RESULT_URL = "https://dashscope.aliyuncs.com/api/v1/tasks/";

    /**
     * 创建异步图像分析任务
     * @param request 封装好的请求对象
     * @return 任务 ID (taskId)
     */
    public List<String> getPictureTags(CreateAiAnalysisRequest request) {
        if (request == null || request.getInput() == null) {
            throw new IllegalArgumentException("请求参数或输入内容不能为空");
        }

        Map<String, Object> input = new HashMap<>();
        List<Map<String, Object>> messages = new ArrayList<>();
        
        Map<String, Object> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        
        List<Map<String, String>> content = new ArrayList<>();
        // 必须包含 image 字段
        content.add(Collections.singletonMap("image", request.getInput().getImageUrl()));
        content.add(Collections.singletonMap("text", "分析这张图片并提取 5 个最重要的标签。\r\n" + //
                        "标签可以涵盖：核心主体、视觉风格、拍摄/绘图场景、主要色调（如 莫兰迪色、暖阳黄）等等。\r\n" + //
                        "要求：\r\n" + //
                        "1. 结果以 JSON 格式返回，例如：{\"tags\": [\"雪人\", \"写实\", \"户外\", \"冷色调\", \"冬季\"]}。\r\n" + //
                        "2. 严禁返回 JSON 以外的任何文字说明。\r\n" + //
                        "3. 确保标签简洁，每个标签不超过 4 个字。"));
        
        userMessage.put("content", content);
        messages.add(userMessage);
        input.put("messages", messages);

        // 3. 构造完整 Payload
        Map<String, Object> payload = new HashMap<>();
        payload.put("model", "qwen3-vl-plus"); // 确保模型名称正确
        payload.put("input", input);

        cn.hutool.http.HttpResponse httpResponse = cn.hutool.http.HttpRequest.post(CREATE_TASK_URL)
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                // .header("X-DashScope-Async", "disable") // 开启异步，返回 task_id
                .body(JSONUtil.toJsonStr(payload))
                .execute();

        String body = httpResponse.body();
        log.info("阿里云原始响应内容: {}", body);

        return syncAiResponse(body);
    }

    /**
     * 查询结果
     */
    public GetAiAnalysisResponse getAiAnalysisResponse(String taskId) {
        String jsonResponse = cn.hutool.http.HttpRequest.get(GET_RESULT_URL + taskId)
                .header("Authorization", "Bearer " + apiKey)
                .execute().body();

        log.info("查询任务结果: {}", jsonResponse);
        return JSONUtil.toBean(jsonResponse, GetAiAnalysisResponse.class);
    }

    /**
     * 解析 AI 返回的结果，提取文本内容
     * @param jsonResponse AI 返回的完整 JSON 响应字符串
     * @return 提取的文本内容
     */
    public List<String> syncAiResponse(String aliyunRawResponse) {
        // 1. 基础非空校验
        if (StrUtil.isBlank(aliyunRawResponse)) {
            return Collections.emptyList();
        }

        try {
            JSONObject fullJson = JSONUtil.parseObj(aliyunRawResponse);

            // 2. 使用链式安全路径获取 (如果中间某一层不存在，getByPath 会返回 null 而不是报错)
            // 路径对应：output.choices[0].message.content[0].text
            Object textObj = fullJson.getByPath("output.choices[0].message.content[0].text");
            
            if (textObj == null) {
                return Collections.emptyList();
            }
            String rawText = textObj.toString();
            rawText = rawText.replaceAll("(?i)```json", "")
                            .replaceAll("```", "")
                            .trim();

            // 4. 第二次解析：解析 text 字符串里的 JSON
            if (!JSONUtil.isTypeJSON(rawText)) {
                return Collections.emptyList();
            }
            
            JSONObject textJson = JSONUtil.parseObj(rawText);
            JSONArray tagsArray = textJson.getJSONArray("tags");

            // 5. 最终转换
            if (tagsArray == null) {
                return Collections.emptyList();
            }
            return tagsArray.toList(String.class);

        } catch (Exception e) {
            log.error("解析 AI 响应数据失败: ", e);
            return Collections.emptyList();
        }
    }
}