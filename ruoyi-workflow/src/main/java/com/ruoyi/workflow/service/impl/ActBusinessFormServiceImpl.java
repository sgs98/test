package com.ruoyi.workflow.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.ruoyi.common.utils.BeanCopyUtils;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.core.domain.PageQuery;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ruoyi.workflow.domain.ActProcessDefForm;
import com.ruoyi.workflow.service.IActProcessDefFormService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.ruoyi.workflow.domain.bo.ActBusinessFormBo;
import com.ruoyi.workflow.domain.vo.ActBusinessFormVo;
import com.ruoyi.workflow.domain.ActBusinessForm;
import com.ruoyi.workflow.mapper.ActBusinessFormMapper;
import com.ruoyi.workflow.service.IActBusinessFormService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * 业务表单Service业务层处理
 *
 * @author gssong
 * @date 2022-08-19
 */
@RequiredArgsConstructor
@Service
public class ActBusinessFormServiceImpl implements IActBusinessFormService {

    private final ActBusinessFormMapper baseMapper;

    private final IActProcessDefFormService iActProcessDefFormService;

    /**
     * 查询业务表单
     */
    @Override
    public ActBusinessFormVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 查询业务表单列表
     */
    @Override
    public TableDataInfo<ActBusinessFormVo> queryPageList(ActBusinessFormBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<ActBusinessForm> lqw = buildQueryWrapper(bo);
        Page<ActBusinessFormVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询业务表单列表
     */
    @Override
    public List<ActBusinessFormVo> queryList(ActBusinessFormBo bo) {
        LambdaQueryWrapper<ActBusinessForm> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<ActBusinessForm> buildQueryWrapper(ActBusinessFormBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<ActBusinessForm> lqw = Wrappers.lambdaQuery();
        lqw.eq(StringUtils.isNotBlank(bo.getFormKey()), ActBusinessForm::getFormKey, bo.getFormKey());
        lqw.like(StringUtils.isNotBlank(bo.getApplyCode()), ActBusinessForm::getApplyCode, bo.getApplyCode());
        lqw.like(StringUtils.isNotBlank(bo.getFormName()), ActBusinessForm::getFormName, bo.getFormName());
        return lqw;
    }

    /**
     * 新增业务表单
     */
    @Override
    public ActBusinessFormVo insertByBo(ActBusinessFormBo bo) {
        ActBusinessForm add = BeanUtil.toBean(bo, ActBusinessForm.class);
        String date = DateUtils.dateTime();
        //TODO 可自行设计
        Long count = baseMapper.selectCount(new LambdaQueryWrapper<>());
        add.setApplyCode("ACT"+date+(count+1));
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        ActBusinessFormVo actBusinessFormVo = new ActBusinessFormVo();
        BeanCopyUtils.copy(add,actBusinessFormVo);
        ActProcessDefForm actProcessDefForm = iActProcessDefFormService.queryByFormId(add.getFormId());
        actBusinessFormVo.setActProcessDefForm(actProcessDefForm);
        Map<String, Object> variableMap = new HashMap<>(16);
        if(ObjectUtil.isNotEmpty(actProcessDefForm) && StringUtils.isNotBlank(actProcessDefForm.getFormVariable())){
            String formValue = actBusinessFormVo.getFormValue();
            JSONObject jsonObject = JSONUtil.parseObj(formValue);
            String[] split = actProcessDefForm.getFormVariable().split(",");
            for (String variableKey : split) {
                if(jsonObject.containsKey(variableKey)){
                    Object value = jsonObject.get(variableKey);
                    variableMap.put(variableKey,value);
                }
            }
        }
        variableMap.put("businessKey",actBusinessFormVo.getId());
        actBusinessFormVo.setVariableMap(variableMap);
        return actBusinessFormVo;
    }



    /**
     * 修改业务表单
     */
    @Override
    public ActBusinessFormVo updateByBo(ActBusinessFormBo bo) {
        ActBusinessForm update = BeanUtil.toBean(bo, ActBusinessForm.class);
        validEntityBeforeSave(update);
        baseMapper.updateById(update);
        ActBusinessFormVo actBusinessFormVo = new ActBusinessFormVo();
        BeanCopyUtils.copy(update,actBusinessFormVo);
        ActProcessDefForm actProcessDefForm = iActProcessDefFormService.queryByFormId(update.getFormId());
        actBusinessFormVo.setActProcessDefForm(actProcessDefForm);
        Map<String, Object> variableMap = new HashMap<>(16);
        if(ObjectUtil.isNotEmpty(actProcessDefForm) && StringUtils.isNotBlank(actProcessDefForm.getFormVariable())){
            String formValue = actBusinessFormVo.getFormValue();
            JSONObject jsonObject = JSONUtil.parseObj(formValue);
            String[] split = actProcessDefForm.getFormVariable().split(",");
            for (String variableKey : split) {
                if(jsonObject.containsKey(variableKey)){
                    Object value = jsonObject.get(variableKey);
                    variableMap.put(variableKey,value);
                }
            }
        }
        variableMap.put("businessKey",actBusinessFormVo.getId());
        actBusinessFormVo.setVariableMap(variableMap);
        return actBusinessFormVo;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(ActBusinessForm entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 批量删除业务表单
     */
    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        if(isValid){
            //TODO 做一些业务上的校验,判断是否需要校验
        }
        return baseMapper.deleteBatchIds(ids) > 0;
    }
}
