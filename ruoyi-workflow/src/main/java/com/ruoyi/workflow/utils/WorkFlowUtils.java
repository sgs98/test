package com.ruoyi.workflow.utils;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.common.core.domain.entity.SysRole;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.helper.LoginHelper;
import com.ruoyi.common.utils.JsonUtils;
import com.ruoyi.common.utils.spring.SpringUtils;
import com.ruoyi.system.domain.SysUserRole;
import com.ruoyi.system.mapper.SysRoleMapper;
import com.ruoyi.system.mapper.SysUserMapper;
import com.ruoyi.system.mapper.SysUserRoleMapper;
import com.ruoyi.workflow.activiti.cmd.ExpressCmd;
import com.ruoyi.workflow.common.constant.ActConstant;
import com.ruoyi.workflow.common.enums.BusinessStatusEnum;
import com.ruoyi.workflow.domain.*;
import com.ruoyi.workflow.domain.bo.SendMessage;
import com.ruoyi.workflow.domain.bo.TaskCompleteREQ;
import com.ruoyi.workflow.domain.vo.ActBusinessRuleVo;
import com.ruoyi.workflow.domain.vo.MultiVo;
import com.ruoyi.workflow.domain.vo.ProcessNode;
import com.ruoyi.workflow.service.*;
import lombok.RequiredArgsConstructor;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.*;
import org.activiti.engine.impl.bpmn.behavior.ParallelMultiInstanceBehavior;
import org.activiti.engine.impl.bpmn.behavior.SequentialMultiInstanceBehavior;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.activiti.engine.*;
import org.activiti.engine.impl.persistence.entity.ExecutionEntityImpl;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.persistence.entity.VariableInstance;
import org.activiti.engine.task.IdentityLink;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.activiti.engine.task.Task;
import org.springframework.util.ReflectionUtils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

import static com.ruoyi.workflow.common.constant.ActConstant.*;

/**
 * @program: ruoyi-vue-plus
 * @description: ??????????????????
 * @author: gssong
 * @created: 2021/10/03 19:31
 */
@Component
@RequiredArgsConstructor
public class WorkFlowUtils {

    private final IActBusinessStatusService iActBusinessStatusService;

    private final TaskService taskService;

    private final SysUserMapper sysUserMapper;

    private final SysRoleMapper sysRoleMapper;

    private final SysUserRoleMapper userRoleMapper;

    private final ManagementService managementService;

    private final HistoryService historyService;

    private final RepositoryService repositoryService;

    private final ISysMessageService iSysMessageService;

    private final IActHiTaskInstService iActHiTaskInstService;

    private final IActBusinessRuleService iActBusinessRuleService;

    private final IActTaskNodeService iActTaskNodeService;

    /**
     * @Description: bpmnModel??????xml
     * @param: jsonBytes
     * @return: byte[]
     * @Author: gssong
     * @Date: 2021/11/5
     */
    public byte[] bpmnJsonToXmlBytes(byte[] jsonBytes) throws IOException {
        if (jsonBytes == null) {
            return null;
        }
        //1. json??????????????? BpmnModel ??????
        ObjectMapper objectMapper = JsonUtils.getObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(jsonBytes);
        BpmnModel bpmnModel = new BpmnJsonConverter().convertToBpmnModel(jsonNode);

        if (bpmnModel.getProcesses().size() == 0) {
            return null;
        }
        //2.???bpmnModel??????xml
        return new BpmnXMLConverter().convertToXML(bpmnModel);
    }

