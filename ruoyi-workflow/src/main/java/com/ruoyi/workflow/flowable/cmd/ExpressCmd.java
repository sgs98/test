package com.ruoyi.workflow.flowable.cmd;

import org.flowable.bpmn.model.SequenceFlow;
import org.flowable.common.engine.impl.interceptor.Command;
import org.flowable.common.engine.impl.interceptor.CommandContext;
import org.flowable.engine.impl.persistence.entity.ExecutionEntityImpl;
import org.flowable.engine.impl.util.condition.ConditionUtil;

public class ExpressCmd implements Command<Boolean> {

    private SequenceFlow sequenceFlow;
    private ExecutionEntityImpl executionEntity;

    public ExpressCmd(SequenceFlow sequenceFlow, ExecutionEntityImpl executionEntity) {
        this.sequenceFlow = sequenceFlow;
        this.executionEntity = executionEntity;
    }

    @Override
    public Boolean execute(CommandContext commandContext) {
        return ConditionUtil.hasTrueCondition(sequenceFlow, executionEntity);
    }
}
