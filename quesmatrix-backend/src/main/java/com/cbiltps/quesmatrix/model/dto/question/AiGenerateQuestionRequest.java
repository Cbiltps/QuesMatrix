package com.cbiltps.quesmatrix.model.dto.question;

import lombok.Data;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * Description: AI 生成题目请求
 * User: lichengxiang
 * Date: 2025-04-07
 * Time: 09:50
 */
@Data
public class AiGenerateQuestionRequest implements Serializable {
    /**
     * 应用 id
     */
    private Long appId;

    /**
     * 题目数
     */
    int questionNumber = 10;

    /**
     * 选项数
     */
    int optionNumber = 2;

    private static final long serialVersionUID = 1L;
}
