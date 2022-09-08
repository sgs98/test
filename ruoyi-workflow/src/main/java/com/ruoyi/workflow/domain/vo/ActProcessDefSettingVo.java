package com.ruoyi.workflow.domain.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;



/**
 *流程定义设置视图对象 act_process_def_form
 *
 * @author gssong
 * @date 2022-08-28
 */
@Data
@ApiModel("流程定义设置视图对象")
@ExcelIgnoreUnannotated
public class ActProcessDefSettingVo {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @ExcelProperty(value = "主键")
    @ApiModelProperty("主键")
    private Long id;

    /**
     * 流程定义id
     */
    @ExcelProperty(value = "流程定义id")
    @ApiModelProperty("流程定义id")
    private String processDefinitionId;

    /**
     * 流程定义key
     */
    @ExcelProperty(value = "流程定义key")
    @ApiModelProperty("流程定义key")
    private String processDefinitionKey;

    /**
     * 流程定义名称
     */
    @ExcelProperty(value = "流程定义名称")
    @ApiModelProperty("流程定义名称")
    private String processDefinitionName;

    /**
     * 表单id
     */
    @ExcelProperty(value = "表单id")
    @ApiModelProperty("表单id")
    private Long formId;

    /**
     * 表单key
     */
    @ExcelProperty(value = "表单key")
    @ApiModelProperty("表单key")
    private String formKey;

    /**
     * 表单名称
     */
    @ExcelProperty(value = "表单名称")
    @ApiModelProperty("表单名称")
    private String formName;

    /**
     * 动态表单中参数id,多个用英文逗号隔开
     */
    @ExcelProperty(value = "动态表单中参数id,多个用英文逗号隔开")
    @ApiModelProperty("动态表单中参数id,多个用英文逗号隔开")
    private String formVariable;


}
