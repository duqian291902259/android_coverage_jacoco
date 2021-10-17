
<template>
  <div v-loading='pageLoading'>
    <h2 style="text-align: center">上传ec文件、diff文件等</h2>

    <el-form ref="ruleForm" :model="form" label-position="left" inline :rules="formRules">
      <el-form-item label="应用名称" style="width: 600px" >
        <el-radio-group v-model="form.appName">
          <el-radio label="cc-android"></el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="APK包的分支名称" prop='branch'>
        <el-input
          clearable
          v-model="form.branch"
          style="width: 220px"
          placeholder="APK包对应的分支名称"
        >
        </el-input>
      </el-form-item>
      <el-form-item label="CommitId" prop='commitId'>
        <el-input
          type="primery"
          clearable
          v-model="form.commitId"
          style="width: 220px"
          placeholder="APK对应的commit-id"
        >
        </el-input>
      </el-form-item>
    </el-form>
    <el-button type="warning" v-if="disableUpload" @click="validateForm">请完善表单再点击上传文件</el-button>
    <el-upload
      :drag='false'
      v-else
      class="upload-demo"
      :action="uploadUrl"
      :on-preview="handlePreview"
      :on-remove="handleRemove"
      :data="form"
      multiple
      :limit="limit"
      :on-exceed="handleExceed"
      :on-success="handleSucceed"
      :on-error="handleError"
      :file-list="fileList"
    >
      <el-button size="small" type="primary">点击上传文件</el-button>
      <div slot="tip" class="el-upload__tip">
        如：/Sdcard/Android/packagename/Cache/jacoco/xxx.ec
      </div>
    </el-upload>
  </div>
</template>
<script>
import { jacocoHost } from "../utils";
export default {
  data() {
    return {
      pageLoading: false,
      form: {
        appName: "cc-android",
        branch: "dev_dq_#411671_coverage",
        commitId: "21acf983",
      },
      limit: 10,
      uploadUrl: `${jacocoHost}/coverage/upload`,
      fileList: [
        // {
        //   name: "food.jpeg",
        //   url: "https://fuss10.elemecdn.com/3/63/4e7f3a15429bfda99bce42a18cdd1jpeg.jpeg?imageMogr2/thumbnail/360x360/format/webp/quality/100",
        // },
      ],
      formRules: {
        branch: [
            { required: true, message: '请填写APK对应的分支名称', trigger: 'blur' }
          ],
        commitId: [
            { required: true, message: '请填写APK对应的commitId"', trigger: 'blur' },
            { min: 8, max: 8, message: 'CommitId长度为8，请重新填写', trigger: 'blur' }
          ],
      }
    };
  },
  computed:{
    disableUpload(){
      let {branch = '', commitId=''} = this.form || {}
      console.warn(!(branch && commitId))
      return !(branch && commitId)
    }
  },
  methods: {
    validateForm(){
      this.$refs['ruleForm'].validate()
    },
    handleRemove(file, fileList) {
      console.log(file, fileList);
    },
    handlePreview(file) {
      console.log(file);
    },
    handleExceed(files, fileList) {
      this.$message.warning(
        `当前限制选择 ${this.limit} 个文件，本次选择了 ${
          files.length
        } 个文件，共选择了 ${files.length + fileList.length} 个文件`
      );
    },
    handleSucceed() {
      this.$message.success(`上传成功`);
    },
    handleError(err) {
      let { type } = err;
      //debugger
      //function(err, file, fileList)
      this.$message.error(`上传失败${type ? ":" + type : ""}`);
    },
    beforeRemove(file, fileList) {
      return this.$confirm(`确定移除 ${file.name}？`);
    },
  }
};
</script>