<template>
  <div id="app" style="margin:5px auto;max-width:720px;">
     <el-tabs v-model="activeName">
      <el-tab-pane label="生成覆盖率报告" name="first"><report/></el-tab-pane>
      <el-tab-pane label="覆盖率报告管理" name="second"><report-manager :allInfo="allInfo"/></el-tab-pane>
      <el-tab-pane label="API实验室" name="third"><upload :allInfo="allInfo"/></el-tab-pane>
      <el-tab-pane label="关于覆盖率System" name="fourth"><about-me/></el-tab-pane>
      <el-tab-pane label="Other" name="fifth">更多功能正在开发...</el-tab-pane>
    </el-tabs>
  </div>
</template>

<script>
import AboutMe from './components/aboutMe.vue'
import Download from './components/download.vue'
import report from './components/report.vue'
import ReportManager from './components/reportManager.vue'
import Upload from './components/upload.vue'
import { requestGet } from "./utils/fetch";
import { jacocoHost } from "./utils";

export default {
  components: {report, Upload, Download, ReportManager, AboutMe},
  data: function() {
    return {
      activeName: 'first',
      form: {
        appName: "android",
        os: "Android"
      },
      allInfo: {}
    }
  },
  watch:{
    activeName: {
      handler(val){
        sessionStorage.setItem('activeName', val || 'first')
      }
    },
    immediate:true
  },
  created(){
    let activeName =  sessionStorage.getItem('activeName') 
    console.log(activeName)
    this.activeName = activeName || 'first'
    this.updateSelectList()
  },
  methods: {
    updateSelectList() {
      requestGet(`${jacocoHost}/api/init`, this.form)
        .then((res) => {
          this.allInfo = res || {}
        })
        .catch((error) => {
          console.error(error);
          this.allInfo = {}
        });
    },
  }
}
</script>

<style>

</style>
