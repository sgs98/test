<template>
  <el-dialog title="表单" :visible.sync="visible" v-if="visible" width="60%" :close-on-click-modal="false" append-to-body>
    <el-form label-width="100px" :model="fromData" :rules="rulesFrom" ref="fromDataRef">
      <el-row>
        <el-col class="line" :span="12">
          <el-form-item label="流程定义Key">
            <el-input v-model="fromData.processDefinitionKey" disabled></el-input>
          </el-form-item>
        </el-col>
        <el-col class="line" :span="12">
          <el-form-item label="流程定义名称">
            <el-input v-model="fromData.processDefinitionName" disabled></el-input>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row>
        <el-col class="line" :span="12">
          <el-form-item label="表单Key" prop="formKey">
            <el-input v-model="fromData.formKey" placeholder="请选择表单" >
             <el-button slot="append" @click="handerOpenForm" icon="el-icon-search"></el-button>
            </el-input>
          </el-form-item>
        </el-col>
        <el-col class="line" :span="12">
          <el-form-item label="表单名称" prop="formName">
            <el-input v-model="fromData.formName" ></el-input>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row>
        <el-col class="line" :span="24">
          <el-form-item label="表单参数">
            <el-input v-model="fromData.formVariable" type="textarea" placeholder="请输入表单参数,动态表单中参数id,多个用英文逗号隔开" />
          </el-form-item>
        </el-col>
      </el-row>
      
    </el-form>
    <el-dialog title="表单" :visible.sync="formVisible" v-if="visible" width="70%" :close-on-click-modal="false" append-to-body>
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

        <el-table v-loading="loading" :highlight-current-row="true" :data="processFormList" @row-click="handleChange">
          <el-table-column label="主键" align="center" prop="id" v-if="false"/>
          <el-table-column label="表单key" align="center" prop="formKey" />
          <el-table-column label="表单名称" align="center" prop="formName" />
          <el-table-column label="表单备注" align="center" prop="formRemark" />
        </el-table>

        <pagination
          v-show="total>0"
          :total="total"
          :page.sync="queryParams.pageNum"
          :limit.sync="queryParams.pageSize"
          @pagination="getList"
        />
      </div>
    </el-dialog>
     <span slot="footer" class="dialog-footer">
      <el-button @click="visible = false">取 消</el-button>
      <el-button type="primary" @click="submitForm()">确 定</el-button>
    </span>
  </el-dialog>
</template>

<script>
import { listProcessEnableForm } from "@/api/workflow/processForm";
import { addProcessDefForm,checkProcessDefFormByDefId } from "@/api/workflow/processDefForm";
export default {
  props:{
    fromData: {
      type: Object,
      default:()=>{}
    }
  },
  name: "ProcessFormList",
  data() {
    return {
      // 显示隐藏
      visible: false,
      formVisible: false,
      // 按钮loading
      buttonLoading: false,
      // 遮罩层
      loading: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 流程单表格数据
      processFormList: [],
      // 弹出层标题
      title: "",
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        formKey: undefined,
        formName: undefined,
      },
      // 表单校验
      rulesFrom: {
        formKey: [
          { required: true, message: '表单Key不能为空', trigger: 'blur' }
        ],
        formName: [
          { required: true, message: '表单名称不能为空', trigger: 'blur' }
        ]
      }
    };
  },
  created() {
  },
  methods: {
    handleFormDesigner(row){
        this.$router.push('/workflow/processFormDesigne/'+row.id)
    },
    /** 查询流程单列表 */
    getList() {
      this.loading = true;
      listProcessEnableForm(this.queryParams).then(response => {
        this.processFormList = response.rows;
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
    // 选中数据
    handleChange(row) {
      this.fromData.formId = row.id
      this.fromData.formKey = row.formKey
      this.fromData.formName = row.formName
      this.formVisible = false;
    },
    // 打开表单
    handerOpenForm(){
      this.getList();
      this.formVisible = true
    },
    // 确认
    submitForm(){
      this.loading = true;
      checkProcessDefFormByDefId(this.fromData.processDefinitionId,this.fromData.formId).then(response => {
        if(response.msg){
          this.$modal.confirm(response.msg).then(() => {
            addProcessDefForm(this.fromData).then(response => {
              this.$modal.msgSuccess("保存成功");
              this.loading = false;
              this.visible = false;
            });
          }).finally(() => {
            this.loading = false;
          });
        }else{
          addProcessDefForm(this.fromData).then(response => {
            this.$modal.msgSuccess("保存成功");
            this.loading = false;
            this.visible = false;
          });
        }
      })
    }
  }
};
</script>
<style scoped>
.line{
  padding-bottom: 20px;
}
</style>