    /**
     * @Description: ??????????????????????????????
     * @param: flowElements ????????????
     * @param: flowElement ??????????????????
     * @param: nextNodes ??????????????????
     * @param: tempNodes ????????????????????????????????????
     * @param: taskId ??????id
     * @param: gateway ??????
     * @return: void
     * @author: gssong
     * @Date: 2022/4/11 13:37
     */
    public void getNextNodeList(Collection<FlowElement> flowElements, FlowElement flowElement, ExecutionEntityImpl executionEntity, List<ProcessNode> nextNodes, List<ProcessNode> tempNodes, String taskId, String gateway) {
        // ?????????????????????????????????
        List<SequenceFlow> outgoingFlows = ((FlowNode) flowElement).getOutgoingFlows();
        // ???????????????????????????????????????
        for (SequenceFlow sequenceFlow : outgoingFlows) {
            // ???????????????????????????
            ProcessNode processNode = new ProcessNode();
            ProcessNode tempNode = new ProcessNode();
            FlowElement outFlowElement = sequenceFlow.getTargetFlowElement();
            if (outFlowElement instanceof UserTask) {
                nextNodeBuild(executionEntity, nextNodes, tempNodes, taskId, gateway, sequenceFlow, processNode, tempNode, outFlowElement);
            } else if (outFlowElement instanceof ExclusiveGateway) { // ????????????
                getNextNodeList(flowElements, outFlowElement, executionEntity, nextNodes, tempNodes, taskId, ActConstant.EXCLUSIVE_GATEWAY);
            } else if (outFlowElement instanceof ParallelGateway) { //????????????
                getNextNodeList(flowElements, outFlowElement, executionEntity, nextNodes, tempNodes, taskId, ActConstant.PARALLEL_GATEWAY);
            } else if (outFlowElement instanceof InclusiveGateway) { //????????????
                getNextNodeList(flowElements, outFlowElement, executionEntity, nextNodes, tempNodes, taskId, ActConstant.INCLUSIVE_GATEWAY);
            } else if (outFlowElement instanceof EndEvent) {
                FlowElement subProcess = getSubProcess(flowElements, outFlowElement);
                if (subProcess == null) {
                    continue;
                }
                getNextNodeList(flowElements, subProcess, executionEntity, nextNodes, tempNodes, taskId, ActConstant.END_EVENT);
            } else if (outFlowElement instanceof SubProcess) {
                Collection<FlowElement> subFlowElements = ((SubProcess) outFlowElement).getFlowElements();
                for (FlowElement element : subFlowElements) {
                    if (element instanceof UserTask) {
                        nextNodeBuild(executionEntity, nextNodes, tempNodes, taskId, gateway, sequenceFlow, processNode, tempNode, element);
                        break;
                    }
                }
            } else {
                throw new ServiceException("????????????????????????");
            }
        }
    }

    /**
     * @Description: ????????????????????????
     * @param: executionEntity
     * @param: nextNodes ??????????????????
     * @param: tempNodes ????????????????????????????????????(??????????????????)
     * @param: taskId ??????id
     * @param: gateway ??????
     * @param: sequenceFlow  ??????
     * @param: processNode ???????????????????????????
     * @param: tempNode  ??????????????????????????????
     * @param: outFlowElement ????????????
     * @return: void
     * @author: gssong
     * @Date: 2022/4/11 13:35
     */
    private void nextNodeBuild(ExecutionEntityImpl executionEntity, List<ProcessNode> nextNodes, List<ProcessNode> tempNodes, String taskId, String gateway, SequenceFlow sequenceFlow, ProcessNode processNode, ProcessNode tempNode, FlowElement outFlowElement) {
        // ?????????????????????????????????????????????????????????????????????
        // ???????????????????????????
        if (ActConstant.EXCLUSIVE_GATEWAY.equals(gateway)) {
            String conditionExpression = sequenceFlow.getConditionExpression();
            //?????????????????????
            if (StringUtils.isNotBlank(conditionExpression)) {
                ExpressCmd expressCmd = new ExpressCmd(sequenceFlow,executionEntity,conditionExpression);
                Boolean condition  = managementService.executeCommand(expressCmd);
                processNodeBuildList(processNode, outFlowElement, ActConstant.EXCLUSIVE_GATEWAY, taskId, condition, nextNodes);
            } else {
                tempNodeBuildList(tempNodes, taskId, tempNode, outFlowElement);
            }
            //????????????
        } else if (ActConstant.INCLUSIVE_GATEWAY.equals(gateway)) {
            String conditionExpression = sequenceFlow.getConditionExpression();
            if (StringUtils.isBlank(conditionExpression)) {
                processNodeBuildList(processNode, outFlowElement, ActConstant.INCLUSIVE_GATEWAY, taskId, true, nextNodes);
            } else {
                ExpressCmd expressCmd = new ExpressCmd(sequenceFlow,executionEntity,conditionExpression);
                Boolean condition  = managementService.executeCommand(expressCmd);
                processNodeBuildList(processNode, outFlowElement, ActConstant.INCLUSIVE_GATEWAY, taskId, condition, nextNodes);
            }
        } else {
            processNodeBuildList(processNode, outFlowElement, ActConstant.USER_TASK, taskId, true, nextNodes);
        }
    }

