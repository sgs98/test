package com.ruoyi.workflow.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.core.domain.PageQuery;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ruoyi.workflow.domain.ActProcessDefSetting;
import com.ruoyi.workflow.mapper.ActProcessDefSettingMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.ruoyi.workflow.domain.bo.ActProcessDefSettingBo;
import com.ruoyi.workflow.domain.vo.ActProcessDefSettingVo;
import com.ruoyi.workflow.service.IActProcessDefSetting;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * 流程定义设置Service业务层处理
 *
 * @author gssong
 * @date 2022-08-28
 */
@RequiredArgsConstructor
@Service
public class ActProcessDefSettingImpl implements IActProcessDefSetting {

    private final ActProcessDefSettingMapper baseMapper;

    /**
     * 查询流程定义设置
     */
    @Override
    public ActProcessDefSettingVo queryById(Long id) {
        return baseMapper.selectVoById(id);
    }

    @Override
    public ActProcessDefSettingVo getProcessDefSettingByDefId(String defId) {
        LambdaQueryWrapper<ActProcessDefSetting> lqw = Wrappers.lambdaQuery();
        lqw.eq(ActProcessDefSetting::getProcessDefinitionId, defId);
        return baseMapper.selectVoOne(lqw);
    }

    @Override
    public List<ActProcessDefSettingVo> getProcessDefSettingByDefIds(List<String> defIds) {
        LambdaQueryWrapper<ActProcessDefSetting> lqw = Wrappers.lambdaQuery();
        lqw.in(ActProcessDefSetting::getProcessDefinitionId, defIds);
        return baseMapper.selectVoList(lqw);
    }

    @Override
    public R<Void> checkProcessDefSetting(String defId, String param, Integer businessType) {
        LambdaQueryWrapper<ActProcessDefSetting> lqw = Wrappers.lambdaQuery();
        lqw.ne(ActProcessDefSetting::getProcessDefinitionId, defId);
        if (0 == businessType) {
            lqw.eq(ActProcessDefSetting::getFormId, param);
            List<ActProcessDefSetting> processDefSettings = baseMapper.selectList(lqw);
            if (CollectionUtil.isNotEmpty(processDefSettings)) {
                String collect = processDefSettings.stream().map(ActProcessDefSetting::getProcessDefinitionKey).collect(Collectors.joining(","));
                return R.ok("表单已被流程【" + collect + "】绑定，是否确认删除绑定，绑定当前选项？");
            }
        } else {
            lqw.eq(ActProcessDefSetting::getComponentName, param);
            List<ActProcessDefSetting> processDefSettings = baseMapper.selectList(lqw);
            if (CollectionUtil.isNotEmpty(processDefSettings)) {
                String collect = processDefSettings.stream().map(ActProcessDefSetting::getComponentName).collect(Collectors.joining(","));
                return R.ok( "组件已被流程【" + collect + "】绑定，是否确认删除绑定，绑定当前选项？");
            }
        }
        return R.ok();
    }

    /**
     * 查询流程定义设置列表
     */
    @Override
    public TableDataInfo<ActProcessDefSettingVo> queryPageList(ActProcessDefSettingBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<ActProcessDefSetting> lqw = buildQueryWrapper(bo);
        Page<ActProcessDefSettingVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询流程定义设置列表
     */
    @Override
    public List<ActProcessDefSettingVo> queryList(ActProcessDefSettingBo bo) {
        LambdaQueryWrapper<ActProcessDefSetting> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<ActProcessDefSetting> buildQueryWrapper(ActProcessDefSettingBo bo) {
        LambdaQueryWrapper<ActProcessDefSetting> lqw = Wrappers.lambdaQuery();
        lqw.eq(StringUtils.isNotBlank(bo.getProcessDefinitionId()), ActProcessDefSetting::getProcessDefinitionId, bo.getProcessDefinitionId());
        lqw.eq(StringUtils.isNotBlank(bo.getProcessDefinitionKey()), ActProcessDefSetting::getProcessDefinitionKey, bo.getProcessDefinitionKey());
        lqw.like(StringUtils.isNotBlank(bo.getProcessDefinitionName()), ActProcessDefSetting::getProcessDefinitionName, bo.getProcessDefinitionName());
        lqw.eq(bo.getFormId() != null, ActProcessDefSetting::getFormId, bo.getFormId());
        lqw.eq(StringUtils.isNotBlank(bo.getFormKey()), ActProcessDefSetting::getFormKey, bo.getFormKey());
        lqw.like(StringUtils.isNotBlank(bo.getFormName()), ActProcessDefSetting::getFormName, bo.getFormName());
        lqw.eq(StringUtils.isNotBlank(bo.getFormVariable()), ActProcessDefSetting::getFormVariable, bo.getFormVariable());
        return lqw;
    }

    /**
     * 新增流程定义设置
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean insertByBo(ActProcessDefSettingBo bo) {
        ActProcessDefSetting add = BeanUtil.toBean(bo, ActProcessDefSetting.class);
        LambdaQueryWrapper<ActProcessDefSetting> lqw = Wrappers.lambdaQuery();
        if(0 == bo.getBusinessType()){
            lqw.eq(ActProcessDefSetting::getFormId, bo.getFormId());
        }else{
            lqw.eq(ActProcessDefSetting::getComponentName, bo.getComponentName());
        }
        baseMapper.delete(lqw);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改流程定义设置
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateByBo(ActProcessDefSettingBo bo) {
        ActProcessDefSetting update = BeanUtil.toBean(bo, ActProcessDefSetting.class);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 批量删除流程定义设置
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
     * @return: com.ruoyi.workflow.domain.ActProcessDefSetting
     * @author: gssong
     * @Date: 2022/8/30 22:10
     */
    @Override
    public ActProcessDefSetting queryByFormId(Long formId) {
        LambdaQueryWrapper<ActProcessDefSetting> lqw = Wrappers.lambdaQuery();
        lqw.eq(ActProcessDefSetting::getFormId, formId);
        return baseMapper.selectOne(lqw);
    }
}
