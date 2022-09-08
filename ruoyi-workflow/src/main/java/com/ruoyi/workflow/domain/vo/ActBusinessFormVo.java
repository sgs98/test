package com.ruoyi.workflow.domain.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.ruoyi.workflow.domain.ActBusinessStatus;
import com.ruoyi.workflow.domain.ActProcessDefSetting;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Map;


/**
 * 业务表单视图对象 act_business_form
 *
 * @author gssong
 * @date 2022-08-19
 */
@Data
@ApiModel("业务表单视图对象")
@ExcelIgnoreUnannotated
public class ActBusinessFormVo {

    private static final long serialVersionUID = 1L;

    /**
     *主键
     */
    @ExcelProperty(value = "主键")
    @ApiModelProperty("主键")
    private Long id;

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
     * 表单内容
     */
    @ExcelProperty(value = "表单内容")
    @ApiModelProperty("表单内容")
    private String formText;

    /**
     * 表单数据
     */
    @ExcelProperty(value = "表单内容")
    @ApiModelProperty(value = "表单数据")
    private String formValue;

    /**
     *单号
     */
    @ExcelProperty(value = "单号")
    @ApiModelProperty("单号")
    private String applyCode;

    /**
     *流程与表单关联对象
     */
    @ExcelProperty(value = "流程与表单关联对象")
    @ApiModelProperty("流程与表单关联对象")
    private ActProcessDefSetting actProcessDefSetting;

    /**
     *流程变量
     */
    @ExcelProperty(value = "流程变量")
    @ApiModelProperty("流程变量")
    private Map<String,Object> variableMap;

    /**
     * 流程实例id
     */
    @ApiModelProperty("流程实例id")
    private String processInstanceId;

    /**
     * 流程状态
     */
    @ExcelProperty(value = "流程状态")
    @ApiModelProperty("流程状态")
    private ActBusinessStatus actBusinessStatus;
}
