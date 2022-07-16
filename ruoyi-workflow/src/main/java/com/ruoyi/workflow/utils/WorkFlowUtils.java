package com.ruoyi.workflow.utils;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
import com.ruoyi.workflow.domain.*;
import com.ruoyi.workflow.domain.bo.SendMessage;
import com.ruoyi.workflow.domain.vo.MultiVo;
import com.ruoyi.workflow.flowable.cmd.DeleteExecutionCmd;
import com.ruoyi.workflow.flowable.cmd.DeleteTaskCmd;
import com.ruoyi.workflow.flowable.cmd.ExpressCmd;
import com.ruoyi.workflow.common.constant.ActConstant;
import com.ruoyi.workflow.common.enums.BusinessStatusEnum;
import com.ruoyi.workflow.domain.vo.ActBusinessRuleVo;
import com.ruoyi.workflow.domain.vo.ProcessNode;
import com.ruoyi.workflow.service.IActBusinessStatusService;
import com.ruoyi.workflow.service.IActHiTaskInstService;
import com.ruoyi.workflow.service.ISysMessageService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.flowable.bpmn.converter.BpmnXMLConverter;
import org.flowable.bpmn.model.*;
import org.flowable.common.engine.api.delegate.Expression;
import org.flowable.editor.language.json.converter.BpmnJsonConverter;
import org.flowable.engine.*;
import org.flowable.engine.impl.bpmn.behavior.ParallelMultiInstanceBehavior;
import org.flowable.engine.impl.bpmn.behavior.SequentialMultiInstanceBehavior;
import org.flowable.engine.impl.persistence.entity.ExecutionEntityImpl;
import org.flowable.identitylink.api.IdentityLink;
import org.flowable.task.api.Task;
import org.flowable.task.service.impl.persistence.entity.TaskEntity;
import org.flowable.variable.api.persistence.entity.VariableInstance;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

import static com.ruoyi.workflow.common.constant.ActConstant.*;

