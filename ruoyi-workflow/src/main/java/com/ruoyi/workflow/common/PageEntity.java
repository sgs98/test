package com.ruoyi.workflow.common;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @program: ruoyi-vue-plus
 * @description: 分页参数
 * @author: gssong
 * @created: 2022-02-26
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class PageEntity {
    /**
     * 分页大小
     */
    @ApiModelProperty("分页大小")
    private Integer pageSize;

    /**
     * 当前页数
     */
    @ApiModelProperty("当前页数")
    private Integer pageNum;

    /**
     * 页码
     * @return
     */
    public Integer getFirstResult() {
        return (pageNum - 1) * pageSize;
    }
}
