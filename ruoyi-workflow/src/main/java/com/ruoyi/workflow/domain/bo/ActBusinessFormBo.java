package com.ruoyi.workflow.domain.bo;

import com.ruoyi.common.core.validate.AddGroup;
import com.ruoyi.common.core.validate.EditGroup;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import javax.validation.constraints.*;


import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 业务表单业务对象 act_business_form
 *
 * @author gssong
 * @date 2022-08-19
 */

@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel("业务表单业务对象")
public class ActBusinessFormBo extends BaseEntity {

    /**
     * 主键
     */
    @ApiModelProperty(value = "主键", required = true)
    @NotNull(message = "主键不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 表单id
     */
    @ApiModelProperty(value = "表单id", required = true)
    @NotNull(message = "表单id不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long formId;

    /**
     * 表单key
     */
    @ApiModelProperty(value = "表单key", required = true)
    @NotBlank(message = "表单key不能为空", groups = { AddGroup.class, EditGroup.class })
    private String formKey;

    /**
     * 表单名称
     */
    @ApiModelProperty("表单名称")
    private String formName;

    /**
     * 表单内容
     */
    @ApiModelProperty(value = "表单内容")
    private String formText;

    /**
     * 表单数据
     */
    @ApiModelProperty(value = "表单数据")
    private String formValue;

    /**
     *单号
     */
    @ApiModelProperty(value = "单号", required = true)
    private String applyCode;


}
