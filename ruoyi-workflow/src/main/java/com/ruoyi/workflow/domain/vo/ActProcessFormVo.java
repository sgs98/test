package com.ruoyi.workflow.domain.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;



/**
 * 流程单视图对象 act_process_form
 *
 * @author gssong
 * @date 2022-08-11
 */
@Data
@ApiModel("流程单视图对象")
@ExcelIgnoreUnannotated
public class ActProcessFormVo {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @ExcelProperty(value = "主键")
    @ApiModelProperty("主键")
    private Long id;

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
     * 表单数据
     */
    @ExcelProperty(value = "表单数据")
    @ApiModelProperty("表单数据")
    private String formDesignerText;

    /**
     * 表单备注
     */
    @ExcelProperty(value = "表单备注")
    @ApiModelProperty("表单备注")
    private String formRemark;

    /**
     * 表单状态
     */
    @ExcelProperty(value = "表单状态")
    @ApiModelProperty("表单状态")
    private Boolean status;


}
