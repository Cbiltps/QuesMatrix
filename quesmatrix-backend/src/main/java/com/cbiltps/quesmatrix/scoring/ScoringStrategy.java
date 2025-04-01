package com.cbiltps.quesmatrix.scoring;

import com.cbiltps.quesmatrix.model.entity.App;
import com.cbiltps.quesmatrix.model.entity.UserAnswer;

import java.util.List;

/**
 * 评分策略
 *
 * @author 
 * @from 
 */
public interface ScoringStrategy {

    /**
     * 执行评分
     *
     * @param choices
     * @param app
     * @return
     * @throws Exception
     */
    UserAnswer doScore(List<String> choices, App app) throws Exception;
}