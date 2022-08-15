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
 * 流程单业务对象 act_process_form
 *
 * @author gssong
 * @date 2022-08-11
 */

@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel("流程单业务对象")
public class ActProcessFormBo extends BaseEntity {

    /**
     * 主键
     */
    @ApiModelProperty(value = "主键", required = true)
    @NotNull(message = "主键不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 表单key
     */
    @ApiModelProperty(value = "表单key", required = true)
    @NotBlank(message = "表单key不能为空", groups = { AddGroup.class, EditGroup.class })
    private String formKey;

    /**
     * 表单名称
     */
    @ApiModelProperty(value = "表单名称", required = true)
    @NotBlank(message = "表单名称不能为空", groups = { AddGroup.class, EditGroup.class })
    private String formName;

    /**
     * 表单数据
     */
    @ApiModelProperty(value = "表单数据")
    private String formDesignerText;

    /**
     * 表单备注
     */
    @ApiModelProperty(value = "表单备注")
    private String formRemark;


}
