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
import com.ruoyi.workflow.domain.vo.ActBusinessFormVo;
import com.ruoyi.workflow.domain.bo.ActBusinessFormBo;
import com.ruoyi.workflow.service.IActBusinessFormService;
import com.ruoyi.common.core.page.TableDataInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiOperation;

/**
 * 业务表单Controller
 *
 * @author gssong
 * @date 2022-08-19
 */
@Validated
@Api(value = "业务表单控制器", tags = {"业务表单管理"})
@RequiredArgsConstructor
@RestController
@RequestMapping("/workflow/businessForm")
public class ActBusinessFormController extends BaseController {

    private final IActBusinessFormService iActBusinessFormService;

    /**
     * 查询业务表单列表
     */
    @ApiOperation("查询业务表单列表")
    @SaCheckPermission("workflow:businessForm:list")
    @GetMapping("/list")
    public TableDataInfo<ActBusinessFormVo> list(ActBusinessFormBo bo, PageQuery pageQuery) {
        return iActBusinessFormService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出业务表单列表
     */
    @ApiOperation("导出业务表单列表")
    @SaCheckPermission("workflow:businessForm:export")
    @Log(title = "业务表单", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(ActBusinessFormBo bo, HttpServletResponse response) {
        List<ActBusinessFormVo> list = iActBusinessFormService.queryList(bo);
        ExcelUtil.exportExcel(list, "业务表单", ActBusinessFormVo.class, response);
    }

    /**
     * 获取业务表单详细信息
     */
    @ApiOperation("获取业务表单详细信息")
    @SaCheckPermission("workflow:businessForm:query")
    @GetMapping("/{id}")
    public R<ActBusinessFormVo> getInfo(@ApiParam("主键")
                                     @NotNull(message = "主键不能为空")
                                     @PathVariable("id") Long id) {
        return R.ok(iActBusinessFormService.queryById(id));
    }

    /**
     * 新增业务表单
     */
    @ApiOperation("新增业务表单")
    @SaCheckPermission("workflow:businessForm:add")
    @Log(title = "业务表单", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody ActBusinessFormBo bo) {
        return toAjax(iActBusinessFormService.insertByBo(bo) ? 1 : 0);
    }

    /**
     * 修改业务表单
     */
    @ApiOperation("修改业务表单")
    @SaCheckPermission("workflow:businessForm:edit")
    @Log(title = "业务表单", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody ActBusinessFormBo bo) {
        return toAjax(iActBusinessFormService.updateByBo(bo) ? 1 : 0);
    }

    /**
     * 删除业务表单
     */
    @ApiOperation("删除业务表单")
    @SaCheckPermission("workflow:businessForm:remove")
    @Log(title = "业务表单", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@ApiParam("主键串")
                                       @NotEmpty(message = "主键不能为空")
                                       @PathVariable Long[] ids) {
        return toAjax(iActBusinessFormService.deleteWithValidByIds(Arrays.asList(ids), true) ? 1 : 0);
    }
}