package com.ruoyi.workflow.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.workflow.common.constant.ActConstant;
import com.ruoyi.workflow.flowable.factory.WorkflowService;
import com.ruoyi.workflow.domain.bo.ModelAdd;
import com.ruoyi.workflow.domain.bo.ModelREQ;
import com.ruoyi.workflow.service.IModelService;
import com.ruoyi.workflow.utils.WorkFlowUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.flowable.bpmn.converter.BpmnXMLConverter;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.cmmn.image.exception.FlowableImageException;
import org.flowable.common.engine.api.FlowableException;
import org.flowable.common.engine.impl.util.io.InputStreamSource;
import org.flowable.editor.constants.ModelDataJsonConstants;
import org.flowable.engine.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.*;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
/**
 * @program: ruoyi-vue-plus
 * @description: 模型业务层
 * @author: gssong
 * @created: 2021/10/17 15:59
 */
@Service
@Slf4j
public class ModelServiceImpl extends WorkflowService implements IModelService {

    @Autowired
    private WorkFlowUtils workFlowUtils;

    /**
     * @Description: 保存模型
     * @param: data
     * @return: com.ruoyi.common.core.domain.R<java.lang.Void>
     * @author: gssong
     * @Date: 2022/5/22 13:51
     */
    @Override
    public R<Void> saveModelXml(Map<String, String> data) {
        try {
            String modelId = data.get("modelId");
            String xml = data.get("xml");
            String svg = data.get("svg");
            Model model = repositoryService.getModel(modelId);
            InputStream in = new ByteArrayInputStream(StrUtil.utf8Bytes(xml));
            InputStreamSource xmlSource = new InputStreamSource(in);
            BpmnXMLConverter bpmnXMLConverter = new BpmnXMLConverter();
            BpmnModel bpmnModel = bpmnXMLConverter.convertToBpmnModel(xmlSource, true, false, "UTF-8");
            byte[] bytes = new BpmnXMLConverter().convertToXML(bpmnModel);
            repositoryService.addModelEditorSource(model.getId(), bytes);

            InputStream svgStream = new ByteArrayInputStream(StrUtil.utf8Bytes(svg));
            TranscoderInput input = new TranscoderInput(svgStream);

            PNGTranscoder transcoder = new PNGTranscoder();
            // Setup output
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            TranscoderOutput output = new TranscoderOutput(outStream);

            // Do the transformation
            transcoder.transcode(input, output);
            final byte[] result = outStream.toByteArray();
            repositoryService.addModelEditorSourceExtra(model.getId(), result);
            outStream.close();
            //repositoryService.addModelEditorSourceExtra(model.getId(),);
            return R.ok();
        } catch (Exception e) {
            throw new FlowableException("Error saving model", e);
        }
    }

    /**
     * @Description: 查询模型信息
     * @param: modelId
     * @return: com.ruoyi.common.core.domain.R<java.lang.String>
     * @author: gssong
     * @Date: 2022/5/22 13:54
     */
    @Override
    public R<Map<String,Object>> getEditorXml(String modelId) {
        Map<String, Object> map = new HashMap<>();
        Model model = repositoryService.getModel(modelId);
        if (model != null) {
            try {
                byte[] modelEditorSource = repositoryService.getModelEditorSource(model.getId());
                map.put("xml",StrUtil.utf8Str(modelEditorSource));
                map.put("model",model);
                return R.ok("操作成功",map);
            } catch (Exception e) {
                throw new FlowableImageException(e.getMessage());
            }
        }
        return R.fail();
    }

    /**
     * @Description: 查询模型列表
     * @param: modelReq 请求参数
     * @return: com.ruoyi.common.core.page.TableDataInfo<org.flowable.engine.repository.Model>
     * @Author: gssong
     * @Date: 2021/10/3
     */
    @Override
    public TableDataInfo<Model> getByPage(ModelREQ modelReq) {
        ModelQuery query = repositoryService.createModelQuery();
        if (StringUtils.isNotEmpty(modelReq.getName())) {
            query.modelNameLike("%" + modelReq.getName() + "%");
        }
        if (StringUtils.isNotEmpty(modelReq.getKey())) {
            query.modelKey(modelReq.getKey());
        }
        query.orderByLastUpdateTime().desc();
        //创建时间降序排列
        query.orderByCreateTime().desc();
        // 分页查询
        List<Model> modelList = query.listPage(modelReq.getFirstResult(), modelReq.getPageSize());
        if (CollectionUtil.isNotEmpty(modelList)) {
            modelList.forEach(e -> {
                boolean isNull = JSONUtil.isNull(JSONUtil.parseObj(e.getMetaInfo()).get(ModelDataJsonConstants.MODEL_DESCRIPTION));
                if (!isNull) {
                    e.setMetaInfo((String) JSONUtil.parseObj(e.getMetaInfo()).get(ModelDataJsonConstants.MODEL_DESCRIPTION));
                } else {
                    e.setMetaInfo("");
                }
            });
        }
        // 总记录数
        long total = query.count();
        return new TableDataInfo(modelList, total);
    }

