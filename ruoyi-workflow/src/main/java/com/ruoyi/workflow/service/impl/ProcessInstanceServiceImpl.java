package com.ruoyi.workflow.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.helper.LoginHelper;
import com.ruoyi.workflow.activiti.cmd.DeleteTaskCmd;
import com.ruoyi.workflow.activiti.config.CustomProcessDiagramGenerator;
import com.ruoyi.workflow.activiti.config.ICustomProcessDiagramGenerator;
import com.ruoyi.workflow.activiti.config.WorkflowConstants;
import com.ruoyi.workflow.common.enums.BusinessStatusEnum;
import com.ruoyi.workflow.domain.ActBusinessStatus;
import com.ruoyi.workflow.domain.ActTaskNode;
import com.ruoyi.workflow.domain.bo.ProcessInstFinishREQ;
import com.ruoyi.workflow.domain.bo.ProcessInstRunningREQ;
import com.ruoyi.workflow.domain.bo.StartREQ;
import com.ruoyi.workflow.domain.vo.ActHistoryInfoVo;
import com.ruoyi.workflow.domain.vo.ProcessInstFinishVo;
import com.ruoyi.workflow.domain.vo.ProcessInstRunningVo;
import com.ruoyi.workflow.activiti.factory.WorkflowService;
import com.ruoyi.workflow.service.*;
import lombok.RequiredArgsConstructor;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FlowNode;
import org.activiti.bpmn.model.ParallelGateway;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.engine.ManagementService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricProcessInstanceQuery;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.runtime.ProcessInstanceQuery;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
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
 * @description: ?????????????????????
 * @author: gssong
 * @created: 2021/10/10 18:38
 */
@Service
@RequiredArgsConstructor
public class ProcessInstanceServiceImpl extends WorkflowService implements IProcessInstanceService {
    private final IActBusinessStatusService iActBusinessStatusService;
    private final IUserService iUserService;
    private final IActTaskNodeService iActTaskNodeService;
    private final ProcessEngine processEngine;
    private final ManagementService managementService;


    /**
     * @Description: ?????????????????????????????????
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
            throw new ServiceException("????????????????????????????????????ID");
        }
        // ???????????????????????????????????????
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
        // ???????????????
        Authentication.setAuthenticatedUserId(LoginHelper.getUserId().toString());
        // ????????????????????????????????????
        Map<String, Object> variables = startReq.getVariables();
        ProcessInstance pi = runtimeService.startProcessInstanceByKey(startReq.getProcessKey(), startReq.getBusinessKey(), variables);
        // ????????????????????? ?????? ??????????????????
        runtimeService.setProcessInstanceName(pi.getProcessInstanceId(), pi.getProcessDefinitionName());
        // ?????????????????????
        List<Task> taskList = taskService.createTaskQuery().processInstanceId(pi.getId()).list();
        if (taskList.size() > 1) {
            throw new ServiceException("???????????????????????????????????????????????????");
        }
        taskService.setAssignee(taskList.get(0).getId(), LoginHelper.getUserId().toString());
        taskService.setVariable(taskList.get(0).getId(),"processInstanceId", pi.getProcessInstanceId());
        // ??????????????????
        iActBusinessStatusService.updateState(startReq.getBusinessKey(), BusinessStatusEnum.DRAFT, taskList.get(0).getProcessInstanceId(), startReq.getClassFullName());

        map.put("processInstanceId", pi.getProcessInstanceId());
        map.put("taskId", taskList.get(0).getId());
        return map;
    }

    /**
     * @Description: ??????????????????id????????????????????????
     * @param: processInstanceId
     * @return: java.util.List<com.ruoyi.workflow.domain.vo.ActHistoryInfoVo>
     * @Author: gssong
     * @Date: 2021/10/16
     */
    @Override
    public List<ActHistoryInfoVo> getHistoryInfoList(String processInstanceId) {
        //????????????????????????
        List<HistoricTaskInstance> list = historyService.createHistoricTaskInstanceQuery().processInstanceId(processInstanceId)
            .orderByHistoricTaskInstanceEndTime().desc().list();
        list.stream().sorted(Comparator.comparing(HistoricTaskInstance::getEndTime, Comparator.nullsFirst(Date::compareTo))).collect(Collectors.toList());
        List<ActHistoryInfoVo> actHistoryInfoVoList = new ArrayList<>();
        for (HistoricTaskInstance historicTaskInstance : list) {
            ActHistoryInfoVo actHistoryInfoVo = new ActHistoryInfoVo();
            BeanUtils.copyProperties(historicTaskInstance, actHistoryInfoVo);
            actHistoryInfoVo.setStatus(actHistoryInfoVo.getEndTime() == null ? "?????????" : "?????????");
            List<Comment> taskComments = taskService.getTaskComments(historicTaskInstance.getId());
            if(CollectionUtil.isNotEmpty(taskComments)){
                actHistoryInfoVo.setCommentId(taskComments.get(0).getId());
                String message = taskComments.stream()
                    .map(Comment::getFullMessage).collect(Collectors.joining("???"));
                if (StringUtils.isNotBlank(message)) {
                    actHistoryInfoVo.setComment(message);
                }
            }
            actHistoryInfoVoList.add(actHistoryInfoVo);
        }
        //??????????????????
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
        //?????????
        List<ActHistoryInfoVo> waitingTask = actHistoryInfoVoList.stream().filter(e -> e.getEndTime() == null).collect(Collectors.toList());
        waitingTask.forEach(e -> {
            if (StringUtils.isNotBlank(e.getOwner())) {
                SysUser sysUser = iUserService.selectUserById(Long.valueOf(e.getOwner()));
                if (ObjectUtil.isNotEmpty(sysUser)) {
                    e.setNickName(sysUser.getNickName());
                }
            }
        });
        //?????????
        List<ActHistoryInfoVo> finishTask = actHistoryInfoVoList.stream().filter(e -> e.getEndTime() != null).collect(Collectors.toList());
        collect.addAll(waitingTask);
        collect.addAll(finishTask);
        return collect;
    }