    /**
     * @Description: ??????????????????(????????????)
     * @param: tempNodes ??????????????????
     * @param: taskId ??????id
     * @param: tempNode ????????????
     * @param: outFlowElement ????????????
     * @return: void
     * @author: gssong
     * @Date: 2022/7/16 19:17
     */
    private void tempNodeBuildList(List<ProcessNode> tempNodes, String taskId, ProcessNode tempNode, FlowElement outFlowElement) {
        tempNode.setNodeId(outFlowElement.getId());
        tempNode.setNodeName(outFlowElement.getName());
        tempNode.setNodeType(ActConstant.EXCLUSIVE_GATEWAY);
        tempNode.setTaskId(taskId);
        tempNode.setExpression(true);
        tempNode.setChooseWay(ActConstant.WORKFLOW_ASSIGNEE);
        tempNode.setAssignee(((UserTask) outFlowElement).getAssignee());
        tempNode.setAssigneeId(((UserTask) outFlowElement).getAssignee());
        tempNodes.add(tempNode);
    }

    /**
     * @Description: ??????????????????
     * @param: processNode ????????????
     * @param: outFlowElement ????????????
     * @param: exclusiveGateway ??????
     * @param: taskId ??????id
     * @param: condition ??????
     * @param: nextNodes ????????????
     * @return: void
     * @author: gssong
     * @Date: 2022/7/16 19:17
     */
    private void processNodeBuildList(ProcessNode processNode, FlowElement outFlowElement, String exclusiveGateway, String taskId, Boolean condition, List<ProcessNode> nextNodes) {
        processNode.setNodeId(outFlowElement.getId());
        processNode.setNodeName(outFlowElement.getName());
        processNode.setNodeType(exclusiveGateway);
        processNode.setTaskId(taskId);
        processNode.setExpression(condition);
        processNode.setChooseWay(ActConstant.WORKFLOW_ASSIGNEE);
        processNode.setAssignee(((UserTask) outFlowElement).getAssignee());
        processNode.setAssigneeId(((UserTask) outFlowElement).getAssignee());
        nextNodes.add(processNode);
    }

    /**
     * @Description: ????????????????????????????????????
     * @param: flowElements????????????
     * @param: endElement ????????????
     * @return: org.flowable.bpmn.model.FlowElement
     * @author: gssong
     * @Date: 2022/7/11 20:39
     */
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

    /**
     * @Description: ??????????????????????????????id
     * @param: businessRule ??????????????????
     * @param: taskId ??????id
     * @param: taskName ????????????
     * @return: ??????????????????????????????
     * @return: java.util.List<java.lang.String>
     * @author: gssong
     * @Date: 2022/4/11 13:35
     */
    public List<String> ruleAssignList(ActBusinessRuleVo businessRule, String taskId, String taskName) {
        try {
            //?????????
            Object obj;
            //????????????
            String methodName = businessRule.getMethod();
            //?????????
            Object beanName = SpringUtils.getBean(businessRule.getBeanName());
            if (StringUtils.isNotBlank(businessRule.getParam())) {
                List<ActBusinessRuleParam> businessRuleParams = JsonUtils.parseArray(businessRule.getParam(), ActBusinessRuleParam.class);
                Class[] paramClass = new Class[businessRuleParams.size()];
                List<Object> params = new ArrayList<>();
                for (int i = 0; i < businessRuleParams.size(); i++) {
                    Map<String, VariableInstance> variables = taskService.getVariableInstances(taskId);
                    if (variables.containsKey(businessRuleParams.get(i).getParam())) {
                        VariableInstance v = variables.get(businessRuleParams.get(i).getParam());
                        String variable = v.getTextValue();
                        switch (businessRuleParams.get(i).getParamType()) {
                            case ActConstant.PARAM_STRING:
                                paramClass[i] = String.valueOf(variable).getClass();
                                params.add(String.valueOf(variable));
                                break;
                            case ActConstant.PARAM_SHORT:
                                paramClass[i] = Short.valueOf(variable).getClass();
                                params.add(Short.valueOf(variable));
                                break;
                            case ActConstant.PARAM_INTEGER:
                                paramClass[i] = Integer.valueOf(variable).getClass();
                                params.add(Integer.valueOf(variable));
                                break;
                            case ActConstant.PARAM_LONG:
                                paramClass[i] = Long.valueOf(variable).getClass();
                                params.add(Long.valueOf(variable));
                                break;
                            case ActConstant.PARAM_FLOAT:
                                paramClass[i] = Float.valueOf(variable).getClass();
                                params.add(Float.valueOf(variable));
                                break;
                            case ActConstant.PARAM_DOUBLE:
                                paramClass[i] = Double.valueOf(variable).getClass();
                                params.add(Double.valueOf(variable));
                                break;
                            case ActConstant.PARAM_BOOLEAN:
                                paramClass[i] = Boolean.valueOf(variable).getClass();
                                params.add(Boolean.valueOf(variable));
                                break;
                        }
                    }
                }
                Method method = ReflectionUtils.findMethod(beanName.getClass(), methodName, paramClass);
                assert method != null;
                obj = ReflectionUtils.invokeMethod(method, beanName, params.toArray());
            } else {
                Method method = ReflectionUtils.findMethod(beanName.getClass(), methodName);
                assert method != null;
                obj = ReflectionUtils.invokeMethod(method, beanName);
            }
            if (obj == null) {
                throw new ServiceException("???" + taskName + "?????????????????????????????????,???????????????????????????,????????????" + businessRule.getBeanName() + "???Bean????????????" + methodName + "?????????");
            }
            return Arrays.asList(obj.toString().split(","));
        } catch (Exception e) {
            throw new ServiceException(e.getMessage());
        }
    }

