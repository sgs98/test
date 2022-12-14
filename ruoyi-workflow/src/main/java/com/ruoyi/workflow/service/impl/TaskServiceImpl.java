package com.ruoyi.workflow.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.helper.LoginHelper;
import com.ruoyi.common.utils.JsonUtils;
import com.ruoyi.workflow.activiti.cmd.*;
import com.ruoyi.workflow.common.constant.ActConstant;
import com.ruoyi.workflow.common.enums.BusinessStatusEnum;
import com.ruoyi.workflow.domain.ActBusinessStatus;
import com.ruoyi.workflow.domain.ActHiTaskInst;
import com.ruoyi.workflow.domain.ActNodeAssignee;
import com.ruoyi.workflow.domain.ActTaskNode;
import com.ruoyi.workflow.domain.bo.*;
import com.ruoyi.workflow.domain.vo.*;
import com.ruoyi.workflow.activiti.factory.WorkflowService;
import com.ruoyi.workflow.mapper.TaskMapper;
import com.ruoyi.workflow.service.*;
import com.ruoyi.workflow.utils.WorkFlowUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.activiti.bpmn.model.*;
import org.activiti.engine.ManagementService;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricTaskInstanceQuery;
import org.activiti.engine.impl.bpmn.behavior.ParallelMultiInstanceBehavior;
import org.activiti.engine.impl.bpmn.behavior.SequentialMultiInstanceBehavior;
import org.activiti.engine.impl.persistence.entity.ExecutionEntityImpl;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.persistence.entity.VariableInstance;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static com.ruoyi.common.helper.LoginHelper.getUserId;

