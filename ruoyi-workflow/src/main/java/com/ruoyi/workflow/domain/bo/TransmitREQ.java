package com.ruoyi.workflow.domain.bo;

import com.ruoyi.common.core.validate.AddGroup;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;


/**
 * @program: ruoyi-vue-plus
 * @description: 转办请求
 * @author: gssong
 * @created: 2022/04/10 14:50
 */
@Data
@Validated
@ApiModel("转办请求")
public class TransmitREQ implements Serializable {

    private static final long serialVersionUID=1L;

    @ApiModelProperty("任务id")
    @NotBlank(message = "任务id为空",groups = AddGroup.class)
    private String taskId;

    @ApiModelProperty(value = "转办人id")
    @NotBlank(message = "转办人不能为空",groups = AddGroup.class)
    private String transmitUserId;

    @ApiModelProperty("审批意见")
    private String comment;

    @ApiModelProperty(value = "消息通知类型", required = true)
    @NotNull(message = "消息通知类型不能为空", groups = { AddGroup.class })
    private List<Integer> sendMessageType;

    @ApiModelProperty(value = "消息通知内容",required = true)
    @NotNull(message = "消息通知内容不能为空", groups = { AddGroup.class })
    private String sendMessage;
}
