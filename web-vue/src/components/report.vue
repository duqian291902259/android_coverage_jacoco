<template>
  <div>
    <h1 style="text-align: center">CC-Android覆盖率报告</h1>
    <el-form ref="form" :model="form" label-width="120px" label-position="left">
      <el-form-item label="应用名称">
        <el-radio-group v-model="form.appName">
          <el-radio label="cc-android"></el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="Git分支名称">
        <el-select
          v-model="form.branch"
          placeholder="请选择当前生成报告的分支"
          style="width: 380px"
        >
          <el-option-group label="请选择当前生成报告的分支">
            <el-option label="master" value="master"></el-option>
            <el-option label="dev" value="dev"></el-option>
            <el-option
              label="dev_dq_#411671_coverage"
              value="dev_dq_#411671_coverage"
            ></el-option>
          </el-option-group>
        </el-select>
        <span style="width: 50px"> -- </span>
        <el-select
          v-model="form.base_branch"
          placeholder="请选择对比的分支"
          clearable
        >
          <el-option-group label="请选择对比的分支">
            <el-option label="master" value="master"></el-option>
            <el-option label="dev" value="dev"></el-option>
          </el-option-group>
        </el-select>
      </el-form-item>

      <el-form-item label="当前CommitId">
        <el-input
          v-model="form.commitId"
          style="width: 380px"
          placeholder="当前apk对应的commit-id"
        >
        </el-input>
      </el-form-item>

      <el-form-item label="对比CommitId">
        <el-input
          v-model="form.commitId2"
          style="width: 380px"
          placeholder="获取差异的commit-id"
        >
        </el-input>
      </el-form-item>
      <el-form-item label="ec上传时间">
        <el-col :span="9">
          <el-date-picker
            type="date"
            placeholder="选择日期"
            v-model="form.date1"
            style="width: 250px"
          ></el-date-picker>
        </el-col>
        <el-col class="line" :span="2" style="text-align: center">-</el-col>
        <el-col :span="6">
          <el-time-picker
            placeholder="选择时间"
            v-model="form.date2"
            style="width: 250px"
          ></el-time-picker>
        </el-col>
      </el-form-item>
      <el-form-item label="增量覆盖率">
        <el-switch v-model="form.incremental"></el-switch>
      </el-form-item>
      <el-form-item label="开发环境">
        <el-radio-group v-model="form.env">
          <el-radio label="Debug"></el-radio>
          <el-radio label="Release" disabled></el-radio>
        </el-radio-group>
      </el-form-item>

      <div style="text-align: center; margin: 10px">
        <el-button type="primary" @click="onSubmit">生成覆盖率报告</el-button>
        <el-button @click="openReport" v-model="form.reportUrl"
          >在线查看覆盖率报告</el-button
        >
        <el-button
          @click="downloadReport"
          style="margin-top: 10px"
          v-model="form.reportZipUrl"
          >下载覆盖率报告</el-button
        >
      </div>

      <el-form-item label="提示信息" v-if="form.desc">
        <el-input type="textarea" v-model="form.desc"></el-input>
      </el-form-item>
    </el-form>
  </div>
</template>

<script>
import { requestGet, requestPost } from "../utils/fetch";
export default {
  data: function () {
    return {
      form: {
        appName: "cc-android",
        branch: "dev_dq_#411671_coverage",
        base_branch: "dev",
        commitId:"c8447a2fe972c7925bd1c52e905f91071ee8d5a2",
        //commitId: "577082371ba3f40f848904baa39083f14b2695b0",
        commitId2: "855e6c13a7a46b5f63cb6b7d5db3e224d38fb1f8",
        date1: "",
        date2: "",
        incremental: false,
        env: "Debug",
        desc: "",
        reportUrl: "",
        reportZipUrl: "",
      },
      response: {},
    };
  },
  methods: {
    onSubmit() {
      if (this.form.branch === "" || this.form.branch === undefined) {
        this.$message.error("分支名不能为空");
        return;
      }

      if (this.form.commitId === "" || this.form.commitId === undefined) {
        this.$message.error("Commit Id不能为空");
        return;
      }

      this.$message.success("正在处理，请稍后查阅...");
      console.warn(this.form);
      requestGet("http://127.0.0.1:8090/coverage/report", this.form)
        .then((res) => {
          console.warn(res);

          let { result = 0, data = "" } = res;
          let msg = `处理结果：${data.data}`;
          if (result != 0) {
            msg = `出错了，呜呜...${data}`;
          }
          this.form.desc = msg;
          this.$message.success(msg);
          this.form.reportUrl = data.reportUrl;
          this.form.reportZipUrl = data.reportZipUrl;

          let logMsg = `reportUrl...${this.form.reportUrl}`;
          console.warn(logMsg);
          console.warn(data.reportZipUrl);
          //this.response = JSON.parse(res.data);
          //console.warn("response =" +this.response);
        })
        .catch((error) => {
          console.error(error);
          var errorMsg = `出错了... ${error}`;
          this.form.desc = errorMsg;
          this.$message.error(errorMsg);
        });

      // requestPost('http://127.0.0.1:8090/coverage/upload', Object.assign({}, this.form, {
      //   appName:"dq-test",
      //   versionCode:"3.8.1"
      // })).then((res)=>{
      //   console.warn(res)
      // }).catch(error=>{
      //   console.error(error)
      // })
    },

    openReport() {
      var url = this.form.reportUrl;
      if (url === "" || url === undefined) {
        this.$message.error("报告未生成");
        return;
      }
      window.open(url);
      console.warn(`open url ${url}`);
    },
    downloadReport() {
      var url = this.form.reportZipUrl;
      if (url === "" || url === undefined) {
        this.$message.error("报告未生成");
        return;
      }
      window.open(url);
      console.warn(`download url ${url}`);
    },
  },
};
</script>