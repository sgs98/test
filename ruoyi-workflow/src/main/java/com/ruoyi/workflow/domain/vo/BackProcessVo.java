package com.ruoyi.workflow.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @program: ruoyi-vue-plus
 * @description: 驳回请求
 * @author: gssong
 * @created: 2021/11/06 22:22
 */
@Data
@ApiModel("驳回请求")
public class BackProcessVo implements Serializable {
    private static final long serialVersionUID=1L;

    @ApiModelProperty(value = "任务id",required = true)
    private String taskId;

    @ApiModelProperty(value = "驳回的目标节点id",required = true)
    private String targetActivityId;

    @ApiModelProperty(value = "审批意见")
    private String comment;

    @ApiModelProperty(value = "消息通知类型", required = true)
    private List<Integer> sendMessageType;

    @ApiModelProperty(value = "消息通知内容",required = true)
    private String sendMessage;
}
