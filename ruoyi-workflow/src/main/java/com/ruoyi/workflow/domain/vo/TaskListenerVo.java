package com.ruoyi.workflow.domain.vo;

import lombok.Data;

/**
 * @program: ruoyi-vue-plus
 * @description: 任务监听对象
 * @author: gssong
 * @created: 2022-06-26
 */
@Data
public class TaskListenerVo {

    /**
     * 事件前后  after,before
     */
    private String eventType;

    /**
     * bean名称
     */
    private String beanName;

}
