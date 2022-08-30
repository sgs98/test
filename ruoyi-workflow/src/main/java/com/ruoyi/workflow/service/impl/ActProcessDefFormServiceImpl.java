package com.ruoyi.workflow.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.validate.AddGroup;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.core.domain.PageQuery;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ruoyi.common.utils.ValidatorUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.ruoyi.workflow.domain.bo.ActProcessDefFormBo;
import com.ruoyi.workflow.domain.vo.ActProcessDefFormVo;
import com.ruoyi.workflow.domain.ActProcessDefForm;
import com.ruoyi.workflow.mapper.ActProcessDefFormMapper;
import com.ruoyi.workflow.service.IActProcessDefFormService;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 流程定义与单配置Service业务层处理
 *
 * @author gssong
 * @date 2022-08-28
 */
@RequiredArgsConstructor
@Service
public class ActProcessDefFormServiceImpl implements IActProcessDefFormService {

    private final ActProcessDefFormMapper baseMapper;

    /**
     * 查询流程定义与单配置
     */
    @Override
    public ActProcessDefFormVo queryById(Long id) {
        return baseMapper.selectVoById(id);
    }

    @Override
    public ActProcessDefFormVo getProcessDefFormByDefId(String defId) {
        LambdaQueryWrapper<ActProcessDefForm> lqw = Wrappers.lambdaQuery();
        lqw.eq(ActProcessDefForm::getProcessDefinitionId, defId);
        return baseMapper.selectVoOne(lqw);
    }

    @Override
    public String checkProcessDefFormByFormId(String defId, String formId) {
        LambdaQueryWrapper<ActProcessDefForm> lqw = Wrappers.lambdaQuery();
        lqw.eq(ActProcessDefForm::getFormId, formId);
        lqw.ne(ActProcessDefForm::getProcessDefinitionId, defId);
        List<ActProcessDefForm> processDefForms = baseMapper.selectList(lqw);
        if (CollectionUtil.isNotEmpty(processDefForms)) {
            String collect = processDefForms.stream().map(ActProcessDefForm::getProcessDefinitionKey).collect(Collectors.joining(","));
            return "表单已被流程【" + collect + "】绑定，是否确认删除绑定，绑定当前表单？";
        }
        return null;
    }

    /**
     * 查询流程定义与单配置列表
     */
    @Override
    public TableDataInfo<ActProcessDefFormVo> queryPageList(ActProcessDefFormBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<ActProcessDefForm> lqw = buildQueryWrapper(bo);
        Page<ActProcessDefFormVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询流程定义与单配置列表
     */
    @Override
    public List<ActProcessDefFormVo> queryList(ActProcessDefFormBo bo) {
        LambdaQueryWrapper<ActProcessDefForm> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<ActProcessDefForm> buildQueryWrapper(ActProcessDefFormBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<ActProcessDefForm> lqw = Wrappers.lambdaQuery();
        lqw.eq(StringUtils.isNotBlank(bo.getProcessDefinitionId()), ActProcessDefForm::getProcessDefinitionId, bo.getProcessDefinitionId());
        lqw.eq(StringUtils.isNotBlank(bo.getProcessDefinitionKey()), ActProcessDefForm::getProcessDefinitionKey, bo.getProcessDefinitionKey());
        lqw.like(StringUtils.isNotBlank(bo.getProcessDefinitionName()), ActProcessDefForm::getProcessDefinitionName, bo.getProcessDefinitionName());
        lqw.eq(bo.getFormId() != null, ActProcessDefForm::getFormId, bo.getFormId());
        lqw.eq(StringUtils.isNotBlank(bo.getFormKey()), ActProcessDefForm::getFormKey, bo.getFormKey());
        lqw.like(StringUtils.isNotBlank(bo.getFormName()), ActProcessDefForm::getFormName, bo.getFormName());
        lqw.eq(StringUtils.isNotBlank(bo.getFormVariable()), ActProcessDefForm::getFormVariable, bo.getFormVariable());
        return lqw;
    }

    /**
     * 新增流程定义与单配置
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean insertByBo(ActProcessDefFormBo bo) {
        ValidatorUtils.validate(bo, AddGroup.class);
        ActProcessDefForm add = BeanUtil.toBean(bo, ActProcessDefForm.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改流程定义与单配置
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateByBo(ActProcessDefFormBo bo) {
        ActProcessDefForm update = BeanUtil.toBean(bo, ActProcessDefForm.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(ActProcessDefForm entity) {
        LambdaQueryWrapper<ActProcessDefForm> lqw = Wrappers.lambdaQuery();
        lqw.eq(ActProcessDefForm::getFormId, entity.getFormId());
        baseMapper.delete(lqw);
    }

    /**
     * 批量删除流程定义与单配置
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        if (isValid) {
            //TODO 做一些业务上的校验,判断是否需要校验
        }
        return baseMapper.deleteBatchIds(ids) > 0;
    }

    /**
     * @Description: 按照表单id查询
     * @param: formId
     * @return: com.ruoyi.workflow.domain.ActProcessDefForm
     * @author: gssong
     * @Date: 2022/8/30 22:10
     */
    @Override
    public ActProcessDefForm queryByFormId(Long formId) {
        LambdaQueryWrapper<ActProcessDefForm> lqw = Wrappers.lambdaQuery();
        lqw.eq(ActProcessDefForm::getFormId, formId);
        return baseMapper.selectOne(lqw);
    }
}
