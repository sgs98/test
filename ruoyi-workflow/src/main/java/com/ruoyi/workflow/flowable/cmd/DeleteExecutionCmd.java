package com.ruoyi.workflow.flowable.cmd;

import org.flowable.common.engine.impl.interceptor.Command;
import org.flowable.common.engine.impl.interceptor.CommandContext;
import org.flowable.engine.impl.persistence.entity.ExecutionEntityManager;
import org.flowable.engine.impl.util.CommandContextUtil;

import java.io.Serializable;

public class DeleteExecutionCmd implements Command<String>, Serializable {

    private String executionId;

    public DeleteExecutionCmd(String executionId) {
        this.executionId = executionId;
    }

    @Override
    public String execute(CommandContext commandContext) {
        ExecutionEntityManager executionEntityManager = CommandContextUtil.getExecutionEntityManager();
        executionEntityManager.delete(executionId);
        return null;
    }
}
