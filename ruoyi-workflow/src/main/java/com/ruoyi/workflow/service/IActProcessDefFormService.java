package com.ruoyi.workflow.service;

import com.ruoyi.common.core.domain.R;
import com.ruoyi.workflow.domain.ActProcessDefForm;
import com.ruoyi.workflow.domain.vo.ActProcessDefFormVo;
import com.ruoyi.workflow.domain.bo.ActProcessDefFormBo;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.core.domain.PageQuery;
import liquibase.pro.packaged.B;

import java.util.Collection;
import java.util.List;

/**
 * 流程定义与单配置Service接口
 *
 * @author gssong
 * @date 2022-08-28
 */
public interface IActProcessDefFormService {

    /**
     * 查询流程定义与单配置
     */
    ActProcessDefFormVo queryById(Long id);

    /**
     * 按流程定义id查询流程定义与单配置详细
     */
    ActProcessDefFormVo getProcessDefFormByDefId(String defId);

    /**
     * 校验表单是否关联
     */
    String checkProcessDefFormByFormId(String defId,String formId);

    /**
     * 查询流程定义与单配置列表
     */
    TableDataInfo<ActProcessDefFormVo> queryPageList(ActProcessDefFormBo bo, PageQuery pageQuery);

    /**
     * 查询流程定义与单配置列表
     */
    List<ActProcessDefFormVo> queryList(ActProcessDefFormBo bo);

    /**
     * 修改流程定义与单配置
     */
    Boolean insertByBo(ActProcessDefFormBo bo);

    /**
     * 修改流程定义与单配置
     */
    Boolean updateByBo(ActProcessDefFormBo bo);

    /**
     * 校验并批量删除流程定义与单配置信息
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);

    /**
     * 按照formId查询
     */
    ActProcessDefForm queryByFormId(Long formId);
}
