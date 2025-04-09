package com.cbiltps.quesmatrix.scoring;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.cbiltps.quesmatrix.manager.AiManager;
import com.cbiltps.quesmatrix.model.dto.question.QuestionAnswerDTO;
import com.cbiltps.quesmatrix.model.dto.question.QuestionContentDTO;
import com.cbiltps.quesmatrix.model.entity.App;
import com.cbiltps.quesmatrix.model.entity.Question;
import com.cbiltps.quesmatrix.model.entity.UserAnswer;
import com.cbiltps.quesmatrix.model.vo.QuestionVO;
import com.cbiltps.quesmatrix.service.QuestionService;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

import static com.cbiltps.quesmatrix.constant.AIConstant.AI_TEST_SCORING_SYSTEM_MESSAGE;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: lichengxiang
 * Date: 2025-04-07
 * Time: 18:24
 */
@ScoringStrategyConfig(appType = 1, scoringStrategy = 1)
public class AiTestScoringStrategy implements ScoringStrategy {

    @Resource
    private QuestionService questionService;

    @Resource
    private AiManager aiManager;

    /**
     * AI 评分系统消息
     */
    @Override
    public UserAnswer doScore(List<String> choices, App app) throws Exception {
        Long appId = app.getId();
        // 1. 根据 id 查询到题目
        Question question = questionService.getOne(
                Wrappers.lambdaQuery(Question.class).eq(Question::getAppId, appId)
        );
        QuestionVO questionVO = QuestionVO.objToVo(question);
        List<QuestionContentDTO> questionContent = questionVO.getQuestionContent();

        // 2. 调用 AI 获取结果
        // 封装 Prompt
        String userMessage = getAiTestScoringUserMessage(app, questionContent, choices);
        // AI 生成
        String result = aiManager.doSyncStableRequest(AI_TEST_SCORING_SYSTEM_MESSAGE, userMessage);
        System.out.println(result);
        // AI 返回的评分数据转换成标准json数组
        String standardJson = questionService.convertAiScoreToJsonArray(result);
        // 3. 构造返回值，填充答案对象的属性
        UserAnswer userAnswer = JSONUtil.toBean(standardJson, UserAnswer.class);
        userAnswer.setAppId(appId);
        userAnswer.setAppType(app.getAppType());
        userAnswer.setScoringStrategy(app.getScoringStrategy());
        userAnswer.setChoices(JSONUtil.toJsonStr(choices));
        return userAnswer;
    }

    /**
     * AI 评分用户消息封装
     *
     * @param app
     * @param questionContentDTOList
     * @param choices
     * @return
     */
    private String getAiTestScoringUserMessage(App app, List<QuestionContentDTO> questionContentDTOList, List<String> choices) {
        StringBuilder userMessage = new StringBuilder();
        userMessage.append(app.getAppName()).append("，" +"\n" + "【【【");
        userMessage.append(app.getAppDesc()).append("】】】，" +"\n");
        List<QuestionAnswerDTO> questionAnswerDTOList = new ArrayList<>();
        for (int i = 0; i < questionContentDTOList.size(); i++) {
            QuestionAnswerDTO questionAnswerDTO = new QuestionAnswerDTO();
            questionAnswerDTO.setTitle(questionContentDTOList.get(i).getTitle());
            questionAnswerDTO.setUserAnswer(choices.get(i));
            questionAnswerDTOList.add(questionAnswerDTO);
        }
        userMessage.append(JSONUtil.toJsonStr(questionAnswerDTOList));
        return userMessage.toString();
    }
}
