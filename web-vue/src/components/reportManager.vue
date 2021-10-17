<template>
  <div>
    <h1 style="text-align: center">覆盖率报告管理</h1>
    <el-table :data="tableData" border fit style="width: 100%">
      <el-table-column fixed prop="date" label="日期" width="120">
      </el-table-column>
      <el-table-column prop="branch" label="分支名称" width="220">
      </el-table-column>
      <el-table-column prop="fileName" label="报告名称" width="220">
      </el-table-column>
      <el-table-column fixed="right" label="操作" width="150">
        <template slot-scope="scope">
          <el-button @click="handleClick(scope.row)" type="text" size="small"
            >查看报告</el-button
          >
          <el-button type="text" size="small">下载报告</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script>
export default {
  methods: {
    handleClick(row) {
      console.log(row);
    },
  },

  data() {
    return {
      tableData: [
        {
          date: "2016-05-02",
          branch: "dev_duqian",
          fileName: "reprot111.zip",
        },
        {
          date: "2016-05-04",
          branch: "master",
          fileName: "reprot1222.zip",
        },
      ],
    };
  },
};
</script>