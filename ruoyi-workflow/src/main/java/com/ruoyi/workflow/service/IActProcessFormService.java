package com.ruoyi.workflow.service;

import com.ruoyi.workflow.domain.vo.ActProcessFormVo;
import com.ruoyi.workflow.domain.bo.ActProcessFormBo;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.core.domain.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 流程单Service接口
 *
 * @author gssong
 * @date 2022-08-11
 */
public interface IActProcessFormService {

    /**
     * 查询流程单
     */
    ActProcessFormVo queryById(Long id);

    /**
     * 查询流程单列表
     */
    TableDataInfo<ActProcessFormVo> queryPageList(ActProcessFormBo bo, PageQuery pageQuery);

    /**
     * 查询流程单列表
     */
    List<ActProcessFormVo> queryList(ActProcessFormBo bo);

    /**
     * 修改流程单
     */
    Boolean insertByBo(ActProcessFormBo bo);

    /**
     * 修改流程单
     */
    Boolean updateByBo(ActProcessFormBo bo);

    /**
     * 校验并批量删除流程单信息
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}
