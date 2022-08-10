package com.ruoyi.workflow.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.helper.LoginHelper;
import com.ruoyi.workflow.flowable.config.CustomDefaultProcessDiagramGenerator;
import com.ruoyi.workflow.common.constant.ActConstant;
import com.ruoyi.workflow.common.enums.BusinessStatusEnum;
import com.ruoyi.workflow.domain.ActBusinessStatus;
import com.ruoyi.workflow.domain.ActTaskNode;
import com.ruoyi.workflow.domain.bo.ProcessInstFinishREQ;
import com.ruoyi.workflow.domain.bo.ProcessInstRunningREQ;
import com.ruoyi.workflow.domain.bo.StartREQ;
import com.ruoyi.workflow.domain.vo.ActHistoryInfoVo;
import com.ruoyi.workflow.domain.vo.ProcessInstFinishVo;
import com.ruoyi.workflow.domain.vo.ProcessInstRunningVo;
import com.ruoyi.workflow.flowable.factory.WorkflowService;
import com.ruoyi.workflow.service.*;
import com.ruoyi.workflow.utils.WorkFlowUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.common.engine.impl.identity.Authentication;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.history.HistoricProcessInstanceQuery;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.engine.runtime.ProcessInstanceQuery;
import org.flowable.engine.task.Comment;
import org.flowable.task.api.Task;
import org.flowable.task.api.TaskQuery;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @program: ruoyi-vue-plus
 * @description: 流程实例业务层
 * @author: gssong
 * @created: 2021/10/10 18:38
 */
@Service
@RequiredArgsConstructor
public class ProcessInstanceServiceImpl extends WorkflowService implements IProcessInstanceService {

    private final IActBusinessStatusService iActBusinessStatusService;
    private final IUserService iUserService;
    private final IActTaskNodeService iActTaskNodeService;
    private final WorkFlowUtils workFlowUtils;