    /**
     * @Description: ????????????????????????
     * @param: o ??????
     * @param: idList ????????????
     * @param: id ??????id
     * @Author: gssong
     * @Date: 2022/1/16
     */
    public void setStatusFileValue(Object o, List<String> idList, String id) {
        Class<?> aClass = o.getClass();
        Field businessStatus;
        try {
            businessStatus = aClass.getDeclaredField(ACT_BUSINESS_STATUS);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            throw new ServiceException("?????????" + ACT_BUSINESS_STATUS + "??????");
        }
        businessStatus.setAccessible(true);
        List<ActBusinessStatus> infoByBusinessKey = iActBusinessStatusService.getListInfoByBusinessKey(idList);
        try {
            if (CollectionUtil.isNotEmpty(infoByBusinessKey)) {
                ActBusinessStatus actBusinessStatus = infoByBusinessKey.stream().filter(e -> e.getBusinessKey().equals(id)).findFirst().orElse(null);
                if (ObjectUtil.isNotEmpty(actBusinessStatus)) {
                    businessStatus.set(o, actBusinessStatus);
                } else {
                    ActBusinessStatus status = new ActBusinessStatus();
                    status.setStatus(BusinessStatusEnum.DRAFT.getStatus());
                    businessStatus.set(o, status);
                }
            } else {
                ActBusinessStatus status = new ActBusinessStatus();
                status.setStatus(BusinessStatusEnum.DRAFT.getStatus());
                businessStatus.set(o, status);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceException("????????????????????????");
        }
    }

    /**
     * @Description: ??????????????????id
     * @param: o ??????
     * @param: idList ????????????
     * @param: id ??????id
     * @return: void
     * @Author: gssong
     * @Date: 2022/1/16
     */
    public void setProcessInstIdFileValue(Object o, List<String> idList, String id) {
        Class<?> aClass = o.getClass();
        Field processInstanceId;
        try {
            processInstanceId = aClass.getDeclaredField(PROCESS_INSTANCE_ID);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            throw new ServiceException("?????????" + PROCESS_INSTANCE_ID + "??????");
        }
        processInstanceId.setAccessible(true);
        List<ActBusinessStatus> infoByBusinessKey = iActBusinessStatusService.getListInfoByBusinessKey(idList);
        try {
            if (CollectionUtil.isNotEmpty(infoByBusinessKey)) {
                ActBusinessStatus actBusinessStatus = infoByBusinessKey.stream().filter(e -> e.getBusinessKey().equals(id)).findFirst().orElse(null);
                if (ObjectUtil.isNotEmpty(actBusinessStatus) && StringUtils.isNotBlank(actBusinessStatus.getProcessInstanceId())) {
                    processInstanceId.set(o, actBusinessStatus.getProcessInstanceId());
                } else {
                    processInstanceId.set(o, "");
                }
            } else {
                processInstanceId.set(o, "");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceException("????????????????????????");
        }
    }

    /**
     * @Description: ???????????????
     * @param: params
     * @param: chooseWay
     * @param: nodeName
     * @return: java.util.List<java.lang.Long>
     * @author: gssong
     * @Date: 2022/4/11 13:36
     */
    public List<Long> getAssigneeIdList(String params, String chooseWay, String nodeName) {
        List<Long> paramList = new ArrayList<>();
        String[] split = params.split(",");
        for (String userId : split) {
            paramList.add(Long.valueOf(userId));
        }
        List<SysUser> list = null;
        LambdaQueryWrapper<SysUser> queryWrapper = Wrappers.lambdaQuery();
        // ?????????id??????
        if (WORKFLOW_PERSON.equals(chooseWay)) {
            queryWrapper.in(SysUser::getUserId, paramList);
            list = sysUserMapper.selectList(queryWrapper);
            //?????????id????????????
        } else if (WORKFLOW_ROLE.equals(chooseWay)) {
            List<SysRole> sysRoles = sysRoleMapper.selectList(new LambdaQueryWrapper<SysRole>().in(SysRole::getRoleId, paramList));
            if (CollectionUtil.isNotEmpty(sysRoles)) {
                List<Long> collectRoleId = sysRoles.stream().map(SysRole::getRoleId).collect(Collectors.toList());
                List<SysUserRole> sysUserRoles = userRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>().in(SysUserRole::getRoleId, collectRoleId));
                queryWrapper.in(SysUser::getUserId, sysUserRoles.stream().map(SysUserRole::getUserId).collect(Collectors.toList()));
                list = sysUserMapper.selectList(queryWrapper);
            }
            //?????????id????????????
        } else if (WORKFLOW_DEPT.equals(chooseWay)) {
            queryWrapper.in(SysUser::getDeptId, paramList);
            list = sysUserMapper.selectList(queryWrapper);
        }
        if (CollectionUtil.isEmpty(list)) {
            throw new ServiceException(nodeName + "??????????????????????????????");
        }
        List<Long> userIds = list.stream().map(SysUser::getUserId).collect(Collectors.toList());
        //????????????
        List<Long> missIds = paramList.stream().filter(id -> !userIds.contains(id)).collect(Collectors.toList());
        if (CollectionUtil.isNotEmpty(missIds)) {
            throw new ServiceException(missIds + "??????ID?????????");
        }
        return userIds;
    }

    /**
     * @Description: ???????????????????????????????????????
     * @param: processDefinitionId ????????????id
     * @param: taskDefinitionKey ????????????id
     * @return: com.ruoyi.workflow.domain.vo.MultiVo
     * @author: gssong
     * @Date: 2022/4/16 13:31
     */
    public MultiVo isMultiInstance(String processDefinitionId, String taskDefinitionKey) {
        BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinitionId);
        FlowNode flowNode = (FlowNode)bpmnModel.getFlowElement(taskDefinitionKey);
        MultiVo multiVo = new MultiVo();
        //?????????????????????????????????
        if(flowNode.getBehavior()  instanceof ParallelMultiInstanceBehavior){
            ParallelMultiInstanceBehavior behavior = (ParallelMultiInstanceBehavior) flowNode.getBehavior();
            if (behavior != null && behavior.getCollectionVariable() != null) {
                String assigneeList = behavior.getCollectionVariable();
                String assignee = behavior.getCollectionElementVariable();
                multiVo.setType(behavior);
                multiVo.setAssignee(assignee);
                multiVo.setAssigneeList(assigneeList);
                return multiVo;
            }
            //?????????????????????????????????
        }else if(flowNode.getBehavior()  instanceof SequentialMultiInstanceBehavior){
            SequentialMultiInstanceBehavior behavior = (SequentialMultiInstanceBehavior) flowNode.getBehavior();
            if (behavior != null && behavior.getCollectionVariable() != null) {
                String assigneeList = behavior.getCollectionVariable();
                String assignee = behavior.getCollectionElementVariable();
                multiVo.setType(behavior);
                multiVo.setAssignee(assignee);
                multiVo.setAssigneeList(assigneeList);
                return multiVo;
            }
        }
        return null;
    }

