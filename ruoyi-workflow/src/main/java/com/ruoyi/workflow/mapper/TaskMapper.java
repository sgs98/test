package com.ruoyi.workflow.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 任务Mapper接口
 *
 * @author gssong
 * @date 2022-07-24
 */
public interface TaskMapper {

    @Update("update act_hi_comment set message_=#{comment},full_msg_=#{comment} where id_ = #{commentId}")
    void editComment(@Param("commentId") String commentId, @Param("comment") String comment);

    @Delete("delete from act_hi_actinst  where task_id_ = #{taskId}")
    void deleteActHiActInstByTaskId(@Param("taskId") String taskId);

    @Delete("delete from act_ru_variable  where execution_id_ = #{executionId}")
    void deleteRuVariableExecutionId(@Param("executionId") String executionId);

    @Delete("delete from act_ru_execution  where id_ = #{executionId} and rev_ = #{revision}")
    void deleteRuExecutionByExecutionId(@Param("executionId") String executionId, @Param("revision")int revision);

    @Delete("delete from act_ru_task a left join act_ru_execution b  on a.EXECUTION_ID_ = b.id_  where  a.id_ = #{taskId}")
    void deleteRuTaskId(@Param("taskId") String taskId);
}