    /**
     * @Description: 提交申请，启动流程实例
     * @param: startReq
     * @return: java.util.Map<java.lang.String, java.lang.Object>
     * @author: gssong
     * @Date: 2021/10/10
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> startWorkFlow(StartREQ startReq) {
        Map<String, Object> map = new HashMap<>();
        if (StringUtils.isBlank(startReq.getBusinessKey())) {
            throw new ServiceException("启动工作流时必须包含业务ID");
        }
        // 判断当前业务是否启动过流程
        List<HistoricProcessInstance> instanceList = historyService.createHistoricProcessInstanceQuery()
            .processInstanceBusinessKey(startReq.getBusinessKey()).list();
        TaskQuery taskQuery = taskService.createTaskQuery();
        List<Task> taskResult = taskQuery.processInstanceBusinessKey(startReq.getBusinessKey()).list();
        if (CollectionUtil.isNotEmpty(instanceList)) {
            ActBusinessStatus info = iActBusinessStatusService.getInfoByBusinessKey(startReq.getBusinessKey());
            if (ObjectUtil.isNotEmpty(info)) {
                BusinessStatusEnum.checkStatus(info.getStatus());
            }
            map.put("processInstanceId", taskResult.get(0).getProcessInstanceId());
            map.put("taskId", taskResult.get(0).getId());
            return map;
        }
        // 设置启动人
        Authentication.setAuthenticatedUserId(LoginHelper.getUserId().toString());
        // 启动流程实例（提交申请）
        Map<String, Object> variables = startReq.getVariables();
        ProcessInstance pi = runtimeService.startProcessInstanceByKey(startReq.getProcessKey(), startReq.getBusinessKey(), variables);
        // 将流程定义名称 作为 流程实例名称
        runtimeService.setProcessInstanceName(pi.getProcessInstanceId(), pi.getProcessDefinitionName());
        // 申请人执行流程
        List<Task> taskList = taskService.createTaskQuery().processInstanceId(pi.getId()).list();
        if (taskList.size() > 1) {
            throw new ServiceException("请检查流程第一个环节是否为申请人！");
        }
        taskService.setAssignee(taskList.get(0).getId(), LoginHelper.getUserId().toString());
        // 更新业务状态
        iActBusinessStatusService.updateState(startReq.getBusinessKey(), BusinessStatusEnum.DRAFT, taskList.get(0).getProcessInstanceId(), startReq.getClassFullName());

        map.put("processInstanceId", pi.getProcessInstanceId());
        map.put("taskId", taskList.get(0).getId());
        return map;
    }

    /**
     * @Description: 通过流程实例id查询流程审批记录
     * @param: processInstanceId
     * @return: java.util.List<com.ruoyi.workflow.domain.vo.ActHistoryInfoVo>
     * @Author: gssong
     * @Date: 2021/10/16
     */
    @Override
    public List<ActHistoryInfoVo> getHistoryInfoList(String processInstanceId) {
        //查询任务办理记录
        List<HistoricTaskInstance> list = historyService.createHistoricTaskInstanceQuery().processInstanceId(processInstanceId)
            .orderByHistoricTaskInstanceEndTime().desc().list();
        list.stream().sorted(Comparator.comparing(HistoricTaskInstance::getEndTime, Comparator.nullsFirst(Date::compareTo))).collect(Collectors.toList());
        List<ActHistoryInfoVo> actHistoryInfoVoList = new ArrayList<>();
        for (HistoricTaskInstance historicTaskInstance : list) {
            ActHistoryInfoVo actHistoryInfoVo = new ActHistoryInfoVo();
            BeanUtils.copyProperties(historicTaskInstance, actHistoryInfoVo);
            actHistoryInfoVo.setStatus(actHistoryInfoVo.getEndTime() == null ? "待处理" : "已处理");
            List<Comment> taskComments = taskService.getTaskComments(historicTaskInstance.getId());
            if(CollectionUtil.isNotEmpty(taskComments)){
                actHistoryInfoVo.setCommentId(taskComments.get(0).getId());
                String message = taskComments.stream()
                    .map(Comment::getFullMessage).collect(Collectors.joining("。"));
                if (StringUtils.isNotBlank(message)) {
                    actHistoryInfoVo.setComment(message);
                }
            }
            actHistoryInfoVoList.add(actHistoryInfoVo);
        }
        //翻译人员名称
        if (CollectionUtil.isNotEmpty(actHistoryInfoVoList)) {
            for (ActHistoryInfoVo actHistoryInfoVo : actHistoryInfoVoList) {
                if (StringUtils.isNotBlank(actHistoryInfoVo.getAssignee())) {
                    List<Long> userIds = new ArrayList<>();
                    Arrays.asList(actHistoryInfoVo.getAssignee().split(",")).forEach(id ->
                        userIds.add(Long.valueOf(id))
                    );
                    List<SysUser> sysUsers = iUserService.selectListUserByIds(userIds);
                    if (CollectionUtil.isNotEmpty(sysUsers)) {
                        actHistoryInfoVo.setNickName(sysUsers.stream().map(SysUser::getNickName).collect(Collectors.joining(",")));
                    }
                }
            }
        }
        List<ActHistoryInfoVo> collect = new ArrayList<>();
        //待办理
        List<ActHistoryInfoVo> waitingTask = actHistoryInfoVoList.stream().filter(e -> e.getEndTime() == null).collect(Collectors.toList());
        waitingTask.forEach(e -> {
            if (StringUtils.isNotBlank(e.getOwner())) {
                SysUser sysUser = iUserService.selectUserById(Long.valueOf(e.getOwner()));
                if (ObjectUtil.isNotEmpty(sysUser)) {
                    e.setNickName(sysUser.getNickName());
                }
            }
        });
        //已办理
        List<ActHistoryInfoVo> finishTask = actHistoryInfoVoList.stream().filter(e -> e.getEndTime() != null).collect(Collectors.toList());
        collect.addAll(waitingTask);
        collect.addAll(finishTask);
        return collect;
    }

