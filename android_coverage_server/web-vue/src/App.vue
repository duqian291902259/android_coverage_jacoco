<template>
  <div id="app" style="margin:5px auto;max-width:720px;">
     <el-tabs v-model="activeName">
      <el-tab-pane label="生成覆盖率报告" name="first"><report/></el-tab-pane>
      <el-tab-pane label="覆盖率报告管理" name="second"><report-manager/></el-tab-pane>
      <el-tab-pane label="API实验室" name="third"><upload/></el-tab-pane>
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