    /**
     * @Description: ???????????????
     * @param: parentTask
     * @param: assignees
     * @return: java.util.List<org.activiti.engine.task.Task>
     * @author: gssong
     * @Date: 2022/5/6 19:18
     */
    public List<Task> createSubTask(List<Task> parentTaskList, String assignees) {
        List<Task> list = new ArrayList<>();
        for (Task parentTask : parentTaskList) {
            String[] userIds = assignees.split(",");
            for (String userId : userIds) {
                TaskEntity newTask = (TaskEntity) taskService.newTask();
                newTask.setParentTaskId(parentTask.getId());
                newTask.setAssignee(userId);
                newTask.setName("????????????-" + parentTask.getName());
                newTask.setProcessDefinitionId(parentTask.getProcessDefinitionId());
                newTask.setProcessInstanceId(parentTask.getProcessInstanceId());
                newTask.setTaskDefinitionKey(parentTask.getTaskDefinitionKey());
                taskService.saveTask(newTask);
                list.add(newTask);
            }
        }
        if (CollectionUtil.isNotEmpty(list) && CollectionUtil.isNotEmpty(parentTaskList)) {
            String processInstanceId = parentTaskList.get(0).getProcessInstanceId();
            String processDefinitionId = parentTaskList.get(0).getProcessDefinitionId();
            List<String> taskIds = list.stream().map(Task::getId).collect(Collectors.toList());
            LambdaQueryWrapper<ActHiTaskInst> wrapper = new LambdaQueryWrapper<>();
            wrapper.in(ActHiTaskInst::getId, taskIds);
            List<ActHiTaskInst> taskInstList = iActHiTaskInstService.list(wrapper);
            if (CollectionUtil.isNotEmpty(taskInstList)) {
                for (ActHiTaskInst hiTaskInst : taskInstList) {
                    hiTaskInst.setProcDefId(processDefinitionId);
                    hiTaskInst.setProcInstId(processInstanceId);
                    hiTaskInst.setStartTime(new Date());
                }
                iActHiTaskInstService.updateBatchById(taskInstList);
            }
        }
        return list;
    }

