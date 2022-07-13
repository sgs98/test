package com.ruoyi.workflow.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.workflow.domain.ActBusinessRuleParam;

import java.util.List;

/**
 * 方法参数Service接口
 *
 * @author gssong
 * @date 2021-12-17
 */
public interface IActBusinessRuleParamService extends IService<ActBusinessRuleParam> {

    /**
     * 按照业务规则id查询
     * @param businessRuleId
     * @return
     */
    List<ActBusinessRuleParam> queryListByBusinessRuleId(Long businessRuleId);

}
