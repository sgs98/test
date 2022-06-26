package com.ruoyi.workflow.listener;

public interface FlowBeforeHandler {
    void handleProcess(String processInstanceId,String taskId);
}
