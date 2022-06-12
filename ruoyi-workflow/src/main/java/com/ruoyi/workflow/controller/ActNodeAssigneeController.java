package com.ruoyi.workflow.controller;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.validate.AddGroup;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.workflow.domain.ActNodeAssignee;
import com.ruoyi.workflow.service.IActNodeAssigneeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;

/**
 * @program: ruoyi-vue-plus
 * @description: 流程定义设置控制层
 * @author: gssong
 * @created: 2021/11/21 13:48
 */
@Validated
@Api(value = "流程定义设置控制层", tags = {"流程定义设置控制层"})
@RequiredArgsConstructor
@RestController
@RequestMapping("/workflow/actNodeAssignee")
public class ActNodeAssigneeController extends BaseController {

    private final IActNodeAssigneeService iActNodeAssigneeService;

    /**
     * @Description: 保存流程定义设置
     * @param: actNodeAssignee
     * @return: com.ruoyi.common.core.domain.R<com.ruoyi.workflow.domain.ActNodeAssignee>
     * @Author: gssong
     * @Date: 2021/11/21
     */
    @PostMapping
    @ApiOperation("保存流程定义设置")
    @Log(title = "流程定义设置", businessType = BusinessType.INSERT)
    public R<ActNodeAssignee> add(@Validated(AddGroup.class) @RequestBody ActNodeAssignee actNodeAssignee){
        return R.ok(iActNodeAssigneeService.add(actNodeAssignee));
    }

    /**
     * @Description: 按照流程定义id和流程节点id查询流程定义设置
     * @param: actNodeAssignee
     * @return: com.ruoyi.common.core.domain.R<com.ruoyi.workflow.domain.ActNodeAssignee>
     * @Author: gssong
     * @Date: 2021/11/21
     */
    @GetMapping("/{processDefinitionId}/{nodeId}")
    @ApiOperation("按照流程定义id和流程节点id查询流程定义设置")
    public R<ActNodeAssignee> getInfoSetting(@ApiParam(value = "流程定义id",required = true) @NotBlank(message = "流程定义id不能为空") @PathVariable String processDefinitionId,
                                             @ApiParam(value = "流程节点id",required = true) @NotBlank(message = "流程节点id不能为空") @PathVariable String nodeId){
        ActNodeAssignee nodeAssignee = iActNodeAssigneeService.getInfoSetting(processDefinitionId,nodeId);
        return R.ok(nodeAssignee);
    }

    /**
     * @Description: 删除
     * @param: id
     * @return: com.ruoyi.common.core.domain.R<java.lang.Void>
     * @Author: gssong
     * @Date: 2021/11/21
     */
    @DeleteMapping("{id}")
    @ApiOperation("删除")
    @Log(title = "流程定义设置", businessType = BusinessType.DELETE)
    public R<Void> del(@ApiParam(value = "主键",required = true) @NotBlank(message = "主键不能为空") @PathVariable String id){
        return toAjax(iActNodeAssigneeService.del(id) ? 1 : 0);
    }

    /**
     * @Description: 复制为最新流程定义
     * @param: id 流程定义id
     * @param: key 流程定义key
     * @return: com.ruoyi.common.core.domain.R<java.lang.Void>
     * @Author: gssong
     * @Date: 2022/03/26
     */
    @PostMapping("copy/{id}/{key}")
    @ApiOperation("复制为最新流程定义")
    @Log(title = "流程定义设置", businessType = BusinessType.INSERT)
    public R<Void> copy(@ApiParam(value = "主键",required = true) @NotBlank(message = "ID不能为空") @PathVariable("id")  String id,
                        @ApiParam(value = "流程Key",required = true) @NotBlank(message = "流程Key不能为空") @PathVariable("key") String key){
        Boolean copy = iActNodeAssigneeService.copy(id, key);
        if(copy){
            return R.ok();
        }
        return R.fail("当前流程未设置人员");
    }
}
