package com.cbiltps.quesmatrix.controller;

import com.cbiltps.quesmatrix.common.BaseResponse;
import com.cbiltps.quesmatrix.common.ErrorCode;
import com.cbiltps.quesmatrix.common.ResultUtils;
import com.cbiltps.quesmatrix.exception.ThrowUtils;
import com.cbiltps.quesmatrix.mapper.UserAnswerMapper;
import com.cbiltps.quesmatrix.model.dto.statistic.AppAnswerCountDTO;
import com.cbiltps.quesmatrix.model.dto.statistic.AppAnswerResultCountDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Description: App 统计分析接口
 * User: lichengxiang
 * Date: 2025-04-14
 * Time: 06:33
 */
@RestController
@RequestMapping("/app/statistic")
@Slf4j
public class AppStatisticController {

    @Resource
    private UserAnswerMapper userAnswerMapper;

    /**
     * 热门应用及回答数统计（top 10）
     *
     * @return
     */
    @GetMapping("/answer_count")
    public BaseResponse<List<AppAnswerCountDTO>> getAppAnswerCount() {
        System.out.println(userAnswerMapper.doAppAnswerCount());
        return ResultUtils.success(userAnswerMapper.doAppAnswerCount());
    }

    /**
     * 某应用回答结果分布统计
     *
     * @param appId
     * @return
     */
    @GetMapping("/answer_result_count")
    public BaseResponse<List<AppAnswerResultCountDTO>> getAppAnswerResultCount(Long appId) {
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR);
        System.out.println(userAnswerMapper.doAppAnswerResultCount(appId));
        return ResultUtils.success(userAnswerMapper.doAppAnswerResultCount(appId));
    }
}
