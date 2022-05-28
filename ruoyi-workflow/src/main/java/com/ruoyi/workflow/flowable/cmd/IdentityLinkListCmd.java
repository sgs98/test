package com.ruoyi.workflow.flowable.cmd;

import org.flowable.common.engine.impl.interceptor.Command;
import org.flowable.common.engine.impl.interceptor.CommandContext;
import org.flowable.engine.impl.util.CommandContextUtil;
import org.flowable.identitylink.service.IdentityLinkService;
import org.flowable.identitylink.service.impl.persistence.entity.IdentityLinkEntity;

import java.util.List;


/**
 * @program: ruoyi-vue-plus
 * @description: 查询人员信息
 * @author: gssong
 * @created: 2022/5/28 16:26
 */
public class IdentityLinkListCmd implements Command<List<IdentityLinkEntity>> {

    private String taskId;

    public IdentityLinkListCmd(String taskId) {
        this.taskId = taskId;
    }

    @Override
    public List<IdentityLinkEntity> execute(CommandContext commandContext) {
        IdentityLinkService identityLinkService = CommandContextUtil.getIdentityLinkService();
        return identityLinkService.findIdentityLinksByTaskId(taskId);
    }
}
