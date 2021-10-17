<template>
  <div>
    <h2 style="text-align: center">覆盖率报告管理</h2>
    <el-form ref="form" :model="form" label-width="120px" label-position="right">
      <el-form-item label="应用名称">
        <el-radio-group v-model="form.appName">
          <el-radio label="cc-android"></el-radio>
        </el-radio-group>
      </el-form-item>
      </el-form>
    <el-table :data="tableData" border fit style="width: 90%;margin:0px auto">
      <el-table-column fixed prop="date" label="日期" width="120">
      </el-table-column>
      <!-- <el-table-column prop="branch" label="分支名称" width="220">
      </el-table-column> -->
      <el-table-column prop="fileName" label="报告名称" width="320">
      </el-table-column>
      <el-table-column fixed="right" label="操作" width="150">
        <template slot-scope="scope">
          <el-button @click="handleClick(scope.row)" type="text" size="small">查看报告</el-button>
          <el-button type="text" size="small">下载报告</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script>
import { requestGet,requestPost } from "../utils/fetch";
import { jacocoHost } from "../utils";
export default {
  data() {
    return {
      form: {
        appName: "cc-android"
      },
      tableData: [
        {
          date: "2016-05-02",
          //branch: "dev_duqian",
          fileName: "dev_duqian_reprot111.zip",
        },
        {
          date: "2016-05-04",
          //branch: "master",
          fileName: "dev_duqian_reprot1222.zip",
        },
      ],
    };
  },
  created(){
     console.warn(`host=${jacocoHost}`);
     this.updateReportList()
  },
  methods: {
    handleClick(row) {
      console.log(row);
    },
    updateReportList(){
        requestGet(`${jacocoHost}/coverage/report/manager`, this.form).then(
        (res) => {
          let {data = []} = res || {}
          console.warn(`updateReportList=${data}`)
        }
      );
    }
  },
};
</script>