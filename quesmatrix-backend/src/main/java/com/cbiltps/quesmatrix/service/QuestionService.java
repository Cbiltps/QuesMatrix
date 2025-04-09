package com.cbiltps.quesmatrix.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cbiltps.quesmatrix.model.dto.question.QuestionQueryRequest;
import com.cbiltps.quesmatrix.model.entity.Question;
import com.cbiltps.quesmatrix.model.vo.QuestionVO;

import javax.servlet.http.HttpServletRequest;

/**
 * 题目服务
 *
 * @author 
 * @from 
 */
public interface QuestionService extends IService<Question> {

    /**
     * 校验数据
     *
     * @param question
     * @param add 对创建的数据进行校验
     */
    void validQuestion(Question question, boolean add);

    /**
     * 获取查询条件
     *
     * @param questionQueryRequest
     * @return
     */
    QueryWrapper<Question> getQueryWrapper(QuestionQueryRequest questionQueryRequest);
    
    /**
     * 获取题目封装
     *
     * @param question
     * @param request
     * @return
     */
    QuestionVO getQuestionVO(Question question, HttpServletRequest request);

    /**
     * 分页获取题目封装
     *
     * @param questionPage
     * @param request
     * @return
     */
    Page<QuestionVO> getQuestionVOPage(Page<Question> questionPage, HttpServletRequest request);

    /**
     * 将AI返回结果转换为标准json数组
     * @param input
     * @return
     */
    String convertAiQuestionsToJsonArray(String input);

    String convertAiScoreToJsonArray(String result);
}