    /**
     * @Description: 通过流程实例id获取历史流程图
     * @param: processInstId
     * @param: response
     * @return: void
     * @Author: gssong
     * @Date: 2021/10/16
     */
    @Override
    public void getHistoryProcessImage(String processInstanceId, HttpServletResponse response) {
        // 设置页面不缓存
        response.setHeader("Pragma", "no-cache");
        response.addHeader("Cache-Control", "must-revalidate");
        response.addHeader("Cache-Control", "no-cache");
        response.addHeader("Cache-Control", "no-store");
        response.setDateHeader("Expires", 0);
        InputStream inputStream = null;
        try {
            String processDefinitionId;
            // 获取当前的流程实例
            ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
            // 如果流程已经结束，则得到结束节点
            if (Objects.isNull(processInstance)) {
                HistoricProcessInstance pi = historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();

                processDefinitionId = pi.getProcessDefinitionId();
            } else {// 如果流程没有结束，则取当前活动节点
                // 根据流程实例ID获得当前处于活动状态的ActivityId合集
                ProcessInstance pi = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
                processDefinitionId = pi.getProcessDefinitionId();
            }

            // 获得活动的节点
            List<HistoricActivityInstance> highLightedFlowList = historyService.createHistoricActivityInstanceQuery().processInstanceId(processInstanceId).orderByHistoricActivityInstanceStartTime().asc().list();

            List<String> highLightedFlows = new ArrayList<>();
            List<String> highLightedNodes = new ArrayList<>();
            //高亮
            for (HistoricActivityInstance tempActivity : highLightedFlowList) {
                if (ActConstant.SEQUENCE_FLOW.equals(tempActivity.getActivityType())) {
                    //高亮线
                    highLightedFlows.add(tempActivity.getActivityId());
                } else {
                    //高亮节点
                    if (tempActivity.getEndTime() == null) {
                        highLightedNodes.add(Color.RED.toString() + tempActivity.getActivityId());
                    } else {
                        highLightedNodes.add(tempActivity.getActivityId());
                    }
                }
            }
            List<String> highLightedNodeList = new ArrayList<>();
            //运行中的节点
            List<String> redNodeCollect = highLightedNodes.stream().filter(e -> e.contains(Color.RED.toString())).collect(Collectors.toList());
            //排除与运行中相同的节点
            for (String nodeId : highLightedNodes) {
                if (!nodeId.contains(Color.RED.toString()) && !redNodeCollect.contains(Color.RED.toString() + nodeId)) {
                    highLightedNodeList.add(nodeId);
                }
            }
            highLightedNodeList.addAll(redNodeCollect);
            BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinitionId);
            CustomDefaultProcessDiagramGenerator diagramGenerator = new CustomDefaultProcessDiagramGenerator();
            inputStream = diagramGenerator.generateDiagram(bpmnModel, "png", highLightedNodeList,
                highLightedFlows, "宋体", "宋体", "宋体",
                null, 1.0, true);
            // 响应相关图片
            response.setContentType("image/png");

            byte[] bytes = IOUtils.toByteArray(inputStream);
            ServletOutputStream outputStream = response.getOutputStream();
            outputStream.write(bytes);
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * @Description: 查询正在运行的流程实例
     * @param: req
     * @return: com.ruoyi.common.core.page.TableDataInfo<com.ruoyi.workflow.domain.vo.ProcessInstRunningVo>
     * @Author: gssong
     * @Date: 2021/10/16
     */
    @Override
    public TableDataInfo<ProcessInstRunningVo> getProcessInstRunningByPage(ProcessInstRunningREQ req) {
        ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery();
        if (StringUtils.isNotBlank(req.getName())) {
            query.processInstanceNameLikeIgnoreCase(req.getName());
        }
        if (StringUtils.isNotBlank(req.getStartUserId())) {
            query.startedBy(req.getStartUserId());
        }
        List<ProcessInstance> processInstances = query.listPage(req.getFirstResult(), req.getPageSize());
        List<ProcessInstRunningVo> processInstRunningVoList = new ArrayList<>();
        long total = query.count();
        for (ProcessInstance pi : processInstances) {
            ProcessInstRunningVo processInstRunningVo = new ProcessInstRunningVo();
            BeanUtils.copyProperties(pi, processInstRunningVo);
            SysUser sysUser = iUserService.selectUserById(Long.valueOf(pi.getStartUserId()));
            if (ObjectUtil.isNotEmpty(sysUser)) {
                processInstRunningVo.setStartUserNickName(sysUser.getNickName());
            }
            processInstRunningVo.setIsSuspended(pi.isSuspended() ? "挂起" : "激活");
            // 查询当前实例的当前任务
            List<Task> taskList = taskService.createTaskQuery().processInstanceId(pi.getProcessInstanceId()).list();
            //办理人
            String currTaskInfo = "";
            //办理人id
            String currTaskInfoId = "";
            //办理人集合
            List<String> nickNameList = null;
            for (Task task : taskList.stream().filter(e -> StringUtils.isBlank(e.getParentTaskId())).collect(Collectors.toList())) {
                if (StringUtils.isNotBlank(task.getAssignee())) {
                    String[] split = task.getAssignee().split(",");
                    List<Long> userIds = new ArrayList<>();
                    for (String userId : split) {
                        userIds.add(Long.valueOf(userId));
                    }
                    //办理人
                    List<SysUser> sysUsers = iUserService.selectListUserByIds(userIds);
                    if (CollectionUtil.isNotEmpty(sysUsers)) {
                        nickNameList = sysUsers.stream().map(SysUser::getNickName).collect(Collectors.toList());
                    }
                }


                currTaskInfo += "任务名【" + task.getName() + "】，办理人【" + StringUtils.join(nickNameList, ",") + "】";
                currTaskInfoId += task.getAssignee();
            }
            processInstRunningVo.setCurrTaskInfo(currTaskInfo);
            processInstRunningVo.setCurrTaskInfoId(currTaskInfoId);
            processInstRunningVoList.add(processInstRunningVo);
        }
        List<ProcessInstRunningVo> list = null;
        if (CollectionUtil.isNotEmpty(processInstRunningVoList)) {
            List<String> processInstanceIds = processInstRunningVoList.stream().map(ProcessInstRunningVo::getProcessInstanceId).collect(Collectors.toList());
            List<ActBusinessStatus> businessStatusList = iActBusinessStatusService.getInfoByProcessInstIds(processInstanceIds);
            processInstRunningVoList.forEach(e -> {
                ActBusinessStatus actBusinessStatus = businessStatusList.stream().filter(t -> t.getProcessInstanceId().equals(e.getProcessInstanceId())).findFirst().orElse(null);
                if (ObjectUtil.isNotEmpty(actBusinessStatus)) {
                    e.setActBusinessStatus(actBusinessStatus);
                }
            });
            list = processInstRunningVoList.stream().sorted(Comparator.comparing(ProcessInstRunningVo::getStartTime).reversed()).collect(Collectors.toList());
        }
        return new TableDataInfo<>(list, total);
    }

    /**
     * @Description: 挂起或激活流程实例
     * @param: data
     * @return: void
     * @Author: gssong
     * @Date: 2021/10/16
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProcInstState(Map<String, Object> data) {
        String processInstId = data.get("processInstId").toString();
        String reason = data.get("reason").toString();
        // 1. 查询指定流程实例的数据
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
            .processInstanceId(processInstId)
            .singleResult();
        // 2. 判断当前流程实例的状态
        if (processInstance.isSuspended()) {
            // 如果是已挂起，则更新为激活状态
            runtimeService.activateProcessInstanceById(processInstId);
        } else {
            // 如果是已激活，则更新为挂起状态
            runtimeService.suspendProcessInstanceById(processInstId);
        }
        ActBusinessStatus businessStatus = iActBusinessStatusService.getInfoByProcessInstId(processInstId);
        if (ObjectUtil.isEmpty(businessStatus)) {
            throw new ServiceException("当前流程异常，未生成act_business_status对象");
        }
        businessStatus.setSuspendedReason(reason);
        iActBusinessStatusService.updateById(businessStatus);
    }

    /**
     * @Description: 作废流程实例，不会删除历史记录
     * @param: processInstId
     * @return: boolean
     * @Author: gssong
     * @Date: 2021/10/16
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteRuntimeProcessInst(String processInstId) {
        try {
            //1.查询流程实例
            ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(processInstId).singleResult();
            //2.删除流程实例
            List<Task> list = taskService.createTaskQuery().processInstanceId(processInstId).list();
            List<Task> subTasks = list.stream().filter(e -> StringUtils.isNotBlank(e.getParentTaskId())).collect(Collectors.toList());
            if (CollectionUtil.isNotEmpty(subTasks)) {
                subTasks.forEach(e -> taskService.deleteTask(e.getId()));
            }
            runtimeService.deleteProcessInstance(processInstId, LoginHelper.getUserId() + "作废了当前流程申请");
            //3. 更新业务状态
            return iActBusinessStatusService.updateState(processInstance.getBusinessKey(), BusinessStatusEnum.INVALID);
        } catch (Exception e) {
            throw new ServiceException(e.getMessage());
        }
    }

    /**
     * @Description: 运行中的实例 删除程实例，删除历史记录，删除业务与流程关联信息
     * @param: processInstId
     * @return: boolean
     * @Author: gssong
     * @Date: 2021/10/16
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteRuntimeProcessAndHisInst(String processInstId) {
        try {
            //1.查询流程实例
            ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(processInstId).singleResult();
            //2.删除流程实例
            List<Task> list = taskService.createTaskQuery().processInstanceId(processInstId).list();
            List<Task> subTasks = list.stream().filter(e -> StringUtils.isNotBlank(e.getParentTaskId())).collect(Collectors.toList());
            if (CollectionUtil.isNotEmpty(subTasks)) {
                subTasks.forEach(e -> taskService.deleteTask(e.getId()));
            }
            runtimeService.deleteProcessInstance(processInstId, LoginHelper.getUserId() + "删除了当前流程申请");
            //3.删除历史记录
            HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstId).singleResult();
            if (ObjectUtil.isNotEmpty(historicProcessInstance)) {
                historyService.deleteHistoricProcessInstance(processInstId);
            }
            //4.删除业务状态
            iActBusinessStatusService.deleteState(processInstance.getBusinessKey());
            //5.删除保存的任务节点
            return iActTaskNodeService.deleteByInstanceId(processInstId);
        } catch (Exception e) {
            throw new ServiceException(e.getMessage());
        }
    }

    /**
     * @Description: 已完成的实例 删除程实例，删除历史记录，删除业务与流程关联信息
     * @param: processInstId
     * @return: boolean
     * @Author: gssong
     * @Date: 2021/10/16
     */
    @Override
    public boolean deleteFinishProcessAndHisInst(String processInstId) {
        try {
            //1.查询流程实例
            HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(processInstId).singleResult();
            //2.删除历史记录
            historyService.deleteHistoricProcessInstance(processInstId);
            //3.删除业务状态
            iActBusinessStatusService.deleteState(historicProcessInstance.getBusinessKey());
            //4.删除保存的任务节点
            return iActTaskNodeService.deleteByInstanceId(processInstId);
        } catch (Exception e) {
            throw new ServiceException(e.getMessage());
        }
    }

    /**
     * @Description: 查询已结束的流程实例
     * @param: req
     * @return: com.ruoyi.common.core.page.TableDataInfo<com.ruoyi.workflow.domain.vo.ProcessInstFinishVo>
     * @Author: gssong
     * @Date: 2021/10/23
     */
    @Override
    public TableDataInfo<ProcessInstFinishVo> getProcessInstFinishByPage(ProcessInstFinishREQ req) {
        HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery()
            .finished() // 已结束的
            .orderByProcessInstanceEndTime().desc();
        ;
        if (StringUtils.isNotEmpty(req.getName())) {
            query.processInstanceNameLikeIgnoreCase(req.getName());
        }
        if (StringUtils.isNotEmpty(req.getStartUserId())) {
            query.startedBy(req.getStartUserId());
        }
        List<HistoricProcessInstance> list = query.listPage(req.getFirstResult(), req.getPageSize());
        long total = query.count();
        List<ProcessInstFinishVo> processInstFinishVoList = new ArrayList<>();
        for (HistoricProcessInstance hpi : list) {
            ProcessInstFinishVo processInstFinishVo = new ProcessInstFinishVo();
            BeanUtils.copyProperties(hpi, processInstFinishVo);
            SysUser sysUser = iUserService.selectUserById(Long.valueOf(hpi.getStartUserId()));
            if (ObjectUtil.isNotEmpty(sysUser)) {
                processInstFinishVo.setStartUserNickName(sysUser.getNickName());
            }
            //业务状态
            ActBusinessStatus businessKey = iActBusinessStatusService.getInfoByBusinessKey(hpi.getBusinessKey());
            if (ObjectUtil.isNotNull(businessKey) && ObjectUtil.isNotEmpty(BusinessStatusEnum.getEumByStatus(businessKey.getStatus()))) {
                processInstFinishVo.setStatus(BusinessStatusEnum.getEumByStatus(businessKey.getStatus()).getDesc());
            }
            processInstFinishVoList.add(processInstFinishVo);
        }
        return new TableDataInfo<>(processInstFinishVoList, total);
    }

    @Override
    public String getProcessInstanceId(String businessKey) {
        String processInstanceId;
        ActBusinessStatus infoByBusinessKey = iActBusinessStatusService.getInfoByBusinessKey(businessKey);
        if (ObjectUtil.isNotEmpty(infoByBusinessKey) && (infoByBusinessKey.getStatus().equals(BusinessStatusEnum.FINISH.getStatus())||infoByBusinessKey.getStatus().equals(BusinessStatusEnum.INVALID.getStatus()))) {
            HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery().processInstanceBusinessKey(businessKey).singleResult();
            processInstanceId = ObjectUtil.isNotEmpty(historicProcessInstance) ? historicProcessInstance.getId() : "";
        } else {
            ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceBusinessKey(businessKey).singleResult();
            processInstanceId = ObjectUtil.isNotEmpty(processInstance) ? processInstance.getProcessInstanceId() : "";
        }
        return processInstanceId;
    }

    /**
     * @Description: 撤销申请
     * @param: processInstId
     * @return: boolean
     * @author: gssong
     * @Date: 2022/1/21
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelProcessApply(String processInstId) {

        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstId).startedBy(LoginHelper.getUserId().toString()).singleResult();
        if (ObjectUtil.isNull(processInstance)) {
            throw new ServiceException("流程不是该审批人提交,撤销失败!");
        }
        //校验流程状态
        ActBusinessStatus actBusinessStatus = iActBusinessStatusService.getInfoByBusinessKey(processInstance.getBusinessKey());
        if (ObjectUtil.isEmpty(actBusinessStatus)) {
            throw new ServiceException("流程异常");
        }
        BusinessStatusEnum.checkCancel(actBusinessStatus.getStatus());
        List<ActTaskNode> listActTaskNode = iActTaskNodeService.getListByInstanceId(processInstId);
        if (CollectionUtil.isEmpty(listActTaskNode)) {
            throw new ServiceException("未查询到撤回节点信息");
        }
        ActTaskNode actTaskNode = listActTaskNode.stream().filter(e -> e.getOrderNo() == 0).findFirst().orElse(null);
        if (ObjectUtil.isNull(actTaskNode)) {
            throw new ServiceException("未查询到撤回节点信息");
        }
        List<Task> taskList = taskService.createTaskQuery().processInstanceId(processInstId).list();
        String processInstanceId = taskList.get(0).getProcessInstanceId();
        for (Task task : taskList) {
            if (task.isSuspended()) {
                throw new ServiceException("【" + task.getName() + "】任务已被挂起");
            }
            taskService.addComment(task.getId(), processInstanceId, "申请人撤销申请");
        }
        try {
            runtimeService.createChangeActivityStateBuilder().processInstanceId(processInstanceId)
                .moveActivityIdsToSingleActivityId(taskList.stream().map(Task::getTaskDefinitionKey).collect(Collectors.toList()), actTaskNode.getNodeId())
                .changeState();
            List<Task> newTaskList = taskService.createTaskQuery().processInstanceId(processInstanceId).list();
            //处理并行会签环节重复节点
            if (CollectionUtil.isNotEmpty(newTaskList) && newTaskList.size() > 0) {
                List<Task> taskCollect = newTaskList.stream().filter(e -> e.getTaskDefinitionKey().equals(actTaskNode.getNodeId())).collect(Collectors.toList());
                if (taskCollect.size() > 1) {
                    taskCollect.remove(0);
                    taskCollect.forEach(workFlowUtils::deleteRuntimeTask);
                }
            }
            List<Task> cancelTaskList = taskService.createTaskQuery().processInstanceId(processInstanceId).list();
            if (CollectionUtil.isNotEmpty(cancelTaskList)) {
                for (Task task : cancelTaskList) {
                    taskService.setAssignee(task.getId(), LoginHelper.getUserId().toString());
                }
                iActTaskNodeService.deleteByInstanceId(processInstId);
            }
            return iActBusinessStatusService.updateState(processInstance.getBusinessKey(), BusinessStatusEnum.CANCEL);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceException("撤销失败:" + e.getMessage());
        }
    }
}
