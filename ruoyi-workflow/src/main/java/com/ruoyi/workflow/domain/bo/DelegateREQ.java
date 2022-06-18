package com.ruoyi.workflow.domain.bo;

import com.ruoyi.common.core.validate.AddGroup;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@Validated
@ApiModel("委托请求")
public class DelegateREQ {

    @ApiModelProperty(value = "任务id",required = true)
    @NotBlank(message = "任务id不能为空",groups = AddGroup.class)
    private String taskId;

    @ApiModelProperty(value = "委派人id",required = true)
    @NotBlank(message = "委派人id不能为空",groups = AddGroup.class)
    private String delegateUserId;

    @ApiModelProperty(value = "委派人",required = true)
    @NotBlank(message = "委派人不能为空",groups = AddGroup.class)
    private String delegateUserName;

    @ApiModelProperty("审批意见")
    private String comment;

    @ApiModelProperty(value = "消息通知类型", required = true)
    @NotNull(message = "消息通知类型不能为空", groups = { AddGroup.class })
    private List<Integer> sendMessageType;

    @ApiModelProperty(value = "消息通知内容",required = true)
    @NotNull(message = "消息通知内容不能为空", groups = { AddGroup.class })
    private String sendMessage;
}
