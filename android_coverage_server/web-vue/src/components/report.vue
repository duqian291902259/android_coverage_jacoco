<template>
  <div>
    <h2 style="text-align: center">Coverage</h2>
    <el-form
      ref="ruleForm"
      :rules="formRules"
      :model="form"
      label-width="150px"
      label-position="right"
    >
      <el-form-item label="应用名称">
        <el-radio-group v-model="form.appName" @change="onSelectApp">
          <el-radio
            v-for="item in appList"
            :label="item"
            :key="item.value"
          ></el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="应用平台">
        <el-radio-group v-model="form.os" @change="onSelectOs">
          <el-radio label="Android"></el-radio>
         <!--  <el-radio label="IOS"></el-radio>
          <el-radio label="PC"></el-radio> -->
        </el-radio-group>
      </el-form-item>
      <el-form-item label="Git分支名称" style="margin-bottom: 18px">
        <el-select
          v-model="form.branch"
          placeholder="请选择生成报告的分支"
          style="width: 300px"
          @change="onSelectedBranch"
          @blur="selectBlur"
          @clear="selectClear"
          prop="branch"
          clearable
          filterable
          allow-create
        >
          <el-option-group
            :label="group.label"
            v-for="group in groups"
            :key="group.label"
          >
            <el-option
              v-for="(item, index) in group.options"
              :label="item.branchName"
              :value="item.branchLabel"
              :key="item.value"
              @click="onOptionSelect(index)"
            ></el-option>
          </el-option-group>
        </el-select>
        <span style="width: 50px;display:none"> -- </span>
        <el-select
          v-model="form.base_branch"
          placeholder="可选择对比的分支"
          clearable
          @change="onVsBranch"
          style="display:none"
        >
          <el-option-group label="可选择对比的分支（非必须）">
            <el-option label="master" value="master"></el-option>
            <el-option label="dev" value="dev"></el-option>
          </el-option-group>
        </el-select>
      </el-form-item>

      <el-form-item
        label="当前CommitId"
        prop="commitId"
        style="margin-bottom: 18px"
      >
        <el-input
          v-model="form.commitId"
          style="width: 300px"
          clearable
          placeholder="应用对应的最新commit-id"
        >
        </el-input>
      </el-form-item>

      <el-form-item
        label="对比CommitId"
        prop="commitId2"
        style="margin-bottom: 18px"
      >
        <el-input
          v-model="form.commitId2"
          style="width: 300px"
          clearable
          placeholder="获取增量报告需要对比的commit-id"
        >
        </el-input>
      </el-form-item>
      <el-form-item label="增量覆盖率">
        <el-switch v-model="form.incremental" @change="onSelectIncremental()" />
      </el-form-item>
      <el-form-item label="开发环境">
        <el-radio-group v-model="form.env">
          <el-radio label="Debug"></el-radio>
          <el-radio label="Release" disabled></el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item label="">
        <div style="">
          <el-button :loading="isLoading" type="primary" @click="onSubmit"
            >生成覆盖率报告</el-button
          >
          <el-button ref="btn_report_online" @click="openReport"
            >在线查看覆盖率报告</el-button
          >
          <el-button
            ref="btn_report_download"
            @click="downloadReport"
            style="margin-top: 5px"
            >下载覆盖率报告</el-button
          >
        </div>
      </el-form-item>
      <el-form-item
        label="提示信息"
        v-if="form.desc"
        style="margin-top: 10px; width: 630px"
      >
        <el-input type="textarea" v-model="form.desc"></el-input>
      </el-form-item>
    </el-form>
  </div>
</template>

