package com.ruoyi.workflow.controller;

import java.util.List;
import java.util.Arrays;

import lombok.RequiredArgsConstructor;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.*;
import cn.dev33.satoken.annotation.SaCheckPermission;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;
import com.ruoyi.common.annotation.RepeatSubmit;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.PageQuery;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.validate.AddGroup;
import com.ruoyi.common.core.validate.EditGroup;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.workflow.domain.vo.ActProcessFormVo;
import com.ruoyi.workflow.domain.bo.ActProcessFormBo;
import com.ruoyi.workflow.service.IActProcessFormService;
import com.ruoyi.common.core.page.TableDataInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiOperation;

/**
 * 流程单Controller
 *
 * @author gssong
 * @date 2022-08-11
 */
@Validated
@Api(value = "流程单控制器", tags = {"流程单管理"})
@RequiredArgsConstructor
@RestController
@RequestMapping("/workflow/processForm")
public class ActProcessFormController extends BaseController {

    private final IActProcessFormService iActProcessFormService;

    /**
     * 查询流程单列表
     */
    @ApiOperation("查询流程单列表")
    @SaCheckPermission("workflow:processForm:list")
    @GetMapping("/list")
    public TableDataInfo<ActProcessFormVo> list(ActProcessFormBo bo, PageQuery pageQuery) {
        return iActProcessFormService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出流程单列表
     */
    @ApiOperation("导出流程单列表")
    @SaCheckPermission("workflow:processForm:export")
    @Log(title = "流程单", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(ActProcessFormBo bo, HttpServletResponse response) {
        List<ActProcessFormVo> list = iActProcessFormService.queryList(bo);
        ExcelUtil.exportExcel(list, "流程单", ActProcessFormVo.class, response);
    }

    /**
     * 获取流程单详细信息
     */
    @ApiOperation("获取流程单详细信息")
    @SaCheckPermission("workflow:processForm:query")
    @GetMapping("/{id}")
    public R<ActProcessFormVo> getInfo(@ApiParam("主键")
                                     @NotNull(message = "主键不能为空")
                                     @PathVariable("id") Long id) {
        return R.ok(iActProcessFormService.queryById(id));
    }

    /**
     * 新增流程单
     */
    @ApiOperation("新增流程单")
    @SaCheckPermission("workflow:processForm:add")
    @Log(title = "流程单", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody ActProcessFormBo bo) {
        return toAjax(iActProcessFormService.insertByBo(bo) ? 1 : 0);
    }

    /**
     * 修改流程单
     */
    @ApiOperation("修改流程单")
    @SaCheckPermission("workflow:processForm:edit")
    @Log(title = "流程单", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody ActProcessFormBo bo) {
        return toAjax(iActProcessFormService.updateByBo(bo) ? 1 : 0);
    }

    /**
     * 删除流程单
     */
    @ApiOperation("删除流程单")
    @SaCheckPermission("workflow:processForm:remove")
    @Log(title = "流程单", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@ApiParam("主键串")
                                       @NotEmpty(message = "主键不能为空")
                                       @PathVariable Long[] ids) {
        return toAjax(iActProcessFormService.deleteWithValidByIds(Arrays.asList(ids), true) ? 1 : 0);
    }
}