    /**
     * @Description: ??????????????????id?????????????????????
     * @param: processInstId
     * @param: response
     * @return: void
     * @Author: gssong
     * @Date: 2021/10/16
     */
    @Override
    public void getHistoryProcessImage(String processInstanceId, HttpServletResponse response) {
        // ?????????????????????
        response.setHeader("Pragma", "no-cache");
        response.addHeader("Cache-Control", "must-revalidate");
        response.addHeader("Cache-Control", "no-cache");
        response.addHeader("Cache-Control", "no-store");
        response.setDateHeader("Expires", 0);
        InputStream inputStream = null;
        try {
            // 1. ??????????????????????????????
            HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(processInstanceId).singleResult();
            // 2. ??????????????????????????????????????????????????????????????????
            List<HistoricActivityInstance> historicActivityInstanceList = historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(processInstanceId)
                .orderByHistoricActivityInstanceStartTime()
                .desc().list();

            // 3. ???????????????????????????id ( ?????????
            List<String> histExecutedActivityIdList = new ArrayList<>();
            for (HistoricActivityInstance activityInstance : historicActivityInstanceList) {
                histExecutedActivityIdList.add(activityInstance.getActivityId());
            }
            BpmnModel bpmnModel = repositoryService.getBpmnModel(historicProcessInstance.getProcessDefinitionId());
            // ???????????????????????????
            CustomProcessDiagramGenerator generator = new CustomProcessDiagramGenerator();
            // ??????????????????id
            List<String> highLightedFlows = generator.getHighLightedFlows(bpmnModel, historicActivityInstanceList);
            // 4. ????????????????????? ????????????
            Set<String> executedActivityIdList = runtimeService.createExecutionQuery().processInstanceId(processInstanceId).list()
                .stream().map(e->e.getActivityId()).collect(Collectors.toSet());

            ICustomProcessDiagramGenerator diagramGenerator = (ICustomProcessDiagramGenerator) processEngine.getProcessEngineConfiguration().getProcessDiagramGenerator();
            inputStream = diagramGenerator.generateDiagram(bpmnModel, "png", histExecutedActivityIdList,
                highLightedFlows, "??????", "??????", "??????", null, 1.0, new Color[] { WorkflowConstants.COLOR_NORMAL, WorkflowConstants.COLOR_CURRENT }, executedActivityIdList);

            // ??????????????????
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
     * @Description: ?????????????????????????????????
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
            processInstRunningVo.setIsSuspended(pi.isSuspended() ? "??????" : "??????");
            // ?????????????????????????????????
            List<Task> taskList = taskService.createTaskQuery().processInstanceId(pi.getProcessInstanceId()).list();
            //?????????
            String currTaskInfo = "";
            //?????????id
            String currTaskInfoId = "";
            //???????????????
            List<String> nickNameList = null;
            for (Task task : taskList.stream().filter(e -> StringUtils.isBlank(e.getParentTaskId())).collect(Collectors.toList())) {
                if (StringUtils.isNotBlank(task.getAssignee())) {
                    String[] split = task.getAssignee().split(",");
                    List<Long> userIds = new ArrayList<>();
                    for (String userId : split) {
                        userIds.add(Long.valueOf(userId));
                    }
                    //?????????
                    List<SysUser> sysUsers = iUserService.selectListUserByIds(userIds);
                    if (CollectionUtil.isNotEmpty(sysUsers)) {
                        nickNameList = sysUsers.stream().map(SysUser::getNickName).collect(Collectors.toList());
                    }
                }


                currTaskInfo += "????????????" + task.getName() + "??????????????????" + StringUtils.join(nickNameList, ",") + "???";
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
     * @Description: ???????????????????????????
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
        // 1. ?????????????????????????????????
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
            .processInstanceId(processInstId)
            .singleResult();
        // 2. ?????????????????????????????????
        if (processInstance.isSuspended()) {
            // ?????????????????????????????????????????????
            runtimeService.activateProcessInstanceById(processInstId);
        } else {
            // ?????????????????????????????????????????????
            runtimeService.suspendProcessInstanceById(processInstId);
        }
        ActBusinessStatus businessStatus = iActBusinessStatusService.getInfoByProcessInstId(processInstId);
        if (ObjectUtil.isEmpty(businessStatus)) {
            throw new ServiceException("??????????????????????????????act_business_status??????");
        }
        businessStatus.setSuspendedReason(reason);
        iActBusinessStatusService.updateById(businessStatus);
    }

    /**
     * @Description: ?????????????????????????????????????????????
     * @param: processInstId
     * @return: boolean
     * @Author: gssong
     * @Date: 2021/10/16
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteRuntimeProcessInst(String processInstId) {
        try {
            //1.??????????????????
            ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(processInstId).singleResult();
            //2.??????????????????
            List<Task> list = taskService.createTaskQuery().processInstanceId(processInstId).list();
            List<Task> subTasks = list.stream().filter(e -> StringUtils.isNotBlank(e.getParentTaskId())).collect(Collectors.toList());
            if (CollectionUtil.isNotEmpty(subTasks)) {
                subTasks.forEach(e -> taskService.deleteTask(e.getId()));
            }
            runtimeService.deleteProcessInstance(processInstId, LoginHelper.getUserId() + "???????????????????????????");
            //3. ??????????????????
            return iActBusinessStatusService.updateState(processInstance.getBusinessKey(), BusinessStatusEnum.INVALID);
        } catch (Exception e) {
            throw new ServiceException(e.getMessage());
        }
    }

    /**
     * @Description: ?????????????????? ????????????????????????????????????????????????????????????????????????
     * @param: processInstId
     * @return: boolean
     * @Author: gssong
     * @Date: 2021/10/16
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteRuntimeProcessAndHisInst(String processInstId) {
        try {
            //1.??????????????????
            ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(processInstId).singleResult();
            //2.??????????????????
            List<Task> list = taskService.createTaskQuery().processInstanceId(processInstId).list();
            List<Task> subTasks = list.stream().filter(e -> StringUtils.isNotBlank(e.getParentTaskId())).collect(Collectors.toList());
            if (CollectionUtil.isNotEmpty(subTasks)) {
                subTasks.forEach(e -> taskService.deleteTask(e.getId()));
            }
            runtimeService.deleteProcessInstance(processInstId, LoginHelper.getUserId() + "???????????????????????????");
            //3.??????????????????
            HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstId).singleResult();
            if (ObjectUtil.isNotEmpty(historicProcessInstance)) {
                historyService.deleteHistoricProcessInstance(processInstId);
            }
            //4.??????????????????
            iActBusinessStatusService.deleteState(processInstance.getBusinessKey());
            //5.???????????????????????????
            return iActTaskNodeService.deleteByInstanceId(processInstId);
        } catch (Exception e) {
            throw new ServiceException(e.getMessage());
        }
    }

    /**
     * @Description: ?????????????????? ????????????????????????????????????????????????????????????????????????
     * @param: processInstId
     * @return: boolean
     * @Author: gssong
     * @Date: 2021/10/16
     */
    @Override
    public boolean deleteFinishProcessAndHisInst(String processInstId) {
        try {
            //1.??????????????????
            HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(processInstId).singleResult();
            //2.??????????????????
            historyService.deleteHistoricProcessInstance(processInstId);
            //3.??????????????????
            iActBusinessStatusService.deleteState(historicProcessInstance.getBusinessKey());
            //4.???????????????????????????
            return iActTaskNodeService.deleteByInstanceId(processInstId);
        } catch (Exception e) {
            throw new ServiceException(e.getMessage());
        }
    }

    /**
     * @Description: ??????????????????????????????
     * @param: req
     * @return: com.ruoyi.common.core.page.TableDataInfo<com.ruoyi.workflow.domain.vo.ProcessInstFinishVo>
     * @Author: gssong
     * @Date: 2021/10/23
     */
    @Override
    public TableDataInfo<ProcessInstFinishVo> getProcessInstFinishByPage(ProcessInstFinishREQ req) {
        HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery()
            .finished() // ????????????
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
            //????????????
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
     * @Description: ????????????
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
            throw new ServiceException("??????????????????????????????,????????????!");
        }
        //??????????????????
        ActBusinessStatus actBusinessStatus = iActBusinessStatusService.getInfoByBusinessKey(processInstance.getBusinessKey());
        if (ObjectUtil.isEmpty(actBusinessStatus)) {
            throw new ServiceException("????????????");
        }
        BusinessStatusEnum.checkCancel(actBusinessStatus.getStatus());
        List<ActTaskNode> listActTaskNode = iActTaskNodeService.getListByInstanceId(processInstId);
        if (CollectionUtil.isEmpty(listActTaskNode)) {
            throw new ServiceException("??????????????????????????????");
        }
        ActTaskNode actTaskNode = listActTaskNode.stream().filter(e -> e.getOrderNo() == 0).findFirst().orElse(null);
        if (ObjectUtil.isNull(actTaskNode)) {
            throw new ServiceException("??????????????????????????????");
        }
        try {
            List<Task> list = taskService.createTaskQuery().processInstanceId(processInstId).list().stream().filter(e->StringUtils.isBlank(e.getParentTaskId())).collect(Collectors.toList());
            for (Task task : list) {
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
                FlowNode targetFlowNode = (FlowNode) bpmnModel.getFlowElement(actTaskNode.getNodeId());
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
                curFlowNode.setOutgoingFlows(targetSequenceFlow);
                // 10. ????????????????????????????????????????????????????????????????????????
                // ?????????????????????????????????
                taskService.addComment(task.getId(), task.getProcessInstanceId(),"?????????????????????");
                taskService.setAssignee(task.getId(), LoginHelper.getUserId().toString());
                // ????????????????????????????????????????????????????????????????????????????????????
                taskService.complete(task.getId());
                // 11. ?????????????????????????????????????????????????????????????????????
                curFlowNode.setOutgoingFlows(oriSequenceFlows);
            }
            // 12. ???????????????????????????????????????
            List<Task> newTaskList = taskService.createTaskQuery().processInstanceId(processInstId).list().stream().filter(e->StringUtils.isBlank(e.getParentTaskId())).collect(Collectors.toList());
            if(newTaskList.size()>1){
                newTaskList.remove(0);
                for (Task task : newTaskList) {
                    DeleteTaskCmd deleteTaskCmd = new DeleteTaskCmd(task.getId());
                    managementService.executeCommand(deleteTaskCmd);
                }
            }
            List<Task> newTasks = taskService.createTaskQuery().processInstanceId(processInstId).list().stream().filter(e->StringUtils.isBlank(e.getParentTaskId())).collect(Collectors.toList());
            for (Task newTask : newTasks) {
                Map<String, Object> variables =new HashMap<>();
                taskService.setVariables(newTask.getId(),variables);
                taskService.setAssignee(newTask.getId(), LoginHelper.getUserId().toString());
                iActTaskNodeService.deleteBackTaskNode(newTask.getProcessInstanceId(),newTask.getTaskDefinitionKey());
            }

            // 13. ??????????????????
            return iActBusinessStatusService.updateState(processInstance.getBusinessKey(), BusinessStatusEnum.CANCEL);
        }catch (Exception e){
            e.printStackTrace();
            throw new ServiceException("????????????:" + e.getMessage());
        }
    }
}
