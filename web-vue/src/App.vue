<template>
  <div id="app" style="width:60%;margin:5px auto;max-width:720px;">
     <el-tabs v-model="activeName">
      <el-tab-pane label="CC-Android覆盖率报告" name="first"><report/></el-tab-pane>
      <el-tab-pane label="覆盖率报告管理" name="second"><report-manager/></el-tab-pane>
      <el-tab-pane label="API实验室（上传）" name="third"><upload/></el-tab-pane>
      <!-- <el-tab-pane label="下载文件" name="third"><download/></el-tab-pane> -->
      <el-tab-pane label="关于Jacoco-Web平台" name="fourth"><about-me/></el-tab-pane>
    </el-tabs>
  </div>
</template>

<script>
import AboutMe from './components/aboutMe.vue'
import Download from './components/download.vue'
import report from './components/report.vue'
import ReportManager from './components/reportManager.vue'
import Upload from './components/upload.vue'
export default {
  components: {report, Upload, Download, ReportManager, AboutMe},
  data: function() {
    return {
      activeName: 'first'
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
  },
  methods: {
    
  }
}
</script>

<style>

</style>
