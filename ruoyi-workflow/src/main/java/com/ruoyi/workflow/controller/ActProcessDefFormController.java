package com.ruoyi.workflow.controller;

import java.util.List;
import java.util.Arrays;

import lombok.RequiredArgsConstructor;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.*;
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
import com.ruoyi.workflow.domain.vo.ActProcessDefFormVo;
import com.ruoyi.workflow.domain.bo.ActProcessDefFormBo;
import com.ruoyi.workflow.service.IActProcessDefFormService;
import com.ruoyi.common.core.page.TableDataInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiOperation;

/**
 * 流程定义与单配置Controller
 *
 * @author gssong
 * @date 2022-08-28
 */
@Validated
@Api(value = "流程定义与单配置控制器", tags = {"流程定义与单配置管理"})
@RequiredArgsConstructor
@RestController
@RequestMapping("/workflow/processDefForm")
public class ActProcessDefFormController extends BaseController {

    private final IActProcessDefFormService iActProcessDefFormService;

    /**
     * 查询流程定义与单配置列表
     */
    @ApiOperation("查询流程定义与单配置列表")
    @GetMapping("/list")
    public TableDataInfo<ActProcessDefFormVo> list(ActProcessDefFormBo bo, PageQuery pageQuery) {
        return iActProcessDefFormService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出流程定义与单配置列表
     */
    @ApiOperation("导出流程定义与单配置列表")
    @Log(title = "流程定义与单配置", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(ActProcessDefFormBo bo, HttpServletResponse response) {
        List<ActProcessDefFormVo> list = iActProcessDefFormService.queryList(bo);
        ExcelUtil.exportExcel(list, "流程定义与单配置", ActProcessDefFormVo.class, response);
    }

    /**
     * 获取流程定义与单配置详细信息
     */
    @ApiOperation("获取流程定义与单配置详细信息")
    @GetMapping("/{id}")
    public R<ActProcessDefFormVo> getInfo(@ApiParam("主键")
                                     @NotNull(message = "主键不能为空")
                                     @PathVariable("id") Long id) {
        return R.ok(iActProcessDefFormService.queryById(id));
    }

    /**
     * 按流程定义id查询流程定义与单配置详细
     */
    @ApiOperation("按流程定义id查询流程定义与单配置详细")
    @GetMapping("/getProcessDefFormByDefId/{defId}")
    public R<ActProcessDefFormVo> getProcessDefFormByDefId(@ApiParam("流程定义id")
                                          @NotNull(message = "流程定义id不能为空")
                                          @PathVariable("defId") String defId) {
        return R.ok(iActProcessDefFormService.getProcessDefFormByDefId(defId));
    }

    /**
     * 校验表单是否关联
     */
    @ApiOperation("校验表单是否关联")
    @GetMapping("/checkProcessDefFormByDefId/{defId}/{formId}")
    public R<Void> checkProcessDefFormByDefId(@ApiParam("流程定义id")
                                                           @NotNull(message = "流程定义id不能为空")
                                                           @PathVariable("defId") String defId,
                                              @NotNull(message = "表单id不能为空")
                                              @PathVariable("formId") String formId) {
        if(formId == null){
            return R.fail("请选择表单");
        }
        String msg = iActProcessDefFormService.checkProcessDefFormByFormId(defId,formId);
        return R.ok(msg);
    }

    /**
     * 新增流程定义与单配置
     */
    @ApiOperation("新增流程定义与单配置")
    @Log(title = "流程定义与单配置", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody ActProcessDefFormBo bo) {
        return toAjax(iActProcessDefFormService.insertByBo(bo) ? 1 : 0);
    }

    /**
     * 修改流程定义与单配置
     */
    @ApiOperation("修改流程定义与单配置")
    @Log(title = "流程定义与单配置", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody ActProcessDefFormBo bo) {
        return toAjax(iActProcessDefFormService.updateByBo(bo) ? 1 : 0);
    }

    /**
     * 删除流程定义与单配置
     */
    @ApiOperation("删除流程定义与单配置")
    @Log(title = "流程定义与单配置", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@ApiParam("主键串")
                                       @NotEmpty(message = "主键不能为空")
                                       @PathVariable Long[] ids) {
        return toAjax(iActProcessDefFormService.deleteWithValidByIds(Arrays.asList(ids), true) ? 1 : 0);
    }
}
