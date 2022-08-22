package com.ruoyi.workflow.utils;

import com.ruoyi.workflow.common.constant.ActConstant;
import com.ruoyi.workflow.domain.vo.ProcessNode;
import com.ruoyi.workflow.flowable.cmd.ExpressCheckCmd;
import com.ruoyi.workflow.flowable.factory.WorkflowService;
import org.apache.commons.lang3.StringUtils;
import org.flowable.bpmn.model.*;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * @program: ruoyi-vue-plus
 * @description: 获取流程审批路线
 * @author: gssong
 * @created: 2022/8/22 18:40
 */
@Component
public class ProcessNodePathUtils extends WorkflowService {

    public List<ProcessNode> nodeList(String processInstanceId){
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
        BpmnModel bpmnModel = repositoryService.getBpmnModel(processInstance.getProcessDefinitionId());
        Collection<FlowElement> flowElements = bpmnModel.getMainProcess().getFlowElements();
        List<Task> list = taskService.createTaskQuery().processInstanceId(processInstanceId).list();
        List<ProcessNode> processNodeList = new ArrayList<>();

        Map<String, Object> variables = runtimeService.getVariables(list.get(0).getExecutionId());
        FlowElement startElement = flowElements.stream().filter(f -> f instanceof StartEvent).findFirst().orElse(null);
        assert startElement != null;
        List<SequenceFlow> outgoingFlows = ((StartEvent) startElement).getOutgoingFlows();
        if(outgoingFlows.size() == 1){
            this.getNextNodeList(processNodeList,flowElements,outgoingFlows.get(0),variables,processInstance.getProcessInstanceId(),null);
        }
        return processNodeList;
    }
    public void getNextNodeList(List<ProcessNode> processNodeList,Collection<FlowElement> flowElements, SequenceFlow sequenceFlow, Map<String, Object> variables,String processInstanceId,String gateway){
        FlowElement targetFlowElement = sequenceFlow.getTargetFlowElement();
        List<SequenceFlow> outgoingFlows = ((FlowNode) targetFlowElement).getOutgoingFlows();
        for (SequenceFlow outgoingFlow : outgoingFlows) {
            FlowElement currentFlowElement = outgoingFlow.getTargetFlowElement();
            if (currentFlowElement instanceof UserTask) {
                nextNodeBuild(processNodeList,flowElements,currentFlowElement,outgoingFlow,variables,processInstanceId,gateway);
            } else if (currentFlowElement instanceof ExclusiveGateway) { // 排他网关
                getNextNodeList(processNodeList,flowElements,outgoingFlow,variables,processInstanceId,ActConstant.EXCLUSIVE_GATEWAY);
            } else if (currentFlowElement instanceof ParallelGateway) { //并行网关
                getNextNodeList(processNodeList,flowElements,outgoingFlow,variables,processInstanceId,ActConstant.PARALLEL_GATEWAY);
            } else if (currentFlowElement instanceof InclusiveGateway) { //包含网关
                getNextNodeList(processNodeList,flowElements,outgoingFlow,variables,processInstanceId,ActConstant.INCLUSIVE_GATEWAY);
            } else if (currentFlowElement instanceof EndEvent) {
                FlowElement subProcess = getSubProcess(flowElements, currentFlowElement);
                if (subProcess != null) {
                    getNextNodeList(processNodeList,flowElements,outgoingFlow,variables,processInstanceId, ActConstant.END_EVENT);
                }
            }
        }

    }

    private void nextNodeBuild(List<ProcessNode> processNodeList,Collection<FlowElement> flowElements, FlowElement currentFlowElement,SequenceFlow sequenceFlow, Map<String, Object> variableMap,String processInstanceId, String gateway){
        String conditionExpression = sequenceFlow.getConditionExpression();
        ProcessNode processNode = new ProcessNode();
        if (ActConstant.EXCLUSIVE_GATEWAY.equals(gateway)) {
            //判断是否有条件
            Boolean condition = false;
            if (StringUtils.isNotBlank(conditionExpression)) {
                ExpressCheckCmd expressCheckCmd = new ExpressCheckCmd(processInstanceId,conditionExpression,variableMap);
                condition = processEngine.getManagementService().executeCommand(expressCheckCmd);
            }

            if(condition){
                processNode.setNodeId(currentFlowElement.getId());
                processNode.setNodeName(currentFlowElement.getName());
                processNodeList.add(processNode);
            }
            //包含网关
        } else if (ActConstant.INCLUSIVE_GATEWAY.equals(gateway)) {
            //判断是否有条件
            Boolean condition = false;
            if (StringUtils.isNotBlank(conditionExpression)) {
                ExpressCheckCmd expressCheckCmd = new ExpressCheckCmd(processInstanceId,conditionExpression,variableMap);
                condition = processEngine.getManagementService().executeCommand(expressCheckCmd);
            }

            if(condition){
                processNode.setNodeId(currentFlowElement.getId());
                processNode.setNodeName(currentFlowElement.getName());
                processNodeList.add(processNode);
            }
        }else{
            processNode.setNodeId(currentFlowElement.getId());
            processNode.setNodeName(currentFlowElement.getName());
            processNodeList.add(processNode);
        }
        this.getNextNodeList(processNodeList,flowElements, sequenceFlow, variableMap,processInstanceId,null);
    }

    public FlowElement getSubProcess(Collection<FlowElement> flowElements, FlowElement endElement) {
        for (FlowElement mainElement : flowElements) {
            if (mainElement instanceof SubProcess) {
                for (FlowElement subEndElement : ((SubProcess) mainElement).getFlowElements()) {
                    if (endElement.equals(subEndElement)) {
                        return mainElement;
                    }
                }
            }
        }
        return null;
    }

}
