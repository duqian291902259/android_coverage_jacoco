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
          placeholder="请选择生成报告的分支"
          style="width: 350px"
        >
          <el-option-group
            :label="group.label"
            v-for="group in groups"
            :key="group.label"
          >
            <el-option
              v-for="item in group.options"
              :label="item.label"
              :value="item.value"
              :key="item.value"
            ></el-option>
          </el-option-group>
        </el-select>
        <span style="width: 50px"> -- </span>
        <el-select
          v-model="form.base_branch"
          placeholder="可选择对比的分支"
          clearable
        >
          <el-option-group label="可选择对比的分支">
            <el-option label="master" value="master"></el-option>
            <el-option label="dev" value="dev"></el-option>
          </el-option-group>
        </el-select>
      </el-form-item>

      <el-form-item label="起始CommitId">
        <el-input
          v-model="form.commitId"
          style="width: 350px"
          placeholder="安装apk对应的commit-id"
        >
        </el-input>
      </el-form-item>

      <el-form-item label="对比CommitId">
        <el-input
          v-model="form.commitId2"
          style="width: 350px"
          placeholder="获取差异的commit-id"
        >
        </el-input>
      </el-form-item>
      <!-- <el-form-item label="ec上传时间">
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
      </el-form-item> -->
      <el-form-item label="增量覆盖率">
        <el-switch v-model="form.incremental"></el-switch>
      </el-form-item>
      <el-form-item label="开发环境">
        <el-radio-group v-model="form.env">
          <el-radio label="Debug"></el-radio>
          <el-radio label="Release" disabled></el-radio>
        </el-radio-group>
      </el-form-item>

      <div style="text-align: center; margin: 0px">
        <el-button :loading="isLoading" type="primary" @click="onSubmit">生成覆盖率报告</el-button>
        <el-button ref="btn_report_online" @click="openReport">在线查看覆盖率报告</el-button>
        <el-button ref="btn_report_download" @click="downloadReport" style="margin-top: 10px"
          >下载覆盖率报告</el-button
        >
      </div>

      <el-form-item label="提示信息" v-if="form.desc"  style="margin-top: 20px">
        <el-input type="textarea" v-model="form.desc"></el-input>
      </el-form-item>
    </el-form>
  </div>
</template>

<script>
import { requestGet,requestPost } from "../utils/fetch";
import { jacocoHost,localHost } from "../utils";
export default {
  data: function () {
    return {
      form: {
        appName: "cc-android",
        branch: "dev_dq_#411671_coverage",
        base_branch: "",
        commitId: "21acf983",
        commitId2: "84f1ad08",
        date1: "",
        date2: "",
        incremental: true,
        env: "Debug",
        desc: "",
      },
      reportUrl: "",
      reportZipUrl: "",
      isLoading: false,
      groups: [
        {
          label: "请选择当前生成报告的分支",
          options: [
            {
              value: "dev",
              label: "dev",
            },
            {
              value: "dev_dq_#411671_coverage",
              label: "dev_dq_#411671_coverage",
            },
          ],
        },
      ],
      response: {},
    };
  },
  created(){
     console.warn(`host=${jacocoHost}`);
     this.updateSelectList()
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

      if (this.form.incremental === true && this.form.base_branch===""&& (this.form.commitId2===""||this.form.commitId2 === undefined)) {
        this.$message.error("增量报告，请填写要对比的CommitId");
        return;
      }

      this.$message.success("正在处理，请稍后查阅...");
      console.warn(this.form);
      this.isLoading = true
      requestGet(`${jacocoHost}/coverage/report`, this.form)
        .then((res) => {
          console.warn(res);

          let { result = 0, data = "" } = res;
          let msg = `处理结果：${data.data}`;
          if (result != 0) {
            msg = `出错了，呜呜...${data}`;
          }
          this.form.desc = msg;
          this.$message.success(msg);
          this.reportUrl = data.reportUrl;
          this.reportZipUrl = data.reportZipUrl;

          let logMsg = `reportUrl...${this.reportUrl}`;
          console.warn(logMsg);
          console.warn(data.reportZipUrl);
          //this.response = JSON.parse(res.data);
          //console.warn("response =" +this.response);
          this.isLoading = false;
        })
        .catch((error) => {
          console.error(error);
          var errorMsg = `出错了... ${error}`;
          this.form.desc = errorMsg;
          this.$message.error(errorMsg);
          this.isLoading = false;
        });

      // requestPost(`${jacocoHost}/coverage/upload`, Object.assign({}, this.form, {
      //   appName:"dq-test",
      //   versionCode:"3.8.1"
      // })).then((res)=>{
      //   console.warn(res)
      // }).catch(error=>{
      //   console.error(error)
      // })
    },

    openReport() {
      var url = this.reportUrl;
      if (url === "" || url === undefined) {
        this.$message.error("报告未生成");
        return;
      }
      window.open(url);
      console.warn(`open url ${url}`);
    },
    downloadReport() {
      var url = this.reportZipUrl;
      if (url === "" || url === undefined) {
        this.$message.error("报告未生成");
        return;
      }
      window.open(url);
      console.warn(`download url ${url}`);
    },
    updateSelectList() {
      requestGet(`${jacocoHost}/api/init`, this.form).then(
        (res) => {
          let {data = []} = res || {}
          this.updateOptions(data);
        }
      );
    },
    updateOptions(date) {
      this.$set(this.groups[0], "options", date);
      this.form.branch = date[0].value;
    },
  },
};
</script>