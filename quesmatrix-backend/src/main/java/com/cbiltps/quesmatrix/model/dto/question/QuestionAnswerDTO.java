package com.cbiltps.quesmatrix.model.dto.question;

import lombok.Data;

/**
 * Created with IntelliJ IDEA.
 * Description: 题目答案封装类（用于 AI 评分）
 * User: lichengxiang
 * Date: 2025-04-07
 * Time: 18:21
 */
@Data
public class QuestionAnswerDTO {

    /**
     * 题目
     */
    private String title;

    /**
     * 用户答案
     */
    private String userAnswer;
}
