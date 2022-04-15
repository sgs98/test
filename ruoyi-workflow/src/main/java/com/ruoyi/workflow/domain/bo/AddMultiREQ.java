package com.ruoyi.workflow.domain.bo;

import com.ruoyi.common.core.validate.AddGroup;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.List;

/**
 * @program: ruoyi-vue-plus
 * @description: 加签参数请求
 * @author: gssong
 * @created: 2022年4月15日13:01:36
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel("加签参数请求")
public class AddMultiREQ implements Serializable {

    private static final long serialVersionUID=1L;

    @NotBlank(message = "加签环节不能为空",groups = AddGroup.class)
    @ApiModelProperty("节点id")
    private String nodeId;

    @NotBlank(message = "流程实例id不能为空",groups = AddGroup.class)
    @ApiModelProperty(value = "流程实例id")
    private String processInstId;

    @NotBlank(message = "加签人员不能为空",groups = AddGroup.class)
    @ApiModelProperty("人员id")
    private List<String> assignees;
}
