package com.ruoyi.workflow.controller;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.annotation.RepeatSubmit;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.workflow.domain.bo.ModelAdd;
import com.ruoyi.workflow.domain.bo.ModelREQ;
import com.ruoyi.workflow.service.IModelService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.repository.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;

@Validated
@Api(value = "模型控制器", tags = {"模型控制器"})
@RestController
@RequestMapping("/workflow/model")
public class ModelController extends BaseController {

    private static Logger logger = LoggerFactory.getLogger(ModelController.class);

    @Autowired
    private IModelService iModelService;

    @Autowired
    private RepositoryService repositoryService;


    /**
     * @Description:  保存模型
     * @param: data
     * @return: void
     * @author: gssong
     * @Date: 2022/5/22 13:47
     */
    @PutMapping
    @ApiOperation("保存模型")
    @Log(title = "保存模型", businessType = BusinessType.INSERT)
    @RepeatSubmit
    public R<Void> saveModelXml(@RequestBody Map<String,String> data) {
        return iModelService.saveModelXml(data);
    }

    /**
     * @Description: 查询模型信息
     * @param: modelId 模型id
     * @return: com.ruoyi.common.core.domain.R<java.lang.String>
     * @author: gssong
     * @Date: 2022/5/22 13:42
     */
    @GetMapping("/getInfo/{modelId}/xml")
    public R<Map<String,Object>> getEditorXml(@PathVariable String modelId) {
        return iModelService.getEditorXml(modelId);
    }

    /**
     * @Description: 查询模型列表
     * @param: modelReq 请求参数
     * @return: com.ruoyi.common.core.page.TableDataInfo<org.flowable.engine.repository.Model>
     * @Author: gssong
     * @Date: 2021/10/3
     */
    @ApiOperation("查询模型列表")
    @GetMapping("/list")
    public TableDataInfo<Model> getByPage(ModelREQ modelReq) {
        return iModelService.getByPage(modelReq);
    }

    /**
     * @Description: 新建模型
     * @param: modelAdd
     * @return: com.ruoyi.common.core.domain.R<org.flowable.engine.repository.Model>
     * @Author: gssong
     * @Date: 2021/10/3
     */
    @ApiOperation("新建模型")
    @Log(title = "模型管理", businessType = BusinessType.INSERT)
    @RepeatSubmit
    @PostMapping
    public R<Model> add(@RequestBody ModelAdd modelAdd) {
        try {
            return iModelService.add(modelAdd);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            logger.error("创建模型失败：" + e.getMessage());
            return R.fail("创建模型失败",null);
        }
    }

    /**
     * @Description: 通过流程定义模型id部署流程定义
     * @param: id 模型id
     * @return: com.ruoyi.common.core.domain.R<java.lang.Void>
     * @Author: gssong
     * @Date: 2021/10/3
     */
    @ApiOperation("通过流程定义模型id部署流程定义")
    @Log(title = "模型管理", businessType = BusinessType.INSERT)
    @RepeatSubmit
    @PostMapping("/deploy/{id}")
    public R<Void> deploy(@PathVariable("id") String id) {
        try {
            return iModelService.deploy(id);
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("流程部署失败:", e.getMessage());
            return R.fail("流程部署失败");
        }
    }

    /**
     * @Description: 删除流程定义模型
     * @param: id 模型id
     * @return: com.ruoyi.common.core.domain.R<java.lang.Void>
     * @Author: gssong
     * @Date: 2021/10/3
     */
    @ApiOperation("删除流程定义模型")
    @Log(title = "模型管理", businessType = BusinessType.DELETE)
    @RepeatSubmit
    @DeleteMapping("/{id}")
    public R<Void> add(@PathVariable String id) {
        repositoryService.deleteModel(id);
        return R.ok();
    }

    /**
     * @Description: 导出流程定义模型zip压缩包
     * @param: modelId
     * @param: response
     * @return: void
     * @Author: gssong
     * @Date: 2021/10/7
     */
    @ApiOperation("导出流程定义模型zip压缩包")
    @GetMapping("/export/zip/{modelId}")
    public void exportZip(@PathVariable String modelId,
                          HttpServletResponse response) {
        iModelService.exportZip(modelId, response);
    }

    /**
     * @Description: 将流程定义转换为模型
     * @param: processDefinitionId 流程定义id
     * @return: com.ruoyi.common.core.domain.R<java.lang.Void>
     * @Author: gssong
     * @Date: 2021/11/6
     */
    @ApiOperation("将流程定义转换为模型")
    @GetMapping("/convertToModel/{processDefinitionId}")
    public R<Void> convertToModel(@PathVariable String processDefinitionId){
        Boolean convertToModel = iModelService.convertToModel(processDefinitionId);
        return convertToModel==true?R.ok():R.fail();
    }

}
