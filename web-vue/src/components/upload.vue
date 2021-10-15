
<template>
  <div>
    <h1 style="text-align: center">上传ec文件、diff文件等</h1>
    <el-upload
      class="upload-demo"
      :action="uploadUrl"
      :on-preview="handlePreview"
      :on-remove="handleRemove"
      :before-remove="beforeRemove"
      :data="otherData"
      multiple
      :limit="3"
      :on-exceed="handleExceed"
      :on-success="handleSucceed"
      :on-error="handleError"
      :file-list="fileList"
    >
      <el-button size="small" type="primary"
        >点击上传文件</el-button
      >
      <div slot="tip" class="el-upload__tip">
        如：/Sdcard/Android/packagename/Cache/jacoco/xxx.ec
      </div>
    </el-upload>
  </div>
</template>
<script>
import { jacocoHost,localHost } from "../utils";
export default {
  data() {
    return {
      uploadUrl:`${jacocoHost}/coverage/upload`,
      otherData: {
        appName: "android",
        versionCode: "3.8.1",
      },
      fileList: [
        // {
        //   name: "food.jpeg",
        //   url: "https://fuss10.elemecdn.com/3/63/4e7f3a15429bfda99bce42a18cdd1jpeg.jpeg?imageMogr2/thumbnail/360x360/format/webp/quality/100",
        // },
      ],
    };
  },
  methods: {
    handleRemove(file, fileList) {
      console.log(file, fileList);
    },
    handlePreview(file) {
      console.log(file);
    },
    handleExceed(files, fileList) {
      this.$message.warning(
        `当前限制选择 3 个文件，本次选择了 ${files.length} 个文件，共选择了 ${
          files.length + fileList.length
        } 个文件`
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
  },
};
</script>