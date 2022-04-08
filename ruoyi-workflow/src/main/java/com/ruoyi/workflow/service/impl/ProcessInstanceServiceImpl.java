package com.ruoyi.workflow.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.helper.LoginHelper;
import com.ruoyi.workflow.activiti.config.CustomProcessDiagramGenerator;
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
import com.ruoyi.workflow.factory.WorkflowService;
import com.ruoyi.workflow.service.*;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.FlowNode;
import org.flowable.bpmn.model.ParallelGateway;
import org.flowable.bpmn.model.SequenceFlow;
import org.flowable.common.engine.impl.identity.Authentication;
import org.flowable.engine.ProcessEngine;
import org.flowable.engine.ProcessEngineConfiguration;
import org.flowable.engine.ProcessEngines;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.history.HistoricProcessInstanceQuery;
import org.flowable.engine.runtime.Execution;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.engine.runtime.ProcessInstanceQuery;
import org.flowable.engine.task.Comment;
import org.flowable.image.ProcessDiagramGenerator;
import org.flowable.task.api.Task;
import org.flowable.task.api.TaskQuery;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
    protected static final Logger logger = LoggerFactory.getLogger(ProcessInstanceServiceImpl.class);

    private final IActBusinessStatusService iActBusinessStatusService;
    private final IUserService iUserService;
    private final IActTaskNodeService iActTaskNodeService;
    private final ProcessEngine processEngine;



    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String,Object> startWorkFlow(StartREQ startReq) {
        Map<String,Object> map = new HashMap<>();
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
            if(ObjectUtil.isNotEmpty(info)){
                BusinessStatusEnum.checkStatus(info.getStatus());
            }
            map.put("processInstanceId",taskResult.get(0).getProcessInstanceId());
            map.put("taskId",taskResult.get(0).getId());
            return map;
        }
        // 设置启动人
        Authentication.setAuthenticatedUserId(LoginHelper.getUserId().toString());
        // 启动流程实例（提交申请）
        Map<String, Object> variables = startReq.getVariables();
        variables.put("status",BusinessStatusEnum.DRAFT.getStatus());
        ProcessInstance pi = runtimeService.startProcessInstanceByKey(startReq.getProcessKey(), startReq.getBusinessKey(),variables);
        // 将流程定义名称 作为 流程实例名称
        runtimeService.setProcessInstanceName(pi.getProcessInstanceId(), pi.getProcessDefinitionName());
        // 申请人执行流程
        List<Task> taskList = taskService.createTaskQuery().processInstanceId(pi.getId()).list();
        for (Task task : taskList) {
            taskService.setAssignee(task.getId(),LoginHelper.getUserId().toString());
        }

        //查询下一个任务
        List<Task> nextList = taskService.createTaskQuery().processInstanceId(pi.getId()).list();
        for (Task task : nextList) {
            // 更新业务状态
            iActBusinessStatusService.updateState(startReq.getBusinessKey(), BusinessStatusEnum.DRAFT, task.getProcessInstanceId(), startReq.getClassFullName());
        }
        map.put("processInstanceId",pi.getProcessInstanceId());
        map.put("taskId",nextList.get(0).getId());
        return map;
    }


    @Override
    public List<ActHistoryInfoVo> getHistoryInfoList(String processInstanceId) {
        //查询任务办理记录
        List<HistoricTaskInstance> list = historyService.createHistoricTaskInstanceQuery().processInstanceId(processInstanceId)
            .orderByHistoricTaskInstanceEndTime().desc().list();
        list.stream().sorted(Comparator.comparing(l -> l.getEndTime(), Comparator.nullsFirst(Date::compareTo))).collect(Collectors.toList());
        List<ActHistoryInfoVo> actHistoryInfoVoList = new ArrayList<>();
        for (HistoricTaskInstance historicTaskInstance : list) {
            ActHistoryInfoVo actHistoryInfoVo = new ActHistoryInfoVo();
            BeanUtils.copyProperties(historicTaskInstance, actHistoryInfoVo);
            actHistoryInfoVo.setStatus(actHistoryInfoVo.getEndTime() == null ? "待处理" : "已处理");
            String message = null;
            if(StringUtils.isEmpty(message)) {
                List<Comment> taskComments = taskService.getTaskComments(historicTaskInstance.getId());
                message = taskComments.stream()
                    .map(m -> m.getFullMessage()).collect(Collectors.joining("。"));
            }
            actHistoryInfoVo.setComment(message);
            actHistoryInfoVoList.add(actHistoryInfoVo);
        }
        //翻译人员名称
        if(CollectionUtil.isNotEmpty(actHistoryInfoVoList)){
            List<String> collect = actHistoryInfoVoList.stream().filter(e->StringUtils.isNotBlank(e.getAssignee())).map(ActHistoryInfoVo::getAssignee).collect(Collectors.toList());
            List<Long> userIds = new ArrayList<>();
            for (String userId : collect) {
                userIds.add(Long.valueOf(userId));
            }
            List<SysUser> sysUsers = iUserService.selectListUserByIds(userIds);
            for (ActHistoryInfoVo actHistoryInfoVo : actHistoryInfoVoList) {
                SysUser sysUser = sysUsers.stream().filter(e -> e.getUserId().toString().equals(actHistoryInfoVo.getAssignee())).findFirst().orElse(null);
                if(ObjectUtil.isNotEmpty(sysUser)){
                    actHistoryInfoVo.setNickName(sysUser.getNickName());
                }
            }

        }
        List<ActHistoryInfoVo> collect = new ArrayList<>();
        //待办理
        List<ActHistoryInfoVo> waitingTask = actHistoryInfoVoList.stream().filter(e -> e.getEndTime() == null).collect(Collectors.toList());
        waitingTask.forEach(e->{
            if(StringUtils.isNotBlank(e.getOwner())){
                SysUser sysUser = iUserService.selectUserById(Long.valueOf(e.getOwner()));
                if(ObjectUtil.isNotEmpty(sysUser)){
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




    public ProcessEngine processEngine() {
        return ProcessEngines.getDefaultProcessEngine();
    }
    /**
     * 生成流程图
     *
     * @param processId 流程部署id
     */
    @SneakyThrows
    @Override
    public void getHistoryProcessImage(String processInstanceId, HttpServletResponse response) {
        HistoricProcessInstance hpi = historyService.createHistoricProcessInstanceQuery()
            .processInstanceId(processInstanceId).singleResult();
        //流程走完的不显示图
       /* if (pi == null) {
            return;
        }*/

        List<HistoricActivityInstance> historyProcess = getHistoryProcess(processInstanceId);
        List<String> activityIds = new ArrayList<>();
        List<String> flows = new ArrayList<>();
        //获取流程图
        BpmnModel bpmnModel = repositoryService.getBpmnModel(hpi.getProcessDefinitionId());
        for (HistoricActivityInstance hi : historyProcess) {
            String activityType = hi.getActivityType();
            if (activityType.equals("sequenceFlow") || activityType.equals("exclusiveGateway")) {
                flows.add(hi.getActivityId());
            } else if (activityType.equals("userTask") || activityType.equals("startEvent")) {
                activityIds.add(hi.getActivityId());
            }
        }
        List<Task> tasks = taskService.createTaskQuery().processInstanceId(processInstanceId).list();
        for (Task task : tasks) {
            activityIds.add(task.getTaskDefinitionKey());
        }
        ProcessEngineConfiguration engConf = processEngine().getProcessEngineConfiguration();
        //定义流程画布生成器
        ProcessDiagramGenerator processDiagramGenerator = engConf.getProcessDiagramGenerator();
        InputStream in = processDiagramGenerator.generateDiagram(bpmnModel, "png", activityIds, flows, engConf.getActivityFontName(), engConf.getLabelFontName(), engConf.getAnnotationFontName(), engConf.getClassLoader(), 1.0, true);
        OutputStream out = null;
        byte[] buf = new byte[1024];
        int legth = 0;
        try {
            out = response.getOutputStream();
            while ((legth = in.read(buf)) != -1) {
                out.write(buf, 0, legth);
            }
        } finally {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
        }


    }

    /**
     * 任务历史
     *
     * @param processId 部署id
     */
    public List<HistoricActivityInstance> getHistoryProcess(String processId) {
        List<HistoricActivityInstance> list = historyService // 历史相关Service
            .createHistoricActivityInstanceQuery() // 创建历史活动实例查询
            .processInstanceId(processId) // 执行流程实例id
            .finished()
            .list();
        return list;
    }

    /**
     * 查询正在运行的流程实例
     * @param req
     * @return
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
            processInstRunningVo.setIsSuspended(pi.isSuspended() == true ? "挂起" : "激活");
            // 查询当前实例的当前任务
            List<Task> taskList = taskService.createTaskQuery().processInstanceId(pi.getProcessInstanceId()).list();
            //办理人
            String currTaskInfo = "";
            //办理人id
            String currTaskInfoId = "";
            //办理人集合
            List<String> nickNameList = null;
            for (Task task : taskList) {
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
            list = processInstRunningVoList.stream().sorted(Comparator.comparing(ProcessInstRunningVo::getStartTime).reversed()).collect(Collectors.toList());
        }
        return new TableDataInfo(list, total);
    }

    /**
     * 挂起或激活流程实例
     * @param processInstId
     */
    @Override
    public void updateProcInstState(String processInstId) {
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
    }

    /**
     * 作废流程实例，不会删除历史记录
     * @param processInstId
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteRuntimeProcessInst(String processInstId) {
        //1.查询流程实例
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
            .processInstanceId(processInstId).singleResult();
        //2.删除流程实例
        runtimeService.deleteProcessInstance(processInstId, LoginHelper.getUserId() + "作废了当前流程申请");
        //3. 更新业务状态
        return iActBusinessStatusService.updateState(processInstance.getBusinessKey(), BusinessStatusEnum.INVALID);
    }

    /**
     * 删除程实例，删除历史记录，删除业务与流程关联信息
     * @param processInstId
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteRuntimeProcessAndHisInst(String processInstId) {
        //1.查询流程实例
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
            .processInstanceId(processInstId).singleResult();
        //2.删除流程实例
        runtimeService.deleteProcessInstance(processInstId, LoginHelper.getUserId() + "删除了当前流程申请");
        //3.删除历史记录
        HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstId).singleResult();
        if(ObjectUtil.isNotEmpty(historicProcessInstance)){
            historyService.deleteHistoricProcessInstance(processInstId);
        }
        //4.删除业务状态
        iActBusinessStatusService.deleteState(processInstance.getBusinessKey());
        //5.删除保存的任务节点
        return iActTaskNodeService.deleteByInstanceId(processInstId);
    }

    /**
     * 删除已完成的实例，删除历史记录，删除业务与流程关联信息
     * @param processInstId
     * @return
     */
    @Override
    public boolean deleteFinishProcessAndHisInst(String processInstId) {
        //1.查询流程实例
        HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery()
            .processInstanceId(processInstId).singleResult();
        //2.删除历史记录
        historyService.deleteHistoricProcessInstance(processInstId);
        //3.删除业务状态
        iActBusinessStatusService.deleteState(historicProcessInstance.getBusinessKey());
        //4.删除保存的任务节点
        return iActTaskNodeService.deleteByInstanceId(processInstId);
    }

    /**
     * 查询已结束的流程实例
     * @param req
     * @return
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
            if (ObjectUtil.isNotNull(businessKey)) {
                processInstFinishVo.setStatus(BusinessStatusEnum.getEumByStatus(businessKey.getStatus()).getDesc());
            }
            processInstFinishVoList.add(processInstFinishVo);
        }
        return new TableDataInfo(processInstFinishVoList, total);
    }

    @Override
    public String getProcessInstanceId(String businessKey) {
        String processInstanceId = null;
        ActBusinessStatus infoByBusinessKey = iActBusinessStatusService.getInfoByBusinessKey(businessKey);
        if(ObjectUtil.isNotEmpty(infoByBusinessKey)&&infoByBusinessKey.getStatus().equals(BusinessStatusEnum.FINISH.getStatus())){
            HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery().processInstanceBusinessKey(businessKey).singleResult();
            processInstanceId = ObjectUtil.isNotEmpty(historicProcessInstance)?historicProcessInstance.getId():"";
        }else{
            ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceBusinessKey(businessKey).singleResult();
            processInstanceId = ObjectUtil.isNotEmpty(processInstance)?processInstance.getProcessInstanceId():"";
        }
        return processInstanceId;
    }

    /**
     * 撤销申请
     * @param processInstId
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelProcessApply(String processInstId) {
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstId).startedBy(LoginHelper.getUserId().toString()).singleResult();
        if(ObjectUtil.isNull(processInstance)){
            throw new ServiceException("流程不是该审批人提交,撤销失败!");
        }
        List<Task> list = taskService.createTaskQuery().processInstanceId(processInstId).list();
        if(list.size()>1){
            throw new ServiceException("当前任务有多人审批不可撤销");
        }
        Task task = list.get(0);
        if (task.isSuspended()) {
            throw new ServiceException("当前任务已被挂起");
        }
        // 1. 获取流程模型实例 BpmnModel
        BpmnModel bpmnModel = repositoryService.getBpmnModel(task.getProcessDefinitionId());
        // 2.当前节点信息
        FlowNode curFlowNode = (FlowNode) bpmnModel.getFlowElement(task.getTaskDefinitionKey());
        // 3.获取当前节点原出口连线
        List<SequenceFlow> sequenceFlowList = curFlowNode.getOutgoingFlows();
        // 4. 临时存储当前节点的原出口连线
        List<SequenceFlow> oriSequenceFlows = new ArrayList<>();
        oriSequenceFlows.addAll(sequenceFlowList);
        // 5. 将当前节点的原出口清空
        sequenceFlowList.clear();
        // 6. 获取目标节点信息
        List<ActTaskNode> listActTaskNode = iActTaskNodeService.getListByInstanceId(processInstId);
        if(CollectionUtil.isEmpty(listActTaskNode)){
            throw new ServiceException("未查询到撤回节点信息");
        }
        ActTaskNode actTaskNode = listActTaskNode.stream().filter(e -> e.getOrderNo()==0).findFirst().orElse(null);
        if(ObjectUtil.isNull(actTaskNode)){
            throw new ServiceException("未查询到撤回节点信息");
        }
        FlowNode targetFlowNode = (FlowNode) bpmnModel.getFlowElement(actTaskNode.getNodeId());
        // 7. 获取目标节点的入口连线
        List<SequenceFlow> incomingFlows = targetFlowNode.getIncomingFlows();
        // 8. 存储所有目标出口
        List<SequenceFlow> targetSequenceFlow = new ArrayList<>();
        for (SequenceFlow incomingFlow : incomingFlows) {
            // 找到入口连线的源头（获取目标节点的父节点）
            FlowNode source = (FlowNode) incomingFlow.getSourceFlowElement();
            List<SequenceFlow> sequenceFlows;
            if (source instanceof ParallelGateway) {
                // 并行网关: 获取目标节点的父节点（并行网关）的所有出口，
                sequenceFlows = source.getOutgoingFlows();
            } else {
                // 其他类型父节点, 则获取目标节点的入口连续
                sequenceFlows = targetFlowNode.getIncomingFlows();
            }
            targetSequenceFlow.addAll(sequenceFlows);
        }
        // 9. 将当前节点的出口设置为新节点
        curFlowNode.setOutgoingFlows(targetSequenceFlow);
        // 10. 完成当前任务，流程就会流向目标节点创建新目标任务
        // 当前任务，完成当前任务
        taskService.addComment(task.getId(), task.getProcessInstanceId(),"申请人撤销申请");
        // 完成任务，就会进行驳回到目标节点，产生目标节点的任务数据
        Map<String, Object> variables =new HashMap<>();
        variables.put("status",BusinessStatusEnum.CANCEL.getStatus());
        taskService.setVariables(task.getId(),variables);
        taskService.complete(task.getId());
        // 11. 完成驳回功能后，将当前节点的原出口方向进行恢复
        curFlowNode.setOutgoingFlows(oriSequenceFlows);
        // 12. 查询目标任务节点历史办理人
        List<Task> newTaskList = taskService.createTaskQuery().processInstanceId(task.getProcessInstanceId()).list();
        for (Task newTask : newTaskList) {
            taskService.setAssignee(newTask.getId(), LoginHelper.getUserId().toString());
        }
        // 13. 删除驳回后的流程节点
        boolean b = iActBusinessStatusService.updateState(processInstance.getBusinessKey(), BusinessStatusEnum.CANCEL);
        return b;
    }
    /**
     * 获取已流经的流程线，需要高亮显示高亮流程已发生流转的线id集合
     *
     * @param bpmnModel
     * @param historicActivityInstanceList
     * @return
     */
    public List<String> getHighLightedFlows(BpmnModel bpmnModel,
                                            List<HistoricActivityInstance> historicActivityInstanceList) {
        // 已流经的流程线，需要高亮显示
        List<String> highLightedFlowIdList = new ArrayList<>();
        // 全部活动节点
        List<FlowNode> allHistoricActivityNodeList = new ArrayList<>();
        // 已完成的历史活动节点
        List<HistoricActivityInstance> finishedActivityInstanceList = new ArrayList<>();

        for (HistoricActivityInstance historicActivityInstance : historicActivityInstanceList) {
            // 获取流程节点
            FlowNode flowNode = (FlowNode) bpmnModel.getMainProcess().getFlowElement(historicActivityInstance
                .getActivityId(), true);
            allHistoricActivityNodeList.add(flowNode);
            // 结束时间不为空，当前节点则已经完成
            if (historicActivityInstance.getEndTime() != null) {
                finishedActivityInstanceList.add(historicActivityInstance);
            }
        }

        FlowNode currentFlowNode = null;
        FlowNode targetFlowNode = null;
        HistoricActivityInstance currentActivityInstance;
        // 遍历已完成的活动实例，从每个实例的outgoingFlows中找到已执行的
        for (int k = 0; k < finishedActivityInstanceList.size(); k++) {
            currentActivityInstance = finishedActivityInstanceList.get(k);
            // 获得当前活动对应的节点信息及outgoingFlows信息
            currentFlowNode = (FlowNode) bpmnModel.getMainProcess().getFlowElement(currentActivityInstance
                .getActivityId(), true);
            // 当前节点的所有流出线
            List<SequenceFlow> outgoingFlowList = currentFlowNode.getOutgoingFlows();

            /**
             * 遍历outgoingFlows并找到已流转的 满足如下条件认为已流转：
             * 1.当前节点是并行网关或兼容网关，则通过outgoingFlows能够在历史活动中找到的全部节点均为已流转
             * 2.当前节点是以上两种类型之外的，通过outgoingFlows查找到的时间最早的流转节点视为有效流转
             * (第2点有问题，有过驳回的，会只绘制驳回的流程线，通过走向下一级的流程线没有高亮显示)
             */
            if ("parallelGateway".equals(currentActivityInstance.getActivityType()) || "inclusiveGateway".equals(
                currentActivityInstance.getActivityType())) {
                // 遍历历史活动节点，找到匹配流程目标节点的
                for (SequenceFlow outgoingFlow : outgoingFlowList) {
                    // 获取当前节点流程线对应的下级节点
                    targetFlowNode = (FlowNode) bpmnModel.getMainProcess().getFlowElement(outgoingFlow.getTargetRef(),
                        true);
                    // 如果下级节点包含在所有历史节点中，则将当前节点的流出线高亮显示
                    if (allHistoricActivityNodeList.contains(targetFlowNode)) {
                        highLightedFlowIdList.add(outgoingFlow.getId());
                    }
                }
            } else {
                /**
                 * 2、当前节点不是并行网关或兼容网关
                 * 【已解决-问题】如果当前节点有驳回功能，驳回到申请节点，
                 * 则因为申请节点在历史节点中，导致当前节点驳回到申请节点的流程线被高亮显示，但实际并没有进行驳回操作
                 */
                List<Map<String, Object>> tempMapList = new ArrayList<>();
                // 当前节点ID
                String currentActivityId = currentActivityInstance.getActivityId();
                int size = historicActivityInstanceList.size();
                boolean ifStartFind = false;
                boolean ifFinded = false;
                HistoricActivityInstance historicActivityInstance;
                // 循环当前节点的所有流出线
                // 循环所有历史节点
                logger.info("【开始】-匹配当前节点-ActivityId=【{}】需要高亮显示的流出线", currentActivityId);
                logger.info("循环历史节点");
                for (int i = 0; i < historicActivityInstanceList.size(); i++) {
                    // // 如果当前节点流程线对应的下级节点在历史节点中，则该条流程线进行高亮显示（【问题】有驳回流程线时，即使没有进行驳回操作，因为申请节点在历史节点中，也会将驳回流程线高亮显示-_-||）
                    // if (historicActivityInstance.getActivityId().equals(sequenceFlow.getTargetRef())) {
                    // Map<String, Object> map = new HashMap<>();
                    // map.put("highLightedFlowId", sequenceFlow.getId());
                    // map.put("highLightedFlowStartTime", historicActivityInstance.getStartTime().getTime());
                    // tempMapList.add(map);
                    // // highLightedFlowIdList.add(sequenceFlow.getId());
                    // }
                    // 历史节点
                    historicActivityInstance = historicActivityInstanceList.get(i);
                    logger.info("第【{}/{}】个历史节点-ActivityId=[{}]", i + 1, size, historicActivityInstance.getActivityId());
                    // 如果循环历史节点中的id等于当前节点id，从当前历史节点继续先后查找是否有当前节点流程线等于的节点
                    // 历史节点的序号需要大于等于已完成历史节点的序号，防止驳回重审一个节点经过两次是只取第一次的流出线高亮显示，第二次的不显示
                    if (i >= k && historicActivityInstance.getActivityId().equals(currentActivityId)) {
                        logger.info("第[{}]个历史节点和当前节点一致-ActivityId=[{}]", i + 1, historicActivityInstance
                            .getActivityId());
                        ifStartFind = true;
                        // 跳过当前节点继续查找下一个节点
                        continue;
                    }
                    if (ifStartFind) {
                        logger.info("[开始]-循环当前节点-ActivityId=【{}】的所有流出线", currentActivityId);

                        ifFinded = false;
                        for (SequenceFlow sequenceFlow : outgoingFlowList) {
                            // 如果当前节点流程线对应的下级节点在其后面的历史节点中，则该条流程线进行高亮显示
                            // 【问题】
                            logger.info("当前流出线的下级节点=[{}]", sequenceFlow.getTargetRef());
                            if (historicActivityInstance.getActivityId().equals(sequenceFlow.getTargetRef())) {
                                logger.info("当前节点[{}]需高亮显示的流出线=[{}]", currentActivityId, sequenceFlow.getId());
                                highLightedFlowIdList.add(sequenceFlow.getId());
                                // 暂时默认找到离当前节点最近的下一级节点即退出循环，否则有多条流出线时将全部被高亮显示
                                ifFinded = true;
                                break;
                            }
                        }
                        logger.info("[完成]-循环当前节点-ActivityId=【{}】的所有流出线", currentActivityId);
                    }
                    if (ifFinded) {
                        // 暂时默认找到离当前节点最近的下一级节点即退出历史节点循环，否则有多条流出线时将全部被高亮显示
                        break;
                    }
                }
                logger.info("【完成】-匹配当前节点-ActivityId=【{}】需要高亮显示的流出线", currentActivityId);
                // if (!CollectionUtils.isEmpty(tempMapList)) {
                // // 遍历匹配的集合，取得开始时间最早的一个
                // long earliestStamp = 0L;
                // String highLightedFlowId = null;
                // for (Map<String, Object> map : tempMapList) {
                // long highLightedFlowStartTime = Long.valueOf(map.get("highLightedFlowStartTime").toString());
                // if (earliestStamp == 0 || earliestStamp <= highLightedFlowStartTime) {
                // highLightedFlowId = map.get("highLightedFlowId").toString();
                // earliestStamp = highLightedFlowStartTime;
                // }
                // }
                // highLightedFlowIdList.add(highLightedFlowId);
                // }

            }

        }
        return highLightedFlowIdList;
    }

    /**
     * 根据流程实例Id,获取实时流程图片
     *
     * @param procInstId
     * @return
     * @throws Exception
     */
    public byte[] generateImageByProcInstId(String procInstId) throws Exception {
        if (StringUtils.isEmpty(procInstId)) {
            logger.error("[错误]-传入的参数procInstId为空！");
            throw new Exception("[异常]-传入的参数procInstId为空！");
        }
        InputStream imageStream = null;
        try {
            // 通过流程实例ID获取历史流程实例
            HistoricProcessInstance historicProcessInstance = getHistoricProcInst(procInstId);

            // 通过流程实例ID获取流程中已经执行的节点，按照执行先后顺序排序
            List<HistoricActivityInstance> historicActivityInstanceList = getHistoricActivityInstAsc(procInstId);


            // 将已经执行的节点ID放入高亮显示节点集合
            List<String> highLightedActivitiIdList = new ArrayList<>();
            for (HistoricActivityInstance historicActivityInstance : historicActivityInstanceList) {
                highLightedActivitiIdList.add(historicActivityInstance.getActivityId());
                logger.info("已执行的节点[{}-{}-{}-{}]", historicActivityInstance.getId(), historicActivityInstance
                    .getActivityId(), historicActivityInstance.getActivityName(), historicActivityInstance
                    .getAssignee());
            }

            // 通过流程实例ID获取流程中正在执行的节点
            List<Execution> runningActivityInstanceList = getRunningActivityInst(procInstId);
            List<String> runningActivitiIdList = new ArrayList<String>();
            for (Execution execution : runningActivityInstanceList) {
                if (StringUtils.isNotEmpty(execution.getActivityId())) {
                    runningActivitiIdList.add(execution.getActivityId());
                    logger.info("执行中的节点[{}-{}-{}]", execution.getId(), execution.getActivityId(), execution.getName());
                }
            }

            // 通过流程实例ID获取已经完成的历史流程实例
            List<HistoricProcessInstance> historicFinishedProcessInstanceList = getHistoricFinishedProcInst(procInstId);

            // 定义流程画布生成器
            ProcessDiagramGenerator processDiagramGenerator = null;
            // 如果还没完成，流程图高亮颜色为绿色，如果已经完成为红色
            // if (!CollectionUtils.isEmpty(historicFinishedProcessInstanceList)) {
            // // 如果不为空，说明已经完成
            // processDiagramGenerator = processEngineConfiguration.getProcessDiagramGenerator();
            // } else {
            processDiagramGenerator = new CustomProcessDiagramGenerator();
            // }

            // 获取流程定义Model对象
            BpmnModel bpmnModel = repositoryService.getBpmnModel(historicProcessInstance.getProcessDefinitionId());


            // 获取已流经的流程线，需要高亮显示高亮流程已发生流转的线id集合
            List<String> highLightedFlowIds = getHighLightedFlows(bpmnModel, historicActivityInstanceList);

            // 使用默认配置获得流程图表生成器，并生成追踪图片字符流
            imageStream = ((CustomProcessDiagramGenerator) processDiagramGenerator)
                .generateDiagramCustom(bpmnModel, "png",
                    highLightedActivitiIdList, runningActivitiIdList, highLightedFlowIds,
                    "宋体", "微软雅黑", "黑体",
                    null, 2.0);
            // 将InputStream数据流转换为byte[]
            byte[] buffer = new byte[imageStream.available()];
            imageStream.read(buffer);
            return buffer;
        } catch (Exception e) {
            logger.error("通过流程实例ID[{}]获取流程图时出现异常！", e);
            throw new Exception("通过流程实例ID" + procInstId + "获取流程图时出现异常！", e);
        } finally {
            if (imageStream != null) {
                imageStream.close();
            }
        }
    }
    /**
     * 通过流程实例ID获取历史流程实例
     *
     * @param procInstId
     * @return
     */
    public HistoricProcessInstance getHistoricProcInst(String procInstId) {
        return historyService.createHistoricProcessInstanceQuery().processInstanceId(procInstId).singleResult();
    }

    /**
     * 通过流程实例ID获取流程中已经执行的节点，按照执行先后顺序排序
     *
     * @param procInstId
     * @return
     */
    public List<HistoricActivityInstance> getHistoricActivityInstAsc(String procInstId) {
        return historyService.createHistoricActivityInstanceQuery().processInstanceId(procInstId)
            .orderByHistoricActivityInstanceId().asc().list();
    }

    /**
     * 通过流程实例ID获取流程中正在执行的节点
     *
     * @param procInstId
     * @return
     */
    public List<Execution> getRunningActivityInst(String procInstId) {
        return runtimeService.createExecutionQuery().processInstanceId(procInstId).list();
    }

    /**
     * 通过流程实例ID获取已经完成的历史流程实例
     *
     * @param procInstId
     * @return
     */
    public List<HistoricProcessInstance> getHistoricFinishedProcInst(String procInstId) {
        return historyService.createHistoricProcessInstanceQuery().processInstanceId(procInstId).finished().list();
    }
}
