package com.ruoyi.workflow.domain.bo;

import com.ruoyi.workflow.common.PageEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @program: ruoyi-vue-plus
 * @description: 模型请求对象
 * @author: gssong
 * @created: 2022-02-26
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel("流程模型请求对象")
public class ModelBo extends PageEntity implements Serializable {

    private static final long serialVersionUID=1L;
    /**
     * 模型名称
     */
    @ApiModelProperty("模型名称")
    private String name;

    /**
     * 模型标识key
     */
    @ApiModelProperty("模型标识key")
    private String key;
}
