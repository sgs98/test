package com.ruoyi.workflow.domain.bo;

import com.ruoyi.common.core.validate.AddGroup;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

@Data
@ApiModel("业务规则业务对象")
public class CancelProcessBo implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "流程实例id",required = true)
    @NotNull(message = "流程实例id", groups = { AddGroup.class })
    private String processInstId;

    @ApiModelProperty(value = "消息通知类型", required = true)
    @NotNull(message = "消息通知类型不能为空", groups = { AddGroup.class })
    private List<Integer> sendMessageType;

    @ApiModelProperty(value = "消息通知内容不能为空",required = true)
    @NotNull(message = "消息通知内容", groups = { AddGroup.class })
    private String sendMessage;
}
