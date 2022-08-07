<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="数据集编码" prop="dbCode">
        <el-input
          v-model="queryParams.dbCode"
          placeholder="请输入数据集编码"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="数据集名字" prop="dbChName">
        <el-input
          v-model="queryParams.dbChName"
          placeholder="请输入数据集名字"
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
          type="primary"
          plain
          icon="el-icon-plus"
          size="mini"
          @click="handleAdd"
          v-hasPermi="['report:reportDb:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="el-icon-edit"
          size="mini"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['report:reportDb:edit']"
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
          v-hasPermi="['report:reportDb:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['report:reportDb:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="reportDbList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="id" align="center" prop="id" v-if="true"/>
      <el-table-column label="主键字段" align="center" prop="jimuReportId" />
      <el-table-column label="数据集编码" align="center" prop="dbCode" />
      <el-table-column label="数据集名字" align="center" prop="dbChName" />
      <el-table-column label="数据源类型" align="center" prop="dbType" />
      <el-table-column label="数据库表名" align="center" prop="dbTableName" />
      <el-table-column label="动态查询SQL" align="center" prop="dbDynSql" />
      <el-table-column label="数据源KEY" align="center" prop="dbKey" />
      <el-table-column label="填报数据源" align="center" prop="tbDbKey" />
      <el-table-column label="填报数据表" align="center" prop="tbDbTableName" />
      <el-table-column label="请求地址" align="center" prop="apiUrl" />
      <el-table-column label="数据源" align="center" prop="dbSource" />
    </el-table>

    <pagination
      v-show="total>0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />
  </div>
</template>

<script>
import { listReportDb} from "@/api/report/reportDb";

export default {
  name: "ReportDb",
  data() {
    return {
      // 按钮loading
      buttonLoading: false,
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
      // 报表数据表格数据
      reportDbList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        dbCode: undefined,
        dbChName: undefined
      }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    /** 查询报表数据列表 */
    getList() {
      this.loading = true;
      listReportDb(this.queryParams).then(response => {
        this.reportDbList = response.rows;
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
    }
  }
};
</script>
