
<template>
  <div v-loading='pageLoading'>
    <h2 style="text-align: center">上传文件</h2>
    <el-form ref="ruleForm" :model="form" label-position="left" inline :rules="formRules">
      <!-- <el-form-item label="应用名称" style="width: 600px" >
        <el-radio-group v-model="form.appName">
          <el-radio label="android"></el-radio>
        </el-radio-group>
      </el-form-item> -->
      <el-form-item label="应用名称">
        <el-radio-group v-model="form.appName"  style="width: 600px" >
          <el-radio
            @change="onSelectApp"
            v-for="item in appList"
            :label="item"
            :key="item.value"
          ></el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="APK包的分支名称" prop='branch' >
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
      <el-button size="big" type="primary">点击上传文件</el-button>
      <div slot="tip" class="el-upload__tip" style="margin-top:20px;">
        可以手动上传文件，如APP ec文件：/Sdcard/Android/packagename/Cache/jacoco/xxx.ec <br/>  <br/> 
        
         本地dev开启coverage开关编译后会在项目的同级目录jacoco_upload里面生成所需上传的文件
      </div>
    </el-upload>
  </div>
</template>
<script>
import { jacocoHost } from "../utils";
import { requestGet } from "../utils/fetch";
export default {
  data() {
    return {
      pageLoading: false,
      form: {
        appName: "android",
        branch: "master",
        commitId: "efb5756b",
      },
      limit: 10,
      uploadUrl: `${jacocoHost}/coverage/upload`,
      fileList: [],
      formRules: {
        branch: [
            { required: true, message: '请填写APK对应的分支名称', trigger: 'blur' }
          ],
        commitId: [
            { required: true, message: '请填写APK对应的commitId', trigger: 'blur' },
            { min: 8, max: 8, message: 'CommitId长度为8，请重新填写', trigger: 'blur' }
          ],
      },
      appList: ['coverage-demo'],
    };
  },
  props:{
    allInfo: {
      type: Object,
      default: ()=>{}
    }
  },
  watch: {
    allInfo: {
      handler(val){
        this.updateAppList(val || {});
      },
      deep: true
    }
  },
  created(){
     console.warn(`host=${jacocoHost}`);
    //  this.updateAppList();
  },
  computed:{
    disableUpload(){
      let {branch = '', commitId=''} = this.form || {}
      console.warn(!(branch && commitId))
      return !(branch && commitId && commitId.length==8)
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
      this.$message.error(`上传失败${type ? ":" + type : ""}`);
    },
    beforeRemove(file, fileList) {
      return this.$confirm(`确定移除 ${file.name}？`);
    },
    updateAppList(res) {
      // requestGet(`${jacocoHost}/api/init`, this.form)
      //   .then((res) => {
      //     let { data = {} } = res || {};
      //     this.appList = data.appList;
      //     //this.form.commitId = this.[0]
      //     console.warn(this.appList);
      //   })
      //   .catch((error) => {
      //     console.error(error);
      //   });
      let { data = {} } = res || {};
      this.appList = data.appList;
      console.warn(this.appList);
      if(this.appList.length>0){
        this.form.appName = this.appList[0]; // 将第一个元素作为默认选项
      }
    },
    onSelectApp(val){
      console.log(val)
      this.updateSelectAppBranchList()
    },
    updateSelectAppBranchList() {
      requestGet(`${jacocoHost}/api/init`, this.form)
        .then((res) => {
          let { data = {} } = res || {};
          let branchList = data.branchList || []
          let {branchName = '', latestCommit =''} = branchList[0] || {}
          this.branchList = branchList;
          this.form.branch = branchName || ''
          this.form.commitId = latestCommit || ''
        })
        .catch((error) => {
          console.error(error);
        });
    },
  }
};
</script>