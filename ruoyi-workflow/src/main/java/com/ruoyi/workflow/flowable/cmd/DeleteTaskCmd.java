package com.ruoyi.workflow.flowable.cmd;

import org.flowable.common.engine.impl.interceptor.CommandContext;
import org.flowable.engine.impl.cmd.NeedsActiveTaskCmd;
import org.flowable.engine.impl.util.CommandContextUtil;
import org.flowable.task.service.TaskService;
import org.flowable.task.service.impl.persistence.entity.TaskEntity;

/**
 * @program: ruoyi-vue-plus
 * @description: 删除任务数据
 * @author: gssong
 * @created: 2022/4/15 20:25
 */
public class DeleteTaskCmd extends NeedsActiveTaskCmd<String> {

    /**
     * 当前任务对应的 act_ru_task 任务id
     */
    public DeleteTaskCmd(String taskId) {
        super(taskId);
    }


    @Override
    protected String execute(CommandContext commandContext, TaskEntity task) {
        TaskService taskService = CommandContextUtil.getTaskService(commandContext);
        taskService.deleteTask(task, true);
        return null;
    }
}