    /**
     * @Description: ??????????????????
     * @param: parentTask
     * @param: createTime
     * @return: org.flowable.task.service.impl.persistence.entity.TaskEntity
     * @author: gssong
     * @Date: 2022/3/13
     */
    public TaskEntity createNewTask(Task currentTask, Date createTime) {
        TaskEntity task = null;
        if (ObjectUtil.isNotEmpty(currentTask)) {
            task = (TaskEntity) taskService.newTask();
            task.setCategory(currentTask.getCategory());
            task.setDescription(currentTask.getDescription());
            task.setTenantId(currentTask.getTenantId());
            task.setAssignee(currentTask.getAssignee());
            task.setName(currentTask.getName());
            task.setProcessDefinitionId(currentTask.getProcessDefinitionId());
            task.setProcessInstanceId(currentTask.getProcessInstanceId());
            task.setTaskDefinitionKey(currentTask.getTaskDefinitionKey());
            task.setPriority(currentTask.getPriority());
            task.setCreateTime(createTime);
            taskService.saveTask(task);
        }
        if (ObjectUtil.isNotNull(task)) {
            ActHiTaskInst hiTaskInst = iActHiTaskInstService.getById(task.getId());
            if (ObjectUtil.isNotEmpty(hiTaskInst)) {
                hiTaskInst.setProcDefId(task.getProcessDefinitionId());
                hiTaskInst.setProcInstId(task.getProcessInstanceId());
                hiTaskInst.setTaskDefKey(task.getTaskDefinitionKey());
                hiTaskInst.setStartTime(createTime);
                iActHiTaskInstService.updateById(hiTaskInst);
            }
        }
        return task;
    }

