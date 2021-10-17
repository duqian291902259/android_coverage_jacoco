<template>
  <div id="app" style="width:60%;margin:5px auto;max-width:720px;">
     <el-tabs v-model="activeName">
      <el-tab-pane label="Android覆盖率报告" name="first"><report/></el-tab-pane>
      <el-tab-pane label="覆盖率报告管理" name="fourth"><report-manager/></el-tab-pane>
      <el-tab-pane label="上传文件" name="second"><upload/></el-tab-pane>
      <el-tab-pane label="下载文件" name="third"><download/></el-tab-pane>
      <el-tab-pane label="系统说明" name="fifth">CC-Jacoco-Web-1.0.1 ---> 待完善,by 杜小菜</el-tab-pane>
    </el-tabs>
  </div>
</template>

<script>
import Download from './components/download.vue'
import report from './components/report.vue'
import ReportManager from './components/reportManager.vue'
import Upload from './components/upload.vue'
export default {
  components: {report, Upload, Download, ReportManager},
  data: function() {
    return {
      activeName: 'first'
    }
  },
  watch:{
    activeName: {
      handler(val){
        sessionStorage.setItem('activeName', val || '')
      }
    }
  },
  created(){
    this.activeName = sessionStorage.getItem('activeName')
  },
  methods: {
    
  }
}
</script>

<style>

</style>
