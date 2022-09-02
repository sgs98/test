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
      <el-form-item label="单号" prop="applyCode">
        <el-input
          v-model="queryParams.applyCode"
          placeholder="请输入单号"
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
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="el-icon-edit"
          size="mini"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['workflow:businessForm:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-delete"
          size="mini"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['workflow:businessForm:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['workflow:businessForm:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="businessFormList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="单号" align="center" prop="applyCode" />
      <el-table-column label="表单key" align="center" prop="formKey" />
      <el-table-column label="表单名称" align="center" prop="formName" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['workflow:businessForm:edit']"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-view"
            @click="handleView(scope.row)"
          >查看</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['workflow:businessForm:remove']"
          >删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total>0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />
    <!-- 动态表单编辑 -->
    <el-dialog :visible.sync="dynamicFormEditVisible" class="self_dialog"
        v-if="dynamicFormEditVisible" 
        center :close-on-click-modal="false" 
        append-to-body>
       <dynamicFormEdit
        :buildData="form.formText" 
        v-model="form.formValue"
        @draftForm="draftProcessForm(arguments)"
        @submitForm="submitProcessForm(arguments)"
        ref="dynamicFormEditVisible"
       />
    </el-dialog>
    <!-- 动态表单查看 -->
    <el-dialog :visible.sync="dynamicFormViewVisible" 
      class="self_dialog" 
      v-if="dynamicFormViewVisible" 
      :close-on-click-modal="false" 
      append-to-body>
       <dynamicFormView
        :buildData="form.formText" 
        v-model="form.formValue"
       />
    </el-dialog>
    <!-- 工作流 -->
    <verify ref="verifyRef" @callSubmit="callSubmit" :taskId="taskId" :taskVariables="taskVariables" :sendMessage="sendMessage"></verify>
  </div>
</template>

<script>
import { listBusinessForm, getBusinessForm, delBusinessForm, updateBusinessForm } from "@/api/workflow/businessForm";
import dynamicFormEdit from './dynamicFormEdit'
import dynamicFormView from './dynamicFormView'
import verify from "@/components/Process/Verify";
import processApi from "@/api/workflow/processInst";
export default {
  name: "BusinessForm",
  components:{
    dynamicFormEdit,
    dynamicFormView,
    verify
  },
  data() {
    return {
      // 遮罩层
      loading: true,
      // 选中数组
      ids: [],
      // 非单个禁用
      single: true,
      // 非多个禁用
      multiple: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 业务表单表格数据
      businessFormList: [],
      // 弹出层标题
      title: "",
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        formKey: undefined,
        formName: undefined,
        applyCode: undefined
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
      },
      //动态表单编辑
      dynamicFormEditVisible: false,
      //动态表单查看
      dynamicFormViewVisible: false,
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
    /** 查询业务表单列表 */
    getList() {
      this.loading = true;
      listBusinessForm(this.queryParams).then(response => {
        this.businessFormList = response.rows;
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
    // 多选框选中数据
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.id)
      this.single = selection.length!==1
      this.multiple = !selection.length
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.loading = true;
      const id = row.id || this.ids
      getBusinessForm(id).then(response => {
        this.loading = false;
        this.form = response.data;
        this.dynamicFormEditVisible = true;
        this.title = response.data.formName;
      });
    },
    /** 查看按钮操作 */
    handleView(row){
      this.loading = true;
      const id = row.id || this.ids
      getBusinessForm(id).then(response => {
        this.loading = false;
        this.form = response.data;
        this.dynamicFormViewVisible = true;
        this.title = response.data.formName;
      });
    },
    
    //暂存
    draftProcessForm(args){
      this.form.formText = args[0]
      this.form.formValue = args[1]
      if (this.form.id != null) {
        updateBusinessForm(this.form).then(response => {
          this.$modal.msgSuccess("修改成功");
          this.dynamicFormEditVisible = false
          this.getList();
        })
      } 
    },
    /** 提交按钮 */
    submitProcessForm(args) {
      this.form.formText = args[0]
      this.form.formValue = args[1]
      if (this.form.id != null) {
        updateBusinessForm(this.form).then(response => {
          this.submitFormApply(response.data)
        })
      }
    },
     //提交流程
     submitFormApply(entity){
        if(!entity.actProcessDefForm){
          this.$modal.msgError("未绑定流程");
          return
        }
        let variables = {
            entity: entity.variableMap
        }
        const data = {
            processKey: entity.actProcessDefForm.processDefinitionKey, // key
            businessKey: entity.id, // 业务id
            variables: variables,
            classFullName: entity.formKey
        }
        // 启动流程
        processApi.startProcessApply(data).then(response => {
            this.taskId = response.data.taskId;
            // 查询下一节点的变量
            this.taskVariables = {
                entity: entity.variableMap,  // 变量
            }
            this.$refs.verifyRef.visible = true
            this.$refs.verifyRef.reset()
        })
    },
    // 提交成功回调
    callSubmit(){
      this.dynamicFormEditVisible = false;
      this.getList();
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const ids = row.id || this.ids;
      this.$modal.confirm('是否确认删除业务表单编号为"' + ids + '"的数据项？').then(() => {
        this.loading = true;
        return delBusinessForm(ids);
      }).then(() => {
        this.loading = false;
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).finally(() => {
        this.loading = false;
      });
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('workflow/businessForm/export', {
        ...this.queryParams
      }, `businessForm_${new Date().getTime()}.xlsx`)
    }
  }
};
</script>
<style scoped>
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
    padding-top: 15px !important;
    overflow: hidden;
    overflow-y: auto;
    margin-bottom: 40px;
}
.self_dialog /deep/ .el-dialog .el-dialog__footer {
    left: 40%;
    bottom: 10px;
    position: absolute;
}
</style>
