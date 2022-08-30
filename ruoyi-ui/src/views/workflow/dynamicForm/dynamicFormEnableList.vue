<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="表单key" prop="formKey">
        <el-input
          v-model="queryParams.formKey"
          placeholder="请输入表单key"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="表单名称" prop="formName">
        <el-input
          v-model="queryParams.formName"
          placeholder="请输入表单名称"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>
    <div class="container-box" v-loading="loading" v-if="dynamicFormList && dynamicFormList.length>0">
      <el-row :gutter="12" style="margin-left: 0px; margin-right: 0px;">
        <el-col :span="4" v-for="(item,index) in dynamicFormList" :key="index">
          <el-card shadow="hover" class="card-item">
            <div slot="header" class="clearfix">
               <el-tooltip class="item" effect="dark" :content="'表单KEY:'+item.formKey" placement="top-start">
                 <span>{{item.formName}}</span>
              </el-tooltip>
              <span style="float: right;" @click="handleApply(item)"><el-link type="primary">提交申请</el-link></span>
            </div>
            <div>
              {{item.formRemark}}
            </div>
          </el-card>
        </el-col>
      </el-row>
      <div class="pagination-box" v-show="total>0" v-loading="loading">
        <pagination
          v-show="total>0"
          :total="total"
          :page.sync="queryParams.pageNum"
          :limit.sync="queryParams.pageSize"
          @pagination="getList"
        />
      </div>
    </div>
    <el-empty class="el-empty-icon" v-else description="暂无数据"></el-empty>
    <!-- 动态表单编辑 -->
    <el-dialog :visible.sync="dynamicFormViewVisible" class="self_dialog"
        v-if="dynamicFormViewVisible" 
        center :close-on-click-modal="false" 
        append-to-body>
       <dynamicFormEdit ref="formViewer" 
       :buildData="formData.formDesignerText" 
       @draftForm="draftProcessForm"
       @submitForm="submitProcessForm"
       />
    </el-dialog>
    <!-- 工作流 -->
    <verify ref="verifyRef" @callSubmit="callSubmit" :taskId="taskId" :taskVariables="taskVariables" :sendMessage="sendMessage"></verify>
  </div>
</template>

<script>
import { listDynamicFormEnable } from "@/api/workflow/dynamicForm";
import dynamicFormEdit from '@/views/workflow/businessForm/dynamicFormEdit'
import { addBusinessForm} from "@/api/workflow/businessForm";
import api from '@/api/workflow/task'
import verify from "@/components/Process/Verify";
export default {
  name: "ProcessForm",
  components:{
    dynamicFormEdit,
    verify
  },
  data() {
    return {
      // 按钮loading
      buttonLoading: false,
      // 遮罩层
      loading: true,
      // 非单个禁用
      single: true,
      // 非多个禁用
      multiple: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 流程单表格数据
      dynamicFormList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 30,
        formKey: undefined,
        formName: undefined,
      },
      // 表单显示隐藏
      dynamicFormViewVisible: false,
      // 表单数据
      formData: {},
      // 任务id
      taskId: '',
      // 流程变量
      taskVariables: {},
      // 站内信
      sendMessage: {}
    };
  },
  created() {
    this.getList();
  },
  methods: {
    
    /** 查询流程单列表 */
    getList() {
      this.loading = true;
      listDynamicFormEnable(this.queryParams).then(response => {
        this.dynamicFormList = response.rows;
        this.total = response.total;
        this.loading = false;
      });
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getList();
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.resetForm("queryForm");
      this.handleQuery();
    },
    //打开表单
    handleApply(row){
       this.formData = row
       this.dynamicFormViewVisible = true
    },
    //暂存
    draftProcessForm(formText,formValue){
      let data = {
        formId: this.formData.id,
        formKey: this.formData.formKey,
        formName: this.formData.formName,
        formText:formText,
        formValue: formValue
      }
      this.buttonLoading = true;
        addBusinessForm(data).then(response => {
          this.$modal.msgSuccess("保存成功");
          this.$router.push('/workflow/from/businessForm')
          this.dynamicFormViewVisible = false
          this.getList();
        }).finally(() => {
          this.buttonLoading = false;
        });
    },
    //提交
    submitProcessForm(){
      addBusinessForm(data).then(response => {
        this.submitFormApply(response.data)
      }).finally(() => {
        this.buttonLoading = false;
      });
    },
    //提交流程
    submitFormApply(entity){
        if(!entity.actProcessDefForm){
          this.$modal.msgError("未绑定流程");
          return
        }
        let variables = {
            entity: entity
        }
        const data = {
            processKey: entity.actProcessDefForm.processDefinitionKey, // key
            businessKey: entity.id, // 业务id
            variables: variables,
            classFullName: entity.formKey
        }
        // 启动流程
        processAip.startProcessApply(data).then(response => {
            this.taskId = response.data.taskId;
            // 查询下一节点的变量
            this.taskVariables = {
                entity: entity,  // 变量
            }
            this.$refs.verifyRef.visible = true
            this.$refs.verifyRef.reset()
        })
    },
    // 提交成功回调
    callSubmit(){
      this.dynamicFormViewVisible = false;
      this.getList();
    },
  }
};
</script>
<style scoped>
  .card-item{
    cursor: pointer;
    height: 100px !important;
    position: relative;
  }
  .clearfix{
    font-size: 14px;
    font-family: '幼圆' !important;
  }
  .container-box{
    height: calc(100vh-120px);
    overflow: hidden;
  }
  .pagination-box{
    position: fixed;
    bottom: 20px;
    right: 20px;
  }
  .el-col{
    padding: 10px;
  }
  .self_dialog {
      display: flex;
      justify-content: center;
      align-items: Center;
      overflow: hidden;
  }
  .self_dialog /deep/ .el-dialog {
      margin: 0 auto !important;
      height: 90%;
      width: 70%;
      overflow: hidden;
      display: flex;
      flex-direction: column;
      padding-left: 15px;
  }
  .self_dialog /deep/ .el-dialog .el-dialog__body {
      padding-top: 5px !important;
      overflow: hidden;
      overflow-y: auto;
      margin-bottom: 40px;
  }
  .self_dialog /deep/ .el-dialog .el-dialog__footer {
      left: 40%;
      bottom: 0;
      position: absolute;
  }
</style>
