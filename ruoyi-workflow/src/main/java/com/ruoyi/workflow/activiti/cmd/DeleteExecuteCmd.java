package com.ruoyi.workflow.activiti.cmd;

import org.flowable.common.engine.impl.interceptor.Command;
import org.flowable.common.engine.impl.interceptor.CommandContext;
import org.flowable.engine.impl.persistence.entity.ExecutionEntityManager;
import org.flowable.engine.impl.util.CommandContextUtil;

import java.io.Serializable;

public class DeleteExecuteCmd implements Command<String>, Serializable {

    private String executeId;

    public DeleteExecuteCmd(String executeId) {
        this.executeId = executeId;
    }

    @Override
    public String execute(CommandContext commandContext) {
        ExecutionEntityManager executionEntityManager = CommandContextUtil.getExecutionEntityManager();
        executionEntityManager.delete(executeId);
        return null;
    }
}
