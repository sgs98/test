package com.ruoyi.workflow.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.common.core.domain.PageQuery;
import com.ruoyi.common.core.validate.AddGroup;
import com.ruoyi.common.core.validate.EditGroup;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.utils.ValidatorUtils;
import com.ruoyi.workflow.domain.ActBusinessRuleParam;
import com.ruoyi.workflow.service.IActBusinessRuleParamService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ruoyi.workflow.domain.bo.ActBusinessRuleBo;
import com.ruoyi.workflow.domain.vo.ActBusinessRuleVo;
import com.ruoyi.workflow.domain.ActBusinessRule;
import com.ruoyi.workflow.mapper.ActBusinessRuleMapper;
import com.ruoyi.workflow.service.IActBusinessRuleService;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * 业务规则Service业务层处理
 *
 * @author gssong
 * @date 2021-12-16
 */
@RequiredArgsConstructor
@Service
public class ActBusinessRuleServiceImpl implements IActBusinessRuleService {

    private final ActBusinessRuleMapper baseMapper;
    @Autowired
    private IActBusinessRuleParamService iActBusinessRuleParamService;

    @Override
    public ActBusinessRuleVo queryById(Long id){
        List<ActBusinessRuleParam> list = iActBusinessRuleParamService.queryListByBusinessRuleId(id);
        ActBusinessRuleVo vo = baseMapper.selectVoById(id);
        vo.setBusinessRuleParams(list);
        return vo;
    }

    @Override
    public TableDataInfo<ActBusinessRuleVo> queryPageList(ActBusinessRuleBo bo, PageQuery pageQuery) {
        Page<ActBusinessRuleVo> result = baseMapper.selectVoPage(pageQuery.build(), buildQueryWrapper(bo));
        return TableDataInfo.build(result);
    }

    @Override
    public List<ActBusinessRuleVo> queryList(ActBusinessRuleBo bo) {
        return baseMapper.selectVoList(buildQueryWrapper(bo));
    }

    private LambdaQueryWrapper<ActBusinessRule> buildQueryWrapper(ActBusinessRuleBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<ActBusinessRule> lqw = Wrappers.lambdaQuery();
        lqw.like(StringUtils.isNotBlank(bo.getFullClass()), ActBusinessRule::getFullClass, bo.getFullClass());
        lqw.like(StringUtils.isNotBlank(bo.getMethod()), ActBusinessRule::getMethod, bo.getMethod());
        return lqw;
    }

    @Override
    public Boolean insertByBo(ActBusinessRuleBo bo) {
        ActBusinessRule add = BeanUtil.toBean(bo, ActBusinessRule.class);
        validEntityBeforeSave(add);
        int flag = baseMapper.insert(add);
        if (flag>0) {
            bo.setId(add.getId());
        }
        List<ActBusinessRuleParam> actBusinessRuleParams = bo.getBusinessRuleParams();
        if(CollectionUtil.isNotEmpty(actBusinessRuleParams)){
            actBusinessRuleParams.forEach(e->{
                e.setFullClassId(add.getId());
                ValidatorUtils.validate(e, AddGroup.class);
            });
            iActBusinessRuleParamService.saveBatch(actBusinessRuleParams);
        }
        return flag>0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateByBo(ActBusinessRuleBo bo) {
        ActBusinessRule update = BeanUtil.toBean(bo, ActBusinessRule.class);
        validEntityBeforeSave(update);
        List<ActBusinessRuleParam> actBusinessRuleParams = bo.getBusinessRuleParams();
        iActBusinessRuleParamService.remove(new LambdaQueryWrapper<ActBusinessRuleParam>().eq(ActBusinessRuleParam::getFullClassId,update.getId()));
        if(CollectionUtil.isNotEmpty(actBusinessRuleParams)){
            actBusinessRuleParams.forEach(e->{
                e.setFullClassId(update.getId());
                ValidatorUtils.validate(e, EditGroup.class);
            });
            iActBusinessRuleParamService.saveBatch(actBusinessRuleParams);
        }
        return baseMapper.updateById(update)>0;
    }

    /**
     * 保存前的数据校验
     *
     * @param entity 实体类数据
     */
    private void validEntityBeforeSave(ActBusinessRule entity){
        //TODO 做一些数据校验,如唯一约束
    }

    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        if(isValid){
            iActBusinessRuleParamService.remove(new LambdaQueryWrapper<ActBusinessRuleParam>().in(ActBusinessRuleParam::getFullClassId,ids));
        }
        return baseMapper.deleteBatchIds(ids)>0;
    }
}
