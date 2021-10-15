<template>
  <div id="app" style="width:60%;margin:20px auto;max-width:760px;">
     <el-tabs v-model="activeName">
      <el-tab-pane label="Android覆盖率报告" name="first">
        <report/>
      </el-tab-pane>
      <el-tab-pane label="上传文件" name="second">
         <upload/>
      </el-tab-pane>
      <el-tab-pane label="下载报告" name="third"><download/></el-tab-pane>
      <!-- <el-tab-pane label="报告管理" name="fourth">报告管理</el-tab-pane> -->
      <el-tab-pane label="系统说明" name="fifth">CC-Jacoco-Web-1.0.1 ---> 待完善,by 杜小菜</el-tab-pane>
    </el-tabs>
  </div>
</template>

<script>
import Download from './components/download.vue'
import report from './components/report.vue'
import Upload from './components/upload.vue'
export default {
  components: {report, Upload, Download},
  data: function() {
    return {
      activeName: 'first'
    }
  },
  methods: {
    
  }
}
</script>

<style>

</style>
