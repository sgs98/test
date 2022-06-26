package com.ruoyi.workflow.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.helper.LoginHelper;
import com.ruoyi.common.utils.spring.SpringUtils;
import com.ruoyi.workflow.common.constant.ActConstant;
import com.ruoyi.workflow.common.enums.BusinessStatusEnum;
import com.ruoyi.workflow.domain.*;
import com.ruoyi.workflow.domain.bo.*;
import com.ruoyi.workflow.domain.vo.*;
import com.ruoyi.workflow.flowable.cmd.AddSequenceMultiInstanceCmd;
import com.ruoyi.workflow.flowable.cmd.DeleteSequenceMultiInstanceCmd;
import com.ruoyi.workflow.flowable.cmd.IdentityLinkListCmd;
import com.ruoyi.workflow.flowable.factory.WorkflowService;
import com.ruoyi.workflow.service.*;
import com.ruoyi.workflow.utils.WorkFlowUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.bpmn.model.*;
import org.apache.commons.lang3.StringUtils;
import org.flowable.engine.ManagementService;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.impl.bpmn.behavior.ParallelMultiInstanceBehavior;
import org.flowable.engine.impl.bpmn.behavior.SequentialMultiInstanceBehavior;
import org.flowable.engine.impl.persistence.entity.ExecutionEntityImpl;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.identitylink.service.impl.persistence.entity.IdentityLinkEntity;
import org.flowable.task.api.TaskQuery;
import org.flowable.task.api.Task;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.flowable.task.api.history.HistoricTaskInstanceQuery;
import org.flowable.task.service.impl.persistence.entity.TaskEntity;
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
 * @description: 任务业务层
 * @author: gssong
 * @created: 2021/10/17 14:57
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TaskServiceImpl extends WorkflowService implements ITaskService {

    protected static Logger logger = LoggerFactory.getLogger(TaskServiceImpl.class);

    private final IUserService iUserService;

    private final IActBusinessStatusService iActBusinessStatusService;

    private final WorkFlowUtils workFlowUtils;

    private final IActTaskNodeService iActTaskNodeService;

    private final IActNodeAssigneeService iActNodeAssigneeService;

    private final IActFullClassService iActFullClassService;

    private final IActHiTaskInstService iActHiTaskInstService;

    private final ManagementService managementService;




    /**
     * @Description: 查询当前用户的待办任务
     * @param: req
     * @return: com.ruoyi.common.core.page.TableDataInfo<com.ruoyi.workflow.domain.vo.TaskWaitingVo>
     * @author: gssong
     * @Date: 2021/10/17
     */
    @Override
    public TableDataInfo<TaskWaitingVo> getTaskWaitByPage(TaskREQ req) {
        //当前登录人
        String currentUserId = LoginHelper.getLoginUser().getUserId().toString();
        TaskQuery query = taskService.createTaskQuery()
            .taskCandidateOrAssigned(currentUserId) // 候选人或者办理人
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
            taskWaitingVo.setAssigneeId(StringUtils.isNotBlank(task.getAssignee())?Long.valueOf(task.getAssignee()):null);
            taskWaitingVo.setProcessStatus(task.isSuspended() == true ? "挂起" : "激活");
            // 查询流程实例
            ProcessInstance pi = runtimeService.createProcessInstanceQuery()
                .processInstanceId(task.getProcessInstanceId()).singleResult();
            //流程发起人
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
        if(CollectionUtil.isNotEmpty(list)){
            //认领与归还标识
            list.forEach(e->{
                IdentityLinkListCmd identityLinkListCmd = new IdentityLinkListCmd(e.getId());
                List<IdentityLinkEntity> identityLinkEntities = managementService.executeCommand(identityLinkListCmd);
                if(CollectionUtil.isNotEmpty(identityLinkEntities)){
                    List<String> collectType = identityLinkEntities.stream().map(IdentityLinkEntity::getType).collect(Collectors.toList());
                    if(StringUtils.isBlank(e.getAssignee())&&collectType.size()>1&&collectType.contains(ActConstant.CANDIDATE)) {
                        e.setIsClaim(false);
                    }else if(StringUtils.isNotBlank(e.getAssignee())&&collectType.size()>1&&collectType.contains(ActConstant.CANDIDATE)){
                        e.setIsClaim(true);
                    }
                }
            });
            //办理人集合
            List<Long> assigneeList = list.stream().map(TaskWaitingVo::getAssigneeId).collect(Collectors.toList());
            if(CollectionUtil.isNotEmpty(assigneeList)){
                List<SysUser> userList = iUserService.selectListUserByIds(assigneeList);
                if(CollectionUtil.isNotEmpty(userList)){
                    list.forEach(e->{
                        SysUser sysUser = userList.stream().filter(t -> StringUtils.isNotBlank(e.getAssignee())&&t.getUserId().compareTo(e.getAssigneeId())==0).findFirst().orElse(null);
                        if(ObjectUtil.isNotEmpty(sysUser)){
                            e.setAssignee(sysUser.getNickName());
                            e.setAssigneeId(sysUser.getUserId());
                        }
                    });
                }
            }
            //业务id集合
            List<String> businessKeyList = list.stream().map(TaskWaitingVo::getBusinessKey).collect(Collectors.toList());
            List<ActBusinessStatus> infoList = iActBusinessStatusService.getListInfoByBusinessKey(businessKeyList);
            if(CollectionUtil.isNotEmpty(infoList)){
                list.forEach(e->{
                    ActBusinessStatus businessStatus = infoList.stream().filter(t -> t.getBusinessKey().equals(e.getBusinessKey())).findFirst().orElse(null);
                    if(ObjectUtil.isNotEmpty(businessStatus)){
                        e.setActBusinessStatus(businessStatus);
                    }
                });
            }
        }
        return new TableDataInfo(list, total);
    }


    /**
     * @Description: 办理任务
     * @param: req
     * @return: java.lang.Boolean
     * @author: gssong
     * @Date: 2021/10/21
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean completeTask(TaskCompleteREQ req) {
        try {
            // 1.查询任务
            Task task = taskService.createTaskQuery().taskId(req.getTaskId()).taskAssignee(getUserId().toString()).singleResult();

            if (ObjectUtil.isNull(task)) {
                throw new ServiceException("任务不存在或您不是当前审批人");
            }

            if (task.isSuspended()) {
                throw new ServiceException("当前任务已被挂起");
            }
            /*workFlowUtils.springInvokeMethod("MultiInstanceExecutionListener","handleProcess"
                ,task.getProcessInstanceId(),task.getId());*/
            //办理委托任务
            if(ObjectUtil.isNotEmpty(task.getDelegationState())&&ActConstant.PENDING.equals(task.getDelegationState().name())){
                taskService.resolveTask(req.getTaskId());
                ActHiTaskInst hiTaskInst = iActHiTaskInstService.getById(task.getId());
                TaskEntity subTask = createNewTask(task, hiTaskInst.getStartTime());
                taskService.addComment(subTask.getId(), task.getProcessInstanceId(), req.getMessage());
                taskService.complete(subTask.getId());
                ActHiTaskInst actHiTaskInst = new ActHiTaskInst();
                actHiTaskInst.setId(task.getId());
                actHiTaskInst.setStartTime(new Date());
                iActHiTaskInstService.updateById(actHiTaskInst);
                return true;
            }

            ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(task.getProcessInstanceId()).singleResult();
            //判断下一节点是否是会签 如果是会签 将选择的人员放到会签变量
            List<ActNodeAssignee> actNodeAssignees = iActNodeAssigneeService.getInfoByProcessDefinitionId(task.getProcessDefinitionId());
            for (ActNodeAssignee actNodeAssignee : actNodeAssignees) {
                String column = actNodeAssignee.getMultipleColumn();
                String assigneeId = actNodeAssignee.getAssigneeId();
                if(actNodeAssignee.getMultiple()&&actNodeAssignee.getIsShow()){
                    List<Long> userIdList = req.getAssignees(actNodeAssignee.getMultipleColumn());
                    if(CollectionUtil.isNotEmpty(userIdList)){
                        taskService.setVariable(task.getId(),column,userIdList);
                    }
                }
                //判断是否有会签并且不需要弹窗选人的节点
                if(actNodeAssignee.getMultiple()&&!actNodeAssignee.getIsShow()&&(StringUtils.isBlank(column) || StringUtils.isBlank(assigneeId))){
                    throw new ServiceException("请检查【"+processInstance.getProcessDefinitionKey()+"】配置 ");
                }
                if(actNodeAssignee.getMultiple()&&!actNodeAssignee.getIsShow()){
                    List<Long> userIds = new ArrayList<>();
                    String[] split = assigneeId.split(",");
                    for (String userId : split) {
                        userIds.add(Long.valueOf(userId));
                    }
                    taskService.setVariable(task.getId(),actNodeAssignee.getMultipleColumn(),userIds);
                }
            }
            // 3. 指定任务审批意见
            taskService.addComment(req.getTaskId(), task.getProcessInstanceId(), req.getMessage());
            // 4. 完成任务
            taskService.setVariables(req.getTaskId(), req.getVariables());
            taskService.complete(req.getTaskId());
            // 5. 记录执行过的流程任务
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
                //如果为设置流程定义配置默认 当前环节可以回退
                if(ObjectUtil.isEmpty(actNodeAssignee)){
                    actTaskNode.setIsBack(true);
                }else{
                    actTaskNode.setIsBack(actNodeAssignee.getIsBack());
                }
                iActTaskNodeService.saveTaskNode(actTaskNode);
            }
            // 更新业务状态为：办理中, 和流程实例id
            iActBusinessStatusService.updateState(processInstance.getBusinessKey(), BusinessStatusEnum.WAITING, task.getProcessInstanceId());
            // 6. 查询下一个任务
            List<Task> taskList = taskService.createTaskQuery().processInstanceId(task.getProcessInstanceId()).list();
            // 7. 如果为空 办结任务
            if (CollectionUtil.isEmpty(taskList)) {
                HistoricProcessInstance hpi = historyService.createHistoricProcessInstanceQuery()
                    .processInstanceId(task.getProcessInstanceId()).singleResult();
                // 更新业务状态已完成 办结流程
                boolean b = iActBusinessStatusService.updateState(hpi.getBusinessKey(), BusinessStatusEnum.FINISH);
                return b;
            }

            //抄送
            if(req.getIsCopy()){
                if(StringUtils.isBlank(req.getAssigneeIds())){
                    throw new ServiceException("抄送人不能为空 ");
                }
                TaskEntity newTask = createNewTask(task, new Date());
                taskService.addComment(newTask.getId(),task.getProcessInstanceId(),
                    LoginHelper.getUsername()+"【抄送】给"+req.getAssigneeNames());
                taskService.complete(newTask.getId());
                workFlowUtils.createSubTask(taskList,req.getAssigneeIds());
            }
            // 8. 如果不为空 指定办理人
            for (Task t : taskList) {
                ActNodeAssignee nodeAssignee = actNodeAssignees.stream().filter(e -> t.getTaskDefinitionKey().equals(e.getNodeId())).findFirst().orElse(null);
                if(ObjectUtil.isNotNull(nodeAssignee)){
                    // 不需要弹窗选人
                    if(!nodeAssignee.getIsShow()){
                        //业务规则选人
                        if(ActConstant.WORKFLOW_RULE.equals(nodeAssignee.getChooseWay())){
                            ActFullClassVo actFullClassVo = iActFullClassService.queryById(nodeAssignee.getFullClassId());
                            //按业务规则选人
                            Object assignee = workFlowUtils.assignList(actFullClassVo, t.getId());
                            List<Long> userIds = new ArrayList<>();
                            String[] splitUserIds = assignee.toString().split(",");
                            for (String userId : splitUserIds) {
                                userIds.add(Long.valueOf(userId));
                            }
                            List<SysUser> userList = iUserService.selectListUserByIds(userIds);
                            if (CollectionUtil.isEmpty(userList)) {
                                throw new ServiceException("【" + t.getName() + "】任务环节未配置审批人");
                            }
                            settingAssignee(t,userList.stream().map(SysUser::getUserId).collect(Collectors.toList()));
                        }else{
                            // 设置审批人员
                            List<Long> assignees = workFlowUtils.assignees(nodeAssignee.getAssigneeId(), nodeAssignee.getChooseWay(), t.getName());
                            settingAssignee(t,assignees);
                        }
                    }else{
                        //弹窗选人 根据当前任务节点id获取办理人
                        if(t.getTaskDefinitionKey().equals(nodeAssignee.getNodeId()) && nodeAssignee.getIsShow()){
                            List<Long> assignees = req.getAssignees(t.getTaskDefinitionKey());
                            //设置选人
                            if (CollectionUtil.isNotEmpty(assignees)) {
                                settingAssignee(t,assignees);
                            } else if (StringUtils.isBlank(t.getAssignee())) {
                                if(taskList.size()==1){
                                    throw new ServiceException("【" + t.getName() + "】任务环节未配置审批人");
                                }
                            }
                        }else{
                            //校验会签环节
                            ActNodeAssignee info = actNodeAssignees.stream().filter(e -> e.getNodeId().equals(t.getTaskDefinitionKey())).findFirst().orElse(null);
                            if(ObjectUtil.isNotNull(info)&&!info.getMultiple()){
                                throw new ServiceException("【" + t.getName() + "】任务环节未配置审批人");
                            }else{
                                for (ActNodeAssignee actNodeAssignee : actNodeAssignees) {
                                    if(actNodeAssignee.getMultiple()&&StringUtils.isNotBlank(actNodeAssignee.getMultipleColumn())){
                                        Object variable = runtimeService.getVariable(t.getExecutionId(), actNodeAssignee.getMultipleColumn());
                                        if(ObjectUtil.isEmpty(variable)){
                                            throw new ServiceException("【" + t.getName() + "】任务环节未配置审批人");
                                        }
                                    }
                                    if(actNodeAssignee.getMultiple()&&StringUtils.isBlank(actNodeAssignee.getMultipleColumn())){
                                        throw new ServiceException("【" + t.getName() + "】任务环节未配置审批人");
                                    }
                                }
                            }
                        }
                    }
                } else if (StringUtils.isBlank(t.getAssignee())) {
                    throw new ServiceException("【" + t.getName() + "】任务环节未配置审批人");
                }
            }
            //发送站内信
            workFlowUtils.sendMessage(req.getSendMessage(),processInstance.getProcessInstanceId());

            return true;
        }catch (Exception e){
            e.printStackTrace();
            logger.error("办理失败:"+e.getMessage());
            throw new ServiceException("办理失败:"+e.getMessage());
        }
    }

    /**
     * @Description: 设置下一环节人员
     * @param: task 任务
     * @param: assignees 办理人
     * @return: void
     * @author: gssong
     * @Date: 2021/10/21
     */
    public void settingAssignee(Task task,List<Long> assignees){
        if (assignees.size() == 1) {
            taskService.setAssignee(task.getId(), assignees.get(0).toString());
        } else {
            // 多个作为候选人
            for (Long assignee : assignees) {
                taskService.addCandidateUser(task.getId(), assignee.toString());
            }
        }
    }


    /**
     * @Description: 查询当前用户的已办任务
     * @param: req
     * @return: com.ruoyi.common.core.page.TableDataInfo<com.ruoyi.workflow.domain.vo.TaskFinishVo>
     * @author: gssong
     * @Date: 2021/10/23
     */
    @Override
    public TableDataInfo<TaskFinishVo> getTaskFinishByPage(TaskREQ req) {
        //当前登录人
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
            taskFinishVo.setAssigneeId(StringUtils.isNotBlank(hti.getAssignee())?Long.valueOf(hti.getAssignee()):null);
            taskFinishVoList.add(taskFinishVo);
        }
        if(CollectionUtil.isNotEmpty(list)){
            //办理人集合
            List<Long> assigneeList = taskFinishVoList.stream().map(TaskFinishVo::getAssigneeId).collect(Collectors.toList());
            if(CollectionUtil.isNotEmpty(assigneeList)){
                List<SysUser> userList = iUserService.selectListUserByIds(assigneeList);
                if(CollectionUtil.isNotEmpty(userList)){
                    taskFinishVoList.forEach(e->{
                        SysUser sysUser = userList.stream().filter(t -> t.getUserId().compareTo(e.getAssigneeId())==0).findFirst().orElse(null);
                        if(ObjectUtil.isNotEmpty(sysUser)){
                            e.setAssignee(sysUser.getNickName());
                            e.setAssigneeId(sysUser.getUserId());
                        }
                    });
                }
            }
        }
        return new TableDataInfo(taskFinishVoList, total);
    }


    /**
     * @Description: 获取目标节点（下一个节点）
     * @param: req
     * @return: java.util.Map<java.lang.String,java.lang.Object>
     * @author: gssong
     * @Date: 2021/10/23
     */
    @Override
    public Map<String,Object> getNextNodeInfo(NextNodeREQ req) {
        Map<String, Object> map = new HashMap<>();
        TaskEntity task = (TaskEntity)taskService.createTaskQuery().taskId(req.getTaskId()).singleResult();
        if (task.isSuspended()) {
            throw new ServiceException("当前任务已被挂起");
        }
        ActNodeAssignee nodeAssignee = iActNodeAssigneeService.getInfo(task.getProcessDefinitionId(), task.getTaskDefinitionKey());
        //可驳回的节点
        List<ActTaskNode> taskNodeList = iActTaskNodeService.getListByInstanceId(task.getProcessInstanceId()).stream().filter(e->e.getIsBack()).collect(Collectors.toList());
        map.put("backNodeList",taskNodeList);
        //当前流程实例状态
        ActBusinessStatus actBusinessStatus = iActBusinessStatusService.getInfoByProcessInstId(task.getProcessInstanceId());
        if(ObjectUtil.isEmpty(actBusinessStatus)){
            throw new ServiceException("当前流程异常，未生成act_business_status对象");
        }else{
            map.put("businessStatus",actBusinessStatus);
        }
        //委托流程
        if(ObjectUtil.isNotEmpty(task.getDelegationState())&&ActConstant.PENDING.equals(task.getDelegationState().name())){
            ActNodeAssignee actNodeAssignee = new ActNodeAssignee();
            actNodeAssignee.setIsDelegate(false);
            actNodeAssignee.setIsTransmit(false);
            actNodeAssignee.setIsCopy(false);
            actNodeAssignee.setAddMultiInstance(false);
            actNodeAssignee.setDeleteMultiInstance(false);
            map.put("setting",actNodeAssignee);
            map.put("list",new ArrayList<>());
            map.put("isMultiInstance",false);
            return map;
        }
        //流程定义设置
        if(ObjectUtil.isNotEmpty(nodeAssignee)){
            map.put("setting",nodeAssignee);
        }else{
            ActNodeAssignee actNodeAssignee = new ActNodeAssignee();
            actNodeAssignee.setIsDelegate(false);
            actNodeAssignee.setIsTransmit(false);
            actNodeAssignee.setIsCopy(false);
            actNodeAssignee.setAddMultiInstance(false);
            actNodeAssignee.setDeleteMultiInstance(false);
            map.put("setting",actNodeAssignee);
        }

        //判断当前是否为会签
        MultiVo isMultiInstance = workFlowUtils.isMultiInstance(task.getProcessDefinitionId(), task.getTaskDefinitionKey());
        if(ObjectUtil.isEmpty(isMultiInstance)){
            map.put("isMultiInstance",false);
        }else{
            map.put("isMultiInstance",true);
        }
        //查询任务
        List<Task> taskList = taskService.createTaskQuery().processInstanceId(task.getProcessInstanceId()).list();
        //可以减签的人员
        if(ObjectUtil.isNotEmpty(isMultiInstance)){
            if(isMultiInstance.getType() instanceof ParallelMultiInstanceBehavior){
                map.put("multiList",multiList(task, taskList,isMultiInstance.getType(),null));
            }else if(isMultiInstance.getType() instanceof SequentialMultiInstanceBehavior){
                List<Long> assigneeList = (List)runtimeService.getVariable(task.getExecutionId(), isMultiInstance.getAssigneeList());
                map.put("multiList",multiList(task, taskList,isMultiInstance.getType(),assigneeList));
            }
        }else{
            map.put("multiList",new ArrayList<>());
        }
        //如果是会签最后一个人员审批选人
        if(CollectionUtil.isNotEmpty(taskList)&&taskList.size()>1){
            //return null;
        }
        taskService.setVariables(task.getId(),req.getVariables());
        //流程定义
        String processDefinitionId = task.getProcessDefinitionId();
        //查询bpmn信息
        BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinitionId);
        //通过任务节点id，来获取当前节点信息
        FlowElement flowElement = bpmnModel.getFlowElement(task.getTaskDefinitionKey());
        // 封装下一个用户任务节点信息
        List<ProcessNode> nextNodes = new ArrayList<>();
        // 保存没有表达式的节点
        List<ProcessNode> tempNodes = new ArrayList<>();
        ExecutionEntityImpl executionEntity = (ExecutionEntityImpl) runtimeService.createExecutionQuery()
            .executionId(task.getExecutionId()).singleResult();
        workFlowUtils.getNextNodes(flowElement,executionEntity, nextNodes, tempNodes, task.getId(), null);
        if(CollectionUtil.isEmpty(tempNodes)&&CollectionUtil.isNotEmpty(nextNodes)){
            Iterator<ProcessNode> iterator = nextNodes.iterator();
            while (iterator.hasNext()) {
                ProcessNode node = iterator.next();
                if (!node.getExpression()){
                    iterator.remove();
                }
            }
        }
        //排它网关  如果下已审批节点变量判断都为false  将保存的临时的节点赋予下一节点
        List<String> exclusiveLists = nextNodes.stream().filter(e -> e.getNodeType().equals(ActConstant.EXCLUSIVE_GATEWAY) && e.getExpression()).
            map(ProcessNode::getNodeType).collect(Collectors.toList());
        if (CollectionUtil.isEmpty(nextNodes) && CollectionUtil.isEmpty(exclusiveLists)) {
            nextNodes.addAll(tempNodes);
        }
        // 返回前端
        List<ProcessNode> nodeList = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(nextNodes)) {
            //排它网关  如果下已审批节点变量判断都为false  将保存的临时的节点赋予下一节点
            List<String> exclusiveList = nextNodes.stream().filter(e -> e.getNodeType().equals(ActConstant.EXCLUSIVE_GATEWAY) && e.getExpression()).
                map(ProcessNode::getNodeType).collect(Collectors.toList());
            if (!CollectionUtil.isEmpty(nextNodes) && CollectionUtil.isEmpty(exclusiveList)) {
                nextNodes.addAll(tempNodes);
            }
            //排它网关
            List<String> exclusiveGatewayList = nextNodes.stream().filter(e -> e.getNodeType().equals(ActConstant.EXCLUSIVE_GATEWAY) && e.getExpression()).
                map(ProcessNode::getNodeType).collect(Collectors.toList());
            if (CollectionUtil.isNotEmpty(exclusiveGatewayList)) {
                nextNodes.forEach(node -> {
                    if ((ActConstant.EXCLUSIVE_GATEWAY.equals(node.getNodeType()) && node.getExpression())) {
                        nodeList.add(node);
                    }
                });
                //设置节点审批人员
                List<ProcessNode> processNodeList = getProcessNodeAssigneeList(nodeList, task.getProcessDefinitionId());
                map.put("list",processNodeList);
                return map;
            } else {
                //设置节点审批人员
                List<ProcessNode> processNodeList = getProcessNodeAssigneeList(nextNodes, task.getProcessDefinitionId());
                map.put("list",processNodeList);
                return map;
            }
        }
        map.put("list",nextNodes);
        return map;
    }


    /**
     * @Description: 可减签人员集合
     * @param: task  当前任务
     * @param: taskList  当前实例所有任务
     * @param: type  会签类型
     * @param: assigneeList 串行会签人员
     * @return: java.util.List<com.ruoyi.workflow.domain.vo.TaskVo>
     * @author: gssong
     * @Date: 2022/4/24 11:17
     */
    private List<TaskVo> multiList(TaskEntity task, List<Task> taskList,Object type,List<Long> assigneeList) {
        List<TaskVo> taskListVo = new ArrayList<>();
        if(type instanceof SequentialMultiInstanceBehavior){
            List<Long> userIds = assigneeList.stream().filter(userId -> !userId.toString().equals(task.getAssignee())).collect(Collectors.toList());
            List<SysUser> sysUsers = null;
            if(CollectionUtil.isNotEmpty(userIds)){
                sysUsers = iUserService.selectListUserByIds(userIds);
            }
            for (Long userId : userIds) {
                TaskVo taskVo = new TaskVo();
                taskVo.setId("串行会签");
                taskVo.setExecutionId("串行会签");
                taskVo.setProcessInstanceId(task.getProcessInstanceId());
                taskVo.setName(task.getName());
                taskVo.setAssigneeId(String.valueOf(userId));
                if(CollectionUtil.isNotEmpty(sysUsers)&&sysUsers.size()>0){
                    SysUser sysUser = sysUsers.stream().filter(u -> u.getUserId().toString().equals(userId.toString())).findFirst().orElse(null);
                    if(ObjectUtil.isNotEmpty(sysUser)){
                        taskVo.setAssignee(sysUser.getNickName());
                    }
                }
                taskListVo.add(taskVo);
            }
            return taskListVo;
        }else if(type instanceof ParallelMultiInstanceBehavior){
            List<Task> tasks = taskList.stream().filter(e ->StringUtils.isBlank(e.getParentTaskId())&& !e.getExecutionId().equals(task.getExecutionId())
                &&e.getTaskDefinitionKey().equals(task.getTaskDefinitionKey())).collect(Collectors.toList());
            if(CollectionUtil.isNotEmpty(tasks)){
                List<Long> userIds = tasks.stream().map(e -> Long.valueOf(e.getAssignee())).collect(Collectors.toList());
                List<SysUser> sysUsers = null;
                if(CollectionUtil.isNotEmpty(userIds)){
                    sysUsers = iUserService.selectListUserByIds(userIds);
                }
                for (Task t : tasks) {
                    TaskVo taskVo = new TaskVo();
                    taskVo.setId(t.getId());
                    taskVo.setExecutionId(t.getExecutionId());
                    taskVo.setProcessInstanceId(t.getProcessInstanceId());
                    taskVo.setName(t.getName());
                    taskVo.setAssigneeId(t.getAssignee());
                    if(CollectionUtil.isNotEmpty(sysUsers)){
                        SysUser sysUser = sysUsers.stream().filter(u -> u.getUserId().toString().equals(t.getAssignee())).findFirst().orElse(null);
                        if(ObjectUtil.isNotEmpty(sysUser)){
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
     * @Description: 设置节点审批人员
     * @param: nodeList 节点列表
     * @param: definitionId 流程定义id
     * @return: java.util.List<com.ruoyi.workflow.domain.vo.ProcessNode>
     * @author: gssong
     * @Date: 2021/10/23
     */
    private List<ProcessNode> getProcessNodeAssigneeList(List<ProcessNode> nodeList, String definitionId) {
        List<ActNodeAssignee> actNodeAssignees = iActNodeAssigneeService.getInfoByProcessDefinitionId(definitionId);
        if (CollectionUtil.isNotEmpty(actNodeAssignees)) {
            for (ProcessNode processNode : nodeList) {
                //校验画流程模型该环节是否放置审批人员
                if (StringUtils.isBlank(processNode.getAssignee())) {
                    if(CollectionUtil.isEmpty(actNodeAssignees)){
                        throw new ServiceException("该流程定义未配置，请联系管理员！");
                    }
                    ActNodeAssignee nodeAssignee = actNodeAssignees.stream().filter(e -> e.getNodeId().equals(processNode.getNodeId())).findFirst().orElse(null);

                    //按角色 部门 人员id 等设置查询人员信息
                    if (ObjectUtil.isNotNull(nodeAssignee) && StringUtils.isNotBlank(nodeAssignee.getAssigneeId())
                        && nodeAssignee.getFullClassId() == null && StringUtils.isNotBlank(nodeAssignee.getAssignee())) {
                        processNode.setChooseWay(nodeAssignee.getChooseWay());
                        processNode.setAssignee(nodeAssignee.getAssignee());
                        processNode.setAssigneeId(nodeAssignee.getAssigneeId());
                        processNode.setIsShow(nodeAssignee.getIsShow());
                        if(nodeAssignee.getMultiple()){
                            processNode.setNodeId(nodeAssignee.getMultipleColumn());
                        }
                        processNode.setMultiple(nodeAssignee.getMultiple());
                        processNode.setMultipleColumn(nodeAssignee.getMultipleColumn());
                        //按照业务规则设置查询人员信息
                    } else if (ObjectUtil.isNotNull(nodeAssignee) && nodeAssignee.getFullClassId() != null) {
                        ActFullClassVo actFullClassVo = iActFullClassService.queryById(nodeAssignee.getFullClassId());
                        Object assignee = workFlowUtils.assignList(actFullClassVo, processNode.getTaskId());
                        processNode.setChooseWay(nodeAssignee.getChooseWay());
                        processNode.setAssignee("");
                        processNode.setAssigneeId(assignee.toString());
                        processNode.setIsShow(nodeAssignee.getIsShow());
                        if(nodeAssignee.getMultiple()){
                            processNode.setNodeId(nodeAssignee.getMultipleColumn());
                        }
                        processNode.setMultiple(nodeAssignee.getMultiple());
                        processNode.setMultipleColumn(nodeAssignee.getMultipleColumn());
                    }else{
                        throw new ServiceException(processNode.getNodeName() + "未配置审批人，请联系管理员！");
                    }
                } else {
                    ActNodeAssignee nodeAssignee = actNodeAssignees.stream().filter(e -> e.getNodeId().equals(processNode.getNodeId())).findFirst().orElse(null);
                    if(ObjectUtil.isNotEmpty(nodeAssignee)){
                        processNode.setChooseWay(nodeAssignee.getChooseWay());
                        processNode.setAssignee(nodeAssignee.getAssignee());
                        processNode.setAssigneeId(nodeAssignee.getAssigneeId());
                        processNode.setIsShow(nodeAssignee.getIsShow());
                        if(nodeAssignee.getMultiple()){
                            processNode.setNodeId(nodeAssignee.getMultipleColumn());
                        }
                        processNode.setMultiple(nodeAssignee.getMultiple());
                        processNode.setMultipleColumn(nodeAssignee.getMultipleColumn());
                    }else{
                        processNode.setChooseWay(ActConstant.WORKFLOW_ASSIGNEE);
                    }
                }
            }
        }
        if (CollectionUtil.isNotEmpty(nodeList)) {
            Iterator<ProcessNode> iterator = nodeList.iterator();
            while (iterator.hasNext()) {
                ProcessNode node = iterator.next();
                // 去除画流程时设置的选人节点  不需要弹窗 选人
                // 去除不需要弹窗选人的节点
                if (ActConstant.WORKFLOW_ASSIGNEE.equals(node.getChooseWay())||!node.getIsShow()) {
                    iterator.remove();
                }
            }
        }
        return nodeList;
    }

    /**
     * @Description: 查询所有用户的已办任务
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
            taskFinishVo.setAssigneeId(StringUtils.isNotBlank(hti.getAssignee())?Long.valueOf(hti.getAssignee()):null);
            taskFinishVoList.add(taskFinishVo);
        }
        if(CollectionUtil.isNotEmpty(list)){
            //办理人集合
            List<Long> assigneeList = taskFinishVoList.stream().map(TaskFinishVo::getAssigneeId).collect(Collectors.toList());
            if(CollectionUtil.isNotEmpty(assigneeList)){
                List<SysUser> userList = iUserService.selectListUserByIds(assigneeList);
                if(CollectionUtil.isNotEmpty(userList)){
                    taskFinishVoList.forEach(e->{
                        SysUser sysUser = userList.stream().filter(t -> t.getUserId().compareTo(e.getAssigneeId())==0).findFirst().orElse(null);
                        if(ObjectUtil.isNotEmpty(sysUser)){
                            e.setAssignee(sysUser.getNickName());
                            e.setAssigneeId(sysUser.getUserId());
                        }
                    });
                }
            }
        }
        return new TableDataInfo(taskFinishVoList, total);
    }

    /**
     * @Description: 查询所有用户的待办任务
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
            taskWaitingVo.setAssigneeId(StringUtils.isNotBlank(task.getAssignee())?Long.valueOf(task.getAssignee()):null);
            taskWaitingVo.setProcessStatus(task.isSuspended() == true ? "挂起" : "激活");
            // 查询流程实例
            ProcessInstance pi = runtimeService.createProcessInstanceQuery()
                .processInstanceId(task.getProcessInstanceId()).singleResult();
            //流程发起人
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
        if(CollectionUtil.isNotEmpty(list)){
            //办理人集合
            List<Long> assigneeList = list.stream().map(TaskWaitingVo::getAssigneeId).collect(Collectors.toList());
            if(CollectionUtil.isNotEmpty(assigneeList)){
                List<SysUser> userList = iUserService.selectListUserByIds(assigneeList);
                if(CollectionUtil.isNotEmpty(userList)){
                    list.forEach(e->{
                        SysUser sysUser = userList.stream().filter(t -> StringUtils.isNotBlank(e.getAssignee())&&t.getUserId().compareTo(e.getAssigneeId())==0).findFirst().orElse(null);
                        if(ObjectUtil.isNotEmpty(sysUser)){
                            e.setAssignee(sysUser.getNickName());
                            e.setAssigneeId(sysUser.getUserId());
                        }
                    });
                }
            }
        }
        return new TableDataInfo(list, total);
    }

    /**
     * @Description: 驳回审批
     * @param: backProcessBo
     * @return: java.lang.String
     * @author: gssong
     * @Date: 2021/11/6
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String backProcess(BackProcessBo backProcessBo) {

        Task task = taskService.createTaskQuery().taskId(backProcessBo.getTaskId()).taskAssignee(getUserId().toString()).singleResult();
        String processInstanceId = task.getProcessInstanceId();
        if (task.isSuspended()) {
            throw new ServiceException("当前任务已被挂起");
        }
        if (ObjectUtil.isNull(task)) {
            throw new ServiceException("当前任务不存在或你不是任务办理人");
        }
        try {
            //判断是否有多个任务
            List<Task> taskList = taskService.createTaskQuery().processInstanceId(processInstanceId).list();
            List<String> preUserTaskList = getPrevUserNodeList(task.getProcessDefinitionId(), backProcessBo.getTargetActivityId(),task);
            //当前单个任务驳回到并行网关
            taskService.addComment(task.getId(), processInstanceId, StringUtils.isNotBlank(backProcessBo.getComment()) ? backProcessBo.getComment() : "驳回");
            if(CollectionUtil.isNotEmpty(preUserTaskList)&&taskList.size()==1){
                runtimeService.createChangeActivityStateBuilder().processInstanceId(processInstanceId)
                    .moveSingleActivityIdToActivityIds(taskList.get(0).getTaskDefinitionKey(), preUserTaskList)
                    .changeState();
                //当前多个任务驳回到单个节点
            }else if(taskList.size()>1&&CollectionUtil.isEmpty(preUserTaskList)){
                runtimeService.createChangeActivityStateBuilder().processInstanceId(processInstanceId)
                    .moveActivityIdsToSingleActivityId(taskList.stream().map(Task::getTaskDefinitionKey).distinct().collect(Collectors.toList()), backProcessBo.getTargetActivityId())
                    .changeState();
                //当前单个节点驳回单个节点
            }else if(taskList.size()==1&&CollectionUtil.isEmpty(preUserTaskList)){
                runtimeService.createChangeActivityStateBuilder().processInstanceId(processInstanceId)
                    .moveActivityIdTo(taskList.get(0).getTaskDefinitionKey(), backProcessBo.getTargetActivityId())
                    .changeState();
                //当前多个节点驳回多个节点
            }else if(taskList.size()>1&&CollectionUtil.isNotEmpty(preUserTaskList)){
                taskList.forEach(e->{
                    if(e.getId().equals(backProcessBo.getTaskId())){
                        runtimeService.createChangeActivityStateBuilder().processInstanceId(processInstanceId)
                            .moveSingleActivityIdToActivityIds(e.getTaskDefinitionKey(), preUserTaskList)
                            .changeState();
                    }else{
                        workFlowUtils.deleteRuntimeTask(e);
                    }
                });
            }else{
                throw new ServiceException("驳回失败");
            }
            List<Task> otherTasks = null;
            if(taskList.size()>1){
                otherTasks = taskList.stream().filter(e->!e.getId().equals(backProcessBo.getTaskId())).collect(Collectors.toList());
            }
            if(CollectionUtil.isNotEmpty(otherTasks)&&otherTasks.size()>0){
                otherTasks.forEach(e->{
                    historyService.deleteHistoricTaskInstance(e.getId());
                });
            }
            //判断是否会签
            LambdaQueryWrapper<ActNodeAssignee> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ActNodeAssignee::getNodeId, backProcessBo.getTargetActivityId());
            wrapper.eq(ActNodeAssignee::getProcessDefinitionId,task.getProcessDefinitionId());
            ActNodeAssignee actNodeAssignee = iActNodeAssigneeService.getOne(wrapper);
            List<Task> newTaskList = taskService.createTaskQuery().processInstanceId(processInstanceId).list();
            //处理并行会签环节重复节点
            if(CollectionUtil.isNotEmpty(newTaskList)&&newTaskList.size()>0){
                List<Task> taskCollect = newTaskList.stream().filter(e -> e.getTaskDefinitionKey().equals(backProcessBo.getTargetActivityId())).collect(Collectors.toList());
                if(taskCollect.size()>1){
                    taskCollect.remove(0);
                    taskCollect.forEach(e->{
                        workFlowUtils.deleteRuntimeTask(e);
                    });
                }
            }
            if(ObjectUtil.isNotEmpty(actNodeAssignee)&&!actNodeAssignee.getMultiple()){
                List<Task> runTaskList = taskService.createTaskQuery().processInstanceId(processInstanceId).list();
                for (Task runTask : runTaskList) {
                    //取之前的历史办理人
                    List<HistoricTaskInstance> oldTargerTaskList = historyService.createHistoricTaskInstanceQuery()
                        .taskDefinitionKey(runTask.getTaskDefinitionKey()) // 节点id
                        .processInstanceId(processInstanceId)
                        .finished() //已经完成才是历史
                        .orderByTaskCreateTime().desc() //最新办理的在最前面
                        .list();
                    if(CollectionUtil.isNotEmpty(oldTargerTaskList)){
                        HistoricTaskInstance oldTargerTask = oldTargerTaskList.get(0);
                        taskService.setAssignee(runTask.getId(), oldTargerTask.getAssignee());
                    }

                }
            }

            //删除驳回后的流程节点
            ActTaskNode actTaskNode = iActTaskNodeService.getListByInstanceIdAndNodeId(task.getProcessInstanceId(), backProcessBo.getTargetActivityId());
            if (ObjectUtil.isNotNull(actTaskNode) && actTaskNode.getOrderNo() == 0) {
                ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
                List<Task> newList = taskService.createTaskQuery().processInstanceId(processInstanceId).list();
                for (Task ta : newList) {
                    Map<String, Object> variables = new HashMap<>();
                    variables.put("status", BusinessStatusEnum.BACK.getStatus());
                    taskService.setVariables(ta.getId(), variables);
                }
                iActBusinessStatusService.updateState(processInstance.getBusinessKey(), BusinessStatusEnum.BACK);
            }
            iActTaskNodeService.deleteBackTaskNode(processInstanceId, backProcessBo.getTargetActivityId());
            //发送站内信
            workFlowUtils.sendMessage(backProcessBo.getSendMessage(),processInstanceId);

        }catch (Exception e){
            e.printStackTrace();
            throw new ServiceException("驳回失败:"+e.getMessage());
        }
        return processInstanceId;
    }

    /**
     * @Description: 获取并行网关下一步审批节点信息
     * @param: processDefinitionId 流程定义id
     * @param: targetActivityId 驳回的节点id
     * @param: task 任务
     * @return: java.util.List<java.lang.String>
     * @author: gssong
     * @Date: 2022/4/10
     */
    public List<String>  getPrevUserNodeList(String processDefinitionId, String targetActivityId, Task task){
        List<String> nodeListId = new ArrayList<>();

        BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinitionId);
        FlowElement flowElement = bpmnModel.getFlowElement(targetActivityId);
        List<SequenceFlow> outgoingFlows = ((FlowNode) flowElement).getIncomingFlows();
        for (SequenceFlow outgoingFlow : outgoingFlows) {
            FlowElement sourceFlowElement = outgoingFlow.getSourceFlowElement();
            //并行网关
            if(sourceFlowElement instanceof ParallelGateway){
                List<SequenceFlow> parallelGatewayOutgoingFlow = ((ParallelGateway) sourceFlowElement).getOutgoingFlows();
                for (SequenceFlow sequenceFlow : parallelGatewayOutgoingFlow) {
                    FlowElement element = sequenceFlow.getTargetFlowElement();
                    if(element instanceof UserTask){
                        nodeListId.add(element.getId());
                    }
                }
             //包容网关
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
     * @Description: 获取历史任务节点，用于驳回功能
     * @param: processInstId
     * @return: java.util.List<com.ruoyi.workflow.domain.ActTaskNode>
     * @author: gssong
     * @Date: 2021/11/6
     */
    @Override
    public List<ActTaskNode> getBackNodes(String processInstId) {
        List<ActTaskNode> list = iActTaskNodeService.getListByInstanceId(processInstId).stream().filter(e->e.getIsBack()).collect(Collectors.toList());
        return list;
    }

    /**
     * @Description: 委派任务
     * @param: taskREQ
     * @return: java.lang.Boolean
     * @author: gssong
     * @Date: 2022/3/4
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean delegateTask(DelegateREQ delegateREQ) {
        if(StringUtils.isBlank(delegateREQ.getDelegateUserId())){
            throw new ServiceException("请选择委托人");
        }
        TaskEntity task = (TaskEntity) taskService.createTaskQuery().taskId(delegateREQ.getTaskId())
            .taskCandidateOrAssigned(LoginHelper.getUserId().toString()).singleResult();
        if(ObjectUtil.isEmpty(task)){
            throw new ServiceException("当前任务不存在或你不是任务办理人");
        }
        try{
            TaskEntity subTask = this.createNewTask(task,new Date());
            taskService.addComment(subTask.getId(), task.getProcessInstanceId(),"【"+LoginHelper.getUsername()+"】委派给【"+delegateREQ.getDelegateUserName()+"】");
            //委托任务
            taskService.delegateTask(delegateREQ.getTaskId(), delegateREQ.getDelegateUserId());
            //办理生成的任务记录
            taskService.complete(subTask.getId());
            ActHiTaskInst actHiTaskInst = new ActHiTaskInst();
            actHiTaskInst.setId(task.getId());
            actHiTaskInst.setStartTime(new Date());
            iActHiTaskInstService.updateById(actHiTaskInst);
            //发送站内信
            workFlowUtils.sendMessage(delegateREQ.getSendMessage(),task.getProcessInstanceId());
            return true;
        }catch (Exception e){
            e.printStackTrace();
            throw new ServiceException(e.getMessage());
        }
    }

    /**
     * @Description: 转办任务
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
        if(ObjectUtil.isEmpty(task)){
            return R.fail("当前任务不存在或你不是任务办理人");
        }
        try {
            TaskEntity subTask = createNewTask(task, new Date());
            taskService.addComment(subTask.getId(), task.getProcessInstanceId(),
                StringUtils.isNotBlank(transmitREQ.getComment())?transmitREQ.getComment():LoginHelper.getUsername()+"转办了任务");
            taskService.complete(subTask.getId());
            taskService.setAssignee(task.getId(),transmitREQ.getTransmitUserId());
            //发送站内信
            workFlowUtils.sendMessage(transmitREQ.getSendMessage(),task.getProcessInstanceId());
            return R.ok();
        }catch (Exception e){
            e.printStackTrace();
            return R.fail();
        }
    }

    /**
     * @Description: 创建流程任务
     * @param: parentTask
     * @param: createTime
     * @return: org.flowable.task.service.impl.persistence.entity.TaskEntity
     * @author: gssong
     * @Date: 2022/3/13
     */
    private TaskEntity createNewTask(Task currentTask, Date createTime){
        TaskEntity task = null;
        if(ObjectUtil.isNotEmpty(currentTask)){
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
        if(ObjectUtil.isNotNull(task)){
            ActHiTaskInst hiTaskInst = iActHiTaskInstService.getById(task.getId());
            if(ObjectUtil.isNotEmpty(hiTaskInst)){
                hiTaskInst.setProcDefId(task.getProcessDefinitionId());
                hiTaskInst.setProcInstId(task.getProcessInstanceId());
                hiTaskInst.setTaskDefKey(task.getTaskDefinitionKey());
                hiTaskInst.setStartTime(createTime);
                iActHiTaskInstService.updateById(hiTaskInst);
            }
        }
        return  task;
    }

    /**
     * @Description: 会签任务加签
     * @param: addMultiREQ
     * @return: com.ruoyi.common.core.domain.R<java.lang.Boolean>
     * @author: gssong
     * @Date: 2022/4/15 13:06
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<Boolean> addMultiInstanceExecution(AddMultiREQ addMultiREQ) {
        String taskId = addMultiREQ.getTaskId();
        Task task = taskService.createTaskQuery().taskId(taskId)
            .taskCandidateOrAssigned(LoginHelper.getUserId().toString()).singleResult();
        if(ObjectUtil.isEmpty(task)){
            throw new ServiceException("当前任务不存在或你不是任务办理人");
        }
        String taskDefinitionKey = task.getTaskDefinitionKey();
        String processInstanceId = task.getProcessInstanceId();
        String processDefinitionId = task.getProcessDefinitionId();
        MultiVo multiVo = workFlowUtils.isMultiInstance(processDefinitionId, taskDefinitionKey);
        if(ObjectUtil.isEmpty(multiVo)){
            throw new ServiceException("当前环节不是会签节点");
        }
        try {
            if(multiVo.getType() instanceof ParallelMultiInstanceBehavior){
                for (Long assignee : addMultiREQ.getAssignees()) {
                    runtimeService.addMultiInstanceExecution(taskDefinitionKey, processInstanceId, Collections.singletonMap(multiVo.getAssignee(), assignee));
                }
            }else if(multiVo.getType() instanceof SequentialMultiInstanceBehavior){
                AddSequenceMultiInstanceCmd addSequenceMultiInstanceCmd = new AddSequenceMultiInstanceCmd(task.getExecutionId(),multiVo.getAssigneeList(),addMultiREQ.getAssignees());
                managementService.executeCommand(addSequenceMultiInstanceCmd);
            }
            List<String> assigneeNames = addMultiREQ.getAssigneeNames();
            String username = LoginHelper.getUsername();
            TaskEntity subTask = createNewTask(task, new Date());
            taskService.addComment(subTask.getId(),processInstanceId,username+"加签【"+String.join(",",assigneeNames)+"】");
            taskService.complete(subTask.getId());
            return R.ok();
        }catch (Exception e){
            e.printStackTrace();
            return R.fail();
        }
    }

    /**
     * @Description: 会签任务减签
     * @param: deleteMultiREQ
     * @return: com.ruoyi.common.core.domain.R<java.lang.Boolean>
     * @author: gssong
     * @Date: 2022/4/16 10:59
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<Boolean> deleteMultiInstanceExecution(DeleteMultiREQ deleteMultiREQ) {
        Task task = taskService.createTaskQuery().taskId(deleteMultiREQ.getTaskId())
            .taskCandidateOrAssigned(LoginHelper.getUserId().toString()).singleResult();
        if(ObjectUtil.isEmpty(task)){
            throw new ServiceException("当前任务不存在或你不是任务办理人");
        }
        String taskDefinitionKey = task.getTaskDefinitionKey();
        String processInstanceId = task.getProcessInstanceId();
        String processDefinitionId = task.getProcessDefinitionId();
        MultiVo multiVo = workFlowUtils.isMultiInstance(processDefinitionId, taskDefinitionKey);
        if(ObjectUtil.isEmpty(multiVo)){
            throw new ServiceException("当前环节不是会签节点");
        }
        try {
            if(multiVo.getType() instanceof ParallelMultiInstanceBehavior){
                for (String executionId : deleteMultiREQ.getExecutionIds()) {
                    runtimeService.deleteMultiInstanceExecution(executionId, false);
                }
                for (String taskId : deleteMultiREQ.getTaskIds()) {
                    historyService.deleteHistoricTaskInstance(taskId);
                }
            }else if(multiVo.getType() instanceof SequentialMultiInstanceBehavior){
                DeleteSequenceMultiInstanceCmd deleteSequenceMultiInstanceCmd = new DeleteSequenceMultiInstanceCmd(task.getAssignee(),task.getExecutionId(),multiVo.getAssigneeList(),deleteMultiREQ.getAssigneeIds());
                managementService.executeCommand(deleteSequenceMultiInstanceCmd);
            }
            List<String> assigneeNames = deleteMultiREQ.getAssigneeNames();
            String username = LoginHelper.getUsername();
            TaskEntity subTask = createNewTask(task, new Date());
            taskService.addComment(subTask.getId(),processInstanceId,username+"减签【"+String.join(",",assigneeNames)+"】");
            taskService.complete(subTask.getId());
            return R.ok();
        }catch (Exception e){
            e.printStackTrace();
            return R.fail();
        }
    }

}
