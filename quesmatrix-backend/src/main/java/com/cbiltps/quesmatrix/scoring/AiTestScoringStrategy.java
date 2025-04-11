package com.cbiltps.quesmatrix.scoring;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
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
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

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

    @Resource
    private RedissonClient redissonClient;

    // 分布式锁的 key
    private static final String AI_ANSWER_LOCK = "AI_ANSWER_LOCK";

    /**
     * AI 评分结果本地缓存
     */
    private final Cache<String, String> answerCacheMap =
            Caffeine.newBuilder().initialCapacity(1024)
                    // 缓存 5 分钟移除
                    .expireAfterAccess(5L, TimeUnit.MINUTES)
                    .build();

    /**
     * AI 评分系统消息
     */
    @Override
    public UserAnswer doScore(List<String> choices, App app) throws Exception {
        Long appId = app.getId();
        String jsonStr = JSONUtil.toJsonStr(choices);
        String cacheKey = buildCacheKey(appId, jsonStr);
        String answerJson = answerCacheMap.getIfPresent(cacheKey);
        // 如果有缓存，直接返回
        if (StrUtil.isNotBlank(answerJson)) {
            // 构造返回值，填充答案对象的属性
            UserAnswer userAnswer = JSONUtil.toBean(answerJson, UserAnswer.class);
            userAnswer.setAppId(appId);
            userAnswer.setAppType(app.getAppType());
            userAnswer.setScoringStrategy(app.getScoringStrategy());
            userAnswer.setChoices(jsonStr);
            return userAnswer;
        }

        // 定义锁
        RLock lock = redissonClient.getLock(AI_ANSWER_LOCK + cacheKey);
        try {
            // 竞争锁
            boolean isLocked = lock.tryLock(3, 15, TimeUnit.SECONDS);
            if (!isLocked) {
                return null; // 没抢到锁, 直接返回
            }
            // 强到锁, 执行后续业务逻辑
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
            // AI 返回的评分数据转换成标准json数组
            String standardJson = questionService.convertAiScoreToJsonArray(result);
            // 缓存结果
            answerCacheMap.put(cacheKey, standardJson);
            // 3. 构造返回值，填充答案对象的属性
            UserAnswer userAnswer = JSONUtil.toBean(standardJson, UserAnswer.class);
            userAnswer.setAppId(appId);
            userAnswer.setAppType(app.getAppType());
            userAnswer.setScoringStrategy(app.getScoringStrategy());
            userAnswer.setChoices(JSONUtil.toJsonStr(choices));
            System.out.println(userAnswer);
            return userAnswer;
        } finally {
            if (lock == null && lock.isLocked()) {
                /**
                 * 当前线程持有锁, 才能释放锁
                 *
                 * 避免发生以下问题:
                 * 当A持有锁超时的时候, B以为A已经释放锁了, 此时B抢到锁.
                 * 同时A超时后释放锁, 但A释放锁释放的是B持有的锁, 导致B持有的锁被释放.
                 * 但是B的业务没执行完, C又抢到锁了
                 */
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
        }
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
        userMessage.append(app.getAppName()).append("，" + "\n" + "【【【");
        userMessage.append(app.getAppDesc()).append("】】】，" + "\n");
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

    /**
     * 构建缓存 key
     *
     * @param appId
     * @param choices
     * @return
     */
    private String buildCacheKey(Long appId, String choices) {
        return DigestUtil.md5Hex(appId + ":" + choices);
    }
}