/**
 * @program: ruoyi-vue-plus
 * @description: ???????????????
 * @author: gssong
 * @created: 2021/10/17 14:57
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TaskServiceImpl extends WorkflowService implements ITaskService {

    private static final Logger logger = LoggerFactory.getLogger(TaskServiceImpl.class);

    private final IUserService iUserService;

    private final IActBusinessStatusService iActBusinessStatusService;

    private final WorkFlowUtils workFlowUtils;

    private final IActTaskNodeService iActTaskNodeService;

    private final IActNodeAssigneeService iActNodeAssigneeService;

    private final IActBusinessRuleService iActBusinessRuleService;

    private final IActHiTaskInstService iActHiTaskInstService;

    private final ManagementService managementService;

    private final TaskMapper taskMapper;


    /**
     * @Description: ?????????????????????????????????
     * @param: req
     * @return: com.ruoyi.common.core.page.TableDataInfo<com.ruoyi.workflow.domain.vo.TaskWaitingVo>
     * @author: gssong
     * @Date: 2021/10/17
     */
    @Override
    public TableDataInfo<TaskWaitingVo> getTaskWaitByPage(TaskREQ req) {
        //???????????????
        String currentUserId = LoginHelper.getLoginUser().getUserId().toString();
        TaskQuery query = taskService.createTaskQuery()
            .taskCandidateOrAssigned(currentUserId) // ????????????????????????
            .orderByTaskCreateTime().asc();
        if (StringUtils.isNotEmpty(req.getTaskName())) {
            query.taskNameLikeIgnoreCase("%" + req.getTaskName() + "%");
        }
        List<Task> taskList = query.listPage(req.getFirstResult(), req.getPageSize());
        long total = query.count();
        List<TaskWaitingVo> list = new ArrayList<>();
        for (Task task : taskList) {
            TaskWaitingVo taskWaitingVo = new TaskWaitingVo();
            BeanUtils.copyProperties(task, taskWaitingVo);
            taskWaitingVo.setAssigneeId(StringUtils.isNotBlank(task.getAssignee()) ? Long.valueOf(task.getAssignee()) : null);
            taskWaitingVo.setProcessStatus(!task.isSuspended() ? "??????" : "??????");
            // ??????????????????
            ProcessInstance pi = runtimeService.createProcessInstanceQuery()
                .processInstanceId(task.getProcessInstanceId()).singleResult();
            //???????????????
            String startUserId = pi.getStartUserId();
            if (StringUtils.isNotBlank(startUserId)) {
                SysUser sysUser = iUserService.selectUserById(Long.valueOf(startUserId));
                if (ObjectUtil.isNotNull(sysUser)) {
                    taskWaitingVo.setStartUserNickName(sysUser.getNickName());
                }
            }
            taskWaitingVo.setProcessDefinitionVersion(pi.getProcessDefinitionVersion());
            taskWaitingVo.setProcessDefinitionName(pi.getProcessDefinitionName());
            taskWaitingVo.setBusinessKey(pi.getBusinessKey());
            list.add(taskWaitingVo);
        }
        if (CollectionUtil.isNotEmpty(list)) {
            //?????????????????????
            list.forEach(e -> {
                List<IdentityLink> identityLinkList = workFlowUtils.getCandidateUser(e.getId());
                if (CollectionUtil.isNotEmpty(identityLinkList)) {
                    List<String> collectType = identityLinkList.stream().map(IdentityLink::getType).collect(Collectors.toList());
                    if (StringUtils.isBlank(e.getAssignee()) && collectType.size() > 1 && collectType.contains(ActConstant.CANDIDATE)) {
                        e.setIsClaim(false);
                    } else if (StringUtils.isNotBlank(e.getAssignee()) && collectType.size() > 1 && collectType.contains(ActConstant.CANDIDATE)) {
                        e.setIsClaim(true);
                    }
                }
            });
            //???????????????
            List<Long> assigneeList = list.stream().map(TaskWaitingVo::getAssigneeId).collect(Collectors.toList());
            if (CollectionUtil.isNotEmpty(assigneeList)) {
                List<SysUser> userList = iUserService.selectListUserByIds(assigneeList);
                if (CollectionUtil.isNotEmpty(userList)) {
                    list.forEach(e -> {
                        SysUser sysUser = userList.stream().filter(t -> StringUtils.isNotBlank(e.getAssignee()) && t.getUserId().compareTo(e.getAssigneeId()) == 0).findFirst().orElse(null);
                        if (ObjectUtil.isNotEmpty(sysUser)) {
                            e.setAssignee(sysUser.getNickName());
                            e.setAssigneeId(sysUser.getUserId());
                        }
                    });
                }
            }
            //??????id??????
            List<String> businessKeyList = list.stream().map(TaskWaitingVo::getBusinessKey).collect(Collectors.toList());
            List<ActBusinessStatus> infoList = iActBusinessStatusService.getListInfoByBusinessKey(businessKeyList);
            if (CollectionUtil.isNotEmpty(infoList)) {
                list.forEach(e -> {
                    ActBusinessStatus businessStatus = infoList.stream().filter(t -> t.getBusinessKey().equals(e.getBusinessKey())).findFirst().orElse(null);
                    if (ObjectUtil.isNotEmpty(businessStatus)) {
                        e.setActBusinessStatus(businessStatus);
                    }
                });
            }
        }
        return new TableDataInfo<>(list, total);
    }


    /**
     * @Description: ????????????
     * @param: req
     * @return: java.lang.Boolean
     * @author: gssong
     * @Date: 2021/10/21
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean completeTask(TaskCompleteREQ req) {
        try {
            // 1.????????????
            Task task = taskService.createTaskQuery().taskId(req.getTaskId()).taskAssignee(getUserId().toString()).singleResult();

            if (ObjectUtil.isNull(task)) {
                throw new ServiceException("??????????????????????????????????????????");
            }

            if (task.isSuspended()) {
                throw new ServiceException("????????????????????????");
            }
            //??????????????????
            if (ObjectUtil.isNotEmpty(task.getDelegationState()) && ActConstant.PENDING.equals(task.getDelegationState().name())) {
                taskService.resolveTask(req.getTaskId());
                ActHiTaskInst hiTaskInst = iActHiTaskInstService.getById(task.getId());
                TaskEntity newTask = workFlowUtils.createNewTask(task, hiTaskInst.getStartTime());
                taskService.addComment(newTask.getId(), task.getProcessInstanceId(), req.getMessage());
                taskService.complete(newTask.getId());
                ActHiTaskInst actHiTaskInst = new ActHiTaskInst();
                actHiTaskInst.setId(task.getId());
                actHiTaskInst.setStartTime(new Date());
                iActHiTaskInstService.updateById(actHiTaskInst);
                return true;
            }

            ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(task.getProcessInstanceId()).singleResult();
            // 2. ????????????????????????????????? ??????????????? ????????????????????????????????????
            List<ActNodeAssignee> actNodeAssignees = iActNodeAssigneeService.getInfoByProcessDefinitionId(task.getProcessDefinitionId());
            for (ActNodeAssignee actNodeAssignee : actNodeAssignees) {
                String column = actNodeAssignee.getMultipleColumn();
                String assigneeId = actNodeAssignee.getAssigneeId();
                if (actNodeAssignee.getMultiple() && actNodeAssignee.getIsShow()) {
                    List<Long> userIdList = req.getAssignees(actNodeAssignee.getMultipleColumn());
                    if (CollectionUtil.isNotEmpty(userIdList)) {
                        taskService.setVariable(task.getId(), column, userIdList);
                    }
                }
                //?????????????????????????????????????????????????????????
                if (actNodeAssignee.getMultiple() && !actNodeAssignee.getIsShow() && (StringUtils.isBlank(column) || StringUtils.isBlank(assigneeId))) {
                    throw new ServiceException("????????????" + processInstance.getProcessDefinitionKey() + "????????? ");
                }
                if (actNodeAssignee.getMultiple() && !actNodeAssignee.getIsShow()) {
                    workFlowUtils.settingAssignee(task, actNodeAssignee, actNodeAssignee.getMultiple());
                }
            }
            // 3. ????????????????????????
            taskService.addComment(req.getTaskId(), task.getProcessInstanceId(), req.getMessage());
            // ????????????
            taskService.setVariables(req.getTaskId(), req.getVariables());
            // ?????????????????????
            List<TaskListenerVo> handleBeforeList = null;
            // ?????????????????????
            List<TaskListenerVo> handleAfterList = null;
            ActNodeAssignee nodeEvent = actNodeAssignees.stream().filter(e -> task.getTaskDefinitionKey().equals(e.getNodeId())).findFirst().orElse(null);
            if (ObjectUtil.isNotEmpty(nodeEvent) && StringUtils.isNotBlank(nodeEvent.getTaskListener())) {
                List<TaskListenerVo> taskListenerVos = JsonUtils.parseArray(nodeEvent.getTaskListener(), TaskListenerVo.class);
                handleBeforeList = taskListenerVos.stream().filter(e -> ActConstant.HANDLE_BEFORE.equals(e.getEventType())).collect(Collectors.toList());
                handleAfterList = taskListenerVos.stream().filter(e -> ActConstant.HANDLE_AFTER.equals(e.getEventType())).collect(Collectors.toList());
            }
            // ???????????????
            if (CollectionUtil.isNotEmpty(handleBeforeList)) {
                for (TaskListenerVo taskListenerVo : handleBeforeList) {
                    workFlowUtils.springInvokeMethod(taskListenerVo.getBeanName(), ActConstant.HANDLE_PROCESS
                        , task.getProcessInstanceId(), task.getId());
                }
            }
            // 4. ????????????
            taskService.complete(req.getTaskId());
            // ???????????????
            if (CollectionUtil.isNotEmpty(handleAfterList)) {
                for (TaskListenerVo taskListenerVo : handleAfterList) {
                    workFlowUtils.springInvokeMethod(taskListenerVo.getBeanName(), ActConstant.HANDLE_PROCESS
                        , task.getProcessInstanceId());
                }
            }
            // 5. ????????????????????????????????????
            workFlowUtils.recordExecuteNode(task, actNodeAssignees);
            // ?????????????????????????????????
            iActBusinessStatusService.updateState(processInstance.getBusinessKey(), BusinessStatusEnum.WAITING, task.getProcessInstanceId());
            // 6. ?????????????????????
            List<Task> taskList = taskService.createTaskQuery().processInstanceId(task.getProcessInstanceId()).list();
            // 7. ???????????? ????????????
            if (CollectionUtil.isEmpty(taskList)) {
                // ??????????????????????????? ????????????
                return iActBusinessStatusService.updateState(processInstance.getBusinessKey(), BusinessStatusEnum.FINISH);
            }

            // ??????
            if (req.getIsCopy()) {
                if (StringUtils.isBlank(req.getAssigneeIds())) {
                    throw new ServiceException("????????????????????? ");
                }
                TaskEntity newTask = workFlowUtils.createNewTask(task, new Date());
                taskService.addComment(newTask.getId(), task.getProcessInstanceId(),
                    LoginHelper.getUsername() + "???????????????" + req.getAssigneeNames());
                taskService.complete(newTask.getId());
                workFlowUtils.createSubTask(taskList, req.getAssigneeIds());
            }
            // ????????????
            Boolean autoComplete = workFlowUtils.autoComplete(processInstance.getProcessInstanceId(), processInstance.getBusinessKey(), actNodeAssignees, req);
            if(autoComplete){
                List<Task> nextTaskList = taskService.createTaskQuery().processInstanceId(task.getProcessInstanceId()).list();
                if (!CollectionUtil.isEmpty(nextTaskList)) {
                    for (Task t : nextTaskList) {
                        ActNodeAssignee nodeAssignee = actNodeAssignees.stream().filter(e -> t.getTaskDefinitionKey().equals(e.getNodeId())).findFirst().orElse(null);
                        if (ObjectUtil.isNull(nodeAssignee)) {
                            throw new ServiceException("????????????" + t.getName() + "???????????????");
                        }
                        workFlowUtils.settingAssignee(t, nodeAssignee, nodeAssignee.getMultiple());
                    }
                }else{
                    // ??????????????????????????? ????????????
                    return iActBusinessStatusService.updateState(processInstance.getBusinessKey(), BusinessStatusEnum.FINISH);
                }
                // ???????????????
                workFlowUtils.sendMessage(req.getSendMessage(), processInstance.getProcessInstanceId());
                return true;
            }
            // 8. ??????????????? ???????????????
            List<Task> nextTaskList = taskService.createTaskQuery().processInstanceId(task.getProcessInstanceId()).list();
            if (CollectionUtil.isEmpty(nextTaskList)) {
                // ??????????????????????????? ????????????
                return iActBusinessStatusService.updateState(processInstance.getBusinessKey(), BusinessStatusEnum.FINISH);
            }
            for (Task t : nextTaskList) {
                ActNodeAssignee nodeAssignee = actNodeAssignees.stream().filter(e -> t.getTaskDefinitionKey().equals(e.getNodeId())).findFirst().orElse(null);
                if (ObjectUtil.isNull(nodeAssignee)) {
                    throw new ServiceException("????????????" + t.getName() + "???????????????");
                }
                // ?????????????????????
                if (!nodeAssignee.getIsShow() && StringUtils.isBlank(t.getAssignee())) {
                    // ????????????
                    workFlowUtils.settingAssignee(t, nodeAssignee, false);
                } else if (nodeAssignee.getIsShow() && StringUtils.isBlank(t.getAssignee())) {
                    // ???????????? ????????????????????????id???????????????
                    List<Long> assignees = req.getAssignees(t.getTaskDefinitionKey());
                    // ????????????
                    if (CollectionUtil.isNotEmpty(assignees)) {
                        workFlowUtils.setAssignee(t, assignees);
                    } else if (StringUtils.isBlank(t.getAssignee())) {
                        if (taskList.size() == 1) {
                            throw new ServiceException("???" + t.getName() + "?????????????????????????????????");
                        } else if (taskList.size() > 1) {
                            List<IdentityLink> candidateUser = workFlowUtils.getCandidateUser(t.getId());
                            if (CollectionUtil.isEmpty(candidateUser)) {
                                throw new ServiceException("???" + t.getName() + "?????????????????????????????????");
                            }
                        }
                    }
                }
            }
            // ???????????????
            workFlowUtils.sendMessage(req.getSendMessage(), processInstance.getProcessInstanceId());

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("????????????:" + e.getMessage());
            throw new ServiceException("????????????:" + e.getMessage());
        }
    }

    /**
     * @Description: ?????????????????????????????????
     * @param: req
     * @return: com.ruoyi.common.core.page.TableDataInfo<com.ruoyi.workflow.domain.vo.TaskFinishVo>
     * @author: gssong
     * @Date: 2021/10/23
     */
    @Override
    public TableDataInfo<TaskFinishVo> getTaskFinishByPage(TaskREQ req) {
        //???????????????
        String username = LoginHelper.getUserId().toString();
        HistoricTaskInstanceQuery query = historyService.createHistoricTaskInstanceQuery()
            .taskAssignee(username).finished().orderByHistoricTaskInstanceStartTime().asc();
        if (StringUtils.isNotBlank(req.getTaskName())) {
            query.taskNameLike(req.getTaskName());
        }
        List<HistoricTaskInstance> list = query.listPage(req.getFirstResult(), req.getPageSize());
        long total = query.count();
        List<TaskFinishVo> taskFinishVoList = new ArrayList<>();
        for (HistoricTaskInstance hti : list) {
            TaskFinishVo taskFinishVo = new TaskFinishVo();
            BeanUtils.copyProperties(hti, taskFinishVo);
            ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(hti.getProcessDefinitionId()).singleResult();
            taskFinishVo.setProcessDefinitionName(processDefinition.getName());
            taskFinishVo.setProcessDefinitionKey(processDefinition.getKey());
            taskFinishVo.setVersion(processDefinition.getVersion());
            taskFinishVo.setAssigneeId(StringUtils.isNotBlank(hti.getAssignee()) ? Long.valueOf(hti.getAssignee()) : null);
            taskFinishVoList.add(taskFinishVo);
        }
        if (CollectionUtil.isNotEmpty(list)) {
            //???????????????
            List<Long> assigneeList = taskFinishVoList.stream().map(TaskFinishVo::getAssigneeId).collect(Collectors.toList());
            if (CollectionUtil.isNotEmpty(assigneeList)) {
                List<SysUser> userList = iUserService.selectListUserByIds(assigneeList);
                if (CollectionUtil.isNotEmpty(userList)) {
                    taskFinishVoList.forEach(e -> {
                        SysUser sysUser = userList.stream().filter(t -> t.getUserId().compareTo(e.getAssigneeId()) == 0).findFirst().orElse(null);
                        if (ObjectUtil.isNotEmpty(sysUser)) {
                            e.setAssignee(sysUser.getNickName());
                            e.setAssigneeId(sysUser.getUserId());
                        }
                    });
                }
            }
        }
        return new TableDataInfo<>(taskFinishVoList, total);
    }


    /**
     * @Description: ???????????????????????????????????????
     * @param: req
     * @return: java.util.Map<java.lang.String, java.lang.Object>
     * @author: gssong
     * @Date: 2021/10/23
     */
    @Override
    public Map<String, Object> getNextNodeInfo(NextNodeREQ req) {
        Map<String, Object> map = new HashMap<>();
        TaskEntity task = (TaskEntity) taskService.createTaskQuery().taskId(req.getTaskId()).singleResult();
        if (task.isSuspended()) {
            throw new ServiceException("????????????????????????");
        }
        ActNodeAssignee nodeAssignee = iActNodeAssigneeService.getInfo(task.getProcessDefinitionId(), task.getTaskDefinitionKey());
        //??????????????????
        List<ActTaskNode> taskNodeList = iActTaskNodeService.getListByInstanceId(task.getProcessInstanceId()).stream().filter(e -> e.getIsBack()).collect(Collectors.toList());
        map.put("backNodeList", taskNodeList);
        //????????????????????????
        ActBusinessStatus actBusinessStatus = iActBusinessStatusService.getInfoByProcessInstId(task.getProcessInstanceId());
        if (ObjectUtil.isEmpty(actBusinessStatus)) {
            throw new ServiceException("??????????????????????????????act_business_status??????");
        } else {
            map.put("businessStatus", actBusinessStatus);
        }
        //????????????
        if (ObjectUtil.isNotEmpty(task.getDelegationState()) && ActConstant.PENDING.equals(task.getDelegationState().name())) {
            ActNodeAssignee actNodeAssignee = new ActNodeAssignee();
            actNodeAssignee.setIsDelegate(false);
            actNodeAssignee.setIsTransmit(false);
            actNodeAssignee.setIsCopy(false);
            actNodeAssignee.setAddMultiInstance(false);
            actNodeAssignee.setDeleteMultiInstance(false);
            map.put("setting", actNodeAssignee);
            map.put("list", new ArrayList<>());
            map.put("isMultiInstance", false);
            return map;
        }
        //??????????????????
        if (ObjectUtil.isNotEmpty(nodeAssignee)) {
            map.put("setting", nodeAssignee);
        } else {
            ActNodeAssignee actNodeAssignee = new ActNodeAssignee();
            actNodeAssignee.setIsDelegate(false);
            actNodeAssignee.setIsTransmit(false);
            actNodeAssignee.setIsCopy(false);
            actNodeAssignee.setAddMultiInstance(false);
            actNodeAssignee.setDeleteMultiInstance(false);
            map.put("setting", actNodeAssignee);
        }

        //???????????????????????????
        MultiVo isMultiInstance = workFlowUtils.isMultiInstance(task.getProcessDefinitionId(), task.getTaskDefinitionKey());
        map.put("isMultiInstance", ObjectUtil.isNotEmpty(isMultiInstance));
        //????????????
        List<Task> taskList = taskService.createTaskQuery().processInstanceId(task.getProcessInstanceId()).list();
        //?????????????????????
        if (ObjectUtil.isNotEmpty(isMultiInstance)) {
            if (isMultiInstance.getType() instanceof ParallelMultiInstanceBehavior) {
                map.put("multiList", multiList(task, taskList, isMultiInstance.getType(), null));
            } else if (isMultiInstance.getType() instanceof SequentialMultiInstanceBehavior) {
                List<Long> assigneeList = (List<Long>) runtimeService.getVariable(task.getExecutionId(), isMultiInstance.getAssigneeList());
                map.put("multiList", multiList(task, taskList, isMultiInstance.getType(), assigneeList));
            }
        } else {
            map.put("multiList", new ArrayList<>());
        }
        //?????????????????????????????????????????????
        if (CollectionUtil.isNotEmpty(taskList) && taskList.size() > 1) {
            //return null;
        }
        taskService.setVariables(task.getId(), req.getVariables());
        //????????????
        String processDefinitionId = task.getProcessDefinitionId();
        //??????bpmn??????
        BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinitionId);
        //??????????????????id??????????????????????????????
        FlowElement flowElement = bpmnModel.getFlowElement(task.getTaskDefinitionKey());
        //????????????
        Collection<FlowElement> flowElements = bpmnModel.getProcesses().get(0).getFlowElements();
        //???????????????????????????????????????
        List<ProcessNode> nextNodeList = new ArrayList<>();
        //??????????????????????????????
        List<ProcessNode> tempNodeList = new ArrayList<>();
        ExecutionEntityImpl executionEntity = (ExecutionEntityImpl) runtimeService.createExecutionQuery()
            .executionId(task.getExecutionId()).singleResult();
        workFlowUtils.getNextNodeList(flowElements, flowElement, executionEntity, nextNodeList, tempNodeList, task.getId(), null);
        if (CollectionUtil.isNotEmpty(nextNodeList) && CollectionUtil.isNotEmpty(nextNodeList.stream().filter(e -> e.getExpression() != null && e.getExpression()).collect(Collectors.toList()))) {
            List<ProcessNode> nodeList = nextNodeList.stream().filter(e -> e.getExpression() != null && e.getExpression()).collect(Collectors.toList());
            List<ProcessNode> processNodeList = getProcessNodeAssigneeList(nodeList, task.getProcessDefinitionId());
            map.put("list", processNodeList);
        } else if (CollectionUtil.isNotEmpty(tempNodeList)) {
            List<ProcessNode> processNodeList = getProcessNodeAssigneeList(tempNodeList, task.getProcessDefinitionId());
            map.put("list", processNodeList);
        } else {
            map.put("list", nextNodeList);
        }
        return map;
    }


    /**
     * @Description: ?????????????????????
     * @param: task  ????????????
     * @param: taskList  ????????????????????????
     * @param: type  ????????????
     * @param: assigneeList ??????????????????
     * @return: java.util.List<com.ruoyi.workflow.domain.vo.TaskVo>
     * @author: gssong
     * @Date: 2022/4/24 11:17
     */
    private List<TaskVo> multiList(TaskEntity task, List<Task> taskList, Object type, List<Long> assigneeList) {
        List<TaskVo> taskListVo = new ArrayList<>();
        if (type instanceof SequentialMultiInstanceBehavior) {
            List<Long> userIds = assigneeList.stream().filter(userId -> !userId.toString().equals(task.getAssignee())).collect(Collectors.toList());
            List<SysUser> sysUsers = null;
            if (CollectionUtil.isNotEmpty(userIds)) {
                sysUsers = iUserService.selectListUserByIds(userIds);
            }
            for (Long userId : userIds) {
                TaskVo taskVo = new TaskVo();
                taskVo.setId("????????????");
                taskVo.setExecutionId("????????????");
                taskVo.setProcessInstanceId(task.getProcessInstanceId());
                taskVo.setName(task.getName());
                taskVo.setAssigneeId(String.valueOf(userId));
                if (CollectionUtil.isNotEmpty(sysUsers) && sysUsers.size() > 0) {
                    SysUser sysUser = sysUsers.stream().filter(u -> u.getUserId().toString().equals(userId.toString())).findFirst().orElse(null);
                    if (ObjectUtil.isNotEmpty(sysUser)) {
                        taskVo.setAssignee(sysUser.getNickName());
                    }
                }
                taskListVo.add(taskVo);
            }
            return taskListVo;
        } else if (type instanceof ParallelMultiInstanceBehavior) {
            List<Task> tasks = taskList.stream().filter(e -> StringUtils.isBlank(e.getParentTaskId()) && !e.getExecutionId().equals(task.getExecutionId())
                && e.getTaskDefinitionKey().equals(task.getTaskDefinitionKey())).collect(Collectors.toList());
            if (CollectionUtil.isNotEmpty(tasks)) {
                List<Long> userIds = tasks.stream().map(e -> Long.valueOf(e.getAssignee())).collect(Collectors.toList());
                List<SysUser> sysUsers = null;
                if (CollectionUtil.isNotEmpty(userIds)) {
                    sysUsers = iUserService.selectListUserByIds(userIds);
                }
                for (Task t : tasks) {
                    TaskVo taskVo = new TaskVo();
                    taskVo.setId(t.getId());
                    taskVo.setExecutionId(t.getExecutionId());
                    taskVo.setProcessInstanceId(t.getProcessInstanceId());
                    taskVo.setName(t.getName());
                    taskVo.setAssigneeId(t.getAssignee());
                    if (CollectionUtil.isNotEmpty(sysUsers)) {
                        SysUser sysUser = sysUsers.stream().filter(u -> u.getUserId().toString().equals(t.getAssignee())).findFirst().orElse(null);
                        if (ObjectUtil.isNotEmpty(sysUser)) {
                            taskVo.setAssignee(sysUser.getNickName());
                        }
                    }
                    taskListVo.add(taskVo);
                }
                return taskListVo;
            }
        }
        return new ArrayList<>();
    }

    /**
     * @Description: ????????????????????????
     * @param: nodeList????????????
     * @param: definitionId ????????????id
     * @return: java.util.List<com.ruoyi.workflow.domain.vo.ProcessNode>
     * @author: gssong
     * @Date: 2021/10/23
     */
    private List<ProcessNode> getProcessNodeAssigneeList(List<ProcessNode> nodeList, String definitionId) {
        List<ActNodeAssignee> actNodeAssignees = iActNodeAssigneeService.getInfoByProcessDefinitionId(definitionId);
        if (CollectionUtil.isNotEmpty(actNodeAssignees)) {
            for (ProcessNode processNode : nodeList) {
                //??????????????????????????????????????????????????????
                if (StringUtils.isBlank(processNode.getAssignee())) {
                    if (CollectionUtil.isEmpty(actNodeAssignees)) {
                        throw new ServiceException("????????????????????????????????????????????????");
                    }
                    ActNodeAssignee nodeAssignee = actNodeAssignees.stream().filter(e -> e.getNodeId().equals(processNode.getNodeId())).findFirst().orElse(null);

                    //????????? ?????? ??????id ???????????????????????????
                    if (ObjectUtil.isNotNull(nodeAssignee) && StringUtils.isNotBlank(nodeAssignee.getAssigneeId())
                        && nodeAssignee.getBusinessRuleId() == null && StringUtils.isNotBlank(nodeAssignee.getAssignee())) {
                        processNode.setChooseWay(nodeAssignee.getChooseWay());
                        processNode.setAssignee(nodeAssignee.getAssignee());
                        processNode.setAssigneeId(nodeAssignee.getAssigneeId());
                        processNode.setIsShow(nodeAssignee.getIsShow());
                        if (nodeAssignee.getMultiple()) {
                            processNode.setNodeId(nodeAssignee.getMultipleColumn());
                        }
                        processNode.setMultiple(nodeAssignee.getMultiple());
                        processNode.setMultipleColumn(nodeAssignee.getMultipleColumn());
                        //??????????????????????????????????????????
                    } else if (ObjectUtil.isNotNull(nodeAssignee) && nodeAssignee.getBusinessRuleId() != null) {
                        ActBusinessRuleVo actBusinessRuleVo = iActBusinessRuleService.queryById(nodeAssignee.getBusinessRuleId());
                        List<String> ruleAssignList = workFlowUtils.ruleAssignList(actBusinessRuleVo, processNode.getTaskId(), processNode.getNodeName());
                        processNode.setChooseWay(nodeAssignee.getChooseWay());
                        processNode.setAssignee("");
                        processNode.setAssigneeId(String.join(",", ruleAssignList));
                        processNode.setIsShow(nodeAssignee.getIsShow());
                        processNode.setBusinessRuleId(nodeAssignee.getBusinessRuleId());
                        if (nodeAssignee.getMultiple()) {
                            processNode.setNodeId(nodeAssignee.getMultipleColumn());
                        }
                        processNode.setMultiple(nodeAssignee.getMultiple());
                        processNode.setMultipleColumn(nodeAssignee.getMultipleColumn());
                    } else {
                        throw new ServiceException(processNode.getNodeName() + "??????????????????????????????????????????");
                    }
                } else {
                    ActNodeAssignee nodeAssignee = actNodeAssignees.stream().filter(e -> e.getNodeId().equals(processNode.getNodeId())).findFirst().orElse(null);
                    if (ObjectUtil.isNotEmpty(nodeAssignee)) {
                        processNode.setChooseWay(nodeAssignee.getChooseWay());
                        processNode.setAssignee(nodeAssignee.getAssignee());
                        processNode.setAssigneeId(nodeAssignee.getAssigneeId());
                        processNode.setIsShow(nodeAssignee.getIsShow());
                        if (nodeAssignee.getMultiple()) {
                            processNode.setNodeId(nodeAssignee.getMultipleColumn());
                        }
                        processNode.setMultiple(nodeAssignee.getMultiple());
                        processNode.setMultipleColumn(nodeAssignee.getMultipleColumn());
                    } else {
                        processNode.setChooseWay(ActConstant.WORKFLOW_ASSIGNEE);
                    }
                }
            }
        }
        if (CollectionUtil.isNotEmpty(nodeList)) {
            // ???????????????????????????????????????  ??????????????? ??????
            // ????????????????????????????????????
            nodeList.removeIf(node -> ActConstant.WORKFLOW_ASSIGNEE.equals(node.getChooseWay()) || !node.getIsShow());
        }
        return nodeList;
    }

    /**
     * @Description: ?????????????????????????????????
     * @param: req
     * @return: com.ruoyi.common.core.page.TableDataInfo<com.ruoyi.workflow.domain.vo.TaskFinishVo>
     * @author: gssong
     * @Date: 2021/10/23
     */
    @Override
    public TableDataInfo<TaskFinishVo> getAllTaskFinishByPage(TaskREQ req) {
        HistoricTaskInstanceQuery query = historyService.createHistoricTaskInstanceQuery()
            .finished().orderByHistoricTaskInstanceStartTime().asc();
        if (StringUtils.isNotBlank(req.getTaskName())) {
            query.taskNameLike(req.getTaskName());
        }
        List<HistoricTaskInstance> list = query.listPage(req.getFirstResult(), req.getPageSize());
        long total = query.count();
        List<TaskFinishVo> taskFinishVoList = new ArrayList<>();
        for (HistoricTaskInstance hti : list) {
            TaskFinishVo taskFinishVo = new TaskFinishVo();
            BeanUtils.copyProperties(hti, taskFinishVo);
            ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(hti.getProcessDefinitionId()).singleResult();
            taskFinishVo.setProcessDefinitionName(processDefinition.getName());
            taskFinishVo.setProcessDefinitionKey(processDefinition.getKey());
            taskFinishVo.setVersion(processDefinition.getVersion());
            taskFinishVo.setAssigneeId(StringUtils.isNotBlank(hti.getAssignee()) ? Long.valueOf(hti.getAssignee()) : null);
            taskFinishVoList.add(taskFinishVo);
        }
        if (CollectionUtil.isNotEmpty(list)) {
            //???????????????
            List<Long> assigneeList = taskFinishVoList.stream().map(TaskFinishVo::getAssigneeId).collect(Collectors.toList());
            if (CollectionUtil.isNotEmpty(assigneeList)) {
                List<SysUser> userList = iUserService.selectListUserByIds(assigneeList);
                if (CollectionUtil.isNotEmpty(userList)) {
                    taskFinishVoList.forEach(e -> {
                        SysUser sysUser = userList.stream().filter(t -> t.getUserId().compareTo(e.getAssigneeId()) == 0).findFirst().orElse(null);
                        if (ObjectUtil.isNotEmpty(sysUser)) {
                            e.setAssignee(sysUser.getNickName());
                            e.setAssigneeId(sysUser.getUserId());
                        }
                    });
                }
            }
        }
        return new TableDataInfo<>(taskFinishVoList, total);
    }

    /**
     * @Description: ?????????????????????????????????
     * @param: req
     * @return: com.ruoyi.common.core.page.TableDataInfo<com.ruoyi.workflow.domain.vo.TaskWaitingVo>
     * @author: gssong
     * @Date: 2021/10/17
     */
    @Override
    public TableDataInfo<TaskWaitingVo> getAllTaskWaitByPage(TaskREQ req) {
        TaskQuery query = taskService.createTaskQuery()
            .orderByTaskCreateTime().asc();
        if (StringUtils.isNotEmpty(req.getTaskName())) {
            query.taskNameLikeIgnoreCase("%" + req.getTaskName() + "%");
        }
        List<Task> taskList = query.listPage(req.getFirstResult(), req.getPageSize());
        long total = query.count();
        List<TaskWaitingVo> list = new ArrayList<>();
        for (Task task : taskList) {
            TaskWaitingVo taskWaitingVo = new TaskWaitingVo();
            BeanUtils.copyProperties(task, taskWaitingVo);
            taskWaitingVo.setAssigneeId(StringUtils.isNotBlank(task.getAssignee()) ? Long.valueOf(task.getAssignee()) : null);
            taskWaitingVo.setProcessStatus(!task.isSuspended() ? "??????" : "??????");
            // ??????????????????
            ProcessInstance pi = runtimeService.createProcessInstanceQuery()
                .processInstanceId(task.getProcessInstanceId()).singleResult();
            //???????????????
            String startUserId = pi.getStartUserId();
            if (StringUtils.isNotBlank(startUserId)) {
                SysUser sysUser = iUserService.selectUserById(Long.valueOf(startUserId));
                if (ObjectUtil.isNotNull(sysUser)) {
                    taskWaitingVo.setStartUserNickName(sysUser.getNickName());
                }
            }
            taskWaitingVo.setProcessDefinitionVersion(pi.getProcessDefinitionVersion());
            taskWaitingVo.setProcessDefinitionName(pi.getProcessDefinitionName());
            taskWaitingVo.setBusinessKey(pi.getBusinessKey());
            //????????????
            MultiVo multiInstance = workFlowUtils.isMultiInstance(task.getProcessDefinitionId(), task.getTaskDefinitionKey());
            taskWaitingVo.setMultiInstance(ObjectUtil.isNotEmpty(multiInstance));
            //????????????
            List<Task> tasks = taskService.createTaskQuery().processInstanceId(task.getProcessInstanceId()).list();
            //?????????????????????
            if (ObjectUtil.isNotEmpty(multiInstance)) {
                if (multiInstance.getType() instanceof ParallelMultiInstanceBehavior) {
                    taskWaitingVo.setTaskVoList(multiList((TaskEntity) task, tasks, multiInstance.getType(), null));
                } else if (multiInstance.getType() instanceof SequentialMultiInstanceBehavior && StringUtils.isNotBlank(task.getExecutionId())) {
                    List<Long> assigneeList = (List<Long>) runtimeService.getVariable(task.getExecutionId(), multiInstance.getAssigneeList());
                    taskWaitingVo.setTaskVoList(multiList((TaskEntity) task, tasks, multiInstance.getType(), assigneeList));
                }
            }
            list.add(taskWaitingVo);
        }
        if (CollectionUtil.isNotEmpty(list)) {
            List<String> businessKeyList = list.stream().map(TaskWaitingVo::getBusinessKey).collect(Collectors.toList());
            List<ActBusinessStatus> infoList = iActBusinessStatusService.getListInfoByBusinessKey(businessKeyList);
            for (TaskWaitingVo e : list) {
                //?????????????????????
                List<IdentityLink> identityLinkList = workFlowUtils.getCandidateUser(e.getId());
                if (CollectionUtil.isNotEmpty(identityLinkList)) {
                    List<String> collectType = identityLinkList.stream().map(IdentityLink::getType).collect(Collectors.toList());
                    if (StringUtils.isBlank(e.getAssignee()) && collectType.size() > 1 && collectType.contains(ActConstant.CANDIDATE)) {
                        e.setIsClaim(false);
                    } else if (StringUtils.isNotBlank(e.getAssignee()) && collectType.size() > 1 && collectType.contains(ActConstant.CANDIDATE)) {
                        e.setIsClaim(true);
                    }
                }
                //???????????????
                List<Long> assigneeList = list.stream().map(TaskWaitingVo::getAssigneeId).filter(Objects::nonNull).collect(Collectors.toList());
                if (CollectionUtil.isNotEmpty(assigneeList)) {
                    List<SysUser> userList = iUserService.selectListUserByIds(assigneeList);
                    if (CollectionUtil.isNotEmpty(userList)) {
                        SysUser sysUser = userList.stream().filter(t -> StringUtils.isNotBlank(e.getAssignee()) && t.getUserId().compareTo(e.getAssigneeId()) == 0).findFirst().orElse(null);
                        if (ObjectUtil.isNotEmpty(sysUser)) {
                            e.setAssignee(sysUser.getNickName());
                            e.setAssigneeId(sysUser.getUserId());
                        }

                    }
                }
                //????????????
                if (CollectionUtil.isNotEmpty(infoList)) {
                    ActBusinessStatus businessStatus = infoList.stream().filter(t -> t.getBusinessKey().equals(e.getBusinessKey())).findFirst().orElse(null);
                    if (ObjectUtil.isNotEmpty(businessStatus)) {
                        e.setActBusinessStatus(businessStatus);
                    }
                }
            }
        }
        return new TableDataInfo<>(list, total);
    }

    /**
     * @Description: ????????????
     * @param: backProcessBo
     * @return: java.lang.String
     * @author: gssong
     * @Date: 2021/11/6
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String backProcess(BackProcessBo backProcessBo) {

        Task task = taskService.createTaskQuery().taskId(backProcessBo.getTaskId()).taskAssignee(getUserId().toString()).singleResult();
        if (task.isSuspended()) {
            throw new ServiceException("????????????????????????");
        }
        if (ObjectUtil.isNull(task)) {
            throw new ServiceException("????????????????????????????????????????????????");
        }
        try {
            //????????????id
            String processInstanceId = task.getProcessInstanceId();
            // 1. ???????????????????????? BpmnModel
            BpmnModel bpmnModel = repositoryService.getBpmnModel(task.getProcessDefinitionId());
            // 2.??????????????????
            FlowNode curFlowNode = (FlowNode) bpmnModel.getFlowElement(task.getTaskDefinitionKey());
            // 3.?????????????????????????????????
            List<SequenceFlow> sequenceFlowList = curFlowNode.getOutgoingFlows();
            // 4. ??????????????????????????????????????????
            List<SequenceFlow> oriSequenceFlows = new ArrayList<>();
            oriSequenceFlows.addAll(sequenceFlowList);
            // 5. ?????????????????????????????????
            sequenceFlowList.clear();
            // 6. ????????????????????????
            FlowNode targetFlowNode = (FlowNode) bpmnModel.getFlowElement(backProcessBo.getTargetActivityId());
            // 7. ?????????????????????????????????
            List<SequenceFlow> incomingFlows = targetFlowNode.getIncomingFlows();
            // 8. ????????????????????????
            List<SequenceFlow> targetSequenceFlow = new ArrayList<>();
            for (SequenceFlow incomingFlow : incomingFlows) {
                // ???????????????????????????????????????????????????????????????
                FlowNode source = (FlowNode) incomingFlow.getSourceFlowElement();
                List<SequenceFlow> sequenceFlows;
                if (source instanceof ParallelGateway) {
                    // ????????????: ??????????????????????????????????????????????????????????????????
                    sequenceFlows = source.getOutgoingFlows();
                } else {
                    // ?????????????????????, ????????????????????????????????????
                    sequenceFlows = targetFlowNode.getIncomingFlows();
                }
                targetSequenceFlow.addAll(sequenceFlows);
            }
            // 9. ??????????????????????????????????????????

            List<SequenceFlow> targetSequenceList = targetSequenceFlow.stream().collect(Collectors
                .collectingAndThen(
                    Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(e -> e.getTargetFlowElement().getId()))),
                    ArrayList::new));

            curFlowNode.setOutgoingFlows(targetSequenceList);

            List<Task> list = taskService.createTaskQuery().processInstanceId(processInstanceId).list();
            for (Task t : list) {
                if (backProcessBo.getTaskId().equals(t.getId())) {
                    taskService.addComment(t.getId(), processInstanceId, StringUtils.isNotBlank(backProcessBo.getComment()) ? backProcessBo.getComment() : "??????");
                    taskService.complete(backProcessBo.getTaskId());
                } else {
                    if(StringUtils.isBlank(t.getParentTaskId())){
                        taskService.complete(t.getId());
                        historyService.deleteHistoricTaskInstance(t.getId());
                        taskMapper.deleteActHiActInstByTaskId(t.getId());
                    }
                }
            }

            // 11. ?????????????????????????????????????????????????????????????????????
            curFlowNode.setOutgoingFlows(oriSequenceFlows);
            // ??????????????????
            LambdaQueryWrapper<ActNodeAssignee> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ActNodeAssignee::getNodeId,backProcessBo.getTargetActivityId());
            wrapper.eq(ActNodeAssignee::getProcessDefinitionId,task.getProcessDefinitionId());
            ActNodeAssignee actNodeAssignee = iActNodeAssigneeService.getOne(wrapper);
            List<Task> newTaskList = taskService.createTaskQuery().processInstanceId(processInstanceId).list();
            if(ObjectUtil.isNotEmpty(actNodeAssignee)&&!actNodeAssignee.getMultiple()){
                for (Task newTask : newTaskList) {

                    DeleteExecutionCmd executionCmd = new DeleteExecutionCmd(newTask.getExecutionId());
                    managementService.executeCommand(executionCmd);
                    // ???????????????????????????
                    List<HistoricTaskInstance> oldTargerTaskList = historyService.createHistoricTaskInstanceQuery()
                        .taskDefinitionKey(newTask.getTaskDefinitionKey()) // ??????id
                        .processInstanceId(processInstanceId)
                        .finished() // ????????????????????????
                        .orderByTaskCreateTime().desc() // ???????????????????????????
                        .list();
                    if(CollectionUtil.isNotEmpty(oldTargerTaskList)){
                        HistoricTaskInstance oldTargerTask = oldTargerTaskList.get(0);
                        taskService.setAssignee(newTask.getId(), oldTargerTask.getAssignee());
                    }
                }
            }

            // 13. ??????????????????????????????
            ActTaskNode actTaskNode = iActTaskNodeService.getListByInstanceIdAndNodeId(task.getProcessInstanceId(), backProcessBo.getTargetActivityId());
            if (ObjectUtil.isNotNull(actTaskNode) && actTaskNode.getOrderNo() == 0) {
                ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
                List<Task> newList = taskService.createTaskQuery().processInstanceId(processInstanceId).list();
                for (Task ta : newList) {
                    Map<String, Object> variables = new HashMap<>();
                    taskService.setVariables(ta.getId(), variables);
                }
                iActBusinessStatusService.updateState(processInstance.getBusinessKey(), BusinessStatusEnum.BACK);
            }
            iActTaskNodeService.deleteBackTaskNode(processInstanceId, backProcessBo.getTargetActivityId());
            return processInstanceId;
        }catch (Exception e){
            throw new ServiceException("????????????:"+e.getMessage());
        }
    }

    /**
     * @Description: ?????????????????????????????????????????????
     * @param: processDefinitionId ????????????id
     * @param: targetActivityId ???????????????id
     * @param: task ??????
     * @return: java.util.List<java.lang.String>
     * @author: gssong
     * @Date: 2022/4/10
     */
    public List<String> getPrevUserNodeList(String processDefinitionId, String targetActivityId, Task task) {
        List<String> nodeListId = new ArrayList<>();

        BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinitionId);
        FlowElement flowElement = bpmnModel.getFlowElement(targetActivityId);
        List<SequenceFlow> outgoingFlows = ((FlowNode) flowElement).getIncomingFlows();
        for (SequenceFlow outgoingFlow : outgoingFlows) {
            FlowElement sourceFlowElement = outgoingFlow.getSourceFlowElement();
            //????????????
            if (sourceFlowElement instanceof ParallelGateway) {
                List<SequenceFlow> parallelGatewayOutgoingFlow = ((ParallelGateway) sourceFlowElement).getOutgoingFlows();
                for (SequenceFlow sequenceFlow : parallelGatewayOutgoingFlow) {
                    FlowElement element = sequenceFlow.getTargetFlowElement();
                    if (element instanceof UserTask) {
                        nodeListId.add(element.getId());
                    }
                }
                //????????????
            }/*else if(sourceFlowElement instanceof InclusiveGateway){
                List<SequenceFlow> inclusiveGatewayOutgoingFlow = ((InclusiveGateway) sourceFlowElement).getOutgoingFlows();
                for (SequenceFlow sequenceFlow : inclusiveGatewayOutgoingFlow) {
                    String conditionExpression = outgoingFlow.getConditionExpression();
                    FlowElement element = sequenceFlow.getTargetFlowElement();
                    if (element instanceof UserTask) {
                        if(StringUtils.isBlank(conditionExpression)){
                            nodeListId.add(element.getId());
                        }else{
                            ExecutionEntityImpl executionEntity = (ExecutionEntityImpl) runtimeService.createExecutionQuery()
                                .executionId(task.getExecutionId()).singleResult();
                            ExpressCmd expressCmd = new ExpressCmd(outgoingFlow,executionEntity);
                            Boolean condition = managementService.executeCommand(expressCmd);
                            if(condition){
                                nodeListId.add(element.getId());
                            }
                        }
                    }
                }
            }*/
        }
        return nodeListId;
    }

    /**
     * @Description: ?????????????????????????????????????????????
     * @param: processInstId
     * @return: java.util.List<com.ruoyi.workflow.domain.ActTaskNode>
     * @author: gssong
     * @Date: 2021/11/6
     */
    @Override
    public List<ActTaskNode> getBackNodes(String processInstId) {
        return iActTaskNodeService.getListByInstanceId(processInstId).stream().filter(ActTaskNode::getIsBack).collect(Collectors.toList());
    }

    /**
     * @Description: ????????????
     * @param: taskREQ
     * @return: java.lang.Boolean
     * @author: gssong
     * @Date: 2022/3/4
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean delegateTask(DelegateREQ delegateREQ) {
        if (StringUtils.isBlank(delegateREQ.getDelegateUserId())) {
            throw new ServiceException("??????????????????");
        }
        TaskEntity task = (TaskEntity) taskService.createTaskQuery().taskId(delegateREQ.getTaskId())
            .taskCandidateOrAssigned(LoginHelper.getUserId().toString()).singleResult();
        if (ObjectUtil.isEmpty(task)) {
            throw new ServiceException("????????????????????????????????????????????????");
        }
        try {
            TaskEntity newTask = workFlowUtils.createNewTask(task, new Date());
            taskService.addComment(newTask.getId(), task.getProcessInstanceId(), "???" + LoginHelper.getUsername() + "???????????????" + delegateREQ.getDelegateUserName() + "???");
            //????????????
            taskService.delegateTask(delegateREQ.getTaskId(), delegateREQ.getDelegateUserId());
            //???????????????????????????
            taskService.complete(newTask.getId());
            ActHiTaskInst actHiTaskInst = new ActHiTaskInst();
            actHiTaskInst.setId(task.getId());
            actHiTaskInst.setStartTime(new Date());
            iActHiTaskInstService.updateById(actHiTaskInst);
            //???????????????
            workFlowUtils.sendMessage(delegateREQ.getSendMessage(), task.getProcessInstanceId());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceException(e.getMessage());
        }
    }

    /**
     * @Description: ????????????
     * @param: transmitREQ
     * @return: com.ruoyi.common.core.domain.R<java.lang.Boolean>
     * @author: gssong
     * @Date: 2022/3/13 13:18
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<Boolean> transmitTask(TransmitREQ transmitREQ) {
        Task task = taskService.createTaskQuery().taskId(transmitREQ.getTaskId())
            .taskCandidateOrAssigned(LoginHelper.getUserId().toString()).singleResult();
        if (ObjectUtil.isEmpty(task)) {
            return R.fail("????????????????????????????????????????????????");
        }
        try {
            TaskEntity newTask = workFlowUtils.createNewTask(task, new Date());
            taskService.addComment(newTask.getId(), task.getProcessInstanceId(),
                StringUtils.isNotBlank(transmitREQ.getComment()) ? transmitREQ.getComment() : LoginHelper.getUsername() + "???????????????");
            taskService.complete(newTask.getId());
            taskService.setAssignee(task.getId(), transmitREQ.getTransmitUserId());
            //???????????????
            workFlowUtils.sendMessage(transmitREQ.getSendMessage(), task.getProcessInstanceId());
            return R.ok();
        } catch (Exception e) {
            e.printStackTrace();
            return R.fail(e.getMessage());
        }
    }

    /**
     * @Description: ??????????????????
     * @param: addMultiREQ
     * @return: com.ruoyi.common.core.domain.R<java.lang.Boolean>
     * @author: gssong
     * @Date: 2022/4/15 13:06
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<Boolean> addMultiInstanceExecution(AddMultiREQ addMultiREQ) {
        Task task;
        if (LoginHelper.isAdmin()) {
            task = taskService.createTaskQuery().taskId(addMultiREQ.getTaskId()).singleResult();
        } else {
            task = taskService.createTaskQuery().taskId(addMultiREQ.getTaskId())
                .taskCandidateOrAssigned(LoginHelper.getUserId().toString()).singleResult();
        }
        if (ObjectUtil.isEmpty(task) && !LoginHelper.isAdmin()) {
            throw new ServiceException("????????????????????????????????????????????????");
        }
        if (task.isSuspended()) {
            throw new ServiceException("????????????????????????");
        }
        String taskDefinitionKey = task.getTaskDefinitionKey();
        String processInstanceId = task.getProcessInstanceId();
        String processDefinitionId = task.getProcessDefinitionId();
        MultiVo multiVo = workFlowUtils.isMultiInstance(processDefinitionId, taskDefinitionKey);
        if (ObjectUtil.isEmpty(multiVo)) {
            throw new ServiceException("??????????????????????????????");
        }
        try {
            if (multiVo.getType() instanceof ParallelMultiInstanceBehavior) {
                for (Long assignee : addMultiREQ.getAssignees()) {
                    AddMultiInstanceExecutionCmd addMultiInstanceExecutionCmd = new AddMultiInstanceExecutionCmd(taskDefinitionKey, processInstanceId, Collections.singletonMap(multiVo.getAssignee(), assignee));
                    managementService.executeCommand(addMultiInstanceExecutionCmd);
                }
            } else if (multiVo.getType() instanceof SequentialMultiInstanceBehavior) {
                AddSequenceMultiInstanceCmd addSequenceMultiInstanceCmd = new AddSequenceMultiInstanceCmd(task.getExecutionId(), multiVo.getAssigneeList(), addMultiREQ.getAssignees());
                managementService.executeCommand(addSequenceMultiInstanceCmd);
            }
            List<String> assigneeNames = addMultiREQ.getAssigneeNames();
            String username = LoginHelper.getUsername();
            TaskEntity newTask = workFlowUtils.createNewTask(task, new Date());
            taskService.addComment(newTask.getId(), processInstanceId, username + "?????????" + String.join(",", assigneeNames) + "???");
            taskService.complete(newTask.getId());
            return R.ok();
        } catch (Exception e) {
            e.printStackTrace();
            return R.fail(e.getMessage());
        }
    }

    /**
     * @Description: ??????????????????
     * @param: deleteMultiREQ
     * @return: com.ruoyi.common.core.domain.R<java.lang.Boolean>
     * @author: gssong
     * @Date: 2022/4/16 10:59
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<Boolean> deleteMultiInstanceExecution(DeleteMultiREQ deleteMultiREQ) {
        Task task;
        if (LoginHelper.isAdmin()) {
            task = taskService.createTaskQuery().taskId(deleteMultiREQ.getTaskId()).singleResult();
        } else {
            task = taskService.createTaskQuery().taskId(deleteMultiREQ.getTaskId())
                .taskCandidateOrAssigned(LoginHelper.getUserId().toString()).singleResult();
        }
        if (ObjectUtil.isEmpty(task) && !LoginHelper.isAdmin()) {
            return R.fail("????????????????????????????????????????????????");
        }
        if (task.isSuspended()) {
            return R.fail("????????????????????????");
        }
        String taskDefinitionKey = task.getTaskDefinitionKey();
        String processInstanceId = task.getProcessInstanceId();
        String processDefinitionId = task.getProcessDefinitionId();
        MultiVo multiVo = workFlowUtils.isMultiInstance(processDefinitionId, taskDefinitionKey);
        if (ObjectUtil.isEmpty(multiVo)) {
            return R.fail("??????????????????????????????");
        }
        try {
            if (multiVo.getType() instanceof ParallelMultiInstanceBehavior) {
                for (String executionId : deleteMultiREQ.getExecutionIds()) {
                    DeleteMultiInstanceExecutionCmd deleteMultiInstanceExecutionCmd = new DeleteMultiInstanceExecutionCmd(executionId,false);
                    managementService.executeCommand(deleteMultiInstanceExecutionCmd);
                }
                for (String taskId : deleteMultiREQ.getTaskIds()) {
                    historyService.deleteHistoricTaskInstance(taskId);
                }
            } else if (multiVo.getType() instanceof SequentialMultiInstanceBehavior) {
                DeleteSequenceMultiInstanceCmd deleteSequenceMultiInstanceCmd = new DeleteSequenceMultiInstanceCmd(task.getAssignee(), task.getExecutionId(), multiVo.getAssigneeList(), deleteMultiREQ.getAssigneeIds());
                managementService.executeCommand(deleteSequenceMultiInstanceCmd);
            }
            List<String> assigneeNames = deleteMultiREQ.getAssigneeNames();
            String username = LoginHelper.getUsername();
            TaskEntity newTask = workFlowUtils.createNewTask(task, new Date());
            taskService.addComment(newTask.getId(), processInstanceId, username + "?????????" + String.join(",", assigneeNames) + "???");
            taskService.complete(newTask.getId());
            return R.ok();
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceException(e.getMessage());
        }
    }

    /**
     * @Description: ???????????????
     * @param: updateAssigneeBo
     * @return: com.ruoyi.common.core.domain.R<java.lang.Void>
     * @author: gssong
     * @Date: 2022/7/17 13:35
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<Void> updateAssignee(UpdateAssigneeBo updateAssigneeBo) {
        List<Task> list = taskService.createNativeTaskQuery().sql("select * from act_ru_task where id_ in " + getInParam(updateAssigneeBo.getTaskIdList())).list();
        if (CollectionUtil.isEmpty(list)) {
            return R.fail("??????????????????????????????");
        }
        try {
            for (Task task : list) taskService.setAssignee(task.getId(), updateAssigneeBo.getUserId());
            return R.ok();
        } catch (Exception e) {
            throw new ServiceException(e.getMessage());
        }
    }

    /**
     * @Description: ???????????????, ?????????????????????in??????.
     * @param: param
     * @return: java.lang.String
     * @author: gssong
     * @Date: 2022/7/22 12:17
     */
    private String getInParam(List<String> param) {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        for (int i = 0; i < param.size(); i++) {
            sb.append("'").append(param.get(i)).append("'");
            if (i != param.size() - 1) {
                sb.append(",");
            }
        }
        sb.append(")");
        return sb.toString();
    }

    /**
     * @Description: ??????????????????
     * @param: taskId
     * @return: com.ruoyi.common.core.domain.R<java.util.List < com.ruoyi.workflow.domain.vo.VariableVo>>
     * @author: gssong
     * @Date: 2022/7/23 14:33
     */
    @Override
    public R<List<VariableVo>> getProcessInstVariable(String taskId) {
        List<VariableVo> variableVoList = new ArrayList<>();
        Map<String, VariableInstance> variableInstances = taskService.getVariableInstances(taskId);
        if (CollectionUtil.isNotEmpty(variableInstances)) {
            for (Map.Entry<String, VariableInstance> entry : variableInstances.entrySet()) {
                VariableVo variableVo = new VariableVo();
                variableVo.setKey(entry.getKey());
                variableVo.setValue(ObjectUtil.isNotEmpty(entry.getValue()) && ObjectUtil.isNotEmpty(entry.getValue().getTextValue())?entry.getValue().getTextValue():"");
                variableVoList.add(variableVo);
            }
        }
        return R.ok(variableVoList);
    }

    /**
     * @Description: ??????????????????
     * @param: commentId
     * @param: comment
     * @return: com.ruoyi.common.core.domain.R<java.lang.Void>
     * @author: gssong
     * @Date: 2022/7/24 13:28
     */
    @Override
    public R<Void> editComment(String commentId, String comment) {
        try {
            taskMapper.editComment(commentId, comment);
            return R.ok();
        } catch (Exception e) {
            e.printStackTrace();
            return R.fail();
        }
    }
}