    /**
     * @Description: 新建模型
     * @param: modelAdd
     * @return: com.ruoyi.common.core.domain.R<org.flowable.engine.repository.Model>
     * @Author: gssong
     * @Date: 2021/10/3
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<Model> add(ModelAdd modelAdd) throws UnsupportedEncodingException {
        int version = 0;

        Model checkModel = repositoryService.createModelQuery().modelKey(modelAdd.getKey()).singleResult();
        if(ObjectUtil.isNotNull(checkModel)){
            return R.fail("模型KEY已存在",null);
        }
        // 1. 初始空的模型
        Model model = repositoryService.newModel();
        model.setName(modelAdd.getName());
        model.setKey(modelAdd.getKey());
        model.setVersion(version);

        // 封装模型json对象
        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put(ModelDataJsonConstants.MODEL_NAME, modelAdd.getName());
        objectNode.put(ModelDataJsonConstants.MODEL_REVISION, version);
        objectNode.put(ModelDataJsonConstants.MODEL_DESCRIPTION, modelAdd.getDescription());
        model.setMetaInfo(objectNode.toString());
        // 保存初始化的模型基本信息数据
        repositoryService.saveModel(model);

        // 封装模型对象基础数据json串
        // {"id":"canvas","resourceId":"canvas","stencilset":{"namespace":"http://b3mn.org/stencilset/bpmn2.0#"},"properties":{"process_id":"未定义"}}
        ObjectNode editorNode = objectMapper.createObjectNode();
        ObjectNode stencilSetNode = objectMapper.createObjectNode();
        stencilSetNode.put("namespace", ActConstant.NAMESPACE);
        editorNode.replace("stencilset", stencilSetNode);
        // 标识key
        ObjectNode propertiesNode = objectMapper.createObjectNode();
        propertiesNode.put("process_id", modelAdd.getKey());
        propertiesNode.put("name", modelAdd.getName());
        editorNode.replace("properties", propertiesNode);

        repositoryService.addModelEditorSource(model.getId(), StrUtil.utf8Bytes(editorNode.toString()));
        return R.ok(model);
    }

    /**
     * @Description: 通过流程定义模型id部署流程定义
     * @param: id 模型id
     * @return: com.ruoyi.common.core.domain.R
     * @Author: gssong
     * @Date: 2021/10/3
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<Void> deploy(String id) throws IOException {
        //1.查询流程定义模型xml
        byte[] xmlBytes = repositoryService.getModelEditorSource(id);
        if (xmlBytes == null) {
            return R.fail("模型数据为空，请先设计流程定义模型，再进行部署");
        }
        // 2. 查询流程定义模型的图片
        byte[] pngBytes = repositoryService.getModelEditorSourceExtra(id);

        // 查询模型的基本信息
        Model model = repositoryService.getModel(id);
        // xml资源的名称 ，对应act_ge_bytearray表中的name_字段
        String processName = model.getName() + ".bpmn20.xml";
        // 图片资源名称，对应act_ge_bytearray表中的name_字段
        String pngName = model.getName() + "." + model.getKey() + ".png";

        // 3. 调用部署相关的api方法进行部署流程定义
        Deployment deployment = repositoryService.createDeployment()
            .name(model.getName()) // 部署名称
            .key(model.getKey()) // 部署标识key
            .addString(processName, StrUtil.utf8Str(xmlBytes)) // bpmn20.xml资源
            .addBytes(pngName, pngBytes) // png资源
            .deploy();

        // 更新 部署id 到流程定义模型数据表中
        model.setDeploymentId(deployment.getId());
        repositoryService.saveModel(model);
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
    @Override
    public void exportZip(String modelId, HttpServletResponse response) {
        ZipOutputStream zipos = null;
        try {
            zipos = new ZipOutputStream(response.getOutputStream());
            // 压缩包文件名
            String zipName = "模型不存在";
            //1.查询模型基本信息
            Model model = repositoryService.getModel(modelId);
            if (ObjectUtil.isNotNull(model)) {
                // 2. 查询流程定义模型的json字节码
                byte[] xmlBytes = repositoryService.getModelEditorSource(modelId);
                // 2.1 将json字节码转换为xml字节码
               // byte[] xmlBytes = workFlowUtils.bpmnJsonXmlBytes(bpmnJsonBytes);
                if (xmlBytes == null) {
                    zipName = "模型数据为空-请先设计流程定义模型，再导出";
                } else {
                    // 压缩包文件名
                    zipName = model.getName() + "." + model.getKey() + ".zip";
                    // 将xml添加到压缩包中(指定xml文件名：请假流程.bpmn20.xml
                    zipos.putNextEntry(new ZipEntry(model.getName() + ".bpmn20.xml"));
                    zipos.write(xmlBytes);
                    //3.查询流程定义模型图片字节码
                    byte[] pngBytes = repositoryService.getModelEditorSourceExtra(modelId);
                    if (pngBytes != null) {
                        zipos.putNextEntry(new ZipEntry(model.getName() + "." + model.getKey() + ".png"));
                        zipos.write(pngBytes);
                    }
                }
            }
            response.setHeader("Content-Disposition",
                "attachment; filename=" + URLEncoder.encode(zipName, ActConstant.UTF_8) + ".zip");
            // 刷出响应流
            response.flushBuffer();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (zipos != null) {
                try {
                    zipos.closeEntry();
                    zipos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * @Description: 将流程定义转换为模型
     * @param: processDefinitionId 流程定义id
     * @return: java.lang.Boolean
     * @Author: gssong
     * @Date: 2021/11/6
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean convertToModel(String processDefinitionId) {
        ProcessDefinition pd = repositoryService.createProcessDefinitionQuery()
            .processDefinitionId(processDefinitionId).singleResult();
        InputStream bpmnStream = repositoryService.getResourceAsStream(pd.getDeploymentId(), pd.getResourceName());
        Model model = repositoryService.createModelQuery().modelKey(pd.getKey()).singleResult();
            try {
                XMLInputFactory xif = XMLInputFactory.newInstance();
                InputStreamReader in = new InputStreamReader(bpmnStream, ActConstant.UTF_8);
                XMLStreamReader xtr = xif.createXMLStreamReader(in);
                BpmnModel bpmnModel = new BpmnXMLConverter().convertToBpmnModel(xtr);
                BpmnXMLConverter converter = new BpmnXMLConverter();
                byte[] xmlBytes = converter.convertToXML(bpmnModel);
                if(ObjectUtil.isNotNull(model)){
                    repositoryService.addModelEditorSource(model.getId(), xmlBytes);
                    InputStream inputStream = repositoryService.getResourceAsStream(pd.getDeploymentId(), pd.getDiagramResourceName());
                    if(inputStream!=null){
                        repositoryService.addModelEditorSourceExtra(model.getId(),IOUtils.toByteArray(inputStream));
                    }
                    return true;
                }else{
                    Model modelData = repositoryService.newModel();
                    modelData.setKey(pd.getKey());
                    modelData.setName(pd.getName());

                    ObjectNode modelObjectNode = new ObjectMapper().createObjectNode();
                    modelObjectNode.put(ModelDataJsonConstants.MODEL_NAME, pd.getName());
                    modelObjectNode.put(ModelDataJsonConstants.MODEL_REVISION, modelData.getVersion());
                    modelObjectNode.put(ModelDataJsonConstants.MODEL_DESCRIPTION, pd.getDescription());
                    modelData.setMetaInfo(modelObjectNode.toString());
                    repositoryService.saveModel(modelData);
                    repositoryService.addModelEditorSource(modelData.getId(), xmlBytes);
                    InputStream inputStream = repositoryService.getResourceAsStream(pd.getDeploymentId(), pd.getDiagramResourceName());
                    if(inputStream!=null){
                        repositoryService.addModelEditorSourceExtra(modelData.getId(),IOUtils.toByteArray(inputStream));
                    }
                    return true;
                }

            } catch (Exception e) {
                e.printStackTrace();
                log.error("转化流程为模型失败:", e.getMessage());
                return false;
            }
    }
}