<script>
import { requestGet, requestPost } from "../utils/fetch";
import { jacocoHost } from "../utils";
export default {
  data: function () {
    return {
      form: {
        appName: "android",
        os: "Android",
        branch: "dev_dq_coverage",
        base_branch: "",
        commitId: "21acf983",
        commitId2: "ea7deb7e",
        incremental: false,
        env: "Debug",
        desc: "",
      },
      reportUrl: "",
      reportZipUrl: "",
      appList: [],
      branchList: [],
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
              value: "dev_dq_coverage",
              label: "dev_dq_coverage",
            },
          ],
        },
      ],
      formRules: {
        branch: [
          {
            required: true,
            message: "APK对应的分支名称不能为空",
            trigger: "blur",
          },
        ],
        commitId: [
          {
            required: true,
            message: "请填写APK对应的commitId",
            trigger: "blur",
          },
          {
            min: 8,
            max: 8,
            message: "CommitId长度为8，请填写提交SHA的前八位",
            trigger: "blur",
          },
        ],
        commitId2: [
          //{ required:this.form.incremental, message: '请填写增量对比的commitId', trigger: 'blur' },
          {
            min: 8,
            max: 8,
            message: "CommitId长度为8，请填写提交SHA的前八位",
            trigger: "blur",
          },
        ],
      },
    };
  },
  created() {
    console.warn(`host=${jacocoHost}`);
    this.updateSelectList();
  },
  methods: {
    validateForm() {
      console.log(this.$refs["ruleForm"]);
      this.$refs["ruleForm"].validate();
    },
    onSelectApp(val) {
      let that = this;
      console.log(val);
      that.updateSelectList();
      this.form.desc = "";
      this.form.os = "Android";
      this.$refs["ruleForm"].clearValidate();
    },
    onSelectIncremental() {
      if (
        (this.form.incremental === true && this.form.commitId2 === "") ||
        this.form.branch === undefined
      ) {
        let tips = "对比的commitId不能为空，请正确填写";
        this.$message.error(tips);
        //this.form.desc = tips
      }
    },
    onSelectOs(val) {
      let that = this;
      console.log(val);
      this.form.desc = "";
      if (this.form.appName != "android") {
        resetFormData();
        return;
      }
      if (val === "Android") {
        //this.form.incremental = true;
        this.updateSelectList(); //待缓存所有的信息
      } else if (val === "IOS") {
        this.form.branch = "dev_coverage";
        this.form.base_branch = "";
        this.form.commitId = "37e9e766";
        this.form.commitId2 = "8cbe0fe8";
        //this.form.incremental = true;
        this.$set(this.groups[0], "options", [
          {
            branchName: "dev_coverage",
            branchLabel: "dev_coverage",
          },
        ]);
      } else if (val === "PC") {
        this.form.branch = "dev";
        this.form.base_branch = "";
        this.form.commitId = "1a34635a";
        this.form.commitId2 = "";
        this.form.incremental = false;
        this.$set(this.groups[0], "options", [
          {
            branchName: "dev",
            branchLabel: "dev",
          },
        ]);
      }
    },
    onSubmit() {
      this.validateForm();
      this.form.desc = "";
      if (this.form.branch === "" || this.form.branch === undefined) {
        this.$message.error("分支名不能为空");
        return;
      }

      if (this.form.commitId === "" || this.form.commitId === undefined) {
        //this.$message.error("Commit Id不能为空");
        return;
      }

      if (
        this.form.incremental === true &&
        this.form.base_branch === "" &&
        (this.form.commitId2 === "" || this.form.commitId2 === undefined)
      ) {
        let tips = "增量报告，请填写要对比的CommitId,或者选择对比的分支";
        this.$message.error(tips);
        this.form.desc = tips;
        return;
      }
      if (
        this.form.commitId.length != 8 ||
        (this.form.incremental === true &&
          this.form.base_branch === "" &&
          this.form.commitId2.length != 8)
      ) {
        return;
      }
      this.form.desc = "正在处理，请稍后查阅...";
      console.warn(this.form);
      this.isLoading = true;

      requestPost(
        `${jacocoHost}/coverage/report`,
        Object.assign({}, this.form, {
          versionCode: "3.8.3", //额外的参数
        })
      )
        //requestGet(`${jacocoHost}/coverage/report`, this.form)
        .then((res) => {
          console.warn(res);

          let { result = 0, data = "" } = res;
          let msg = `${data.data}`;
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
          this.isLoading = false;
        })
        .catch((error) => {
          console.error(error);
          var errorMsg = `出错了... ${error}`;
          this.form.desc = errorMsg;
          this.$message.error(errorMsg);
          this.isLoading = false;
        });
    },
    openReport() {
      var url = this.reportUrl;
      if (url === "" || url === undefined) {
        this.$message.error("报告未生成，请点击生成按钮");
        return;
      }
      window.open(url);
      console.warn(`open url ${url}`);
    },
    downloadReport() {
      var url = this.reportZipUrl;
      if (url === "" || url === undefined) {
        this.$message.error("报告未生成，请点击生成按钮");
        return;
      }
      window.open(url);
      console.warn(url);
    },
    selectBlur(e) {
      var that = this;
      var branchName = e.target.value;
      if (branchName !== "") {
        this.form.branch = branchName;
        that.branchList.forEach((item, index) => {
          console.warn(item);
          if (item.branchName === branchName) {
            this.form.commitId = item.latestCommit;
            this.form.commitId2 = item.oldCommit;
          } else {
            this.form.commitId = "";
            this.form.commitId2 = "";
          }
        });
        //this.$forceUpdate()   // 强制更新
      }
      console.warn("selectBlur");
      console.warn(e.target.value);
    },
    selectClear() {
      this.value = "";
      this.form.commitId = "";
      this.form.commitId2 = "";
      //this.$forceUpdate()
      console.warn("selectClear");
    },
    onSelectedBranch(branchName) {
      console.warn(branchName);
      console.warn(this.branchList);
      this.value = branchName;
      //this.$forceUpdate()
      this.branchList.forEach((item, index) => {
        console.warn(item);
        if (item.branchName == branchName) {
          this.form.commitId = item.latestCommit;
          this.form.commitId2 = item.oldCommit;
        }
      });
    },
    onVsBranch(branchName) {
      this.form.commitId2 = "";
    },
    onOptionSelect(index) {
      console.warn(index);
      this.form.commitId = this.branchList[index].latestCommit;
    },
    updateSelectList() {
      requestGet(`${jacocoHost}/api/init`, this.form)
        .then((res) => {
          let { data = {} } = res || {};
          this.updateOptions(data.branchList);
          this.branchList = data.branchList;
          this.appList = data.appList;
          console.warn(this.branchList);
          console.warn("/api/init");
          console.warn(this.appList);
        })
        .catch((error) => {
          console.error(error);
          this.resetFormData();
        });
    },
    updateOptions(data) {
      console.warn(data);
      this.$set(this.groups[0], "options", data);
      if (data != null && data[0]) {
        let firstBranch = data[0];
        this.form.branch = firstBranch.branchName;
        this.form.commitId = firstBranch.latestCommit;
        this.form.commitId2 = firstBranch.oldCommit;
      } else {
        this.resetFormData();
      }
    },
    resetFormData() {
      this.form.branch = "";
      this.form.base_branch = "";
      this.form.commitId = "";
      this.form.commitId2 = "";
    },
  },
};
</script>
<style scoped>
.el-form-item {
  margin-bottom: 8px;
}
</style>