    /**
     * @Description: ???????????????
     * @param: sendMessage
     * @param: processInstanceId
     * @return: void
     * @author: gssong
     * @Date: 2022/6/18 13:26
     */
    public void sendMessage(SendMessage sendMessage, String processInstanceId) {
        List<SysMessage> messageList = new ArrayList<>();
        List<Task> taskList = taskService.createTaskQuery().processInstanceId(processInstanceId).list();
        for (Task taskInfo : taskList) {
            if (StringUtils.isNotBlank(taskInfo.getAssignee())) {
                SysMessage sysMessage = new SysMessage();
                sysMessage.setSendId(LoginHelper.getUserId());
                sysMessage.setRecordId(Long.valueOf(taskInfo.getAssignee()));
                sysMessage.setType(1);
                sysMessage.setTitle(sendMessage.getTitle());
                sysMessage.setMessageContent(sendMessage.getMessageContent() + ",??????????????????");
                sysMessage.setStatus(0);
                messageList.add(sysMessage);
            } else {
                List<IdentityLink> identityLinkList = getCandidateUser(taskInfo.getId());
                if (CollectionUtil.isNotEmpty(identityLinkList)) {
                    for (IdentityLink identityLink : identityLinkList) {
                        SysMessage sysMessage = new SysMessage();
                        sysMessage.setSendId(LoginHelper.getUserId());
                        sysMessage.setRecordId(Long.valueOf(identityLink.getUserId()));
                        sysMessage.setType(1);
                        sysMessage.setTitle(sendMessage.getTitle());
                        sysMessage.setMessageContent(sendMessage.getMessageContent() + ",??????????????????");
                        sysMessage.setStatus(0);
                        messageList.add(sysMessage);
                    }
                }
            }
        }
        if (CollectionUtil.isNotEmpty(messageList)) {
            iSysMessageService.sendBatchMessage(messageList);
        }
    }

    /**
     * @Description: ??????bean?????????
     * @param: serviceName bean??????
     * @param: methodName ????????????
     * @param: params ??????
     * @author: gssong
     * @Date: 2022/6/26 15:37
     */
    public void springInvokeMethod(String serviceName, String methodName, Object... params) {
        Object service = SpringUtils.getBean(serviceName);
        Class<?>[] paramClass = null;
        if (Objects.nonNull(params)) {
            int paramsLength = params.length;
            paramClass = new Class[paramsLength];
            for (int i = 0; i < paramsLength; i++) {
                paramClass[i] = params[i].getClass();
            }
        }
        // ????????????
        Method method = ReflectionUtils.findMethod(service.getClass(), methodName, paramClass);
        // ????????????
        assert method != null;
        ReflectionUtils.invokeMethod(method, service, params);
    }

    /**
     * @Description: ???????????????
     * @param: taskId
     * @return: java.util.List<org.flowable.identitylink.api.IdentityLink>
     * @author: gssong
     * @Date: 2022/7/9 17:55
     */
    public List<IdentityLink> getCandidateUser(String taskId) {
        return taskService.getIdentityLinksForTask(taskId);
    }

    /**
     * @Description: ??????????????????
     * @param: processInstanceId ????????????id
     * @param: businessKey ??????id
     * @param: actNodeAssignees ??????????????????
     * @return: java.lang.Boolean
     * @author: gssong
     * @Date: 2022/7/12 21:27
     */
    public Boolean autoComplete(String processInstanceId, String businessKey, List<ActNodeAssignee> actNodeAssignees, TaskCompleteREQ req) {

        List<Task> taskList = taskService.createTaskQuery().processInstanceId(processInstanceId).list();
        if (CollectionUtil.isEmpty(taskList)) {
            iActBusinessStatusService.updateState(businessKey, BusinessStatusEnum.FINISH);
        }
        for (Task task : taskList) {
            ActNodeAssignee nodeAssignee = actNodeAssignees.stream().filter(e -> task.getTaskDefinitionKey().equals(e.getNodeId())).findFirst().orElse(null);
            if (ObjectUtil.isNull(nodeAssignee)) {
                throw new ServiceException("????????????" + task.getName() + "???????????????");
            }

            if (!nodeAssignee.getAutoComplete()) {
                return false;
            }
            settingAssignee(task, nodeAssignee, nodeAssignee.getMultiple());
            List<Long> assignees = req.getAssignees(task.getTaskDefinitionKey());
            if (!nodeAssignee.getIsShow() && CollectionUtil.isNotEmpty(assignees) && assignees.contains(LoginHelper.getUserId())) {
                taskService.addComment(task.getId(), task.getProcessInstanceId(), "????????????????????????????????????");
                taskService.complete(task.getId());
                recordExecuteNode(task, actNodeAssignees);
            } else {
                settingAssignee(task, nodeAssignee, nodeAssignee.getMultiple());
            }

        }
        List<Task> list = taskService.createTaskQuery().processInstanceId(processInstanceId)
            .taskCandidateOrAssigned(LoginHelper.getUserId().toString()).list();
        if(CollectionUtil.isEmpty(list)){
            return false;
        }
        for (Task task : list) {
            taskService.addComment(task.getId(), task.getProcessInstanceId(), "????????????????????????????????????");
            taskService.complete(task.getId());
            recordExecuteNode(task, actNodeAssignees);
        }
        autoComplete(processInstanceId, businessKey, actNodeAssignees, req);
        return true;
    }