/**
 * @program: ruoyi-vue-plus
 * @description: 工作流工具栏
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

    /**
     * @Description: bpmnModel转为xml
     * @param: jsonBytes
     * @return: byte[]
     * @Author: gssong
     * @Date: 2021/11/5
     */
    public byte[] bpmnJsonToXmlBytes(byte[] jsonBytes) throws IOException {
        if (jsonBytes == null) {
            return null;
        }
        // 1. json字节码转成 BpmnModel 对象
        ObjectMapper objectMapper = JsonUtils.getObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(jsonBytes);
        BpmnModel bpmnModel = new BpmnJsonConverter().convertToBpmnModel(jsonNode);

        if (bpmnModel.getProcesses().size() == 0) {
            return null;
        }
        //2.将bpmnModel转为xml
        byte[] bytes = new BpmnXMLConverter().convertToXML(bpmnModel);
        return bytes;
    }

    /**
     * @Description: 获取下一审批节点信息
     * @param: flowElements 全部节点
     * @param: flowElement 当前节点信息
     * @param: nextNodes 下一节点信息
     * @param: tempNodes 保存没有表达式的节点信息
     * @param: taskId 任务id
     * @param: gateway 网关
     * @return: void
     * @author: gssong
     * @Date: 2022/4/11 13:37
     */
    public void getNextNodes(Collection<FlowElement> flowElements, FlowElement flowElement, ExecutionEntityImpl executionEntity, List<ProcessNode> nextNodes, List<ProcessNode> tempNodes, String taskId, String gateway) {
        // 获取当前节点的连线信息
        List<SequenceFlow> outgoingFlows = ((FlowNode) flowElement).getOutgoingFlows();
        // 当前节点的所有下一节点出口
        for (SequenceFlow sequenceFlow : outgoingFlows) {
            // 下一节点的目标元素
            ProcessNode processNode = new ProcessNode();
            ProcessNode tempNode = new ProcessNode();
            FlowElement outFlowElement = sequenceFlow.getTargetFlowElement();
            if (outFlowElement instanceof UserTask) {
                buildNode(executionEntity, nextNodes, tempNodes, taskId, gateway, sequenceFlow, processNode, tempNode, outFlowElement);
            } else if (outFlowElement instanceof ExclusiveGateway) { // 排他网关
                getNextNodes(flowElements, outFlowElement, executionEntity, nextNodes, tempNodes, taskId, ActConstant.EXCLUSIVE_GATEWAY);
            } else if (outFlowElement instanceof ParallelGateway) { //并行网关
                getNextNodes(flowElements, outFlowElement, executionEntity, nextNodes, tempNodes, taskId, ActConstant.PARALLEL_GATEWAY);
            } else if (outFlowElement instanceof InclusiveGateway) { //包含网关
                getNextNodes(flowElements, outFlowElement, executionEntity, nextNodes, tempNodes, taskId, ActConstant.INCLUSIVE_GATEWAY);
            } else if (outFlowElement instanceof EndEvent) {
                FlowElement subProcess = getSubProcess(flowElements, outFlowElement);
                if (subProcess == null) {
                    continue;
                }
                getNextNodes(flowElements, subProcess, executionEntity, nextNodes, tempNodes, taskId, ActConstant.END_EVENT);
            } else if (outFlowElement instanceof SubProcess) {
                Collection<FlowElement> subFlowElements = ((SubProcess) outFlowElement).getFlowElements();
                for (FlowElement element : subFlowElements) {
                    if (element instanceof UserTask) {
                        buildNode(executionEntity, nextNodes, tempNodes, taskId, gateway, sequenceFlow, processNode, tempNode, element);
                        break;
                    }
                }
            } else {
                throw new ServiceException("未识别出节点类型");
            }
        }
    }

    /**
     * @Description: 构建下一审批节点
     * @param: executionEntity
     * @param: nextNodes 下一节点信息
     * @param: tempNodes 保存没有表达式的节点信息
     * @param: taskId 任务id
     * @param: gateway 网关
     * @param: sequenceFlow  节点
     * @param: processNode 下一节点的目标元素
     * @param: tempNode  保存没有表达式的节点
     * @param: outFlowElement 目标节点
     * @return: void
     * @author: gssong
     * @Date: 2022/4/11 13:35
     */
    private void buildNode(ExecutionEntityImpl executionEntity, List<ProcessNode> nextNodes, List<ProcessNode> tempNodes, String taskId, String gateway, SequenceFlow sequenceFlow, ProcessNode processNode, ProcessNode tempNode, FlowElement outFlowElement) {
        // 用户任务，则获取响应给前端设置办理人或者候选人
        // 判断是否为排它网关
        if (ActConstant.EXCLUSIVE_GATEWAY.equals(gateway)) {
            String conditionExpression = sequenceFlow.getConditionExpression();
            //判断是否有条件
            if (StringUtils.isNotBlank(conditionExpression)) {
                ExpressCmd expressCmd = new ExpressCmd(sequenceFlow, executionEntity);
                Boolean condition = managementService.executeCommand(expressCmd);
                if (condition) {
                    processNode.setNodeId(outFlowElement.getId());
                    processNode.setNodeName(outFlowElement.getName());
                    processNode.setNodeType(ActConstant.EXCLUSIVE_GATEWAY);
                    processNode.setTaskId(taskId);
                    processNode.setExpression(true);
                    processNode.setChooseWay(ActConstant.WORKFLOW_ASSIGNEE);
                    processNode.setAssignee(((UserTask) outFlowElement).getAssignee());
                    processNode.setAssigneeId(((UserTask) outFlowElement).getAssignee());
                    nextNodes.add(processNode);
                } else {
                    processNode.setNodeId(outFlowElement.getId());
                    processNode.setNodeName(outFlowElement.getName());
                    processNode.setNodeType(ActConstant.EXCLUSIVE_GATEWAY);
                    processNode.setTaskId(taskId);
                    processNode.setExpression(false);
                    processNode.setChooseWay(ActConstant.WORKFLOW_ASSIGNEE);
                    processNode.setAssignee(((UserTask) outFlowElement).getAssignee());
                    processNode.setAssigneeId(((UserTask) outFlowElement).getAssignee());
                    nextNodes.add(processNode);
                }
            } else {
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
            //包含网关
        } else if (ActConstant.INCLUSIVE_GATEWAY.equals(gateway)) {
            String conditionExpression = sequenceFlow.getConditionExpression();
            if (StringUtils.isBlank(conditionExpression)) {
                processNode.setNodeId(outFlowElement.getId());
                processNode.setNodeName(outFlowElement.getName());
                processNode.setNodeType(ActConstant.EXCLUSIVE_GATEWAY);
                processNode.setTaskId(taskId);
                processNode.setExpression(true);
                processNode.setChooseWay(ActConstant.WORKFLOW_ASSIGNEE);
                processNode.setAssignee(((UserTask) outFlowElement).getAssignee());
                processNode.setAssigneeId(((UserTask) outFlowElement).getAssignee());
                nextNodes.add(processNode);
            } else {
                ExpressCmd expressCmd = new ExpressCmd(sequenceFlow, executionEntity);
                Boolean condition = managementService.executeCommand(expressCmd);
                if (condition) {
                    processNode.setNodeId(outFlowElement.getId());
                    processNode.setNodeName(outFlowElement.getName());
                    processNode.setNodeType(ActConstant.EXCLUSIVE_GATEWAY);
                    processNode.setTaskId(taskId);
                    processNode.setExpression(true);
                    processNode.setChooseWay(ActConstant.WORKFLOW_ASSIGNEE);
                    processNode.setAssignee(((UserTask) outFlowElement).getAssignee());
                    processNode.setAssigneeId(((UserTask) outFlowElement).getAssignee());
                    nextNodes.add(processNode);
                }
            }
        } else {
            processNode.setNodeId(outFlowElement.getId());
            processNode.setNodeName(outFlowElement.getName());
            processNode.setNodeType(ActConstant.USER_TASK);
            processNode.setTaskId(taskId);
            processNode.setExpression(true);
            processNode.setChooseWay(ActConstant.WORKFLOW_ASSIGNEE);
            processNode.setAssignee(((UserTask) outFlowElement).getAssignee());
            processNode.setAssigneeId(((UserTask) outFlowElement).getAssignee());
            nextNodes.add(processNode);
        }
    }

    /**
     * @Description: 判断是否为主流程结束节点
     * @param: flowElements全部节点
     * @param: endElement 结束节点
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
     * @Description: 查询业务规则中的人员id
     * @param: actFullClass 业务规则对象
     * @param: taskId 任务id
     * @return: 查询业务规则
     * @return: java.lang.Object
     * @author: gssong
     * @Date: 2022/4/11 13:35
     */
    public Object assignList(ActBusinessRuleVo businessRule, String taskId) {
        try {
            //返回值
            Object obj;
            //方法名称
            String methodName = businessRule.getMethod();
            //全类名
            Object beanName = SpringUtils.getBean(businessRule.getBeanName());
            List<ActBusinessRuleParam> businessRuleParams;
            if (StringUtils.isNotBlank(businessRule.getParam())) {
                businessRuleParams = JsonUtils.parseArray(businessRule.getParam(), ActBusinessRuleParam.class);
                Class[] paramClass = new Class[businessRuleParams.size()];
                List<Object> params = new ArrayList<>();
                for (int i = 0; i < Objects.requireNonNull(businessRuleParams).size(); i++) {
                    Map<String, VariableInstance> variables = taskService.getVariableInstances(taskId);
                    if (variables.containsKey(businessRuleParams.get(i).getParam())) {
                        VariableInstance v = variables.get(businessRuleParams.get(i).getParam());
                        String variable = v.getTextValue();
                        switch (businessRuleParams.get(i).getParamType()) {
                            case ActConstant.PARAM_BYTE:
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
            return obj;
        } catch (Exception e) {
            throw new ServiceException(e.getMessage());
        }
    }

    /**
     * @Description: 设置业务流程参数
     * @param: o 对象
     * @param: idList 主键集合
     * @param: id 主键id
     * @Author: gssong
     * @Date: 2022/1/16
     */
    public void setStatusFileValue(Object o, List<String> idList, String id) {
        Class<?> aClass = o.getClass();
        Field businessStatus;
        try {
            businessStatus = aClass.getDeclaredField(ACT_BUSINESSS_TATUS);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            throw new ServiceException("未找到" + ACT_BUSINESSS_TATUS + "属性");
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
            throw new ServiceException("设置流程状态失败");
        }
    }

    /**
     * @Description: 设置流程实例id
     * @param: o 对象
     * @param: idList 主键集合
     * @param: id 主键id
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
            throw new ServiceException("未找到" + PROCESS_INSTANCE_ID + "属性");
        }
        processInstanceId.setAccessible(true);
        List<ActBusinessStatus> infoByBusinessKey = iActBusinessStatusService.getListInfoByBusinessKey(idList);
        try {
            if (CollectionUtil.isNotEmpty(infoByBusinessKey)) {
                ActBusinessStatus actBusinessStatus = infoByBusinessKey.stream().filter(e -> e.getBusinessKey().equals(id)).findFirst().orElse(null);
                if (ObjectUtil.isNotEmpty(actBusinessStatus)) {
                    processInstanceId.set(o, actBusinessStatus.getProcessInstanceId());
                } else {
                    processInstanceId.set(o, "");
                }
            } else {
                processInstanceId.set(o, "");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceException("设置流程状态失败");
        }
    }

    /**
     * @Description: 查询审批人
     * @param: params
     * @param: chooseWay
     * @param: nodeName
     * @return: java.util.List<java.lang.Long>
     * @author: gssong
     * @Date: 2022/4/11 13:36
     */
    public List<Long> assignees(String params, String chooseWay, String nodeName) {
        List<Long> paramList = new ArrayList<>();
        String[] split = params.split(",");
        for (String userId : split) {
            paramList.add(Long.valueOf(userId));
        }
        List<SysUser> list = null;
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        // 按用户id查询
        if (WORKFLOW_PERSON.equals(chooseWay)) {
            queryWrapper.in(SysUser::getUserId, paramList);
            list = sysUserMapper.selectList(queryWrapper);
            //按角色id查询用户
        } else if (WORKFLOW_ROLE.equals(chooseWay)) {
            List<SysRole> sysRoles = sysRoleMapper.selectList(new LambdaQueryWrapper<SysRole>().in(SysRole::getRoleId, paramList));
            if (CollectionUtil.isNotEmpty(sysRoles)) {
                List<Long> collectRoleId = sysRoles.stream().map(e -> e.getRoleId()).collect(Collectors.toList());
                List<SysUserRole> sysUserRoles = userRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>().in(SysUserRole::getRoleId, collectRoleId));
                queryWrapper.in(SysUser::getUserId, sysUserRoles.stream().map(e -> e.getUserId()).collect(Collectors.toList()));
                list = sysUserMapper.selectList(queryWrapper);
            }
            //按部门id查询用户
        } else if (WORKFLOW_DEPT.equals(chooseWay)) {
            queryWrapper.in(SysUser::getDeptId, paramList);
            list = sysUserMapper.selectList(queryWrapper);
        }
        if (CollectionUtil.isEmpty(list)) {
            throw new ServiceException(nodeName + "任务环节未配置审批人");
        }
        List<Long> userIds = list.stream().map(e -> e.getUserId()).collect(Collectors.toList());
        //校验人员
        List<Long> missIds = paramList.stream().filter(id -> !userIds.contains(id)).collect(Collectors.toList());
        if (CollectionUtil.isNotEmpty(missIds)) {
            throw new ServiceException(missIds + "人员ID不存在");
        }
        return userIds;
    }

    /**
     * @Description: 删除正在执行的任务
     * @param: task
     * @return: void
     * @author: gssong
     * @Date: 2022/4/11 13:36
     */
    public void deleteRuntimeTask(Task task) {
        DeleteTaskCmd deleteTaskCmd = new DeleteTaskCmd(task.getId());
        managementService.executeCommand(deleteTaskCmd);
        DeleteExecutionCmd deleteExecutionCmd = new DeleteExecutionCmd(task.getExecutionId());
        managementService.executeCommand(deleteExecutionCmd);
        historyService.deleteHistoricTaskInstance(task.getId());
        historyService.createNativeHistoricActivityInstanceQuery()
            .sql("DELETE  FROM ACT_HI_ACTINST WHERE EXECUTION_ID_ = '" + task.getExecutionId() + "'").list();
    }

    /**
     * @Description: 判断当前节点是否为会签节点
     * @param: processDefinitionId 流程定义id
     * @param: taskDefinitionKey 当前节点id
     * @return: java.lang.Boolean
     * @author: gssong
     * @Date: 2022/4/16 13:31
     */
    public MultiVo isMultiInstance(String processDefinitionId, String taskDefinitionKey) {
        BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinitionId);
        FlowNode flowNode = (FlowNode) bpmnModel.getFlowElement(taskDefinitionKey);
        MultiVo multiVo = new MultiVo();
        //判断是否为并行会签节点
        if (flowNode.getBehavior() instanceof ParallelMultiInstanceBehavior) {
            ParallelMultiInstanceBehavior behavior = (ParallelMultiInstanceBehavior) flowNode.getBehavior();
            if (behavior != null && behavior.getCollectionExpression() != null) {
                Expression collectionExpression = behavior.getCollectionExpression();
                String assigneeList = collectionExpression.getExpressionText();
                String assignee = behavior.getCollectionElementVariable();
                multiVo.setType(behavior);
                multiVo.setAssignee(assignee);
                multiVo.setAssigneeList(assigneeList);
                return multiVo;
            }
            //判断是否为串行会签节点
        } else if (flowNode.getBehavior() instanceof SequentialMultiInstanceBehavior) {
            SequentialMultiInstanceBehavior behavior = (SequentialMultiInstanceBehavior) flowNode.getBehavior();
            if (behavior != null && behavior.getCollectionExpression() != null) {
                Expression collectionExpression = behavior.getCollectionExpression();
                String assigneeList = collectionExpression.getExpressionText();
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
     * @Description: 创建子任务
     * @param: parentTask
     * @return: java.util.List<org.flowable.task.api.Task>
     * @author: gssong
     * @Date: 2022/5/6 19:18
     */
    public List<Task> createSubTask(List<Task> parentTaskList, String assignees) {
        List<Task> list = new ArrayList<>();
        for (Task parentTask : parentTaskList) {
            List<String> userIds = Arrays.asList(assignees.split(","));
            for (String userId : userIds) {
                TaskEntity newTask = (TaskEntity) taskService.newTask();
                newTask.setParentTaskId(parentTask.getId());
                newTask.setAssignee(userId);
                newTask.setName("【抄送】-" + parentTask.getName());
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
     * @Description: 创建流程任务
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
     * @Description: 发送站内信
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
                sysMessage.setMessageContent(sendMessage.getMessageContent() + ",请您注意查收");
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
                        sysMessage.setMessageContent(sendMessage.getMessageContent() + ",请您注意查收");
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
     * @Description: 执行bean中方法
     * @param: serviceName bean名称
     * @param: methodName 方法名称
     * @param: params 参数
     * @author: gssong
     * @Date: 2022/6/26 15:37
     */
    public void springInvokeMethod(String serviceName, String methodName, Object... params) {
        Object service = SpringUtils.getBean(serviceName);
        Class<? extends Object>[] paramClass = null;
        if (Objects.nonNull(params)) {
            int paramsLength = params.length;
            paramClass = new Class[paramsLength];
            for (int i = 0; i < paramsLength; i++) {
                paramClass[i] = params[i].getClass();
            }
        }
        // 找到方法
        Method method = ReflectionUtils.findMethod(service.getClass(), methodName, paramClass);
        // 执行方法
        assert method != null;
        ReflectionUtils.invokeMethod(method, service, params);
    }

    /**
     * @Description: 获取候选人
     * @param: taskId
     * @return: java.util.List<org.flowable.identitylink.api.IdentityLink>
     * @author: gssong
     * @Date: 2022/7/9 17:55
     */
    public List<IdentityLink> getCandidateUser(String taskId) {
        return taskService.getIdentityLinksForTask(taskId);
    }

    /**
     * @Description: 办理任务
     * @param: processInstanceId 流程实例id
     * @param: actNodeAssignees 流程定义设置
     * @return: void
     * @author: gssong
     * @Date: 2022/7/12 21:27
     */
    public void completeTask(String processInstanceId, List<ActNodeAssignee> actNodeAssignees) {
        List<Task> list = taskService.createTaskQuery().processInstanceId(processInstanceId)
            .taskCandidateOrAssigned(LoginHelper.getUserId().toString()).list();
        if (CollectionUtil.isNotEmpty(list)) {
            for (Task task : list) {
                ActNodeAssignee actNodeAssignee = actNodeAssignees.stream().filter(e -> e.getNodeId().equals(task.getTaskDefinitionKey())).findFirst().orElse(null);
                if (ObjectUtil.isEmpty(actNodeAssignee)) {
                    throw new ServiceException("请检查【" + task.getName() + "】节点配置");
                }
                if (actNodeAssignee.getAutoComplete()) {
                    taskService.setAssignee(task.getId(), LoginHelper.getUserId().toString());
                    taskService.complete(task.getId());
                }
            }
            completeTask(processInstanceId, actNodeAssignees);
        }
    }
}
