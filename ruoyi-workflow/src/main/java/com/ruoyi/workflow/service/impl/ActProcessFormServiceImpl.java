package com.ruoyi.workflow.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.core.domain.PageQuery;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.ruoyi.workflow.domain.bo.ActProcessFormBo;
import com.ruoyi.workflow.domain.vo.ActProcessFormVo;
import com.ruoyi.workflow.domain.ActProcessForm;
import com.ruoyi.workflow.mapper.ActProcessFormMapper;
import com.ruoyi.workflow.service.IActProcessFormService;

import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * 流程单Service业务层处理
 *
 * @author gssong
 * @date 2022-08-11
 */
@RequiredArgsConstructor
@Service
public class ActProcessFormServiceImpl implements IActProcessFormService {

    private final ActProcessFormMapper baseMapper;

    /**
     * 查询流程单
     */
    @Override
    public ActProcessFormVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 查询流程单列表
     */
    @Override
    public TableDataInfo<ActProcessFormVo> queryPageList(ActProcessFormBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<ActProcessForm> lqw = buildQueryWrapper(bo);
        Page<ActProcessFormVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询流程单列表
     */
    @Override
    public List<ActProcessFormVo> queryList(ActProcessFormBo bo) {
        LambdaQueryWrapper<ActProcessForm> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<ActProcessForm> buildQueryWrapper(ActProcessFormBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<ActProcessForm> lqw = Wrappers.lambdaQuery();
        lqw.like(StringUtils.isNotBlank(bo.getFormKey()), ActProcessForm::getFormKey, bo.getFormKey());
        lqw.like(StringUtils.isNotBlank(bo.getFormName()), ActProcessForm::getFormName, bo.getFormName());
        return lqw;
    }

    /**
     * 新增流程单
     */
    @Override
    public Boolean insertByBo(ActProcessFormBo bo) {
        ActProcessForm add = BeanUtil.toBean(bo, ActProcessForm.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改流程单
     */
    @Override
    public Boolean updateByBo(ActProcessFormBo bo) {
        ActProcessForm update = BeanUtil.toBean(bo, ActProcessForm.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(ActProcessForm entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 批量删除流程单
     */
    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        if(isValid){
            //TODO 做一些业务上的校验,判断是否需要校验
        }
        return baseMapper.deleteBatchIds(ids) > 0;
    }
}
