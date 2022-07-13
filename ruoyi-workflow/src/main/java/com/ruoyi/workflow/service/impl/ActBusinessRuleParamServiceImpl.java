package com.ruoyi.workflow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import com.ruoyi.workflow.domain.ActBusinessRuleParam;
import com.ruoyi.workflow.mapper.ActBusinessRuleParamMapper;
import com.ruoyi.workflow.service.IActBusinessRuleParamService;

import java.util.List;

/**
 * 方法参数Service业务层处理
 *
 * @author gssong
 * @date 2021-12-17
 */
@Service
public class ActBusinessRuleParamServiceImpl extends ServiceImpl<ActBusinessRuleParamMapper, ActBusinessRuleParam> implements IActBusinessRuleParamService {


    @Override
    public List<ActBusinessRuleParam> queryListByBusinessRuleId(Long fullClassId) {
        LambdaQueryWrapper<ActBusinessRuleParam> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ActBusinessRuleParam::getFullClassId,fullClassId);
        queryWrapper.orderByAsc(ActBusinessRuleParam::getOrderNo);
        return baseMapper.selectList(queryWrapper);
    }
}
