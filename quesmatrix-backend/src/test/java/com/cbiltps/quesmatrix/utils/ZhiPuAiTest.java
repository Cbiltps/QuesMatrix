package com.cbiltps.quesmatrix.utils;

import cn.hutool.json.JSONUtil;
import com.cbiltps.quesmatrix.model.dto.question.QuestionContentDTO;
import com.cbiltps.quesmatrix.service.QuestionService;
import com.zhipu.oapi.ClientV4;
import com.zhipu.oapi.Constants;
import com.zhipu.oapi.service.v4.model.ChatCompletionRequest;
import com.zhipu.oapi.service.v4.model.ChatMessage;
import com.zhipu.oapi.service.v4.model.ChatMessageRole;
import com.zhipu.oapi.service.v4.model.ModelApiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: lichengxiang
 * Date: 2025-04-07
 * Time: 11:42
 */
@SpringBootTest
public class ZhiPuAiTest {
    @Resource
    private ClientV4 clientV4;

    @Resource
    private QuestionService questionService;

    @Test
    public void test() {
        // 初始化客户端
//        ClientV4 client = new ClientV4.Builder(KeyConstant.KEY).build();
        // 构建请求
        List<ChatMessage> messages = new ArrayList<>();
        ChatMessage chatMessage = new ChatMessage(ChatMessageRole.USER.value(), "请说说天秤座男生的性格特点?");
        messages.add(chatMessage);
//        String requestId = String.format(requestIdTemplate, System.currentTimeMillis());
        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
                .model(Constants.ModelChatGLM4)
                .stream(Boolean.FALSE)
                .invokeMethod(Constants.invokeMethod)
                .messages(messages)
                .build();
        ModelApiResponse invokeModelApiResp = clientV4.invokeModelApi(chatCompletionRequest);
        System.out.println("model output:" + invokeModelApiResp.getData().getChoices().get(0));
    }
}