    /**
     * @Description: ????????????????????????
     * @param: task ????????????
     * @param: actNodeAssignee ????????????
     * @param: multiple ?????????????????????
     * @return: void
     * @author: gssong
     * @Date: 2022/7/8
     */
    public void settingAssignee(Task task, ActNodeAssignee actNodeAssignee, Boolean multiple) {
        //?????????????????????
        if (ActConstant.WORKFLOW_RULE.equals(actNodeAssignee.getChooseWay())) {
            ActBusinessRuleVo actBusinessRuleVo = iActBusinessRuleService.queryById(actNodeAssignee.getBusinessRuleId());
            List<String> ruleAssignList = ruleAssignList(actBusinessRuleVo, task.getId(), task.getName());
            List<Long> userIdList = new ArrayList<>();
            for (String userId : ruleAssignList) {
                userIdList.add(Long.valueOf(userId));
            }
            if (multiple) {
                taskService.setVariable(task.getId(), actNodeAssignee.getMultipleColumn(), userIdList);
            } else {
                setAssignee(task, userIdList);
            }
        } else {
            if (StringUtils.isBlank(actNodeAssignee.getAssigneeId())) {
                throw new ServiceException("????????????" + task.getName() + "???????????????");
            }
            // ??????????????????
            List<Long> assignees = getAssigneeIdList(actNodeAssignee.getAssigneeId(), actNodeAssignee.getChooseWay(), task.getName());
            if (multiple) {
                taskService.setVariable(task.getId(), actNodeAssignee.getMultipleColumn(), assignees);
            } else {
                setAssignee(task, assignees);
            }
        }
    }

    /**
     * @Description: ??????????????????
     * @param: task ??????
     * @param: assignees ?????????
     * @return: void
     * @author: gssong
     * @Date: 2021/10/21
     */
    public void setAssignee(Task task, List<Long> assignees) {
        if (assignees.size() == 1) {
            taskService.setAssignee(task.getId(), assignees.get(0).toString());
        } else {
            // ?????????????????????
            for (Long assignee : assignees) {
                taskService.addCandidateUser(task.getId(), assignee.toString());
            }
        }
    }


    /**
     * @Description: ??????????????????
     * @param: task
     * @param: actNodeAssignees
     * @return: void
     * @author: gssong
     * @Date: 2022/7/29 20:57
     */
    public void recordExecuteNode(Task task, List<ActNodeAssignee> actNodeAssignees) {
        List<ActTaskNode> actTaskNodeList = iActTaskNodeService.getListByInstanceId(task.getProcessInstanceId());
        ActTaskNode actTaskNode = new ActTaskNode();
        actTaskNode.setNodeId(task.getTaskDefinitionKey());
        actTaskNode.setNodeName(task.getName());
        actTaskNode.setInstanceId(task.getProcessInstanceId());
        if (CollectionUtil.isEmpty(actTaskNodeList)) {
            actTaskNode.setOrderNo(0);
            actTaskNode.setIsBack(true);
            iActTaskNodeService.save(actTaskNode);
        } else {
            ActNodeAssignee actNodeAssignee = actNodeAssignees.stream().filter(e -> e.getNodeId().equals(task.getTaskDefinitionKey())).findFirst().orElse(null);
            //??????????????????????????????????????? ????????????????????????
            if (ObjectUtil.isEmpty(actNodeAssignee)) {
                actTaskNode.setIsBack(true);
            } else {
                actTaskNode.setIsBack(actNodeAssignee.getIsBack());
            }
            iActTaskNodeService.saveTaskNode(actTaskNode);
        }
    }
}
