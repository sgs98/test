package com.ruoyi.workflow.domain.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;



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


}
