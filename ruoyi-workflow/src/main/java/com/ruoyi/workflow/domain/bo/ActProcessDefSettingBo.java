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
 * 流程定义设置业务对象 act_process_def_Setting
 *
 * @author gssong
 * @date 2022-08-28
 */

@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel("流程定义设置对象")
public class ActProcessDefSettingBo extends BaseEntity {

    /**
     * 主键
     */
    @ApiModelProperty(value = "主键", required = true)
    @NotNull(message = "主键不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 流程定义id
     */
    @ApiModelProperty(value = "流程定义id", required = true)
    @NotBlank(message = "流程定义id不能为空", groups = { AddGroup.class, EditGroup.class })
    private String processDefinitionId;

    /**
     * 流程定义key
     */
    @ApiModelProperty(value = "流程定义key", required = true)
    @NotBlank(message = "流程定义key不能为空", groups = { AddGroup.class, EditGroup.class })
    private String processDefinitionKey;

    /**
     * 流程定义名称
     */
    @ApiModelProperty(value = "流程定义名称", required = true)
    @NotBlank(message = "流程定义名称不能为空", groups = { AddGroup.class, EditGroup.class })
    private String processDefinitionName;

    /**
     * 业务类型，0动态表单，1业务单据
     */
    @ApiModelProperty(value = "业务类型，0动态表单，1业务单据", required = true)
    @NotNull(message = "业务类型不能为空", groups = { AddGroup.class, EditGroup.class })
    private Integer businessType;

    /**
     * 组件名称
     */
    @ApiModelProperty(value = "组件名称", required = true)
    private String componentName;

    /**
     * 表单id
     */
    @ApiModelProperty(value = "表单id", required = true)
    private Long formId;

    /**
     * 表单key
     */
    @ApiModelProperty(value = "表单key", required = true)
    private String formKey;

    /**
     * 表单名称
     */
    @ApiModelProperty(value = "表单名称", required = true)
    private String formName;

    /**
     * 动态表单中参数id,多个用英文逗号隔开
     */
    @ApiModelProperty(value = "动态表单中参数id,多个用英文逗号隔开")
    private String formVariable;

    /**
     * 备注
     */
    @ApiModelProperty(value = "备注")
    private String remark;



}
