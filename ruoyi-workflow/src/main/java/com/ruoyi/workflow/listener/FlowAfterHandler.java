package com.ruoyi.workflow.listener;

public interface FlowAfterHandler {
    void handleProcess(String processInstanceId);
}
